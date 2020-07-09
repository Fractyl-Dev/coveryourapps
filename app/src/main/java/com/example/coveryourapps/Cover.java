package com.example.coveryourapps;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class Cover {
    private String coverType, id, recipientID, senderID, memo, notes, status;
    private ArrayList<String> usersInvolved;
    private Date createdTime;

    //Cash
    private Double cashAmount;


    //Lending
    private ImageView lendingImage;
    private String lendingDueDate;

    //Waivers
    private String templateID, content;

    public Cover() {
        // Default empty constructor required for pulling users from FireStore usersDB
    }

    //Cash Constructor
    public Cover(Double cashAmount, String type, String memo, String notes, String status, ArrayList<String> usersInvolved) {
        this.cashAmount = cashAmount;
        this.coverType = type;
        this.memo = memo;
        this.notes = notes;
        this.status = status;
        this.usersInvolved = usersInvolved;
    }

    //Lending Constructor
    public Cover(ImageView lendingImage, String lendingDueDate, String type, String memo, String notes, String status, ArrayList<String> usersInvolved) {
        //this.lendingImage = lendingImage;
        this.lendingDueDate = lendingDueDate;
        this.coverType = type;
        this.memo = memo;
        this.notes = notes;
        this.status = status;
        this.usersInvolved = usersInvolved;
    }

    //Waiver Constructor
    public Cover(String waiverTemplateID, String waiverContractString, String type, String memo, String notes, String status, ArrayList<String> usersInvolved) {
        this.templateID = waiverTemplateID;
        this.content = waiverContractString;
        this.coverType = type;
        this.memo = memo;
        this.notes = notes;
        this.status = status;
        this.usersInvolved = usersInvolved;
    }


    public String getType() {
        return coverType;
    }

    public void setType(String type) {
        this.coverType = type;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getUsersInvolved() {
        return usersInvolved;
    }

    public void setUsersInvolved(ArrayList<String> usersInvolved) {
        this.usersInvolved = usersInvolved;
    }


    public Double getCashAmount() {
        if (cashAmount != null) {
            return cashAmount;
        }
        return 0.0;
    }

    public void setCashAmount(Double cashAmount) {
        this.cashAmount = cashAmount;
    }

    public ImageView getLendingImage() {
        return lendingImage;
    }

    public void setLendingImage(ImageView lendingImage) {
        this.lendingImage = lendingImage;
    }

    public String getLendingDueDate() {
        if (lendingDueDate != null) {
            return lendingDueDate;
        }
        return "";
    }

    public void setLendingDueDate(String lendingDueDate) {
        this.lendingDueDate = lendingDueDate;
    }

    public String getWaiverTemplateID() {
        if (lendingDueDate != null) {
            return templateID;
        }
        return "";
    }

    public void setWaiverTemplateID(String waiverTemplateID) {
        this.templateID = waiverTemplateID;
    }

    public String getWaiverContractString() {
        if (lendingDueDate != null) {
            return content;
        }
        return "";
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

    public String getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    public String getSenterID() {
        return senderID;
    }

    public void setSenterID(String senterID) {
        this.senderID = senterID;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getTemplateID() {
        return templateID;
    }

    public void setTemplateID(String templateID) {
        this.templateID = templateID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
