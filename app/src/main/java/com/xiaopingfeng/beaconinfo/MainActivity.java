package com.xiaopingfeng.beaconinfo;

import android.content.DialogInterface;
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
import android.widget.Toast;

import com.brtbeacon.sdk.BRTBeacon;
import com.brtbeacon.sdk.BRTBeaconManager;
import com.brtbeacon.sdk.BRTThrowable;
import com.brtbeacon.sdk.IBle;
import com.brtbeacon.sdk.Utils;
import com.brtbeacon.sdk.callback.BRTBeaconManagerListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Boolean isOnViewing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //获取单例
        beaconManager = BRTBeaconManager.getInstance(this);
        // 注册应用 APPKEY申请:http://brtbeacon.com/main/index.shtml
        beaconManager.registerApp("fe04bb47f55a43cbbfc859a2fc7ada1c");
        // 开启Beacon扫描服务
        beaconManager.startService();

        BRTBeaconManagerListener beaconManagerListener = new BRTBeaconManagerListener() {

            @Override
            public void onUpdateBeacon(ArrayList<BRTBeacon> beacons) {
                // Beacon信息更新
            }

            @Override
            public void onNewBeacon(BRTBeacon beacon) {
                // 发现一个新的Beacon
                double distance = Utils.computeAccuracy(beacon);
                Log.i("beacon", "yoyo(" + Utils.computeAccuracy(beacon) + ")" + beacon);
                if (distance > 0 && distance < 0.3 && !isOnViewing) {
                    isOnViewing = true;
                    WebView webView = new WebView(MainActivity.this);
                    webView.loadUrl("http://beaconinfo.leanapp.cn/");

                    Toast.makeText(MainActivity.this, "new beacon " + distance, Toast.LENGTH_LONG).show();
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setView(webView);
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
//                    webView.setWebViewClient(new WebViewClient() {
//                        @Override
//                        public void onPageFinished(WebView view, String url) {
//                            super.onPageFinished(view, url);
////                            Toast.makeText(MainActivity.this, "Loaded!", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    ((View)MainActivity.this).playSoundEffect(SoundEffectConstants.CLICK);

                    dialog.show();
                }
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
