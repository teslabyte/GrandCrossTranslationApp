package com.example.grandcrosstranslationapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    private static String originalDatabaseLocation;
    private static String originalBackupLocation;
    private static String translatedDatabaseLocation;

    private int READ_CODE = 1;
    private int WRITE_CODE = 2;
    private int PERMISSION_ALL = 3;

    private boolean permissionsGranted = false;

    private String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createPaths();
        addNotification();

        if(!checkForPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                || !checkForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        else initialize();

    }

    private boolean findCurrentDatabase(){
            String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.netmarble.nanatsunotaizai/files/SqliteData";
            File f = new File(path);
            File files[] = f.listFiles();
            for (File a : files) {
                if (a.getName().equals("LocalizeString.sqlite")) return true;
            }
            return false;
    }

    private void copyDatabase(String src, String dst){
        File originalDatabase = new File(src);
        File copiedFile = new File(dst);
        try (InputStream in = new FileInputStream(originalDatabase)) {
            try (OutputStream out = new FileOutputStream(copiedFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (Exception e){

            }
        } catch (Exception e){

        }
    }

    public boolean checkForPermission(String permission){
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void addToTextView(String toAdd, boolean toReset){
        TextView v1 = (TextView)findViewById(R.id.textView);
        if(!toReset){
            v1.append("\n" + toAdd);
        }
        else v1.setText(toAdd);
    }

    private boolean createDatabasesFolder(){
        boolean f1, f2, f3;
        String path = Environment.getExternalStorageDirectory().toString()+"/TranslatedGC";
        File folder = new File(path);
        if(!folder.exists()) f1 = folder.mkdirs();
        else f1 = true;

        File folder1 = new File(path+"/OriginalDatabase");
        if(!folder1.exists()) f2 = folder1.mkdirs();
        else f2 = true;

        File folder2 = new File(path+"/TranslatedDatabase");
        if(!folder2.exists()) f3 = folder2.mkdirs();
        else f3 = true;

        return f1 && f2 && f3;
    }

    private void initialize(){
        if(findCurrentDatabase()) addToTextView("JAPANESE DATABASE FOUND!",false);
        else addToTextView("JAPANESE DATABASE NO FOUND!", false);

    }

    private void copyRaw(String source){
        InputStream in = getResources().openRawResource(R.raw.localizestring);
        try {
            FileOutputStream out = new FileOutputStream(source);
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

    public void translateButton(View view){
        if(!new File(originalBackupLocation).exists()) copyDatabase(originalDatabaseLocation, originalBackupLocation);
        addToTextView("DATABASE BACKUP DONE", false);
        //copyDatabase(translatedDatabaseLocation, originalDatabaseLocation);
        copyRaw(originalDatabaseLocation);
        addToTextView("TRANSLATION DONE", false);

    }

    private void createPaths(){
        originalDatabaseLocation = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.netmarble.nanatsunotaizai/files/SqliteData/localizestring.sqlite";
        originalBackupLocation = Environment.getExternalStorageDirectory().toString() + "/TranslatedGC/OriginalDatabase/localizestring.sqlite";
        translatedDatabaseLocation = Environment.getExternalStorageDirectory().toString() + "/TranslatedGC/TranslatedDatabase/localizestring.sqlite";
    }

    private void addNotification(){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("TRANSLATE_7DS",
                    "TRANSLATE",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(getApplicationContext(), ActionReceiver.class);
        intent.putExtra("action","translate");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "TRANSLATE_7DS")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("7DS-Translate")
                .setContentText("Grand Cross translation is running...")
                .addAction(R.drawable.ic_translate, "Translate", pi);


        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == READ_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //databaseFound = findCurrentDatabase();
            }
            else{
                Log.d("TEST", "THERE IS NO READ PERMISSION");
            }
        }
        else if(requestCode == WRITE_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                createDatabasesFolder();
            }
            else{
                Log.d("TEST", "THERE IS NO WRITE PERMISSION");
            }
        }
        else if(requestCode == PERMISSION_ALL){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                permissionsGranted = true;
                initialize();
            }
            else{
                addToTextView("PERMISSIONS NOT GRANTED!\nTHIS APP WON'T WORK", true);
            }
        }
    }

}
