package com.sisu.dns;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;
    private static final int VPN_REQUEST = 100;
    private String pendingDns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new Bridge(), "SisuNative");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class Bridge {
        @JavascriptInterface
        public void startVpn(String dnsList) {
            pendingDns = dnsList;
            Intent intent = VpnService.prepare(MainActivity.this);
            if (intent != null) {
                startActivityForResult(intent, VPN_REQUEST);
            } else {
                launchService(dnsList);
            }
        }

        @JavascriptInterface
        public void stopVpn() {
            Intent i = new Intent(MainActivity.this, SisuVpnService.class);
            i.setAction("STOP");
            startService(i);
            webView.post(() -> webView.evaluateJavascript(
                "window.onNativeVpnResult(false)", null));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST) {
            if (resultCode == RESULT_OK) {
                launchService(pendingDns);
            } else {
                webView.post(() -> webView.evaluateJavascript(
                    "window.onNativeVpnResult(false)", null));
            }
        }
    }

    private void launchService(String dnsList) {
        Intent i = new Intent(this, SisuVpnService.class);
        i.setAction("START");
        i.putExtra("DNS", dnsList);
        startService(i);
        webView.post(() -> webView.evaluateJavascript(
            "window.onNativeVpnResult(true)", null));
    }
          }
