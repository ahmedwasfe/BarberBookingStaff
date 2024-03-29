package com.ahmet.barberbookingstaff.model;

import java.util.List;

public class FCMResponse {

    private long multicastId;
    private int success;
    private int failure;
    private int canonicalIds;
    private List<Result> mLIstResult;

    public FCMResponse() {
    }

    public long getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(long multicastId) {
        this.multicastId = multicastId;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCanonicalIds() {
        return canonicalIds;
    }

    public void setCanonicalIds(int canonicalIds) {
        this.canonicalIds = canonicalIds;
    }

    public List<Result> getmLIstResult() {
        return mLIstResult;
    }

    public void setmLIstResult(List<Result> mLIstResult) {
        this.mLIstResult = mLIstResult;
    }
}

class Result{

    private String messageId;

    public Result() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
