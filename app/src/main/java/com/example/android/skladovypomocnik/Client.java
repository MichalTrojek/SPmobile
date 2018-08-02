package com.example.android.skladovypomocnik;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<String, Void, Void> {


    private int port = 8889;

    private String ip;

    public Client(String ip) {
        this.ip = ip;
    }

    @Override
    protected Void doInBackground(String... voids) {
        String message = voids[0];
        try (Socket socket = new Socket(ip, port)) {
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.write(message);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Override
//    protected Void doInBackground(String... voids) {
//        String message = voids[0];
//        try (Socket socket = new Socket(ip, port);
//             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
//            Log.d("TEST", input.readLine());
//            out.write(message);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


}
