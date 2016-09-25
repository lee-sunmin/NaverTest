package com.example.hongssang.navertest;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.NMapPOIflagType;
import com.nhn.android.mapviewer.NMapViewerResourceProvider;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import java.io.FileNotFoundException;
import java.io.InputStream;

//import android.database.sqlite.SQLiteOpenHelper;

public class MainActivity extends NMapActivity implements NMapView.OnMapStateChangeListener, NMapView.OnMapViewTouchEventListener {
    public static Context mContext;

    public static final String CLIENT_ID = "EacoUq5gGV_fYxm9nqSM";
    public NMapView mMapView = null;
    NMapController mMapController = null;
    LinearLayout MapContainer, MapWrap, InputWrap;
    Button search_button;
    NGeoPoint gp;
    int dataindex = 0;

    // 오버레이의 리소스를 제공하기 위한 객체
    NMapViewerResourceProvider mMapViewerResourceProvider = null;
    // 오버레이 관리자
    NMapOverlayManager mOverlayManager;
    private NMapLocationManager mMapLocationManager;
    private NMapMyLocationOverlay mMyLocationOverlay;
    private NMapCompassManager mMapCompassManager;
    private NMapPOIdataOverlay poiDataOverlay;

    int bottom_mode = 2;
    //0.search, 1,register, 2.nothing
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapContainer = (LinearLayout) findViewById(R.id.map);
        mMapView = new NMapView(this);
        mMapView.setClientId(CLIENT_ID);
        MapContainer.addView(mMapView);
        mMapView.setClickable(true);
        mMapView.setOnMapStateChangeListener(this);
        mMapView.setOnMapViewTouchEventListener(this);
        mMapView.setBuiltInZoomControls(true, null);

        mMapController = mMapView.getMapController();

        setButtons();
        mContext = this;
    }

    private void setButtons(){
        search_button = (Button) findViewById(R.id.main_search_tab);
        search_button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                if(bottom_mode != 0) {
                    changeMapWrapperWeight(0.5f);
                    changeInputWrapperWeight(0.4f);
                    final FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.input, new SearchFragment());
                    transaction.commit();
                    bottom_mode = 0;
                }
            }
        });
    }

    public void changeMapWrapperWeight(float weight){
        MapWrap = (LinearLayout) findViewById(R.id.map_wrap);
        Log.v("Debug", "changeMapWrapperWeight called, weight is " + weight);
        LinearLayout.LayoutParams map_params = (LinearLayout.LayoutParams) MapWrap.getLayoutParams();
        map_params.weight = weight;
        MapWrap.setLayoutParams(map_params);

        map_params = (LinearLayout.LayoutParams) MapContainer.getLayoutParams();
        map_params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        MapContainer.setLayoutParams(map_params);
    }

    public void changeInputWrapperWeight(float weight){
        InputWrap = (LinearLayout) findViewById(R.id.input_wrap);
        Log.v("Debug", "changeInputWrapperWeight called, weight is " + weight);
        LinearLayout.LayoutParams input_params = (LinearLayout.LayoutParams) InputWrap.getLayoutParams();
        input_params.weight = weight;
        InputWrap.setLayoutParams(input_params);
    }

    public void register(Intent data){
        Log.v("Debug", "register called");
        // 오버레이 리소스 관리객체 할당
        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);

        // 오버레이 관리자 추가
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);

        // 오버레이들을 관리하기 위한 id값 생성
        int markerId = NMapPOIflagType.PIN;

        Uri imageUri = data.getParcelableExtra("image");
        Drawable drawableImage;
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(imageUri);
//            drawableImage = Drawable.createFromStream(inputStream, imageUri.toString() );
//        } catch (FileNotFoundException e) {
//            drawableImage = getResources().getDrawable(R.drawable.penguins);
//        }

        // 표시할 위치 데이터를 지정한다. 마지막 인자가 오버레이를 인식하기 위한 id값
        NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
        poiData.beginPOIdata(1);
        poiData.addPOIitem(gp, "위치", markerId, 0);
        poiData.endPOIdata();

        // 위치 데이터를 사용하여 오버레이 생성
        poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

        poiDataOverlay.showAllPOIdata(0);
        Log.v("Debug", "register end");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case 1:
                Log.v("onActivityResult", "resultCode is 1");
                register(data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {
        if(nMapError == null){
            NGeoPoint location = startMyLocation();
            if (location == null){
                // 재시도.
            }
            else mMapController.setMapCenter(location,13);
        }
        else{
            android.util.Log.e("NMAP", "onMapInitHandler: error=" + nMapError.toString());
        }
    }

    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

        @Override
        public boolean onLocationChanged(NMapLocationManager locationManager,
                                         NGeoPoint myLocation) {
            Log.d("myLog", "myLocation  lat " + myLocation.getLatitude());
            Log.d("myLog", "myLocation  lng " + myLocation.getLongitude());

            return true;
        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager locationManager) {

            Toast.makeText(MainActivity.this,
                    "Your current location is temporarily unavailable.",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLocationUnavailableArea(
                NMapLocationManager locationManager, NGeoPoint myLocation) {

            Toast.makeText(MainActivity.this,
                    "Your current location is unavailable area.",
                    Toast.LENGTH_LONG).show();
            stopMyLocation();
        }
    };

    private NGeoPoint startMyLocation() {
        mMapLocationManager = new NMapLocationManager(this);
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);
        // 현재 위치 탐색을 시작한다.

        boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(false);
        if (isMyLocationEnabled) {
            return mMapLocationManager.getMyLocation();
        } else {
            if (mMapLocationManager.isMyLocationEnabled())
                return mMapLocationManager.getMyLocation();
            return new NGeoPoint(126.978371, 37.5666091);
        }
    }

    private void stopMyLocation() {
        if (mMyLocationOverlay != null) {
            mMapLocationManager.disableMyLocation();

            if (mMapView.isAutoRotateEnabled()) {
                mMyLocationOverlay.setCompassHeadingVisible(false);
                mMapCompassManager.disableCompass();
                mMapView.setAutoRotateEnabled(false, false);
                MapContainer.requestLayout();
            }
        }
    }

    @Override
    public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {

    }

    @Override
    public void onMapCenterChangeFine(NMapView nMapView) {

    }

    @Override
    public void onZoomLevelChange(NMapView nMapView, int i) {

    }

    @Override
    public void onAnimationStateChange(NMapView nMapView, int i, int i1) {

    }

    @Override
    public void onLongPress(NMapView nMapView, MotionEvent motionEvent) {
        gp = nMapView.getMapProjection().fromPixels((int)motionEvent.getX(), (int)motionEvent.getY());
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("xvalue", gp.getLatitude());
        intent.putExtra("yvalue", gp.getLongitude());
        startActivityForResult(intent, 0);
    }

    @Override
    public void onLongPressCanceled(NMapView nMapView) {

    }

    @Override
    public void onTouchDown(NMapView nMapView, MotionEvent motionEvent) {

    }

    @Override
    public void onTouchUp(NMapView nMapView, MotionEvent motionEvent) {

    }

    @Override
    public void onScroll(NMapView nMapView, MotionEvent motionEvent, MotionEvent motionEvent1) {

    }

    @Override
    public void onSingleTapUp(NMapView nMapView, MotionEvent motionEvent) {
    }


    private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {

        @Override
        public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
            // POI clicked
        }

        @Override
        public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            Log.v("test","click!!");
        }
    };
}
