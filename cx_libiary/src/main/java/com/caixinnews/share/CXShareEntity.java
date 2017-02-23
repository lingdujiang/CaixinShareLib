package com.caixinnews.share;


public class CXShareEntity {

    public String platform = "";
    public boolean isSilent = false;
    public String title = "";
    public String url = "";
    public String titleUrl = "";
    public String summary = "";//对应统一分享接口的text字段，旧的逻辑里面的summary字段
    public String sourceId = "";
    public String appId = "";
    public String imagePath = "";
    public ICXShareCallback callback;

}
