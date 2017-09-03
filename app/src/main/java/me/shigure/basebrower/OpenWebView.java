package me.shigure.basebrower;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import static android.content.ContentValues.TAG;

/**
 * Created by Shigure on 2017/9/3.
 */

public class OpenWebView extends WebView implements View.OnKeyListener {
    private Context context ;
    public OpenWebView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public OpenWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public OpenWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init(){
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        openWebStorage(settings);
        openWebDatabase(context,settings);
        openApplicationCache(context,settings);
        openSupportPlugin(settings);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        setWebViewClient(new BaseWebViewClient());
        setOnKeyListener(this);
    }


    /**
     * 开启浏览器缓存
     * @param settings 设定对象
     */
    private void openWebStorage(WebSettings settings){
        settings.setDomStorageEnabled(true);
    }

    /**
     * 打开浏览器数据库开关 官方不推荐使用
     * @param context 上下文
     * @param settings 设置对象
     */
    private void openWebDatabase(Context context , WebSettings settings){
        settings.setDatabaseEnabled(true);
        final String dbPath = context.getDir("db",Context.MODE_PRIVATE).getPath();
        settings.setDatabasePath(dbPath);
    }

    /**
     * 打开APP缓存开关 官方不推荐使用
     * @param context 上下文
     * @param settings 设置对象
     */
    private void openApplicationCache(Context context , WebSettings settings){
        settings.setAppCacheEnabled(true);
        final String cachePath = context.getDir("cache",Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(cachePath);
        settings.setAppCacheMaxSize(5*1024*1024);
    }

    /**
     * 开启插件支持
     */
    private void openSupportPlugin(WebSettings setting){
        setting.setUseWideViewPort(false);  //将图片调整到适合webview的大小
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局
        setting.supportMultipleWindows();  //多窗口
        setting.setAllowFileAccess(true);  //设置可以访问文件
        setting.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点
        setting.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        setting.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        setting.setLoadsImagesAutomatically(true);  //支持自动加载图片
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if (i == KeyEvent.KEYCODE_BACK && canGoBack()) { // 表示按返回键
                // 时的操作
                goBack(); // 后退
                // webview.goForward();//前进
                return true; // 已处理
            }
        }
        return false;
    }

    private class BaseWebViewClient extends WebViewClient{
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url.contains("[tag]"))
            {
                String localPath = url.replaceFirst("^http.*[tag]\\]", "");
                try
                {
                    InputStream is = context.getAssets().open(localPath);
                    Log.d(TAG, "shouldInterceptRequest: localPath " + localPath);
                    String mimeType = "text/javascript";
                    if (localPath.endsWith("css"))
                    {
                        mimeType = "text/css";
                    }
                    return new WebResourceResponse(mimeType, "UTF-8", is);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    public void load(String url){
        loadUrl(url);
    }

    /**
     *  开启两指缩放
     */
    public void openZoom(){
        getSettings().setSupportZoom(true);
    }

    /**
     * 清理缓存
     */
    public void clearCache(){
        clearCache(true);
        clearHistory();
    }


}
