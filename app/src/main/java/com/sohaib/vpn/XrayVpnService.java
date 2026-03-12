package com.sohaib.vpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class XrayVpnService extends VpnService {

    private ParcelFileDescriptor vpnInterface = null;
    private Process xrayProcess = null;
    private static final String TAG = "SohaibVPN";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "بدء خدمة الـ VPN...");
        setupVpn();
        return START_STICKY;
    }

    private void setupVpn() {
        try {
            if (vpnInterface != null) return;

            Builder builder = new Builder();
            builder.setSession("Sohaib VPN")
				.addAddress("10.0.0.1", 24)
				.addDnsServer("8.8.8.8")
				.addRoute("0.0.0.0", 0);

            vpnInterface = builder.establish();
            Log.d(TAG, "تم إنشاء واجهة الشبكة بنجاح");

            runXrayCore();

        } catch (Exception e) {
            Log.e(TAG, "خطأ في الإعداد: " + e.getMessage());
        }
    }

    private void runXrayCore() {
        try {
            File coreFile = new File(getFilesDir(), "xray_engine");
            File configFile = new File(getFilesDir(), "config.json");

            // نسخ وفك تشفير المحرك
            decodeAndSaveAsset("xray_assets.txt", coreFile);

            // نسخ ملف الإعدادات بشكل عادي
            copyNormalAsset("config.json", configFile);

            // إعطاء صلاحية التنفيذ
            coreFile.setExecutable(true);

            // التشغيل الفعلي
            String cmd = coreFile.getAbsolutePath() + " run -c " + configFile.getAbsolutePath();
            xrayProcess = Runtime.getRuntime().exec(cmd);

            Log.d(TAG, "تم تشغيل Xray بنجاح!");

        } catch (Exception e) {
            Log.e(TAG, "فشل في تشغيل المحرك: " + e.getMessage());
        }
    }

    // دالة فك التشفير من Base64 (لخداع AIDE)
    private void decodeAndSaveAsset(String assetName, File outFile) throws Exception {
        InputStream is = getAssets().open(assetName);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        // فك التشفير برمجياً
        byte[] decodedBytes = Base64.decode(buffer.toByteArray(), Base64.DEFAULT);

        FileOutputStream os = new FileOutputStream(outFile);
        os.write(decodedBytes);
        os.close();
        is.close();
    }

    private void copyNormalAsset(String assetName, File outFile) throws Exception {
        InputStream is = getAssets().open(assetName);
        FileOutputStream os = new FileOutputStream(outFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        os.close();
        is.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (xrayProcess != null) xrayProcess.destroy();
        try {
            if (vpnInterface != null) vpnInterface.close();
        } catch (Exception e) {
            Log.e(TAG, "Error closing VPN");
        }
    }
}

