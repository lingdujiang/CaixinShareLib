package com.caixinnews.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.util.Random;

/**
 * Created by admin on 2017/1/24.
 */
public class CaixinLogin {
    private Context mContext;
    private AuthInfo mAuthInfo;

    /**
     * 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能
     */
    private Oauth2AccessToken mAccessToken;

    /**
     * 注意：SsoHandler 仅当 SDK 支持 SSO 时有效
     */
    public static SsoHandler mSsoHandler;

    public CaixinLogin(Activity activity) {
        this.mContext = activity;
    }

    /**
     * 微博登录(授权)
     *
     * @param listener
     */
    public void LoginFromWeibo(WeiboAuthListener listener) {
        mAuthInfo = new AuthInfo(mContext, Constants.APP_KEY_WEIBO, Constants.REDIRECT_URL_WEIBO,
                Constants.SCOPE_WEIBO);
        mSsoHandler = new SsoHandler((Activity) mContext, mAuthInfo);
        //如果手机安装了微博客户端则使用客户端授权,没有则进行网页授权
        mSsoHandler.authorize(listener);
    }

    /**
     * 微博SSO 授权时，需要在 相应activity的onActivityResult()} 中调用,否则WeiboAuthListener接口中回调方法不会执行
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public static void onActivityResultForWeibo(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
            mSsoHandler = null;
        }
    }

    /**
     * QQ登录
     *
     * @param iUiListener
     */
    public void LoginFromQQ(IUiListener iUiListener) {
        Tencent mTencent = Tencent.createInstance(Constants.APP_ID_QQ, mContext);
        mTencent.login((Activity) mContext, "all", iUiListener);
    }

    public void LoginOutFromQQ() {
        Tencent mTencent = Tencent.createInstance(Constants.APP_ID_QQ, mContext);
        mTencent.logout(mContext);
    }

    /**
     * QQ登录时，需要在 相应activity的onActivityResult()} 中调用，否则回调中方法不执行
     */
    public static void onActivityResultForQQ(int requestCode, int resultCode,
                                             Intent data, IUiListener uiListener) {
        if (requestCode == com.tencent.connect.common.Constants.REQUEST_LOGIN ||
                requestCode == com.tencent.connect.common.Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, uiListener);
        }
    }


    /**
     * 微信登录(授权)
     */
    public void LoginByWeixin() {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        IWXAPI api = WXAPIFactory.createWXAPI(mContext, Constants.APP_ID, false);
        // 将该APP注册到微信
        api.registerApp(Constants.APP_ID);
        boolean isWeChatInstalled = api.isWXAppInstalled();
//        boolean isSupportAPI = api.isWXAppSupportAPI();
        //这里由于该方法返回结果有问题，暂时不判断是否支持的条件
        if (isWeChatInstalled) {//&& isSupportAPI
            // 第一步: send OAUTH request
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = getRandom();
            api.sendReq(req);
        } else if (isWeChatInstalled) {// && !isSupportAPI
            Toast.makeText(mContext, "微信版本过低,请安装最新版本的微信!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, "未安装微信客户端", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 获取一个随机数
     *
     * @return
     */
    private String getRandom() {
        int max = 2147483647;
        int min = 1;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        String randomsString = s + "";
        saveStateToSP(randomsString);
        return randomsString;

    }

    public void saveStateToSP(String randomsString) {
        SharedPreferences preferences = mContext.getSharedPreferences("weixin_auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("state", randomsString);
        editor.commit();
    }

    public static String getStateFromSP(Context mContext) {
        SharedPreferences preferences = mContext.getSharedPreferences("weixin_auth", Context.MODE_PRIVATE);
        return preferences.getString("state", "");
    }


}
