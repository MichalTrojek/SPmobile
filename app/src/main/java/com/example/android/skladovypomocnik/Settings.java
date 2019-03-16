package com.example.android.skladovypomocnik;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Settings {
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;


    public Settings(Context context) {
        prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getIp() {
        return prefs.getString("ip", "");
    }

    public void setIP(String ip) {
        editor = prefs.edit();
        editor.putString("ip", ip);
        editor.apply();
    }

    public int getCurrentDatabaseVersion() {
        return prefs.getInt("currentDbVersion", -99);
    }

    public void setCurrentDatabaseVersion(int currentDbVersion) {
        editor = prefs.edit();
        editor.putInt("currentDbVersion", currentDbVersion);
        editor.apply();
    }


    public void setArticles(ArrayList<Article> articles) {
        editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(articles);
        editor.putString("articles", json);
        editor.apply();
    }

    public ArrayList<Article> getArticles() {
        try {
            Gson gson = new Gson();
            String json = prefs.getString("articles", null);
            Type type = new TypeToken<ArrayList<Article>>() {
            }.getType();
            ArrayList<Article> articles;
            articles = gson.fromJson(json, type);
            if (articles == null) {
                articles = new ArrayList<>();
            }
            return articles;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


}
