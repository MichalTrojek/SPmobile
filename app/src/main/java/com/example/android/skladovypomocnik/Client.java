package com.example.android.skladovypomocnik;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<String, Void, Void> {

    private boolean connectedToServer = true;
    private int port = 8889;
    private Context context;
    private String ip;

    public Client(String ip, Context c) {
        this.ip = ip;
        this.context = c;
    }


    @Override
    protected Void doInBackground(String... voids) {
        String message = voids[0];
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 500);
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.write(message);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            connectedToServer = false; //  this means that server is offline
            e.printStackTrace();
        }
        return null;
    }

    // sends info about success or fail
    @Override
    protected void onPostExecute(Void v) {
        if (connectedToServer) {
            Toast.makeText(context, "Data vyexportována.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Exportování selhalo.", Toast.LENGTH_SHORT).show();
        }
    }


}
