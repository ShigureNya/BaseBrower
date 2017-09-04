package me.shigure.basebrower;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    private String indexUrl = null ;
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
        String url = getCurrentUrl();
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if (i == KeyEvent.KEYCODE_BACK && canGoBack()) { // 表示按返回键
                // 时的操作
                goBack(); // 后退
                // webview.goForward();//前进
            }else if(TextUtils.equals(url,indexUrl)){   //如果回退界面是当前主页 再按一次推出则退出程序
                exit();
                return true ;
            }
        }
        return true ;
    }
    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(context, "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            System.exit(0);
        }
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
        indexUrl = url ;
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
    private static boolean isAnimStart = false ;
    private static int currentProgress = 0 ;

    public void openLoadProgress(final ProgressBar mProgressBar){
        setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setAlpha(1.0f);
            }
        });
        setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                currentProgress = mProgressBar.getProgress();
                if (newProgress >= 100 && !isAnimStart) {
                    // 防止调用多次动画
                    isAnimStart = true;
                    mProgressBar.setProgress(newProgress);
                    // 开启属性动画让进度条平滑消失
                    startDismissAnimation(mProgressBar,mProgressBar.getProgress());
                } else {
                    // 开启属性动画让进度条平滑递增
                    startProgressAnimation(mProgressBar , newProgress);
                }
            }
        });
    }
    /**
     * progressBar消失动画
     */
    private void startDismissAnimation(final ProgressBar mProgressBar , final int progress) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mProgressBar, "alpha", 1.0f, 0.0f);
        anim.setDuration(1500);  // 动画时长
        anim.setInterpolator(new DecelerateInterpolator());     // 减速
        // 关键, 添加动画进度监听器
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();      // 0.0f ~ 1.0f
                int offset = 100 - progress;
                mProgressBar.setProgress((int) (progress + offset * fraction));
            }
        });

        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.GONE);
                isAnimStart = false;
            }
        });
        anim.start();
    }

    /**
     * progressBar递增动画
     */
    private void startProgressAnimation(ProgressBar mProgressBar , int newProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar, "progress", currentProgress, newProgress);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    public String getCurrentUrl(){
        return getUrl();
    }


}
