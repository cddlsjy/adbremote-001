package com.swx.adbremote.activity.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.swx.adbremote.R;
import com.swx.adbremote.enums.SettingLayoutEnums;
import com.swx.adbremote.utils.Constant;
import com.swx.adbremote.utils.SharedData;

import java.util.HashMap;
import java.util.Map;

public class SettingLayoutActivity extends AppCompatActivity implements View.OnClickListener {
    private int settingLayoutNav;
    private int settingLayoutQuickAccess;
    private int settingLayoutBackground;
    private Map<Integer, ConstraintLayout> layoutNavMap;
    private Map<Integer, RadioButton> rbNavMap;
    private Map<Integer, ConstraintLayout> layoutQuickAccessMap;
    private Map<Integer, RadioButton> rbQuickAccessMap;
    private Map<Integer, ConstraintLayout> layoutBackgroundMap;
    private Map<Integer, RadioButton> rbBackgroundMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initSetting();
        init();
        initEvent();
    }

    /**
     * 初始化配置数据
     */
    private void initSetting() {
        settingLayoutNav = SharedData.getInstance().
                getInt(Constant.KEY_SETTING_LAYOUT_NAV, SettingLayoutEnums.NAVIGATION_DIRECTION.code());
        settingLayoutQuickAccess = SharedData.getInstance().
                getInt(Constant.KEY_SETTING_LAYOUT_QUICK_ACCESS, SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code());
        settingLayoutBackground = SharedData.getInstance().
                getInt("key_setting_layout_background", SettingLayoutEnums.BACKGROUND_DARK.code());
    }

    private void init() {
        layoutNavMap = new HashMap<>();
        layoutQuickAccessMap = new HashMap<>();
        layoutBackgroundMap = new HashMap<>();
        layoutNavMap.put(SettingLayoutEnums.NAVIGATION_DIRECTION.code(), (ConstraintLayout) findViewById(R.id.layout_direction_key));
        layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code(), (ConstraintLayout) findViewById(R.id.layout_tv_app));
        layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code(), (ConstraintLayout) findViewById(R.id.layout_media_button));
        layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_NONE.code(), (ConstraintLayout) findViewById(R.id.layout_none));
        layoutBackgroundMap.put(SettingLayoutEnums.BACKGROUND_DARK.code(), (ConstraintLayout) findViewById(R.id.layout_bg_dark));
        layoutBackgroundMap.put(SettingLayoutEnums.BACKGROUND_GRAY.code(), (ConstraintLayout) findViewById(R.id.layout_bg_gray));
        layoutBackgroundMap.put(SettingLayoutEnums.BACKGROUND_LIGHT.code(), (ConstraintLayout) findViewById(R.id.layout_bg_light));

        rbNavMap = new HashMap<>();
        rbQuickAccessMap = new HashMap<>();
        rbBackgroundMap = new HashMap<>();
        rbNavMap.put(SettingLayoutEnums.NAVIGATION_DIRECTION.code(), (RadioButton) findViewById(R.id.rb_direction_key));
        rbQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code(), (RadioButton) findViewById(R.id.rb_tv_app));
        rbQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code(), (RadioButton) findViewById(R.id.rb_media_button));
        rbQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_NONE.code(), (RadioButton) findViewById(R.id.rb_none));
        rbBackgroundMap.put(SettingLayoutEnums.BACKGROUND_DARK.code(), (RadioButton) findViewById(R.id.rb_bg_dark));
        rbBackgroundMap.put(SettingLayoutEnums.BACKGROUND_GRAY.code(), (RadioButton) findViewById(R.id.rb_bg_gray));
        rbBackgroundMap.put(SettingLayoutEnums.BACKGROUND_LIGHT.code(), (RadioButton) findViewById(R.id.rb_bg_light));

        setBGSelect(settingLayoutNav);
        setBGSelect(settingLayoutQuickAccess);
        setBGSelect(settingLayoutBackground);
    }

    @SuppressWarnings("all")
    private void initEvent() {
        findViewById(R.id.btn_setting_layout_back).setOnClickListener(this);
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutNavMap.entrySet()) {
            Integer key = entry.getKey();
            entry.getValue().setOnClickListener(SettingLayoutActivity.this);
            rbNavMap.get(key).setOnClickListener(SettingLayoutActivity.this);
        }
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutQuickAccessMap.entrySet()) {
            Integer key = entry.getKey();
            entry.getValue().setOnClickListener(SettingLayoutActivity.this);
            rbQuickAccessMap.get(key).setOnClickListener(SettingLayoutActivity.this);
        }
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutBackgroundMap.entrySet()) {
            Integer key = entry.getKey();
            entry.getValue().setOnClickListener(SettingLayoutActivity.this);
            rbBackgroundMap.get(key).setOnClickListener(SettingLayoutActivity.this);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_setting_layout_back) {
            finish();
        } else if (id == R.id.layout_direction_key || id == R.id.rb_direction_key) {
            setNavigation(SettingLayoutEnums.NAVIGATION_DIRECTION.code());
        } else if (id == R.id.layout_tv_app || id == R.id.rb_tv_app) {
            setQuickAccess(SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code());
        } else if (id == R.id.layout_media_button || id == R.id.rb_media_button) {
            setQuickAccess(SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code());
        } else if (id == R.id.layout_none || id == R.id.rb_none) {
            setQuickAccess(SettingLayoutEnums.QUICK_ACCESS_NONE.code());
        } else if (id == R.id.layout_bg_dark || id == R.id.rb_bg_dark) {
            setBackground(SettingLayoutEnums.BACKGROUND_DARK.code());
        } else if (id == R.id.layout_bg_gray || id == R.id.rb_bg_gray) {
            setBackground(SettingLayoutEnums.BACKGROUND_GRAY.code());
        } else if (id == R.id.layout_bg_light || id == R.id.rb_bg_light) {
            setBackground(SettingLayoutEnums.BACKGROUND_LIGHT.code());
        }
    }

    @SuppressWarnings("all")
    private void setBGSelect(int code) {
        ConstraintLayout layout = layoutNavMap.get(code);
        if (layout != null) {
            layoutNavMap.get(code).setBackgroundResource(R.drawable.background_4border_selected);
            rbNavMap.get(code).setChecked(true);
        } else if (layoutQuickAccessMap.get(code) != null) {
            layoutQuickAccessMap.get(code).setBackgroundResource(R.drawable.background_4border_selected);
            rbQuickAccessMap.get(code).setChecked(true);
        } else if (layoutBackgroundMap.get(code) != null) {
            layoutBackgroundMap.get(code).setBackgroundResource(R.drawable.background_4border_selected);
            rbBackgroundMap.get(code).setChecked(true);
        }
    }

    @SuppressWarnings("all")
    private void setNavigation(int code) {
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutNavMap.entrySet()) {
            Integer key = entry.getKey();
            if (key == code) {
                // 选中的
                entry.getValue().setBackgroundResource(R.drawable.background_4border_selected);
                rbNavMap.get(key).setChecked(true);
                SharedData.getInstance().put(Constant.KEY_SETTING_LAYOUT_NAV, code).commit();
            } else {
                entry.getValue().setBackgroundResource(R.drawable.background_4border);
                rbNavMap.get(key).setChecked(false);
            }
        }
    }

    @SuppressWarnings("all")
    private void setQuickAccess(int code) {
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutQuickAccessMap.entrySet()) {
            Integer key = entry.getKey();
            if (key == code) {
                entry.getValue().setBackgroundResource(R.drawable.background_4border_selected);
                rbQuickAccessMap.get(key).setChecked(true);
                SharedData.getInstance().put(Constant.KEY_SETTING_LAYOUT_QUICK_ACCESS, code).commit();
            } else {
                entry.getValue().setBackgroundResource(R.drawable.background_4border);
                rbQuickAccessMap.get(key).setChecked(false);
            }
        }
    }

    @SuppressWarnings("all")
    private void setBackground(int code) {
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutBackgroundMap.entrySet()) {
            Integer key = entry.getKey();
            if (key == code) {
                entry.getValue().setBackgroundResource(R.drawable.background_4border_selected);
                rbBackgroundMap.get(key).setChecked(true);
                SharedData.getInstance().put("key_setting_layout_background", code).commit();
            } else {
                entry.getValue().setBackgroundResource(R.drawable.background_4border);
                rbBackgroundMap.get(key).setChecked(false);
            }
        }
    }
}