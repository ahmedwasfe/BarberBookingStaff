package com.ahmet.barberbookingstaff.model;

import com.ahmet.barberbookingstaff.common.Common;

public class Token {

    private String token, user;
    private Common.TOKEN_TYPE tokenType;

    public Token() {}

    public Token(String token, String user, Common.TOKEN_TYPE tokenType) {
        this.token = token;
        this.user = user;
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String userPhone) {
        this.user = userPhone;
    }

    public Common.TOKEN_TYPE getTokenType() {
        return tokenType;
    }

    public void setTokenType(Common.TOKEN_TYPE tokenType) {
        this.tokenType = tokenType;
    }
}
