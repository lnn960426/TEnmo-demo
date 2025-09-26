package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {
    List<Transfer> getTransferbyUserId(int userId);

    Transfer getTransferById(int transferID);

    boolean createSendTransfer(int fromUserId, int toUserId, BigDecimal amount);

    boolean createRequestTransfer(int fromUserId, int toUserId, BigDecimal amount);

    List<Transfer> getPendingRequests(int userId);
    boolean updateTransferStatus(int transferId, int transferStatusId);

}







