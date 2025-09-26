package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {
        private int transferId;
        private int fromUserId;
        private int toUserId;
    private String fromUsername;
    private String toUsername;
    private BigDecimal transferAmount;
    private int transferTypeId;
    private int transferStatusId;


    public Transfer() {

    }



        public int getTransferTypeId() {
            return transferTypeId;
        }

        public void setTransferTypeId(int transferTypeId) {
            this.transferTypeId = transferTypeId;
        }

        public int getTransferStatusId() {
            return transferStatusId;
        }

        public void setTransferStatusId(int transferStatusId) {
            this.transferStatusId = transferStatusId;
        }

        public int getTransferId() {
            return transferId;
        }

        public void setTransferId(int transferId) {
            this.transferId = transferId;
        }

        public int getFromUserId() {
            return fromUserId;
        }

        public void setFromUserId(int fromUserId) {
            this.fromUserId = fromUserId;
        }

        public int getToUserId() {
            return toUserId;
        }

        public void setToUserId(int toUserId) {
            this.toUserId = toUserId;
        }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public BigDecimal getTransferAmount() {
            return transferAmount;
        }

        public void setTransferAmount(BigDecimal transferAmount) {
            this.transferAmount = transferAmount;
        }

    }


