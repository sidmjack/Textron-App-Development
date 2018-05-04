package com.sidneyjackson.textron;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {

    WebView webView;

    //Initialize Application Context
    private static Context appContext;

    //Get Application Context (for use in external functions)
    public static Context getContext() {
        return appContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        webView =(WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(), "jquery.js");
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/wifi.html");
    }

}
