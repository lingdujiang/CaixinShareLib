package com.caixinnews.share;


public interface ICXShareCallback {

    public void onSuccess(Object o);

    public void onCancel();

    public void onError(Exception error);

}
