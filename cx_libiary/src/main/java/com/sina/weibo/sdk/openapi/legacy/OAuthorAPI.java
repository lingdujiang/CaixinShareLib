package com.sina.weibo.sdk.openapi.legacy;

import android.content.Context;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.openapi.AbsOpenAPI;

/**
 * Created by admin on 2017/2/24.
 */
public class OAuthorAPI extends AbsOpenAPI {

    /** POST 请求方式 */
    public static final String HTTPMETHOD_POST  = "POST";
    /** GET 请求方式 */
    public static final String HTTPMETHOD_GET   = "GET";

    /**
     * 构造函数，使用各个 API 接口提供的服务前必须先获取 Token。
     *
     * @param context
     * @param appKey
     * @param accessToken
     */
    public OAuthorAPI(Context context, String appKey, Oauth2AccessToken accessToken) {
        super(context, appKey, accessToken);
    }

    @Override
    public void requestAsync(String url, WeiboParameters params, String httpMethod, RequestListener listener) {
        super.requestAsync(url, params, httpMethod, listener);
    }
}
