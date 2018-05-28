package com.example.administrator.radomfood.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.baidu.mapapi.search.core.PoiInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.administrator.radomfood.R;

import java.util.List;

public class FoodListAdapter extends BaseQuickAdapter<PoiInfo, BaseViewHolder> {

    public FoodListAdapter(int layoutResId, @Nullable List<PoiInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PoiInfo item) {
        helper.setText(R.id.tv_poi_name, item.name)
                .setText(R.id.tv_poi_phone, item.phoneNum)
                .setText(R.id.tv_poi_address, item.address)
                .setGone(R.id.tv_poi_phone, !TextUtils.isEmpty(item.phoneNum));
    }
}
