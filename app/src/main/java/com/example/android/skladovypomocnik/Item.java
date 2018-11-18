package com.example.android.skladovypomocnik;

public class Item {

    private String ean, name, price;

    public Item(String ean, String name, String price) {
        this.ean = ean;
        this.name = name;
        this.price = price;
    }


    public String getEan() {
        return this.ean;
    }

    public String getName() {
        return this.name;
    }

    public String getPrice() {
        return this.price;
    }
}
