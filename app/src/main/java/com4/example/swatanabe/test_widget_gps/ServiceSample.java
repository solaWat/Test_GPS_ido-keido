package com4.example.swatanabe.test_widget_gps;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by s.Watanabe on 2017/02/24.
 */


public class ServiceSample extends Service implements LocationListener {
    private static final String TAG = ServiceSample.class.getSimpleName();
    private final ServiceSample self = this;

    private final String START_ACTION = "START_ACTION";
    private final String FINISH_ACTION = "FINISH_ACTION";

    private LocationManager mLocationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // ボタンが押された時に発行されるインテントを準備する
        Intent startIntent = new Intent(START_ACTION);
        Intent finishIntent = new Intent(FINISH_ACTION);

        PendingIntent startPendingIntent = PendingIntent.getService(this, 0, startIntent, 0);
        PendingIntent finishPendingIntent = PendingIntent.getService(this, 0, finishIntent, 0);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.new_app_widget);
        remoteViews.setOnClickPendingIntent(R.id.startGetLocation, startPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.finishGetLocation, finishPendingIntent);

        if (START_ACTION.equals(intent.getAction())) {

            Log.d(TAG, "位置情報取得開始");

            getMyLocation();

            remoteViews.setTextViewText(R.id.latitude, "位置情報取得中...");
            remoteViews.setTextViewText(R.id.longitude, "位置情報取得中...");

        } else if (FINISH_ACTION.equals(intent.getAction())) {
            if (checkPermission()) {

                Log.d(TAG, "位置情報取得完了");

                //位置情報の取得終了
                mLocationManager.removeUpdates(this);

                remoteViews.setTextViewText(R.id.latitude, null);
                remoteViews.setTextViewText(R.id.longitude, null);

            }
        }

        // AppWidgetの画面更新
        ComponentName thisWidget = new ComponentName(this, NewAppWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, remoteViews);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getMyLocation() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Criteriaオブジェクトを生成
        Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_LOW); //位置情報の精度
        criteria.setPowerRequirement(Criteria.POWER_HIGH); //消費電力
        criteria.setAltitudeRequired(false); //高度情報取得の有無
        criteria.setSpeedRequired(false); //速度情報取得の有無
        criteria.setBearingRequired(false); //方向情報取得の有無
        criteria.setCostAllowed(false); //費用を許可するか

        String provider = mLocationManager.getBestProvider(criteria, true);

        if (checkPermission()) {
            // LocationListenerを登録
            mLocationManager.requestLocationUpdates(provider, 0, 0, this);
        } else {
            Log.d(TAG, "位置情報取得不可");

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.new_app_widget);
            remoteViews.setTextViewText(R.id.latitude, "権限不足のため、位置情報取得に失敗しました");
            remoteViews.setTextViewText(R.id.longitude, "権限不足のため、位置情報取得に失敗しました");

            // AppWidgetの画面更新
            ComponentName thisWidget = new ComponentName(this, NewAppWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, remoteViews);
        }
    }

    private boolean checkPermission() {

        boolean ret = true;
        if (getPackageManager().checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, getPackageName())
                == PackageManager.PERMISSION_GRANTED
                && getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, getPackageName())
                == PackageManager.PERMISSION_GRANTED
                ) {
        //パーミッションあり
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public void onLocationChanged(Location location) {

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.new_app_widget);
        remoteViews.setTextViewText(R.id.latitude, Double.toString(location.getLatitude()));
        remoteViews.setTextViewText(R.id.longitude, Double.toString(location.getLongitude()));

        // AppWidgetの画面更新
        ComponentName thisWidget = new ComponentName(this, NewAppWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, remoteViews);

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}