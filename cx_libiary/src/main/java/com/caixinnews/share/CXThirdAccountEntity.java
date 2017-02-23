package com.caixinnews.share;

import java.io.Serializable;

/**
 * Created by admin on 2017/2/15.
 */
public class CXThirdAccountEntity implements Serializable {
    public String fromId = "";// 第三方平台ID 1 腾讯微博 2 新浪微博 3 腾讯QQ 4 微信账号 5 淘宝账号
    public String userId = "";// 第三方平台账号ID
    public String openid = ""; // 第三方平台OPENID
    public String stoken = ""; // 第三方平台access token
    public String rtoken = ""; // 第三方平台reflush token
    public String expirein = "0"; // 第三方平台token有效期
    public String expiretime = "0";// 第三方平台TOKEN过期时间
    public String nickname = "";// 第三方平台用户昵称
    public String userlink = ""; // 第三方平台用户链接
    public String portrait = ""; // 第三方平台用户头像
    public String userinfo = ""; // 第三方平台用户信息
}
