package com.example.android.skladovypomocnik.auth;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Authentication {

    public static final String TAG = Authentication.class.getSimpleName();
    private static volatile Authentication INSTANCE;


    private static final String IS_TURNED_OFF = "isturnedoff";

    private static final String PREFERENCES_NAME = "cz.mtr.analyzaprodeju.authentication";


    private android.content.SharedPreferences mPrefs;


    private JsonObject jsonObject;


    public Authentication(Context context) {
        if (INSTANCE != null) {
            throw new RuntimeException("Use getInstance() to get single instance of this class.");
        } else {
            mPrefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void init(Context context) {
        if (INSTANCE == null) {
            synchronized (Authentication.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Authentication(context);
                }
            }
        }
    }

    public static synchronized Authentication getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(Authentication.class.getSimpleName() + "is not initilized, call init() method first");
        }
        return INSTANCE;
    }




    private void setIsTurnedOff(Boolean off) {
        mPrefs.edit().putBoolean(IS_TURNED_OFF, off).commit();
    }

    public boolean isTurnedOff() {
        return mPrefs.getBoolean(IS_TURNED_OFF, false);
    }




    public void check() {
        getData();

    }


    private void getData() {

        final String url = "http://skladovypomocnik.cz/authinfo.json";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    jsonObject = new Gson().fromJson(data, JsonObject.class);
                    setIsTurnedOff(jsonObject.get("readerTurnOff").getAsBoolean());
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });


    }


}
