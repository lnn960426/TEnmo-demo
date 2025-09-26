package com.techelevator.tenmo.model;

public class SendTransferRequest {
    private int toUserId;
    private double amount;
    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


}
