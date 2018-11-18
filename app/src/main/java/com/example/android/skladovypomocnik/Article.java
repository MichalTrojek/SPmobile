package com.example.android.skladovypomocnik;

public class Article {

    private String ean, name, price;
    private int amount;


    public Article(String ean, int amount, String name, String price) {
        this.ean = ean;
        this.amount = amount;
        this.name = name;
        this.price = price;
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

    public String getPrice() {
        return this.price;
    }
}
