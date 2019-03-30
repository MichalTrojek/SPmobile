package com.example.android.skladovypomocnik;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class DatabaseLoaderAsyncTask extends AsyncTask<Void, Integer, HashMap<String, Item>> {


    private Context context;
    DatabaseAccess db;

    private AlertDialog dialog;
    private ProgressBar progress;
    private TextView loadingInfoTextView;
    int max;


    public DatabaseLoaderAsyncTask(Context context, AlertDialog dialog, ProgressBar progress, TextView loadingInfoTextView) {
        this.context = context;
        this.db = db;
        this.dialog = dialog;
        this.progress = progress;
        this.loadingInfoTextView = loadingInfoTextView;
        this.db = DatabaseAccess.getInstance(context);
        db.open();
    }

    @Override
    protected void onPreExecute() {
        max = (int) db.getTableSize();
        progress.setMax(max);
        progress.setProgress(0);
        loadingInfoTextView.setText("Velikost databáze : " + max);
        dialog.show();

    }


    @Override
    protected HashMap<String, Item> doInBackground(Void... voids) {
        HashMap<String, Item> map = new HashMap<>();
        Cursor cursor = db.getDb().rawQuery("Select EAN, NAME, PRICE from articles", new String[]{});
        cursor.moveToFirst();
        int counter = 0;
        while (!cursor.isAfterLast()) {
            map.put(cursor.getString(0), new Item(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
            cursor.moveToNext();
            counter++;
            publishProgress(counter);
        }
        return map;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progress.setProgress(values[0]);
    }


    @Override
    protected void onPostExecute(HashMap<String, Item> map) {
        db.close();
        Model.getInstance().setNamesAndPrices(map);
        Toast.makeText(context, "Nahráno " + map.size() + " položek", Toast.LENGTH_SHORT).show();
        dialog.setCancelable(true);
        dialog.hide();
        dialog.dismiss();
    }

}

