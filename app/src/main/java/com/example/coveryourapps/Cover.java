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
    private boolean droppedDown;//Used to keep the cover dropped down after refreshing on home screen
    private ArrayList<String> pictures;

    public Cover() {
        droppedDown = false;
        // Default empty constructor required for pulling users from FireStore usersDB
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

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public boolean isDroppedDown() {
        return droppedDown;
    }

    public void setDroppedDown(boolean droppedDown) {
        this.droppedDown = droppedDown;
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
