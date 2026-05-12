package com.swx.adbremote;

import static com.swx.adbremote.utils.ADBConnectUtil.callable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.swx.adbremote.activity.ConnectInstanceActivity;
import com.swx.adbremote.activity.SettingActivity;
import com.swx.adbremote.adapter.QuickAccessAppsAdapter;
import com.swx.adbremote.adapter.ViewPager2Adapter;
import com.swx.adbremote.components.InputKeyboardDialog;
import com.swx.adbremote.components.IndicatorView;
import com.swx.adbremote.components.QuestionDialog;
import com.swx.adbremote.database.DBManager;
import com.swx.adbremote.entity.AppItem;
import com.swx.adbremote.entity.ConnectInstance;
import com.swx.adbremote.enums.InputLanguageEnums;
import com.swx.adbremote.enums.SettingLayoutEnums;
import com.swx.adbremote.fragment.NumKeyboardFragment;
import com.swx.adbremote.fragment.RoundMenuFragment;
import com.swx.adbremote.utils.ADBConnectUtil;
import com.swx.adbremote.utils.BeanUtil;
import com.swx.adbremote.utils.Constant;
import com.swx.adbremote.utils.MetricsUtil;
import com.swx.adbremote.utils.RecyclerViewItemEqspa;
import com.swx.adbremote.utils.ScheduleUtil;
import com.swx.adbremote.utils.SharedData;
import com.swx.adbremote.utils.ThreadPoolService;
import com.swx.adbremote.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int WHAT_LIST_QUICK_ACCESS = -1;
    private ViewPager2 viewPagerPanel;
    private ImageButton btnSwitchPanel;
    private LinearLayout llChooseTvConnect;
    private TextView tvConnectAlias;
    private TextView tvConnectIp;
    private EditText etIpAddress;
    private ImageButton btnMainScan;
    private Button btnConnect;
    private int settingQuickAccess;
    private boolean isHapticFeedback;
    private boolean quickAccessOrderChange;
    private Map<Integer, ViewGroup> layoutQuickAccessMap;
    private ScheduledExecutorService executorService;
    private QuickAccessAppsAdapter mQuickAccessAppsAdapter;
    ScheduledFuture<?> scheduledFuture;
    private Handler handler;
    InputKeyboardDialog textInputDialog;
    QuestionDialog chineseInputQuestionDialog;
    private String originKeyboard;
    private RecyclerView mRvQuickAccess;

    private ADBConnectUtil.ShellExecCallable switchKeyboardCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initEvent();
        ToastUtil.init(getApplication());
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSetting();
        if (tvConnectAlias != null) {
            ConnectInstance bean = ADBConnectUtil.getConnectedBean();
            if (bean != null) {
                tvConnectAlias.setText(bean.getAlias());
            } else {
                tvConnectAlias.setText(this.getString(R.string.text_connect_android_tv));
            }
        }
        if (settingQuickAccess == SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code()) {
            getQuickAccessData();
        }
        updateConnectionStatus();
    }

    private void init() {
        btnSwitchPanel = findViewById(R.id.btn_switch_panel);
        executorService = ScheduleUtil.getExecutorService();
        viewPagerPanel = findViewById(R.id.viewPagerPanel);

        if (viewPagerPanel != null) {
            ArrayList<Fragment> list = new ArrayList<>();
            list.add(new RoundMenuFragment());
            list.add(new NumKeyboardFragment());
            viewPagerPanel.setAdapter(new ViewPager2Adapter(this, list));
            IndicatorView indicator = findViewById(R.id.indicator);
            if (indicator != null) {
                indicator.setIndicatorCount(2);
                viewPagerPanel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        indicator.setCurrentSelectedPosition(position);
                        indicator.postInvalidate();
                        if (btnSwitchPanel != null) {
                            if (position == 1) {
                                btnSwitchPanel.setImageResource(R.drawable.ic_tel_keyboard);
                            } else {
                                btnSwitchPanel.setImageResource(R.drawable.ic_round_menu);
                            }
                        }
                    }
                });
            }
        } else {
            setupLandButtons();
        }

        llChooseTvConnect = findViewById(R.id.tv_choose_connect);
        tvConnectAlias = findViewById(R.id.tv_connect_alias);
        tvConnectIp = findViewById(R.id.tv_connect_ip);
        etIpAddress = findViewById(R.id.et_ip_address);
        btnMainScan = findViewById(R.id.btn_main_scan);
        btnConnect = findViewById(R.id.btn_connect);
        mRvQuickAccess = findViewById(R.id.layout_quick_access_app);

        if (mRvQuickAccess != null) {
            mQuickAccessAppsAdapter = new QuickAccessAppsAdapter(LayoutInflater.from(this), LinearLayoutManager.HORIZONTAL);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mRvQuickAccess.setLayoutManager(layoutManager);
            mRvQuickAccess.addItemDecoration(new RecyclerView.ItemDecoration() {
                private final int unit = MetricsUtil.dp2px(4);

                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    RecyclerViewItemEqspa.equilibriumAssignmentOfLinear(unit, outRect, view, parent);
                }
            });
            mRvQuickAccess.setAdapter(mQuickAccessAppsAdapter);
        }

        switchKeyboardCallback = (result, msg) -> {
            if (!result) {
                ToastUtil.showToastThread(MainActivity.this.getString(R.string.text_connection_failed));
                return;
            }
            if (msg.contains("cannot be selected for")) {
                ToastUtil.showToastThread(MainActivity.this.getString(R.string.text_switch_failed));
            } else {
                ToastUtil.showToastThread(MainActivity.this.getString(R.string.text_switch_successful));
            }
        };
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == WHAT_LIST_QUICK_ACCESS) {
                    if (mQuickAccessAppsAdapter != null) {
                        mQuickAccessAppsAdapter.setData(BeanUtil.castList(msg.obj, AppItem.class));
                    }
                }
            }
        };

        layoutQuickAccessMap = new HashMap<>();
        View mediaLayout = findViewById(R.id.layout_quick_access_media_button);
        if (mediaLayout != null) {
            layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code(), (ViewGroup) mediaLayout);
        }
        if (mRvQuickAccess != null) {
            layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code(), mRvQuickAccess);
        }
        View noneLayout = findViewById(R.id.layout_quick_access_none);
        if (noneLayout != null) {
            layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_NONE.code(), (ViewGroup) noneLayout);
        }
    }

    private void setupLandButtons() {
        ImageButton btnUp = findViewById(R.id.btn_up);
        ImageButton btnDown = findViewById(R.id.btn_down);
        ImageButton btnLeft = findViewById(R.id.btn_left);
        ImageButton btnRight = findViewById(R.id.btn_right);
        ImageButton btnOk = findViewById(R.id.btn_ok);
        ImageButton btnMouse = findViewById(R.id.btn_mouse_mode);

        ADBConnectUtil.ShellExecCallable callable = ADBConnectUtil.callable;
        if (btnUp != null) btnUp.setOnClickListener(v -> { hapticFeedback(v); ADBConnectUtil.pressUp(callable); });
        if (btnDown != null) btnDown.setOnClickListener(v -> { hapticFeedback(v); ADBConnectUtil.pressDown(callable); });
        if (btnLeft != null) btnLeft.setOnClickListener(v -> { hapticFeedback(v); ADBConnectUtil.pressLeft(callable); });
        if (btnRight != null) btnRight.setOnClickListener(v -> { hapticFeedback(v); ADBConnectUtil.pressRight(callable); });
        if (btnOk != null) btnOk.setOnClickListener(v -> { hapticFeedback(v); ADBConnectUtil.pressOk(callable, false); });
        if (btnMouse != null) btnMouse.setOnClickListener(v -> ToastUtil.show("鼠标模式开发中"));
    }

    private void initEvent() {
        if (mQuickAccessAppsAdapter != null) {
            mQuickAccessAppsAdapter.setOnItemClickListener(this::handleQuickAccessRvItemClick);
        }
        if (llChooseTvConnect != null) llChooseTvConnect.setOnClickListener(this);
        if (btnMainScan != null) btnMainScan.setOnClickListener(this);
        if (btnConnect != null) btnConnect.setOnClickListener(this);
        if (etIpAddress != null) {
            etIpAddress.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    connectToDevice();
                    return true;
                }
                return false;
            });
        }

        setOnClickListenerIfExists(R.id.btn_text_input, this);
        setOnClickListenerIfExists(R.id.btn_mute, this);
        setOnClickListenerIfExists(R.id.btn_setting, this);
        setOnClickListenerIfExists(R.id.btn_switch_panel, this);
        setOnClickListenerIfExists(R.id.btn_tv_home, this);
        setOnClickListenerIfExists(R.id.btn_tv_back, this);
        setOnClickListenerIfExists(R.id.btn_tv_shutdown, this);
        setOnClickListenerIfExists(R.id.btn_tv_media_play_pause, this);
        setOnClickListenerIfExists(R.id.btn_tv_media_prev, this);
        setOnClickListenerIfExists(R.id.btn_tv_media_next, this);
        setOnClickListenerIfExists(R.id.btn_tv_media_fast_forward, this);
        setOnClickListenerIfExists(R.id.btn_tv_media_rewind, this);
        setOnClickListenerIfExists(R.id.btn_tv_menu, this);
        setOnClickListenerIfExists(R.id.btn_up, this);
        setOnClickListenerIfExists(R.id.btn_down, this);
        setOnClickListenerIfExists(R.id.btn_left, this);
        setOnClickListenerIfExists(R.id.btn_right, this);
        setOnClickListenerIfExists(R.id.btn_ok, this);

        ImageView btnTurnUpVolume = findViewById(R.id.btn_turn_up_volume);
        ImageView btnTurnDownVolume = findViewById(R.id.btn_turn_down_volume);
        if (btnTurnUpVolume != null) {
            btnTurnUpVolume.setOnLongClickListener(this::handleLongClick);
            btnTurnUpVolume.setOnTouchListener(this::handleLongpressBtnKeyEvent);
            btnTurnUpVolume.setOnClickListener(this);
        }
        if (btnTurnDownVolume != null) {
            btnTurnDownVolume.setOnLongClickListener(this::handleLongClick);
            btnTurnDownVolume.setOnTouchListener(this::handleLongpressBtnKeyEvent);
            btnTurnDownVolume.setOnClickListener(this);
        }
    }

    private void setOnClickListenerIfExists(int viewId, View.OnClickListener listener) {
        View v = findViewById(viewId);
        if (v != null) v.setOnClickListener(listener);
    }

    private void initSetting() {
        try {
            SharedData sharedData = SharedData.getInstance();
            if (sharedData != null) {
                settingQuickAccess = sharedData.getInt(Constant.KEY_SETTING_LAYOUT_QUICK_ACCESS, SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code());
                isHapticFeedback = sharedData.getBoolean(Constant.KEY_SETTING_BEHAVIOR_HAPTIC_FEEDBACK, true);
                quickAccessOrderChange = sharedData.getBoolean(Constant.KEY_QUICK_ACCESS_ORDER_CHANGE, false);
            }
        } catch (Exception e) {
            settingQuickAccess = SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code();
            isHapticFeedback = true;
            quickAccessOrderChange = false;
        }
        if (layoutQuickAccessMap != null && !layoutQuickAccessMap.isEmpty()) {
            for (Map.Entry<Integer, ViewGroup> entry : layoutQuickAccessMap.entrySet()) {
                ViewGroup vg = entry.getValue();
                if (vg != null) {
                    Integer key = entry.getKey();
                    vg.setVisibility(key == settingQuickAccess ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void getQuickAccessData() {
        if (mQuickAccessAppsAdapter == null) return;
        if (mQuickAccessAppsAdapter.getItemCount() > 0 && !quickAccessOrderChange) {
            return;
        }
        try {
            SharedData sharedData = SharedData.getInstance();
            if (sharedData != null) {
                sharedData.put(Constant.KEY_QUICK_ACCESS_ORDER_CHANGE, false).commit();
            }
        } catch (Exception ignored) {}
        ThreadPoolService.newTask(() -> {
            List<AppItem> apps = DBManager.getInstance().getAppManager().list();
            apps.sort(Comparator.comparingInt(AppItem::getPriority));
            Message message = new Message();
            message.what = WHAT_LIST_QUICK_ACCESS;
            message.obj = apps;
            handler.sendMessage(message);
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_choose_connect) {
            Intent intent = new Intent(this, ConnectInstanceActivity.class);
            startActivity(intent);
        } else if (id == R.id.btn_main_scan) {
            showScanDialog();
        } else if (id == R.id.btn_connect) {
            toggleConnection();
        } else if (id == R.id.btn_setting) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.btn_switch_panel) {
            if (viewPagerPanel != null) {
                int currentItem = viewPagerPanel.getCurrentItem();
                viewPagerPanel.setCurrentItem(currentItem == 0 ? 1 : 0);
            }
        } else if (id == R.id.btn_text_input) {
            showBottomSheetDialog();
        } else {
            handleTvKeyEvent(id, view);
        }
    }

    public void handleQuickAccessRvItemClick(int position, AppItem app) {
        if (TextUtils.isEmpty(app.getUrl())) return;
        ADBConnectUtil.startApp(app.getUrl(), callable);
    }

    @SuppressWarnings("all")
    private boolean handleLongpressBtnKeyEvent(View view, MotionEvent event) {
        if (event == null) return false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
        }
        return false;
    }

    private boolean handleLongClick(View view) {
        scheduledFuture = executorService.scheduleWithFixedDelay(() -> {
            hapticFeedback(view);
            if (view.getId() == R.id.btn_turn_up_volume) {
                ADBConnectUtil.turnUpVolume(callable);
            } else if (view.getId() == R.id.btn_turn_down_volume) {
                ADBConnectUtil.turnDownVolume(callable);
            } else if (view.getId() == R.id.btn_input_backspace) {
                ADBConnectUtil.pressDel(callable);
            }
        }, 200L, 200L, TimeUnit.MILLISECONDS);
        if (view.getId() == R.id.btn_turn_up_volume) {
            ADBConnectUtil.turnUpVolume(callable);
        } else if (view.getId() == R.id.btn_turn_down_volume) {
            ADBConnectUtil.turnDownVolume(callable);
        } else if (view.getId() == R.id.btn_input_backspace) {
            ADBConnectUtil.pressDel(callable);
        }
        return true;
    }

    private void handleTvKeyEvent(int id, View view) {
        hapticFeedback(view);
        if (id == R.id.btn_tv_home) {
            ADBConnectUtil.pressHome(callable);
        } else if (id == R.id.btn_tv_back) {
            ADBConnectUtil.pressBack(callable);
        } else if (id == R.id.btn_turn_up_volume) {
            ADBConnectUtil.turnUpVolume(callable);
        } else if (id == R.id.btn_turn_down_volume) {
            ADBConnectUtil.turnDownVolume(callable);
        } else if (id == R.id.btn_tv_shutdown) {
            ADBConnectUtil.pressPower(callable);
        } else if (id == R.id.btn_tv_media_play_pause) {
            ADBConnectUtil.pressMediaPlayPause(callable);
        } else if (id == R.id.btn_tv_media_prev) {
            ADBConnectUtil.pressMediaPrev(callable);
        } else if (id == R.id.btn_tv_media_next) {
            ADBConnectUtil.pressMediaNext(callable);
        } else if (id == R.id.btn_tv_media_fast_forward) {
            ADBConnectUtil.pressMediaFastForward(callable);
        } else if (id == R.id.btn_tv_media_rewind) {
            ADBConnectUtil.pressMediaRewind(callable);
        } else if (id == R.id.btn_tv_menu) {
            ADBConnectUtil.pressMenu(callable);
        } else if (id == R.id.btn_mute) {
            ADBConnectUtil.pressMute(callable);
        } else if (id == R.id.btn_up) {
            ADBConnectUtil.pressUp(callable);
        } else if (id == R.id.btn_down) {
            ADBConnectUtil.pressDown(callable);
        } else if (id == R.id.btn_left) {
            ADBConnectUtil.pressLeft(callable);
        } else if (id == R.id.btn_right) {
            ADBConnectUtil.pressRight(callable);
        } else if (id == R.id.btn_ok) {
            ADBConnectUtil.pressOk(callable, false);
        }

    }

    private void hapticFeedback(View view) {
        if (isHapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    private void showBottomSheetDialog() {
        if (textInputDialog == null) {
            textInputDialog = new InputKeyboardDialog(this);
            textInputDialog.setOnConfirmClickListener(new InputKeyboardDialog.OnClickListener() {
                @Override
                public void onConfirmClick(View view, String text, int type) {
                    hapticFeedback(view);
                    if (type == InputLanguageEnums.CHINESE.code()) {
                        ADBConnectUtil.inputTextWithADBKeyboard(text, callable);
                    } else {
                        ADBConnectUtil.inputText(text, callable);
                    }
                }

                @Override
                public void onErrorClick() {
                    showChineseInputQuestion();
                }

                @Override
                public void onLanguageToggleClick(int type) {
                    if (type == InputLanguageEnums.CHINESE.code()) {
                        ADBConnectUtil.getDefaultKeyboard((result, msg) -> {
                            if (!result) {
                                ToastUtil.showToastThread(MainActivity.this.getString(R.string.text_connection_failed));
                            } else {
                                String currentKeyboard = msg.replace("\n", "");
                                if (Constant.VALE_ADB_KEYBOARD_URL.equals(currentKeyboard)) {
                                    ToastUtil.showToastThread("已是ADBKeyboard");
                                    return;
                                }
                                ;
                                originKeyboard = currentKeyboard;
                                ADBConnectUtil.switch2ADBKeyboard(switchKeyboardCallback);
                            }
                        });

                    } else {
                        if (TextUtils.isEmpty(originKeyboard)) return;
                        ADBConnectUtil.switchKeyboard(originKeyboard, switchKeyboardCallback);
                    }
                    try {
                        SharedData sharedData = SharedData.getInstance();
                        if (sharedData != null) {
                            sharedData.put(Constant.KEY_INPUT_LANGUAGE, type).commit();
                        }
                    } catch (Exception ignored) {}
                }

                @Override
                public void onEnterClick(View view) {
                    hapticFeedback(view);
                    ADBConnectUtil.pressEnter(callable);
                }

                @Override
                public void onBackspaceClick(View view) {
                    hapticFeedback(view);
                    ADBConnectUtil.pressDel(callable);
                }

                @Override
                public boolean onBackspaceLongClick(View view) {
                    return handleLongClick(view);
                }

                @Override
                public boolean onBackspaceTouch(View view, MotionEvent event) {
                    return handleLongpressBtnKeyEvent(view, event);
                }
            });
        }
        textInputDialog.show();
    }

    private void showChineseInputQuestion() {
        chineseInputQuestionDialog = new QuestionDialog(this,
                this.getString(R.string.text_input_chinese),
                this.getString(R.string.text_input_chinese_tip));
        chineseInputQuestionDialog.setPositiveBtnText(this.getString(R.string.text_download));
        chineseInputQuestionDialog.setOnButtonClickListener(new QuestionDialog.OnButtonClickListener() {
            @Override
            public void onPositiveClick(View view) {
                ADBConnectUtil.openWebUrl(Constant.URL_ADB_KEYBOARD_DOWNLOAD, callable);
                chineseInputQuestionDialog.dismiss();
            }

            @Override
            public void onNegativeClick(View view) {
                chineseInputQuestionDialog.dismiss();
            }
        });
        chineseInputQuestionDialog.show();
    }

    private void showScanDialog() {
        com.swx.adbremote.components.DeviceScanDialog scanDialog = new com.swx.adbremote.components.DeviceScanDialog(this);
        scanDialog.setOnDeviceConnectedListener(success -> {
            if (success) {
                updateConnectionStatus();
            }
        });
        scanDialog.show();
    }

    private void toggleConnection() {
        ConnectInstance bean = ADBConnectUtil.getConnectedBean();
        if (bean != null) {
            disconnect();
        } else {
            connectToDevice();
        }
    }

    private void connectToDevice() {
        String ip = etIpAddress.getText().toString().trim();
        if (TextUtils.isEmpty(ip)) {
            ToastUtil.show("请输入IP地址");
            return;
        }
        
        ADBConnectUtil.connect(ip, 5555, new ADBConnectUtil.OnConnectListener() {
            @Override
            public void onConnectSuccess() {
                ToastUtil.show("连接成功");
                updateConnectionStatus();
            }

            @Override
            public void onConnectFailed(String error) {
                ToastUtil.show("连接失败: " + error);
            }
        });
    }

    private void disconnect() {
        ADBConnectUtil.disconnect();
        updateConnectionStatus();
        ToastUtil.show("已断开连接");
    }

    private void updateConnectionStatus() {
        ConnectInstance bean = ADBConnectUtil.getConnectedBean();
        if (bean != null) {
            if (etIpAddress != null) etIpAddress.setText(bean.getIp());
            if (etIpAddress != null) etIpAddress.setEnabled(false);
            if (btnMainScan != null) btnMainScan.setEnabled(false);
            if (btnConnect != null) {
                btnConnect.setText("断开");
                btnConnect.setBackgroundResource(R.drawable.background_btn_circle_pm);
            }
            if (llChooseTvConnect != null) {
                llChooseTvConnect.setVisibility(View.VISIBLE);
            }
            if (tvConnectAlias != null) {
                tvConnectAlias.setText(bean.getAlias());
            }
            if (tvConnectIp != null) {
                tvConnectIp.setText(bean.getIp() + ":" + bean.getPort());
            }
        } else {
            if (etIpAddress != null) etIpAddress.setEnabled(true);
            if (btnMainScan != null) btnMainScan.setEnabled(true);
            if (btnConnect != null) {
                btnConnect.setText("连接");
                btnConnect.setBackgroundResource(R.drawable.background_btn_circle_ps);
            }
            if (llChooseTvConnect != null) {
                llChooseTvConnect.setVisibility(View.GONE);
            }
        }
    }
}
