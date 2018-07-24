package com.example.android.skladovypomocnik;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Sender extends AsyncTask<String, Void, Void> {


    int port = 8889;
    //    Socket s;
//    DataOutputStream os;
//    PrintWriter pw;
    String ip;

    public Sender(String ip) {
        this.ip = ip;
    }

    @Override
    protected Void doInBackground(String... voids) {
        String message = voids[0];
//        try (Socket s = new Socket(ip, port)) {
//            pw = new PrintWriter(s.getOutputStream());
//            pw.write(message);
//            pw.flush();
//            pw.close();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try (Socket s = new Socket(ip, port)) {
            try (PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                out.write(message);
                s.close();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
