package com.example.grandcrosstranslationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ActionReceiver extends BroadcastReceiver {

    private static String originalDatabaseLocation;
    private static String originalBackupLocation;
    private static String translatedDatabaseLocation;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();

        String action=intent.getStringExtra("action");
        if(action.equals("translate")){
            translateBackground(context);
        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    private void translateBackground(Context context){
        createPaths();
        copyRaw(context, originalDatabaseLocation);
    }

    private void createPaths(){
        originalDatabaseLocation = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.netmarble.nanatsunotaizai/files/SqliteData/localizestring.sqlite";
        originalBackupLocation = Environment.getExternalStorageDirectory().toString() + "/TranslatedGC/OriginalDatabase/localizestring.sqlite";
        translatedDatabaseLocation = Environment.getExternalStorageDirectory().toString() + "/TranslatedGC/TranslatedDatabase/localizestring.sqlite";
    }

    private void copyRaw(Context context, String dst){
        InputStream in = context.getResources().openRawResource(R.raw.localizestring);
        try {
            FileOutputStream out = new FileOutputStream(dst);
            byte[] buff = new byte[1024];
            int read = 0;

            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (Exception e){
            Log.e("Exception", e.getMessage());
        }
    }
}
