package me.shigure.basebrower;

import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    private OpenWebView webView ;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (OpenWebView) findViewById(R.id.web);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        webView.load("http://47.89.20.85:8090/Omega/index.html");
        webView.openLoadProgress(progressBar);
    }
}
