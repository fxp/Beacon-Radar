package com.xiaopingfeng.beaconinfo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.IDNA;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.BoringLayout;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.brtbeacon.sdk.BRTBeacon;
import com.brtbeacon.sdk.BRTBeaconConfig;
import com.brtbeacon.sdk.BRTBeaconManager;
import com.brtbeacon.sdk.BRTThrowable;
import com.brtbeacon.sdk.IBle;
import com.brtbeacon.sdk.Utils;
import com.brtbeacon.sdk.callback.BRTBeaconManagerListener;
import com.brtbeacon.sdk.utils.L;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public class FixedSizeLimitedArrayList extends ArrayList<Object> {
        @Override
        public boolean add(Object o) {
            int n = 10;
            if (this.size() < n) {
                return super.add(o);
            }
            return false;
        }
    }


    final static double TRIGGER_DISTANCE = 1.5;

    Boolean isOnViewing = false;
    FixedSizeLimitedArrayList currentBeacons = new FixedSizeLimitedArrayList();

    HashMap<String, String> beaconInfoMap = new HashMap<>();

    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();


    private void stopTimer() {
        if (mTimer1 != null) {
            mTimer1.cancel();
            mTimer1.purge();
        }
    }

    private void startTimer() {
        mTimer1 = new Timer();
        mTt1 = new TimerTask() {
            public void run() {
                AVQuery<AVObject> avQuery = new AVQuery<>("BeaconInfo");
                avQuery.findInBackground(new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {
                        if (e == null) {
                            HashMap<String, String> newBeaconInfoMap = new HashMap<>();
//                            HashMap<String, String> oldBeaconInfoMap = beaconInfoMap;
                            for (AVObject info : list) {
                                newBeaconInfoMap.put(
                                        info.get("uuid") + "," + info.get("major") + "," + info.get("minor"),
                                        info.getString("infoUrl")
                                );
                            }
                            beaconInfoMap = newBeaconInfoMap;
//                            oldBeaconInfoMap.clear();
                            Log.i("beaconinfo", beaconInfoMap.keySet().toString());
                        } else {
                            e.printStackTrace();
                        }
                    }
                });

                Log.i("beacon", "info updated");

            }
        };
        mTimer1.schedule(mTt1, 1, 10 * 1000);
    }

    private final Handler handler_beacons = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            statusText.setText(new Date().toString() + " (" + msg.obj + ")");

        }
    };


    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.i("activity", "handler1!!!");
//            String infoUrl = "http://www.baidu.com";
            String infoUrl = (String) msg.obj;
            final WebView webView = new WebView(MainActivity.this);
            webView.loadUrl(infoUrl);

//                    Toast.makeText(MainActivity.this, "new beacon " + distance, Toast.LENGTH_LONG).show();
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setView(webView);
//            dialog.setTitle(infoUrl);
//                    dialog.setTitle("Nearest beacon " + distance);
//                    dialog.setTitle("我的iBeacon");
            dialog.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i("Click", "YESSSSSS");
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Log.i("Click", "dismiss");
                    isOnViewing = false;
                }
            });
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
//                    dialog.setTitle(view.getTitle());
                    dialog.setTitle(view.getTitle());

//                    Toast.makeText(MainActivity.this, "Loaded!", Toast.LENGTH_LONG).show();
                }
            });
