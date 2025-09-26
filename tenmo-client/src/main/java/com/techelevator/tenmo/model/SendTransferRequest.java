package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class SendTransferRequest {

    private int toUserId;

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    private BigDecimal amount;

    public SendTransferRequest(){

    }

    public SendTransferRequest(int toUserId,BigDecimal amount){
        this.toUserId = toUserId;
        this.amount = amount;
    }


}
