package com.caixinnews.share;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.User;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2017/2/6.
 */
public class ICXLoginCallback implements IUiListener, WeiboAuthListener, RequestListener {

    private Context mContext;
    private CXThirdAccountEntity entity = new CXThirdAccountEntity();

    public ICXLoginCallback(Context _mContext) {
        this.mContext = _mContext;
    }

    /**
     * QQ第一次返回
     * *
     * {
     * "access_token": "72EE3B0E89D83C77F1A1189531455990",
     * "authority_cost": 0,
     * "expires_in": 7776000,
     * "login_cost": 1082,
     * "msg": "",
     * "openid": "6EA4D02981C04716FF0EAA3E4F82A46E",
     * "pay_token": "B56CD54D7F96B1E532962202090FD4A5",
     * "pf": "desktop_m_qq-10000144-android-2002-",
     * "pfkey": "414ecdbf26808772c1a5387957cb7325",
     * "query_authority_cost": 683,
     * "ret": 0
     * }
     *
     * @param response
     */
    @Override
    public void onComplete(Object response) {
        final Tencent mTencent = Tencent.createInstance(Constants.APP_ID_QQ, mContext);
        JSONObject jsonResponse = (JSONObject) response;
        if (null == response || jsonResponse.length() == 0) {
            doWithError();
            return;
        }
        try {
            entity.fromId = "3";//QQ为3
            entity.stoken = jsonResponse.getString("access_token");
            entity.openid = jsonResponse.getString("openid");
            entity.userId = jsonResponse.getString("openid");
            entity.expirein = jsonResponse.getString("expires_in");
            if (!TextUtils.isEmpty(entity.stoken) && !TextUtils.isEmpty(entity.openid)) {
                mTencent.setAccessToken(entity.stoken, entity.expirein);
                mTencent.setOpenId(entity.openid);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //获取用户其他信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserInfo mInfo = new UserInfo(mContext, mTencent.getQQToken());
                mInfo.getUserInfo(uiListener);
            }
        }).start();
    }

    IUiListener uiListener = new IUiListener() {
        /**
         * {
         "city": "广州",
         "figureurl": "http://qzapp.qlogo.cn/qzapp/100761191/6EA4D02981C04716FF0EAA3E4F82A46E/30",
         "figureurl_1": "http://qzapp.qlogo.cn/qzapp/100761191/6EA4D02981C04716FF0EAA3E4F82A46E/50",
         "figureurl_2": "http://qzapp.qlogo.cn/qzapp/100761191/6EA4D02981C04716FF0EAA3E4F82A46E/100",
         "figureurl_qq_1": "http://q.qlogo.cn/qqapp/100761191/6EA4D02981C04716FF0EAA3E4F82A46E/40",
         "figureurl_qq_2": "http://q.qlogo.cn/qqapp/100761191/6EA4D02981C04716FF0EAA3E4F82A46E/100",
         "gender": "男",
         "is_lost": 0,
         "is_yellow_vip": "0",
         "is_yellow_year_vip": "0",
         "level": "0",
         "msg": "",
         "nickname": "Goldmoon",
         "province": "广东",
         "ret": 0,
         "vip": "0",
         "yellow_vip_level": "0"
         }
         */
        @Override
        public void onComplete(Object response) {
            if (response != null) {
                JSONObject json = (JSONObject) response;
                try {
                    entity.portrait = json.getString("figureurl_qq_1");
                    entity.nickname = json.getString("nickname");
                    entity.expiretime = "";//TODO token到期时间,目前返回数据里拿不到
                    entity.userlink = "";
                    entity.userinfo = json.toString();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            doComplete(entity);
        }

        @Override
        public void onError(UiError uiError) {
            doWithError();
        }

        @Override
        public void onCancel() {
            doWithCancel();
        }
    };

    @Override
    public void onError(UiError uiError) {
        doWithError();
    }

    /**
     * 微博第一次返回
     * <p/>
     * Bundle[{_weibo_transaction=1487127179393, access_token=2.00i4JV3CKHhD2Dc5957febf70UEHQy,
     * refresh_token=2.00i4JV3CKHhD2D3371859d95hqdRbE, expires_in=2650019,
     * _weibo_appPackage=com.sina.weibo, com.sina.weibo.intent.extra.NICK_NAME=学不來含蓄,
     * userName=学不來含蓄, uid=2164769290, com.sina.weibo.intent.extra.USER_ICON=null}]
     *
     * @param bundle
     */
    @Override
    public void onComplete(Bundle bundle) {
        final Oauth2AccessToken mAccessToken = Oauth2AccessToken.parseAccessToken(bundle);
        if (mAccessToken.isSessionValid()) {
            entity.expiretime = String.valueOf(mAccessToken.getExpiresTime());
            entity.stoken = mAccessToken.getToken();
            entity.userId = mAccessToken.getUid();
            entity.openid = mAccessToken.getUid();
            entity.fromId = "2";//2为新浪微博

            //获取用户其他信息
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UsersAPI usersAPI = new UsersAPI(mContext,
                            Constants.APP_KEY_WEIBO, mAccessToken);
                    usersAPI.show(Long.parseLong(mAccessToken.getUid()), ICXLoginCallback.this);
                }
            }).start();

        } else {
            // 以下几种情况，您会收到 Code：
            // 1. 当您未在平台上注册的应用程序的包名与签名时；
            // 2. 当您注册的应用程序包名与签名不正确时；
            // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
            doWithError();
        }

    }

    /**
     * 微博第二次返回
     *
     * @param response
     */
    @Override
    public void onComplete(String response) {
        if (!TextUtils.isEmpty(response)) {
            User user = User.parse(response);
            try {
                entity.portrait = user.avatar_hd;
                entity.nickname = user.screen_name;
                entity.userlink = "http://weibo.com/" + user.profile_url;
                entity.userinfo = response;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        doComplete(entity);
    }

    @Override
    public void onWeiboException(WeiboException e) {
        doWithWeiboException();
    }

    @Override
    public void onCancel() {
        doWithCancel();
    }

    /**
     * 登录及用户信息请求完成回调
     */
    public void doComplete(CXThirdAccountEntity entity) {
    }

    /**
     * QQ返回错误的处理
     */
    protected void doWithError() {
    }

    /**
     * 微博返回错误的处理
     */
    protected void doWithWeiboException() {
    }

    /**
     * 登录取消的处理
     */
    protected void doWithCancel() {
    }


}
