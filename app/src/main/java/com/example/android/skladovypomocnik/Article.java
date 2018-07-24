package com.example.android.skladovypomocnik;

public class Article {

    private String ean;
    private int amount;


    public Article(String ean, int amount) {
        this.ean = ean;
        this.amount = amount;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getEan() {
        return this.ean;
    }

    public int getAmount() {
        return this.amount;
    }


}
