package com.example.coveryourapps;

public class ContractTemplate {
    String title, text;

    public ContractTemplate() {
        //Required empty constructor for firebase querys
    }
    public ContractTemplate(String title, String text) {
        this.title = title;
        this.text = text;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
