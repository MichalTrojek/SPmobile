package com.example.android.skladovypomocnik;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Func;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

public class DatabaseDownloader {

    private int onlineDbVersionNumber;
    private Context context;
    private AlertDialog dialog;
    private ProgressBar progress;
    private Settings settings;

    public DatabaseDownloader(Context context, int onlineDbVersionNumber, AlertDialog dialog, ProgressBar progress, Settings settings) {
        this.context = context;
        this.onlineDbVersionNumber = onlineDbVersionNumber;
        this.dialog = dialog;
        this.progress = progress;
        this.settings = settings;
    }

    public void download(String databaseUrl) {
        downloadDatabaseFromUrl(databaseUrl);
    }

    private void downloadDatabaseFromUrl(String databaseUrl) {
        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(context).setDownloadConcurrentLimit(3).build();

        Fetch fetch = Fetch.Impl.getInstance(fetchConfiguration);
        String sdCard = Environment.getExternalStorageDirectory().toString();
        final Request request = new Request("http://www.skladovypomocnik.cz/BooksDatabase.db", sdCard + "/temp/BooksDatabase.db");
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.WIFI_ONLY);


        fetch.enqueue(request, new Func<Request>() {
            @Override
            public void call(@NotNull Request result) {

            }
        }, new Func<Error>() {
            @Override
            public void call(@NotNull Error result) {
                Toast.makeText(context, "Error: " + result.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        FetchListener fetchListener = new FetchListener() {
            @Override
            public void onAdded(Download download) {

            }


            @Override
            public void onQueued(Download download, boolean b) {
                if (request.getId() == download.getId()) {

                }
            }

            @Override
            public void onWaitingNetwork(Download download) {
                dialog.hide();
                dialog.dismiss();
                Toast.makeText(context, "Stahování databáze bylo přerušeno zdůvody ztráty spojení s WIFI.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCompleted(Download download) {
                try {
                    String sdCard = Environment.getExternalStorageDirectory().toString();
                    File sourceFile = new File(sdCard + "/temp/BooksDatabase.db");
                    File destinationFile = new File("/data/data/com.mtr.spmobile/databases/BooksDatabase.db");
                    copyFileFromExternalToInternalMemory(sourceFile, destinationFile);
                    Toast.makeText(context
                            , "Stahovaní dokončeno", Toast.LENGTH_SHORT).show();
                    dialog.setCancelable(true);
                    dialog.hide();
                    dialog.dismiss();

                    restartApplication();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void restartApplication() {
                Intent intent = new Intent(context, MainActivity.class);
                int mPendingIntentId = 123321;
                PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 3000, mPendingIntent);
                System.exit(0);
            }


            @Override
            public void onError(Download download, Error error, Throwable throwable) {

            }

            @Override
            public void onDownloadBlockUpdated(Download download, DownloadBlock downloadBlock,
                                               int i) {

            }

            @Override
            public void onStarted(Download download, List<? extends DownloadBlock> list,
                                  int i) {
                Toast.makeText(context, "Začalo stahování, čekejte.", Toast.LENGTH_SHORT).show();
                progress.setMax(100);
                progress.setProgress(0);
                dialog.show();
            }

            @Override
            public void onProgress(@NotNull Download download, long l, long l1) {
                progress.setProgress(download.getProgress());
            }

            @Override
            public void onPaused(Download download) {

            }

            @Override
            public void onResumed(Download download) {

            }

            @Override
            public void onCancelled(Download download) {

            }

            @Override
            public void onRemoved(Download download) {

            }

            @Override
            public void onDeleted(Download download) {

            }
        };

        fetch.addListener(fetchListener);
    }

    public void copyFileFromExternalToInternalMemory(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
        settings.setCurrentDatabaseVersion(onlineDbVersionNumber);
        deleteDir(new File("/sdcard/temp"));
    }

    private void deleteDir(File file) {
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                deleteEmptyDir(file);
            } else {
                File files[] = file.listFiles();
                for (File fileDelete : files) {
                    deleteDir(fileDelete);
                }

                if (file.list().length == 0) {
                    deleteEmptyDir(file);
                }
            }
        } else {
            file.delete();
        }
    }

    private void deleteEmptyDir(File file) {
        file.delete();
    }

}
