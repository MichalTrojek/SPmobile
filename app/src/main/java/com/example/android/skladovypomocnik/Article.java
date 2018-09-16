package com.example.android.skladovypomocnik;

public class Article {

    private String ean, name;
    private int amount;


    public Article(String ean, int amount, String name) {
        this.ean = ean;
        this.amount = amount;
        this.name = name;
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

    public String getName() {
        return this.name;
    }


}
