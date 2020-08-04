package com.example.coveryourapps;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class Cover {
    private String coverType, id, docID, memo, status, senderID, recipientID, content;//ID is the gay one Apple needs, docID is where firestore id is saved
    private User sender, recipient;
    private Date createdTime;

    public Cover() {
        // Default empty constructor required for pulling users from FireStore usersDB
    }
    public Cover(Map<String, Object> map){
        this.content = Objects.requireNonNull(map.get("content")).toString();
        this.coverType = Objects.requireNonNull(map.get("coverType")).toString();
        this.createdTime = (Date) map.get("createdTime");
        this.id = Objects.requireNonNull(map.get("id")).toString();
        this.memo = Objects.requireNonNull(map.get("memo")).toString();
        this.recipientID = Objects.requireNonNull(map.get("recipientID")).toString();
        this.senderID = Objects.requireNonNull(map.get("senderID")).toString();
        this.status = Objects.requireNonNull(map.get("status")).toString();
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    public void setWaiverContractString(String waiverContractString) {
        this.content = waiverContractString;
    }

    public String getCoverType() {
        return coverType;
    }

    public void setCoverType(String coverType) {
        this.coverType = coverType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    @Override
    public String toString() {
        return "Cover{" +
                "coverType='" + coverType + '\'' +
                ", id='" + id + '\'' +
                ", docID='" + docID + '\'' +
                ", memo='" + memo + '\'' +
                ", status='" + status + '\'' +
                ", senderID='" + senderID + '\'' +
                ", recipientID='" + recipientID + '\'' +
                ", content='" + content + '\'' +
                ", sender=" + sender +
                ", recipient=" + recipient +
                ", createdTime=" + createdTime +
                '}';
    }
}
