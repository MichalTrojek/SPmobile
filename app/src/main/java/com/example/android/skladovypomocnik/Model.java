package com.example.android.skladovypomocnik;

import java.util.HashMap;
import java.util.List;

public class Model {

    private static Model INSTANCE;
    private HashMap<String, String> mapOfNames;
    private List<Article> articles;

    public Model() {

    }


    public static synchronized Model getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Model();
        }
        return INSTANCE;
    }


    public void setMapOfNames(HashMap<String, String> mapOfNames) {
        this.mapOfNames = mapOfNames;
    }

    public HashMap<String, String> getMapOfNames() {
        return this.mapOfNames;
    }

    public String getName(String ean) {
        return mapOfNames.get(ean);
    }


}