//                    ((View)MainActivity.this).playSoundEffect(SoundEffectConstants.CLICK);
            dialog.show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("activity", "onResume");
        isOnViewing = false;

    }

    TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.statusText);

        AVOSCloud.initialize(this, "T9Mk1ewUPC71yyu0y81vrY1V-gzGzoHsz", "s2qKRVpMKBIipzsvFMws7HYN");
        startTimer();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN
                        },
                        123);
            }
        }


        //获取单例
        beaconManager = BRTBeaconManager.getInstance(this);
        // 注册应用 APPKEY申请:http://brtbeacon.com/main/index.shtml
        beaconManager.registerApp("fe04bb47f55a43cbbfc859a2fc7ada1c");
        // 开启Beacon扫描服务
        beaconManager.startService();
        L.enableDebugLogging(false);

        BRTBeaconManagerListener beaconManagerListener = new BRTBeaconManagerListener() {

            @Override
            public void onUpdateBeacon(ArrayList<BRTBeacon> beacons) {
                Log.i("beacon", "size:" + beacons.size());
                Message beaconsMessage = new android.os.Message();
                beaconsMessage.obj = beacons.size();

                handler_beacons.sendMessage(beaconsMessage);

                for (BRTBeacon beacon : beacons) {
                    // Beacon信息更新
                    // 发现一个新的Beacon
                    Log.i("beacon", "info:" + beacon.toString());
                    double distance = Utils.computeAccuracy(beacon);
                    String beaconId = beacon.getUuid() + "," + beacon.getMajor() + "," + beacon.getMinor();
                    boolean hasInfo = beaconInfoMap.containsKey(beaconId);
                    boolean isInRange = (distance < TRIGGER_DISTANCE);
                    Log.i("beacon", beaconId + "," + Utils.computeAccuracy(beacon) + "," + hasInfo + "," + isInRange);
                    currentBeacons.add(distance);
                    if (distance < 0) {
                        continue;
                    }

                    if (!isOnViewing && hasInfo && isInRange) {
                        isOnViewing = true;
                        String infoUrl = beaconInfoMap.get(beaconId);
//                        WebView webView = new WebView(MainActivity.this);
//                        webView.loadUrl(infoUrl);
//
////                    Toast.makeText(MainActivity.this, "new beacon " + distance, Toast.LENGTH_LONG).show();
//                        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
//                        dialog.setView(webView);
//                        dialog.setTitle(infoUrl);
////                    dialog.setTitle("Nearest beacon " + distance);
////                    dialog.setTitle("我的iBeacon");
//                        dialog.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                Log.i("Click", "YESSSSSS");
//                            }
//                        });
//                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                            @Override
//                            public void onDismiss(DialogInterface dialogInterface) {
//                                Log.i("Click", "dismiss");
//                                isOnViewing = false;
//                            }
//                        });
//                        webView.setWebViewClient(new WebViewClient() {
//                            @Override
//                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                                return false;
//                            }
//
////                        @Override
////                        public void onPageFinished(WebView view, String url) {
////                            super.onPageFinished(view, url);
////                            Toast.makeText(MainActivity.this, "Loaded!", Toast.LENGTH_LONG).show();
////                        }
//                        });
////                    ((View)MainActivity.this).playSoundEffect(SoundEffectConstants.CLICK);
//                        dialog.show();

//                        Intent i = new Intent(MainActivity.this, InfoActivity.class);
//                        startActivity(i);
                        Message alertMessage = new android.os.Message();
                        alertMessage.obj = infoUrl;

                        handler.sendMessage(alertMessage);

                        break;
                    }
                }
            }

            @Override
            public void onNewBeacon(BRTBeacon beacon) {

            }

            @Override
            public void onGoneBeacon(BRTBeacon beacon) {
                // 一个Beacon消失
            }

            @Override
            public void onError(BRTThrowable brtThrowable) {
                Log.i("beacon", "err" + brtThrowable.toString());

            }
        };
        beaconManager.setBRTBeaconManagerListener(beaconManagerListener);
        beaconManager.startRanging();

    }

    private BRTBeaconManager beaconManager;

    /**
     * 创建Beacon连接需要传递此参数
     *
     * @return IBle
     */
    public IBle getIBle() {
        return beaconManager.getIBle();
    }

    /**
     * 获取Beacon管理对象
     *
     * @return BRTBeaconManager
     */
    public BRTBeaconManager getBRTBeaconManager() {
        return beaconManager;
    }


}
