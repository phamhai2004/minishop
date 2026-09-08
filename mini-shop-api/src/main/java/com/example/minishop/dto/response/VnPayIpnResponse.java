package com.example.minishop.dto.response;

public class VnPayIpnResponse {

    private String RspCode;

    private String Message;

    public VnPayIpnResponse() {
    }

    public VnPayIpnResponse(
            String rspCode,
            String message
    ) {
        RspCode = rspCode;
        Message = message;
    }

    public String getRspCode() {
        return RspCode;
    }

    public void setRspCode(
            String rspCode
    ) {
        RspCode = rspCode;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(
            String message
    ) {
        Message = message;
    }
}