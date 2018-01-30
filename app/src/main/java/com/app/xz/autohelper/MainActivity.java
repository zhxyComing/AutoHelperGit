package com.app.xz.autohelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView tvTip, tvTipMode, btnOpenMode;
    private Button btnOpenService;
    private LinearLayout mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        tvTip = findViewById(R.id.tip);
        btnOpenService = findViewById(R.id.openService);
        tvTipMode = findViewById(R.id.tip_mode);
        btnOpenMode = findViewById(R.id.openMode);
        mode = findViewById(R.id.mode);

        btnOpenService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openService();
            }
        });

        btnOpenMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AutoService.strongMode) {
                    AutoService.strongMode = false;
                    btnOpenMode.setText("开启强力模式");
                    tvTipMode.setText("强力模式未启动");
                    tvTipMode.setBackgroundColor(Color.parseColor("#cccccc"));
                } else {
                    AutoService.strongMode = true;
                    btnOpenMode.setText("关闭强力模式");
                    tvTipMode.setText("强力模式已启动");
                    tvTipMode.setBackgroundColor(Color.parseColor("#880000"));
                }
            }
        });

        WindowManager wm = this.getWindowManager();
        MyApplication.screenWidth = wm.getDefaultDisplay().getWidth();
        MyApplication.screenHeight = wm.getDefaultDisplay().getHeight();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isAccessibilitySettingsOn(this)) {
            btnOpenService.setVisibility(View.GONE);
            tvTip.setBackgroundColor(Color.parseColor("#03A9F4"));
            tvTip.setText("服务运行中");
            mode.setVisibility(View.VISIBLE);
        } else {
            btnOpenService.setVisibility(View.VISIBLE);
            tvTip.setBackgroundColor(Color.parseColor("#cc0000"));
            tvTip.setText("当前服务未启动");
            mode.setVisibility(View.GONE);
        }

        if (AutoService.strongMode) {
            btnOpenMode.setText("关闭强力模式");
            tvTipMode.setText("强力模式已启动");
            tvTipMode.setBackgroundColor(Color.parseColor("#880000"));
        } else {
            btnOpenMode.setText("开启强力模式");
            tvTipMode.setText("强力模式未启动");
            tvTipMode.setBackgroundColor(Color.parseColor("#cccccc"));
        }

    }

    //前往开启服务
    private void openService() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    //检测服务是否开启
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + AutoService.class.getCanonicalName();

        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {

        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {

            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
