package com.shafiq.playhub;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.webkit.WebResourceError;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.widget.FrameLayout;
import androidx.webkit.WebViewAssetLoader;

public final class MainActivity extends Activity {
  private static final String LIVE_URL="https://shafiqjumaa.github.io/shafiq-graphics/playhub/";
  private boolean offlineFallback=false;
  private WebView webView;
  private FrameLayout root;
  private View customView;
  private WebChromeClient.CustomViewCallback customCallback;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    root=new FrameLayout(this);
    root.setBackgroundColor(0xff07141f);
    setContentView(root);
    final WebViewAssetLoader loader=new WebViewAssetLoader.Builder()
        .addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
    webView=new WebView(this);
    webView.setBackgroundColor(0xff07141f);
    root.addView(webView,new FrameLayout.LayoutParams(-1,-1));
    WebSettings s=webView.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setAllowFileAccess(false);
    s.setAllowContentAccess(false);
    s.setAllowUniversalAccessFromFileURLs(false);
    s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    webView.addJavascriptInterface(new Bridge(),"Android");
    webView.setWebViewClient(new WebViewClient() {
      @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest req) {
        return loader.shouldInterceptRequest(req.getUrl());
      }
      @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest req) {
        String host=req.getUrl().getHost();
        if("appassets.androidplatform.net".equals(host)||"shafiqjumaa.github.io".equals(host))return false;
        try{startActivity(new Intent(Intent.ACTION_VIEW,req.getUrl()));}catch(Exception ignored){}
        return true;
      }
      @Override public void onReceivedError(WebView v,WebResourceRequest req,WebResourceError error){
        if(req.isForMainFrame()&&!offlineFallback){
          offlineFallback=true;
          v.loadUrl("https://appassets.androidplatform.net/assets/index.html");
        }
      }
    });
    webView.setWebChromeClient(new WebChromeClient() {
      @Override public void onShowCustomView(View view,CustomViewCallback cb) {
        if(customView!=null){cb.onCustomViewHidden();return;}
        customView=view;customCallback=cb;
        root.addView(view,new FrameLayout.LayoutParams(-1,-1));
        webView.setVisibility(View.GONE);
      }
      @Override public void onHideCustomView(){hideCustom();}
    });
    webView.loadUrl(LIVE_URL+"?apk=1&cache="+System.currentTimeMillis());
  }
  private void hideCustom() {
    if(customView==null)return;
    root.removeView(customView);customView=null;
    webView.setVisibility(View.VISIBLE);
    if(customCallback!=null){customCallback.onCustomViewHidden();customCallback=null;}
  }
  private final class Bridge {
    @JavascriptInterface public void setLandscape(boolean enabled) {
      runOnUiThread(()->setRequestedOrientation(enabled?
          ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED));
    }
    @JavascriptInterface public void copyText(String s) {
      runOnUiThread(()->((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE))
        .setPrimaryClip(ClipData.newPlainText("PlayHub",s)));
    }
    @JavascriptInterface public void shareText(String s) {
      runOnUiThread(()->{
        Intent i=new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,s);
        startActivity(Intent.createChooser(i,"شارك دعوة اللعب"));
      });
    }
  }
  @Override public void onBackPressed(){
    if(customView!=null){hideCustom();return;}
    if(webView!=null)webView.evaluateJavascript(
      "window.PlayHubAndroidBack?window.PlayHubAndroidBack():history.back()",null);
    else super.onBackPressed();
  }
  @Override public void onDestroy(){
    if(webView!=null){root.removeView(webView);webView.destroy();webView=null;}
    super.onDestroy();
  }
}
