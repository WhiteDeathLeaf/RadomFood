package com.example.administrator.radomfood.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.administrator.radomfood.R;
import com.example.administrator.radomfood.adapter.FoodListAdapter;
import com.example.administrator.radomfood.custom.LoadingDialog;
import com.example.administrator.radomfood.utils.MapUtil;
import com.example.administrator.radomfood.utils.StorageUtil;
import com.example.administrator.radomfood.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class HomeActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.fab_main_map)
    FloatingActionButton mFabMap;
    @BindView(R.id.tv_main_location)
    TextView mTvLocation;
    @BindView(R.id.tv_main_search_pageCapacity)
    TextView mTvSearchPageCapacity;
    @BindView(R.id.tv_main_search_radius)
    TextView mTvSearchRadius;
    @BindView(R.id.fab_main_select_food)
    FloatingActionButton mFabSelectFood;
    @BindView(R.id.tv_main_today_eat)
    TextView mTvTodayEat;
    @BindView(R.id.rv_main_food_list)
    RecyclerView mRvFoodList;
    @BindView(R.id.fab_main_view_controller)
    FloatingActionButton mFabViewController;
    @BindView(R.id.mv_main_map)
    MapView mMapView;
    @BindView(R.id.ll_function_layout)
    LinearLayout mLlFunctionLayout;
    @BindView(R.id.rv_main_dislike_food_list)
    RecyclerView mRvDislikeFoodList;
    @BindView(R.id.dl_main)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.fab_main_dislike)
    FloatingActionButton mFabDislike;

    private static final int RC_LOCATION = 1;

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private PoiSearch mPoiSearch;
    private FoodListAdapter mFoodListAdapter;
    private DialogFragment mLoadingDialog;
    private LinearLayoutManager mLayoutManager;
    private BottomSheetBehavior<LinearLayout> mBehavior;
    private FoodListAdapter mDislikeFoodListAdapter;

    private int mWhich = 0;
    private int mTime = 0;
    private boolean isRandom = false;
    private String mChoiceItem;
    private String mAddrStr;
    private LatLng mLatLng;

    @BindArray(R.array.pageCapacity)
    String[] mPageCapacitys;
    @BindArray(R.array.radius)
    String[] mRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initBottomSheet();
        initMap();
        initFoodList();
        requiresLocationPermission();
    }

    /**
     * 初始化功能视图
     */
    private void initBottomSheet() {
        mBehavior = BottomSheetBehavior.from(mLlFunctionLayout);
        mBehavior.setPeekHeight(getResources().getDisplayMetrics().heightPixels);
        mBehavior.setBottomSheetCallback(mBottomSheetCallback);
    }

    /**
     * 初始化地图
     */
    private void initMap() {
        mMapView.removeViewAt(1);
        mMapView.showScaleControl(true);
        mMapView.showZoomControls(true);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
    }

    /**
     * 初始化门店列表
     */
    private void initFoodList() {
        mLayoutManager = new LinearLayoutManager(this);
        mRvFoodList.setLayoutManager(mLayoutManager);
        mFoodListAdapter = new FoodListAdapter(R.layout.item_food, null);
        mRvFoodList.setAdapter(mFoodListAdapter);
        mFoodListAdapter.setOnItemClickListener(mOnItemClickListener);
        mFoodListAdapter.setOnItemLongClickListener(mOnItemLongClickListener);

        List<PoiInfo> dislikeFood = StorageUtil.getInstance(this).getDislikeFood();
        mRvDislikeFoodList.setLayoutManager(new LinearLayoutManager(this));
        if (dislikeFood != null && !dislikeFood.isEmpty())
            mDislikeFoodListAdapter = new FoodListAdapter(R.layout.item_food, dislikeFood);
        else
            mDislikeFoodListAdapter = new FoodListAdapter(R.layout.item_food, null);
        mDislikeFoodListAdapter.bindToRecyclerView(mRvDislikeFoodList);
        mDislikeFoodListAdapter.setEmptyView(R.layout.layout_dislike_empty);
        mDislikeFoodListAdapter.setOnItemClickListener(mOnItemClickListener_Dislike);
        mDislikeFoodListAdapter.setOnItemLongClickListener(mOnItemLongClickListener_Dislike);
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setCoorType("bd0911");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setIsNeedLocationPoiList(true);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(mBdAbstractLocationListener);
    }

    private void initPOI() {
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(mOnGetPoiSearchResultListener);
    }

    /**
     * POI搜索
     *
     * @param latLng 经纬度
     */
    private void searchPOI(LatLng latLng) {
        PoiNearbySearchOption poiOption = new PoiNearbySearchOption();
        int radius = Integer.parseInt(mTvSearchRadius.getText().toString().substring(5));
        int pageNum = 1;
        int pageCapacity = Integer.parseInt(mTvSearchPageCapacity.getText().toString().substring(5));
        poiOption.keyword("美食").sortType(PoiSortType.distance_from_near_to_far).location(latLng)
                .radius(radius).pageNum(pageNum).pageCapacity(pageCapacity);
        mPoiSearch.searchNearby(poiOption);
    }

    //----------------------------------------------------------------------------------------------
    //Listener
    private BaseQuickAdapter.OnItemClickListener mOnItemClickListener = (adapter, view, position) -> {
        PoiInfo poiInfo = mFoodListAdapter.getData().get(position);
        navigationDialog(poiInfo.location, poiInfo.name);
    };

    private BaseQuickAdapter.OnItemLongClickListener mOnItemLongClickListener = new BaseQuickAdapter.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
            PoiInfo poiInfo = mFoodListAdapter.getData().get(position);
            dislikeDialog(poiInfo.name, poiInfo.uid, position, poiInfo);
            return true;
        }
    };

    private BaseQuickAdapter.OnItemClickListener mOnItemClickListener_Dislike = (adapter, view, position) -> {
        PoiInfo poiInfo = mDislikeFoodListAdapter.getData().get(position);
        navigationDialog(poiInfo.location, poiInfo.name);
    };

    private BaseQuickAdapter.OnItemLongClickListener mOnItemLongClickListener_Dislike = new BaseQuickAdapter.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
            PoiInfo poiInfo = mDislikeFoodListAdapter.getData().get(position);
            recoverDislikeDialog(poiInfo.name, poiInfo.uid, position);
            return true;
        }
    };
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN)
                mFabViewController.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    private BDAbstractLocationListener mBdAbstractLocationListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mAddrStr = bdLocation.getAddrStr();
            double latitude = bdLocation.getLatitude();
            double longitude = bdLocation.getLongitude();
            mLatLng = MapUtil.bdLatLag(longitude, latitude);
            mTvLocation.setText(mAddrStr);
            MyLocationData locationData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    .latitude(mLatLng.latitude)
                    .longitude(mLatLng.longitude).build();
            mBaiduMap.setMyLocationData(locationData);
            MapStatus mapStatus = new MapStatus.Builder().target(mLatLng).zoom(18.0f).build();
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
            searchPOI(mLatLng);
        }
    };

    private OnGetPoiSearchResultListener mOnGetPoiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                List<PoiInfo> allPoi = poiResult.getAllPoi();
                List<PoiInfo> filterPoi = new ArrayList<>();
                for (PoiInfo poiInfo : allPoi) {
                    if (!StorageUtil.getInstance(HomeActivity.this).isDislike(poiInfo.uid))
                        filterPoi.add(poiInfo);
                }
                mFoodListAdapter.replaceData(filterPoi);
                mRvFoodList.scrollToPosition(0);
            } else {
                ToastUtil.newInstance(HomeActivity.this).show(getString(R.string.tip_failed, poiResult.error));
            }
            loadingStop();
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mLocationClient.unRegisterLocationListener(mBdAbstractLocationListener);
        mPoiSearch.destroy();
        mMapView.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    //----------------------------------------------------------------------------------------------
    //Click
    @OnClick({R.id.fab_main_view_controller, R.id.fab_main_search, R.id.fab_main_map, R.id.fab_main_select_food, R.id.tv_main_search_pageCapacity, R.id.tv_main_search_radius, R.id.fab_main_dislike})
    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.fab_main_view_controller:
                if (mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    mFabViewController.setVisibility(View.GONE);
                    mFabDislike.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.fab_main_search:
                mLocationClient.restart();
                loadingStart();
                break;
            case R.id.fab_main_map:
                if (mBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    mFabDislike.setVisibility(View.GONE);
                }
                break;
            case R.id.fab_main_select_food:
                if (!mFoodListAdapter.getData().isEmpty() && !isRandom) {
                    mHandler.sendEmptyMessageDelayed(1, 100);
                } else {
                    String content = mFoodListAdapter.getData().isEmpty() ? getString(R.string.tip_none) : isRandom ? getString(R.string.tip_running) : getString(R.string.tip_waring);
                    ToastUtil.newInstance(this).show(content);
                }
                break;
            case R.id.tv_main_search_pageCapacity:
                dialogChoice(getString(R.string.setting_capacity), R.drawable.list_icon, mPageCapacitys, mTvSearchPageCapacity);
                break;
            case R.id.tv_main_search_radius:
                dialogChoice(getString(R.string.setting_radius), R.drawable.radius_icon, mRadius, mTvSearchRadius);
                break;
            case R.id.fab_main_dislike:
                mDrawerLayout.openDrawer(Gravity.START);
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    //Loading
    private void loadingStart() {
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.show(getSupportFragmentManager(), "dialogFragment");
    }

    private void loadingStop() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismiss();
    }

    //----------------------------------------------------------------------------------------------
    //Dialog
    private void recoverDislikeDialog(String name, String uid, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.recover_title)
                .setIcon(R.drawable.like_icon)
                .setMessage(getString(R.string.recover_content, name))
                .setPositiveButton(R.string.recover_positive, (dialog, which) -> {
                    StorageUtil.getInstance(HomeActivity.this).removeDislike(uid);
                    mDislikeFoodListAdapter.remove(position);
                })
                .setNegativeButton(R.string.recover_negative, null)
                .create().show();
    }

    private void dislikeDialog(String name, String uid, int position, PoiInfo poiInfo) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dislike_title)
                .setIcon(R.drawable.dislike_icon)
                .setMessage(getString(R.string.dislike_content, name))
                .setPositiveButton(R.string.dislike_positive, (dialog, which) -> {
                    StorageUtil.getInstance(HomeActivity.this).saveDislikeFood(uid, poiInfo);
                    mFoodListAdapter.remove(position);
                    mDislikeFoodListAdapter.addData(poiInfo);
                })
                .setNegativeButton(R.string.dislike_negative, null)
                .create().show();
    }

    private void navigationDialog(LatLng endLocation, String endName) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.navigation_title)
                .setIcon(R.drawable.navigation_icon)
                .setMessage(getString(R.string.navigation_content, mAddrStr, endName))
                .setPositiveButton(R.string.navigation_positive, (dialog, which) -> {
                    NaviParaOption paraOption = new NaviParaOption().startPoint(mLatLng).endPoint(endLocation).startName(mAddrStr).endName(endName);
                    BaiduMapNavigation.setSupportWebNavi(true);
                    BaiduMapNavigation.openBaiduMapWalkNavi(paraOption, HomeActivity.this);
                })
                .setNegativeButton(R.string.navigation_negative, null)
                .create().show();
    }

    private void dialogChoice(String title, @DrawableRes int icon, final String[] items, final TextView textView) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(icon)
                .setSingleChoiceItems(items, mWhich,
                        (dialog, which) -> {
                            mChoiceItem = items[which];
                            mWhich = which;
                        })
                .setPositiveButton(R.string.setting_positive, (dialog, which) -> {
                    if (mChoiceItem != null) {
                        textView.setText(mChoiceItem);
                        mLocationClient.restart();
                        loadingStart();
                    }
                    mChoiceItem = null;
                })
                .setNegativeButton(R.string.setting_negative, (dialog, which) -> {
                    textView.setText(items[mWhich]);
                    mChoiceItem = null;
                })
                .create().show();
    }

    //----------------------------------------------------------------------------------------------
    //Handler
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final List<PoiInfo> data = mFoodListAdapter.getData();
            if (!data.isEmpty() && mTime < 20) {
                int position = new Random().nextInt(data.size());
                PoiInfo poiInfo = data.get(position);
                mTvTodayEat.setText(poiInfo.name);
                mTime++;
                if (mTime >= 20) {
                    mTime = 0;
                    isRandom = false;
                    mLayoutManager.scrollToPositionWithOffset(position, 0);
                } else {
                    mHandler.sendEmptyMessageDelayed(1, 100);
                    isRandom = true;
                }
            }
        }
    };

    //----------------------------------------------------------------------------------------------
    //动态权限
    @AfterPermissionGranted(RC_LOCATION)
    private void requiresLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            initLocation();
            initPOI();
            mLocationClient.start();
            loadingStart();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.main_location), RC_LOCATION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Toast.makeText(this, R.string.main_location, Toast.LENGTH_SHORT).show();
        }
    }

    //----------------------------------------------------------------------------------------------
    //返回键
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START))
            mDrawerLayout.closeDrawer(Gravity.START);
        else
            super.onBackPressed();
    }
}
