package com.example.hongssang.navertest;

import android.app.FragmentTransaction;
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

//import android.database.sqlite.SQLiteOpenHelper;

public class MainActivity extends NMapActivity implements NMapView.OnMapStateChangeListener, NMapView.OnMapViewTouchEventListener {

    public static final String CLIENT_ID = "EacoUq5gGV_fYxm9nqSM";
    public NMapView mMapView = null;
    NMapController mMapController = null;
    LinearLayout MapContainer = null;
    Button search_button, register_button, add_button;
    NGeoPoint gp;

    // 오버레이의 리소스를 제공하기 위한 객체
    NMapViewerResourceProvider mMapViewerResourceProvider = null;
    // 오버레이 관리자
    NMapOverlayManager mOverlayManager;
    private NMapLocationManager mMapLocationManager;
    private NMapMyLocationOverlay mMyLocationOverlay;
    private NMapCompassManager mMapCompassManager;
    private NMapPOIdataOverlay poiDataOverlay;

    int bottom_mode = 0;
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

        search_button = (Button) findViewById(R.id.button_search);
        search_button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                final FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.bottom_container, new SearchFragment());
                transaction.commit();
                bottom_mode = 0;

               register_button.setText("REGISTER");
            }
        });

        register_button = (Button) findViewById(R.id.button_register);
        register_button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                if(bottom_mode != 1) {
                    final FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.bottom_container, new RegisterFragment());
                    transaction.commit();
                    bottom_mode = 1;

                    register_button.setText("REGISTER NOW");
                }
                else{
                    register();
                }
            }
        });
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

    public void register(){
        Log.v("Debug", "register called");
        TextView tempX = (TextView) findViewById(R.id.x_value);
        TextView tempY = (TextView) findViewById(R.id.y_value);
        Double X = new Double(tempX.getText().toString());
        Double Y = new Double(tempY.getText().toString());
        Log.v("Double", X.toString());
        Log.v("Double", Y.toString());
        // 오버레이 리소스 관리객체 할당
        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);

        // 오버레이 관리자 추가
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);

        // 오버레이들을 관리하기 위한 id값 생성
        int markerId = NMapPOIflagType.PIN;

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
        if(bottom_mode == 1){
            gp = nMapView.getMapProjection().fromPixels((int)motionEvent.getX(), (int)motionEvent.getY());
            TextView tempX = (TextView) findViewById(R.id.x_value);
            tempX.setText(Double.toString( gp.getLatitude() ) );
            TextView tempY = (TextView) findViewById(R.id.y_value);
            tempY.setText(Double.toString( gp.getLongitude() ) );
        }
    }
}
