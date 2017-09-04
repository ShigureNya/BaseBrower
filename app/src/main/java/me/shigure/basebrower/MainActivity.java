package me.shigure.basebrower;

import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    private OpenWebView webView ;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipe ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (OpenWebView) findViewById(R.id.web);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        webView.load("http://47.89.20.85:8090/WaterCloud/index.html");
        webView.openLoadProgress(progressBar);
        swipe.setProgressViewEndTarget (true,290);
        swipe.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        webView.reload();
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipe.setRefreshing(false);
            }
        });
    }
}
