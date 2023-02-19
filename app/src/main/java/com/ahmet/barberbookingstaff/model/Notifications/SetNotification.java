package com.ahmet.barberbookingstaff.model.Notifications;

import com.google.firebase.firestore.FieldValue;

public class SetNotification {

    private String uuid, title, content;
    private boolean read;
    private FieldValue serverTimestamp;

    public SetNotification() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public FieldValue getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(FieldValue serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
}
