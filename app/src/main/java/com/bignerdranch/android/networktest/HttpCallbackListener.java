package com.bignerdranch.android.networktest;

/**
 * Created by Administrator on 2017/5/16/016.
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
