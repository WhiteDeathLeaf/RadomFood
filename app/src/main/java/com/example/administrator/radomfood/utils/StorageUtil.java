package com.example.administrator.radomfood.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.baidu.mapapi.search.core.PoiInfo;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StorageUtil {

    private static final String SP_KEY = "sp_key";
    private static StorageUtil mStorageUtil;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    @SuppressLint("CommitPrefEdits")
    private StorageUtil(Context context) {
        mSharedPreferences = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public static StorageUtil getInstance(Context context) {
        if (mStorageUtil == null) {
            synchronized (StorageUtil.class) {
                if (mStorageUtil == null) {
                    mStorageUtil = new StorageUtil(context);
                }
            }
        }
        return mStorageUtil;
    }

    public void saveDislikeFood(String uid, PoiInfo poiInfo) {
        if (!mSharedPreferences.contains(uid)) {
            mEditor.putString(uid, new Gson().toJson(poiInfo));
            mEditor.commit();
        }
    }

    public boolean isDislike(String uid) {
        return mSharedPreferences.contains(uid);
    }

    public void removeDislike(String uid) {
        if (mSharedPreferences.contains(uid)) {
            mEditor.remove(uid);
            mEditor.commit();
        }
    }

    public List<PoiInfo> getDislikeFood() {
        ArrayList<String> list = new ArrayList<>((Collection<String>) mSharedPreferences.getAll().values());
        List<PoiInfo> poiInfos = new ArrayList<>();
        for (String json : list) {
            poiInfos.add(new Gson().fromJson(json, PoiInfo.class));
        }
        return poiInfos;
    }
}
