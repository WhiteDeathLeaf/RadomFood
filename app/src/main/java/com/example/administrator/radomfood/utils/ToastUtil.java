package com.example.administrator.radomfood.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static ToastUtil mToastUtil;
    private Toast mToast;

    @SuppressLint("ShowToast")
    public ToastUtil(Context context) {
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public static ToastUtil newInstance(Context context) {
        if (mToastUtil == null)
            synchronized (ToastUtil.class) {
                if (mToastUtil == null) {
                    mToastUtil = new ToastUtil(context);
                }
            }
        return mToastUtil;
    }

    public void show(String content) {
        mToast.setText(content);
        mToast.show();
    }
}
