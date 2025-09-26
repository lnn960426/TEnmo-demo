package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Transfer> getTransferbyUserId(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = """ 
                     SELECT t.transfer_id,
                     t.transfer_type_id,
                     t.transfer_status_id,
                     from_account.user_id AS from_user_id,
                     from_user.username AS from_username,
                     t.account_to,
                     to_account.user_id AS to_user_id,
                     to_user.username AS to_username,
                     t.amount
                     FROM transfer t
                     JOIN account from_account ON t.account_from = from_account.account_id
                     JOIN tenmo_user from_user ON from_account.user_id = from_user.user_id
                     JOIN account to_account ON t.account_to = to_account.account_id
                     JOIN tenmo_user to_user ON to_account.user_id = to_user.user_id
                     WHERE from_account.user_id = ? OR to_account.user_id = ?
                """;
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);
            while (results.next()) {
                transfers.add(mapRowToTransfers(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transfers;
    }

    @Override
    public Transfer getTransferById(int transferID) {
        String sql = """ 
                SELECT t.transfer_id,
                t.transfer_type_id,
                t.transfer_status_id,
                t.amount,
                from_account.account_id AS from_account_id,
                from_user.user_id AS from_user_id,
                from_user.username AS from_username,
                to_account.account_id AS to_account_id,
                to_user.user_id AS to_user_id,
                to_user.username AS to_username
                FROM transfer t
                JOIN account from_account ON t.account_from = from_account.account_id
                JOIN tenmo_user from_user ON from_account.user_id = from_user.user_id
                JOIN account to_account ON t.account_to = to_account.account_id
                JOIN tenmo_user to_user ON to_account.user_id = to_user.user_id
                WHERE t.transfer_id = ?
                 """;
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferID);
            if (result.next()) {
                return mapRowToTransfers(result);
            } else {
                throw new DaoException("Transfer Id " + transferID + " not found.");
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    @Override
    public boolean createSendTransfer(int fromUserId, int toUserId, BigDecimal amount) {

        String sqlFrom = "SELECT account_id, balance FROM account WHERE user_id = ?";
        String sqlTo = "SELECT account_id FROM account WHERE user_id = ?";

        try {

            //get sender's & receiver accounts
            SqlRowSet fromResult = jdbcTemplate.queryForRowSet(sqlFrom, fromUserId);
            SqlRowSet toResult = jdbcTemplate.queryForRowSet(sqlTo, toUserId);

            //if can't find sender or receiver
            if (!fromResult.next() || !toResult.next()) {
                System.out.println("One of the account of both accounts not found.");
                return false;
            }

            int fromAccountId = fromResult.getInt("account_id");
            BigDecimal fromBalance = fromResult.getBigDecimal("balance");
            int toAccountId = toResult.getInt("account_id");

            //if balance is not enough
            if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(fromBalance) > 0) {
                System.out.println("Invalid amount.");
                return false;
            }

            // deduct from sender
            String sqlDeduct = "UPDATE account SET balance = balance - ? WHERE account_id = ?";
            jdbcTemplate.update(sqlDeduct, amount, fromAccountId);

            //add to receiver
            String sqlAdd = "UPDATE account SET balance = balance + ? WHERE account_id =?";
            jdbcTemplate.update(sqlAdd, amount, toAccountId);


            final int TRANSFER_TYPE_SEND = 2;
            final int TRANSFER_STATUS_APPROVE = 2;


            // record the transfer to table
            String sqlInsert = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES(?, ?, ?, ?, ?)";
            jdbcTemplate.update(sqlInsert, TRANSFER_TYPE_SEND, TRANSFER_STATUS_APPROVE, fromAccountId, toAccountId, amount);

            return true;

        } catch (Exception e) {
            System.out.println("Error during transfer: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createRequestTransfer(int fromUserId, int toUserId, BigDecimal amount) {
        String sqlGetAccount = "SELECT account_id FROM account WHERE user_id = ?";

        try {
            // System.out.println("fromUserId =" +fromUserId + "touserid=" + toUserId);
            SqlRowSet fromResult = jdbcTemplate.queryForRowSet(sqlGetAccount, fromUserId);
            SqlRowSet toResult = jdbcTemplate.queryForRowSet(sqlGetAccount, toUserId);


            if (!fromResult.next()) {
                System.out.println("From account NOT FOUND" + fromUserId);
                return false;
            }

            if (!toResult.next()) {
                System.out.println("To account Not Found for userId =" + toUserId);
                return false;
            }

            int fromAccountId = toResult.getInt("account_id");
            int toAccountId = fromResult.getInt("account_id");

            final int TRANSFER_TYPE_REQUEST = 1;
            final int TRANSFER_STATUS_PENDING = 1;

            String sql = """
                    INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)
                    VALUES (?, ?, ?, ?, ?)
                    """;

            jdbcTemplate.update(sql, TRANSFER_TYPE_REQUEST, TRANSFER_STATUS_PENDING, fromAccountId, toAccountId, amount);
            return true;

        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    @Override
    public List<Transfer> getPendingRequests(int userId) {
        List<Transfer> getPendingRequestByUserId = new ArrayList<>();
        String sql = """
                    SELECT t.transfer_id,
                       t.transfer_type_id,
                       t.transfer_status_id,
                       t.amount,
                       from_account.account_id AS from_account_id,
                       from_user.user_id AS from_user_id,
                       from_user.username AS from_username,
                       to_account.account_id AS to_account_id,
                       to_user.user_id AS to_user_id,
                       to_user.username AS to_username
                       FROM transfer t
                       JOIN account from_account ON t.account_from = from_account.account_id
                       JOIN tenmo_user from_user ON from_account.user_id = from_user.user_id
                       JOIN account to_account ON t.account_to = to_account.account_id
                       JOIN tenmo_user to_user ON to_account.user_id = to_user.user_id
                       WHERE from_account.user_id = ? AND t.transfer_status_id = 1
                """;
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

            while (results.next()) {
                getPendingRequestByUserId.add(mapRowToTransfers(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return getPendingRequestByUserId;

    }

    @Override
    public boolean updateTransferStatus(int transferId, int transferStatusId) {
        try {

            //System.out.println("START UPDATE");
            //System.out.println("Requested: transferID = " + transferId + "," + "status= " + transferStatusId);
            //transfer detail
            String sql = " SELECT transfer_type_id, account_from, account_to, amount FROM transfer WHERE transfer_id = ?";
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferId);

            if (!result.next()) {
                throw new RuntimeException("No transfer found.");
            }

            int transferTypeId = result.getInt("transfer_type_id");
            int accountFrom = result.getInt("account_from");
            int accountTo = result.getInt("account_to");
            BigDecimal amount = result.getBigDecimal("amount");

           // System.out.println("Transfer detail: type=" + transferTypeId + ", from=" + accountFrom + ", to = " + accountTo + ", amount = " + amount);

            //if approve
            if (transferStatusId == 2) {

               // System.out.println("Approve transfer, check balance from accountFrom = " + accountFrom);

                String balanceSql = "SELECT balance FROM account WHERE account_id = ?";
                SqlRowSet balanceResult = jdbcTemplate.queryForRowSet(balanceSql, accountFrom);

                if (!balanceResult.next()) {
                    throw new RuntimeException("account not found.");
                }

                BigDecimal fromBalance = balanceResult.getBigDecimal("balance");

                if (fromBalance.compareTo(amount) < 0) {
                    throw new RuntimeException("Not enough balance in account");
                }

                //deduct money from user
                //System.out.println("Deducting: " + amount + "from account: " + accountFrom);
                String sqlDeduct = "UPDATE account SET balance = balance - ? WHERE account_id = ?";
                jdbcTemplate.update(sqlDeduct, amount, accountFrom);

                //add money to receiver
                //System.out.println("Adding: " + amount + "to account: " + accountTo);
                String sqlAdd = "UPDATE account SET balance = balance + ? WHERE account_id =?";
                jdbcTemplate.update(sqlAdd, amount, accountTo);

                //System.out.println("Balance updates: deduct = " + sqlDeduct + ", added =" +sqlAdd);

            } else if (transferStatusId == 3) {
                System.out.println("Transfer rejected.");
            }

            //update transfer status
            String sqlUpdate = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
            int numberOfRows = jdbcTemplate.update(sqlUpdate, transferStatusId, transferId);

            //System.out.println("updated: " + sqlUpdate);


            if (numberOfRows == 0) {
                    throw new RuntimeException("Transfer status update fail.");
                }

           // System.out.println("update end");
                return true;

        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

    }

            private Transfer mapRowToTransfers(SqlRowSet rowSet) {
                Transfer transfers = new Transfer();
                transfers.setTransferId(rowSet.getInt("transfer_id"));
                transfers.setTransferTypeId(rowSet.getInt("transfer_type_id"));
                transfers.setTransferStatusId(rowSet.getInt("transfer_status_id"));


                transfers.setFromUserId(rowSet.getInt("from_user_id"));
                transfers.setFromUsername(rowSet.getString("from_username"));

                transfers.setToUserId(rowSet.getInt("to_user_id"));
                transfers.setToUsername(rowSet.getString("to_username"));
               //transfers.setFromUserId(rowSet.getInt("account_from"));
               // transfers.setToUserId(rowSet.getInt("account_to"));
                transfers.setTransferAmount(rowSet.getBigDecimal("amount"));


                return transfers;

            }
        }


