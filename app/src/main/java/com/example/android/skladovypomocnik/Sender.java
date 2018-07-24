package com.example.android.skladovypomocnik;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Sender extends AsyncTask<String, Void, Void> {


    private int port = 8889;

    private String ip;

    public Sender(String ip) {
        this.ip = ip;
    }

    @Override
    protected Void doInBackground(String... voids) {
        String message = voids[0];
        try (Socket s = new Socket(ip, port)) {
            try (PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                out.write(message);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
