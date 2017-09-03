package me.shigure.basebrower;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private OpenWebView webView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (OpenWebView) findViewById(R.id.web);
        webView.load("http://www.qq.com");
    }
}
