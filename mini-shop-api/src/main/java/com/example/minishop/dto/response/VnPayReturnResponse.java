package com.example.minishop.dto.response;

public class VnPayReturnResponse {

    private boolean success;

    private String message;

    private String transactionRef;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(
            boolean success
    ) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message
    ) {
        this.message = message;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(
            String transactionRef
    ) {
        this.transactionRef =
                transactionRef;
    }


}