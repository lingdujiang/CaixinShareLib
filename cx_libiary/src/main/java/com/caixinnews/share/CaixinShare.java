package com.caixinnews.share;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.open.utils.ThreadManager;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CaixinShare {
    public static final String SHARE_PLATFORM_WEICHAT = "Wechat";
    public static final String SHARE_PLATFORM_MOMENT = "WechatMoments";
    public static final String SHARE_PLATFORM_QQ = "QQ";
    public static final String SHARE_PLATFORM_WEIBO = "SinaWeibo";
    public static final String SHARE_PLATFORM_EMAIL = "Email";


    private Context context;
    private boolean isInstalledWeibo;
    private int supportApiLevel;

    public CaixinShare(Activity activity) {
        this.context = activity;
    }

    public static void init(String APP_ID_WX,String APP_ID_QQ,String APP_KEY_WEIBO,String REDIRECT_URL_WEIBO,String SCOPE_WEIBO){
        Constants.APP_ID = APP_ID_WX;
        Constants.APP_ID_QQ = APP_ID_QQ;
        Constants.APP_KEY_WEIBO = APP_KEY_WEIBO;
        Constants.REDIRECT_URL_WEIBO = REDIRECT_URL_WEIBO;
        Constants.SCOPE_WEIBO = SCOPE_WEIBO;
    }

    private static final int THUMB_SIZE = 150;
    public void shareToPlatform(CXShareEntity entity) {
        switch (entity.platform){
            case SHARE_PLATFORM_WEICHAT:
            case SHARE_PLATFORM_MOMENT:
                shareToWeiChat(entity);
                break;
            case SHARE_PLATFORM_WEIBO:
                shareToWeibo(entity);
                break;
            case SHARE_PLATFORM_QQ:
                shareToQQ(entity);
                break;
            case SHARE_PLATFORM_EMAIL:
                shareToEmail(entity);
                break;
        }
    }

    /**
     * 分享到微信聊天、微信朋友圈
     *
     * @param entity
     */
    public void shareToWeiChat(CXShareEntity entity) {
        // 获取IWXAPI的实例
        IWXAPI api = WXAPIFactory.createWXAPI(context, Constants.APP_ID, false);

        int mTargetScene = SendMessageToWX.Req.WXSceneSession;
        if (SHARE_PLATFORM_MOMENT.equals(entity.platform)) {
            mTargetScene = SendMessageToWX.Req.WXSceneTimeline;
        }

        // 将该app注册到微信
        api.registerApp(Constants.APP_ID);


        if (!TextUtils.isEmpty(entity.url) && !TextUtils.isEmpty(entity.title)) {//网页分享
            WXWebpageObject webpage = new WXWebpageObject();
            webpage.webpageUrl = entity.url;

            WXMediaMessage msg = new WXMediaMessage(webpage);
            msg.title = entity.title;
            msg.description = entity.summary;

            Bitmap bmp = BitmapFactory.decodeFile(entity.imagePath);
            Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
            bmp.recycle();
            msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("webpage");
            req.message = msg;
            req.scene = mTargetScene;
            api.sendReq(req);
        } else if (!TextUtils.isEmpty(entity.imagePath)) {//图片分享

            WXImageObject imgObj = new WXImageObject();
            imgObj.setImagePath(entity.imagePath);

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = imgObj;


            Bitmap bmp = BitmapFactory.decodeFile(entity.imagePath);
            Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
            bmp.recycle();
            msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("img");
            req.message = msg;
            req.scene = mTargetScene;
            api.sendReq(req);
        } else if (!TextUtils.isEmpty(entity.title)) {//分享纯文字
            WXTextObject textObj = new WXTextObject();
            textObj.text = entity.title;

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = textObj;
            msg.description = entity.summary;

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("text");
            req.message = msg;
            req.scene = mTargetScene;

            api.sendReq(req);
        } else {
            Toast.makeText(context, "数据错误", Toast.LENGTH_LONG).show();
        }
    }



    int shareType = -1;//qq分享类别

    public void shareToQQ(CXShareEntity entity) {
        Tencent mTencent = Tencent.createInstance(Constants.APP_ID_QQ, context);

        final Bundle params = new Bundle();

        if (TextUtils.isEmpty(entity.url) && !TextUtils.isEmpty(entity.imagePath)) {//url为空，imagepath不为空则认为图片分享
            shareType = QQShare.SHARE_TO_QQ_TYPE_IMAGE;
        } else if (!TextUtils.isEmpty(entity.title)) {
            shareType = QQShare.SHARE_TO_QQ_TYPE_DEFAULT;
        }

        if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
            params.putString(QQShare.SHARE_TO_QQ_TITLE, entity.title);
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, entity.url);
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, entity.summary);
        }
        params.putString(shareType == QQShare.SHARE_TO_QQ_TYPE_IMAGE ? QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL
                : QQShare.SHARE_TO_QQ_IMAGE_URL, entity.imagePath);

        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "AppName");
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, shareType);
//        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        doShareToQQ(params, mTencent);
    }

    private void doShareToQQ(final Bundle params, final Tencent mTencent) {
        // QQ分享要在主线程做
        ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.shareToQQ((Activity) context, params, qqShareListener);
                }
            }
        });
    }

    /**
     * 选择
     */
    public void shareDependsPlate(CXShareEntity entity) {
        String emailSubject = entity.title;
        String emailBody = entity.summary;
        Intent it = new Intent(Intent.ACTION_SEND);
        it.setType("text/plain");
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(it, 0);
        if (!resInfo.isEmpty()) {
            List<Intent> recommonedIntents = new ArrayList<Intent>();
            for (ResolveInfo info : resInfo) {
                Intent targeted = new Intent(Intent.ACTION_SEND);
                targeted.setType("text/plain");
                targeted.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityInfo activityInfo = info.activityInfo;
                if (activityInfo.packageName.toLowerCase().contains("mail") || activityInfo.name.toLowerCase().contains("mail")) {
                    targeted.putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
                    targeted.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
                    recommonedIntents.add(targeted);
                } else {
                    continue;
                }

                targeted.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
                recommonedIntents.add(targeted);

            }
            if (recommonedIntents.size() < 1) {
                return;
            }
            Intent chooserIntent = Intent.createChooser(recommonedIntents.remove(recommonedIntents.size() - 1), "请选择邮件应用");
            if (chooserIntent == null) {
                return;
            }
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, recommonedIntents.toArray(new Parcelable[]{}));
            try {
                context.startActivity(chooserIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "未找到邮件应用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void shareToEmail(CXShareEntity entity) {
        if(!TextUtils.isEmpty(entity.imagePath)){//&&!TextUtils.isEmpty(entity.title)
            Intent email = new Intent(android.content.Intent.ACTION_SEND);
            File file = new File(entity.imagePath);
            email.setType("application/octet-stream");
            String emailTitle = entity.title;
            String emailContent = entity.summary;
            email.putExtra(android.content.Intent.EXTRA_SUBJECT, emailTitle);
            email.putExtra(android.content.Intent.EXTRA_TEXT, emailContent);
            email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            //调用系统的邮件系统
//            context.startActivity(Intent.createChooser(email, "请选择邮件发送软件"));
            filterApp(email,entity,1);
        }else if(TextUtils.isEmpty(entity.imagePath)){
            Intent email = new Intent(android.content.Intent.ACTION_SEND);
            email.setType("plain/text");
            String emailTitle = entity.title;
            String emailContent = entity.summary;
            email.putExtra(android.content.Intent.EXTRA_SUBJECT, emailTitle);
            email.putExtra(android.content.Intent.EXTRA_TEXT, emailContent);
//            context.startActivity(Intent.createChooser(email, "请选择邮件发送软件"));
            filterApp(email,entity,0);
        }

    }


    private void filterApp(Intent intent,CXShareEntity entity,int type){//type:0纯文字1带附件

        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        if (!resInfo.isEmpty()) {
            List<Intent> recommonedIntents = new ArrayList<Intent>();
            for (ResolveInfo info : resInfo) {
                Intent targeted = new Intent(Intent.ACTION_SEND);
                if(type == 1){
                    targeted.setType("application/octet-stream");
                }else{
                    targeted.setType("text/plain");
                }
                targeted.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityInfo activityInfo = info.activityInfo;
                if (activityInfo.packageName.contains("mail") || activityInfo.name.contains("mail")) {
                    targeted.putExtra(android.content.Intent.EXTRA_SUBJECT, entity.title);
                    targeted.putExtra(android.content.Intent.EXTRA_TEXT, entity.summary);
                    targeted.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
                    if(type==1){
                        File file = new File(entity.imagePath);
                        targeted.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    }
                    recommonedIntents.add(targeted);
                } else {
                    continue;
                }

            }
            if (recommonedIntents.size() < 1) {
                return;
            }else if(recommonedIntents.size()==1){
                context.startActivity(recommonedIntents.get(0));
            }else{
                Intent chooserIntent = Intent.createChooser(recommonedIntents.remove(recommonedIntents.size() - 1), "分享到");
                if (chooserIntent == null) {
                    return;
                }
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, recommonedIntents.toArray(new Parcelable[]{}));
                try {
                    context.startActivity(chooserIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(context, "未找到邮件应用", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }


    IUiListener qqShareListener = new IUiListener() {
        @Override
        public void onCancel() {
            if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
                Toast.makeText(context, "分享取消", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onComplete(Object response) {
            Toast.makeText(context, "分享完成", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(UiError e) {
            Toast.makeText(context, "分享失败", Toast.LENGTH_LONG).show();
        }
    };

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public void shareToWeibo(CXShareEntity entity) {
        //TODO 授权
//        auth();
        //将APP注册到微博
        registerAppToWeibo();
        //分享
        if(isInstalledWeibo){
            share_Weibo_client(entity);
        }else {
            Toast.makeText(context, "微博客户端未安装或不支持分享", Toast.LENGTH_LONG).show();
        }

    }

    private static void auth() {

    }

    private void share_Weibo_client(CXShareEntity entity) {
        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        if (!TextUtils.isEmpty(entity.summary)) {
            TextObject textObject = new TextObject();
            textObject.text = entity.summary;
            weiboMessage.textObject = textObject;
        }
        if (!TextUtils.isEmpty(entity.imagePath)) {
            Bitmap bitmap = BitmapFactory.decodeFile(entity.imagePath);
            ImageObject imageObject = new ImageObject();
            imageObject.setImageObject(bitmap);
            weiboMessage.imageObject = imageObject;
        }
        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;
        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest((Activity)context,request);
    }

    private static IWeiboShareAPI mWeiboShareAPI;

    private void registerAppToWeibo() {
        // 创建微博 SDK 接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(context, Constants.APP_KEY_WEIBO);
        // 获取微博客户端相关信息，如是否安装、支持 SDK 的版本
        isInstalledWeibo = mWeiboShareAPI.isWeiboAppInstalled();
        supportApiLevel = mWeiboShareAPI.getWeiboAppSupportAPI();   //TODO 如何进行判断
        // 注册到新浪微博
        mWeiboShareAPI.registerApp();
    }
}
