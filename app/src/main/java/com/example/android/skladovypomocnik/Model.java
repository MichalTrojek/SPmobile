package com.example.android.skladovypomocnik;

import java.util.HashMap;

public class Model {

    private static Model INSTANCE;
    private HashMap<String, Item> mapOfNames;


    public Model() {

    }


    public static synchronized Model getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Model();
        }
        return INSTANCE;
    }


    public void setNamesAndPrices(HashMap<String, Item> mapOfNames) {
        this.mapOfNames = mapOfNames;
    }

    public HashMap<String, Item> getNamesAndPrices() {
        return this.mapOfNames;
    }

    public String getName(String ean) {
        try {
            return mapOfNames.get(ean).getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NÁZEV NENALEZEN";
    }


    public String getPrice(String ean) {
        try {
            return mapOfNames.get(ean).getPrice();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "***";
    }


}
