package com.bs.function;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bs.easy_chat.R;
import com.bs.util.BaseActivity;

public class WebActivity extends BaseActivity {

    LinearLayout backView;
    WebView webView;
    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_layout);

        ((TextView)findViewById(R.id.title_name)).setText(getIntent().getExtras().getString("title"));

        backView = (LinearLayout) findViewById(R.id.back_layout);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setMax(100);

        webView = (WebView) findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        initWebView(webView);
        webView.loadUrl(getIntent().getExtras().getString("link"));
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            if(webView.canGoBack())
                webView.goBack();
            else
                this.finish();
        }
        return true;
    }

    public void  initWebView(WebView wv)
    {
        //获取设置对象
        WebSettings settings = wv.getSettings();
        //启用JavaScript--启用此项jQuery Mobile才能正常工作
        settings.setJavaScriptEnabled(true);

        //启用手动缩放工具 并且隐藏缩放工具
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        //将网页内容自适应屏幕大小
        settings.setUseWideViewPort(true);
        //settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setLoadWithOverviewMode(true);

        //设置字体显示的缩放比，默认为100
        settings.setTextZoom(100);
        //设置使用缓存 优先使用网络加载
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        //设置禁用横向滚动条
        wv.setHorizontalScrollBarEnabled(false);
        //设置启用纵向滚动条
        wv.setVerticalScrollBarEnabled(true);

        //设置进度条
        wv.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if(newProgress>=100)
                {
                    progressBar.setVisibility(view.GONE);
                }else
                {
                    progressBar.setVisibility(view.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view,newProgress);
            }
        });
        wv.setWebViewClient
        (
            new WebViewClient()
            {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if(url == null) return false;

                    if(url.startsWith("http")||url.startsWith("https"))
                    {
                        view.loadUrl(url);
                        return true;
                    }

                    //如果访问的网站检测出是手机用户并且想要跳转到它的APP
                    else
                    {
                        try{
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                            return true;
                        }catch (Exception e){//手机中没有安装它的APP
                            return false;
                        }
                    }
                }
            }
        );
    }
}
