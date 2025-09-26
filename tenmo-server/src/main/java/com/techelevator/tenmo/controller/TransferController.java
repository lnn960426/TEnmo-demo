package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.SendTransferRequest;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/transfers")
@PreAuthorize("isAuthenticated()")
public class TransferController {
    public TransferController(TransferDao transferDao, UserDao userDao) {
        this.transferDao = transferDao;
        this.userDao = userDao;
    }

    private final TransferDao transferDao;
    private final UserDao userDao;

    @GetMapping
    public List<Transfer> getTransfersByUserId(Principal principal) {
        String username = principal.getName();
        User user = userDao.getUserByUsername(username);
        return transferDao.getTransferbyUserId(user.getId());
    }


    @GetMapping("/{id}")
    public Transfer getTransferById(@PathVariable int id) {
        Transfer transfer = transferDao.getTransferById(id);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found");
        }
        return transfer;
    }

    @PostMapping("/send")
    public String sendTeBucks(@RequestBody SendTransferRequest request, Principal principal) {

        String username = principal.getName();
        User sender = userDao.getUserByUsername(username);

        boolean success = transferDao.createSendTransfer(sender.getId(), request.getToUserId(), BigDecimal.valueOf(request.getAmount()));

        if (success) {
            return "Transaction Successfully.";
        } else {
            return "Failed Transaction.";
        }

    }

    @PostMapping("/request")
    public String requestTeBucks(@RequestBody SendTransferRequest request, Principal principal) {
        //System.out.println("requestTeBucks call to userid =" + request.getToUserId() + "amount" + request.getAmount());

        String username = principal.getName();
        User requester = userDao.getUserByUsername(username);

        boolean success = transferDao.createRequestTransfer(requester.getId(), request.getToUserId(), BigDecimal.valueOf(request.getAmount()));

        if (success) {
            return "Requested Successfully.";
        } else {
            return "Failed to Request.";
        }

    }

    @GetMapping("/pending")
    public List<Transfer> getPendingRequests(Principal principal) {
        String username = principal.getName();
        User user = userDao.getUserByUsername(username);
        return transferDao.getPendingRequests(user.getId());
    }

    @PostMapping("/pending/{transfer_id}/approve")
    public String updateApprovePending(@PathVariable("transfer_id") int transferId) {
        boolean success = transferDao.updateTransferStatus(transferId, 2);
        if (success) {
            return "The transfer has been approved";
        } else {
            return "Failed to approve";
        }
    }

    @PostMapping("/pending/{transfer_id}/reject")
    public String updateRejectPending(@PathVariable("transfer_id") int transferId) {
        boolean success = transferDao.updateTransferStatus(transferId, 3);
        if (success) {
            return "The transfer is rejected";
        } else {
            return "failed to reject";
        }


    }


}
