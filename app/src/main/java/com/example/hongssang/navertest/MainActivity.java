package com.example.hongssang.navertest;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class MainActivity extends NMapActivity implements NMapView.OnMapStateChangeListener,
        NMapView.OnMapViewTouchEventListener, AppCompatCallback {
    // DB
    SQLiteDatabase db;
    private DBAdapter helper;
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
    private AppCompatDelegate delegate;

    //0.search, 1,register, 2.nothing
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //let's create the delegate, passing the activity at both arguments (Activity, AppCompatCallback)
        delegate = AppCompatDelegate.create(this, this);

        //we need to call the onCreate() of the AppCompatDelegate
        delegate.onCreate(savedInstanceState);

        //we use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_main);

        //Finally, let's add the Toolbar
        Toolbar toolbar= (Toolbar) findViewById(R.id.my_awesome_toolbar);
        delegate.setSupportActionBar(toolbar);

        //
        helper = new DBAdapter(this);

        MapContainer = (LinearLayout) findViewById(R.id.map);
        mMapView = new NMapView(this);
        mMapView.setClientId(CLIENT_ID);
        MapContainer.addView(mMapView);
        mMapView.setClickable(true);
        mMapView.setOnMapStateChangeListener(this);
        mMapView.setOnMapViewTouchEventListener(this);
        mMapView.setBuiltInZoomControls(true, null);
        mMapController = mMapView.getMapController();

        settingAllPOI();
        setButtons();
        mContext = this;
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.your_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
*/
    // 앱 실행시 데이터베이스에서 정보 읽어와서 모든 POI 뿌려준다.
    public void settingAllPOI(){
        // 1) db의 데이터를 읽어와서
        // 2) 결과 저장 3) 해당 데이터를 꺼내 사용
        // 표시할 위치 데이터를 지정한다. 마지막 인자가 오버레이를 인식하기 위한 id값
        String name, call, menu, image;
        Double x, y;

        db = helper.getReadableDatabase();
        Cursor cursor = db.query("mytable",null,null,null,null,null,null);

        while (cursor.moveToNext()){
            name = cursor.getString(cursor.getColumnIndex("name"));
            call = cursor.getString(cursor.getColumnIndex("call"));
            menu = cursor.getString(cursor.getColumnIndex("menu"));
            x = cursor.getDouble(cursor.getColumnIndex("x"));
            y = cursor.getDouble(cursor.getColumnIndex("y"));
            image = cursor.getString(cursor.getColumnIndex("image"));
            int markerId = NMapPOIflagType.PIN;

            Log.d("db","name: "+name+",call :"+ call + ",menu :"+menu+", x:"+x+",y:"+y);
            NGeoPoint temp = new NGeoPoint(x,y);

            mMapViewerResourceProvider = new NMapViewerResourceProvider(this);
            mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);

            NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
            poiData.beginPOIdata(1);
            poiData.addPOIitem(temp, name, markerId, 0);
            poiData.endPOIdata();
            // 위치 데이터를 사용하여 오버레이 생성
            poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
            POIdataStateChangeListener onPOIdataStateChangeListener = new POIdataStateChangeListener();
            onPOIdataStateChangeListener.setInfo(name, call, menu, Uri.parse(image));
            poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

            poiDataOverlay.showAllPOIdata(0);
        }
    }

    private void setButtons(){

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

        String name = data.getStringExtra("name");
        String call = data.getStringExtra("call");
        String menu = data.getStringExtra("menu");
        Uri image = data.getParcelableExtra("image");
        Double x = gp.getLongitude();
        Double y = gp.getLatitude();

        insert(name,call,menu,x,y, image.toString());

        // 표시할 위치 데이터를 지정한다. 마지막 인자가 오버레이를 인식하기 위한 id값
        NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
        poiData.beginPOIdata(1);
        poiData.addPOIitem(gp, name, markerId, 0);
        poiData.endPOIdata();

        // 위치 데이터를 사용하여 오버레이 생성
        poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
        POIdataStateChangeListener onPOIdataStateChangeListener = new POIdataStateChangeListener();
        onPOIdataStateChangeListener.setInfo(name, call, menu,image);
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

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    private class POIdataStateChangeListener implements NMapPOIdataOverlay.OnStateChangeListener {
        String name, call, menu;
        Uri image;
        public void setInfo(String name, String call, String menu ,Uri image){
            this.name = name;
            this.call = call;
            this.menu = menu;
            this.image = image;
        }
        @Override
        public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
            // POI clicked
        }
        @Override
        public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            Log.v("test","click!!");
            Intent intent = new Intent(mContext, InfoActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("call", call);
            intent.putExtra("menu", menu);
            intent.putExtra("image", image);
            startActivity(intent);
        }
    };

    // DB
    void deleteAll() {
        db = helper.getWritableDatabase();

        db.delete("mytable", null, null);
    }

    void insert(String name, String call, String menu, Double x, Double y, String image){
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("call", call);
        values.put("menu", menu);
        values.put("x",x);
        values.put("y",y);
        values.put("image", image);
        Log.i("db insert","name: "+name+",call :"+ call + ",menu :"+menu+", image"+image);

        db.insert("mytable", null, values);
    }

    public void delete (String name){
        db = helper.getWritableDatabase();
        db.delete("mytable","name=?",new String[]{name});
        Log.i("db",name + "정상적으로 삭제 되었습니다.");
    }

    public void select(){
        // 1) db의 데이터를 읽어와서
        // 2) 결과 저장 3) 해당 데이터를 꺼내 사용

        db = helper.getReadableDatabase();
        Cursor cursor = db.query("mytable",null,null,null,null,null,null);

        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String lat = cursor.getString(cursor.getColumnIndex("lat"));
            String lng = cursor.getString(cursor.getColumnIndex("lng"));

            Log.i("db","name: "+name+",lat :"+ lat + ",lng :"+lng);
        }
    }
}
