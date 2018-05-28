package com.example.administrator.radomfood.utils;

import android.content.Context;
import android.text.TextUtils;

import com.example.administrator.radomfood.R;

public class DrawableUtil {

    private DrawableUtil() {
    }

    public static Integer getImageRes(Context context, String image) {
        Integer resID = R.drawable.app_icon;
        if (TextUtils.isEmpty(image)) {
            return resID;
        } else {
            resID = context.getResources().getIdentifier(context.getPackageName() + ":drawable/" + image, null, null);
            if (resID == 0)
                resID = R.drawable.app_icon;
            return resID;
        }
    }
}
