package com.sisu.dns;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class SisuVpnService extends VpnService {
    private static final String TAG = "SisuVpn";
    private ParcelFileDescriptor tun;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        String action = intent.getAction();
        if ("STOP".equals(action)) {
            stopVpn();
        } else if ("START".equals(action)) {
            startVpn(intent.getStringExtra("DNS"));
        }
        return START_STICKY;
    }

    private void startVpn(String dnsCsv) {
        if (dnsCsv == null || dnsCsv.isEmpty()) return;
        stopVpn();
        List<String> dnsList = Arrays.asList(dnsCsv.split(","));
        try {
            Builder b = new Builder();
            b.setSession("SISU_DNS");
            b.addAddress("10.111.222.1", 32);
            b.addRoute("10.111.222.0", 24);
            for (String dns : dnsList) {
                dns = dns.trim();
                if (dns.isEmpty()) continue;
                try { b.addDnsServer(dns); } catch (Exception e) {
                    Log.w(TAG, "skip: " + dns);
                }
            }
            tun = b.establish();
            Log.i(TAG, "VPN established");
        } catch (Exception e) {
            Log.e(TAG, "fail", e);
        }
    }

    private void stopVpn() {
        try {
            if (tun != null) { tun.close(); tun = null; }
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() { super.onDestroy(); stopVpn(); }

    @Override
    public void onRevoke() { super.onRevoke(); stopVpn(); }
}
