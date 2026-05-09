package com.swx.adbremote.components;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.swx.adbremote.R;
import com.swx.adbremote.adapter.DeviceScanAdapter;
import com.swx.adbremote.entity.DeviceInfo;
import com.swx.adbremote.service.IpScanService;
import com.swx.adbremote.utils.ADBConnectUtil;
import com.swx.adbremote.utils.ToastUtil;

import java.util.List;

public class DeviceScanDialog extends Dialog {
    private RecyclerView rvDevices;
    private TextView tvDialogTitle;
    private TextView tvDeviceCount;
    private TextView tvScanProgress;
    private Button btnCancel;
    private LinearLayout layoutScanning;
    private LinearLayout layoutEmpty;
    
    private DeviceScanAdapter mAdapter;
    private IpScanService ipScanService;
    private OnDeviceSelectedListener mSelectedListener;
    private OnDeviceConnectedListener mConnectedListener;
    private boolean isScanning = false;
    private boolean connectDirectly = false;

    public interface OnDeviceSelectedListener {
        void onDeviceSelected(DeviceInfo device);
    }

    public interface OnDeviceConnectedListener {
        void onDeviceConnected(boolean success);
    }

    public DeviceScanDialog(@NonNull Context context) {
        super(context, R.style.inputDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_scan);
        
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int widthPixels = getContext().getResources().getDisplayMetrics().widthPixels;
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = widthPixels - 60;
            getWindow().setAttributes(params);
        }
        
        initView();
        initAdapter();
        initEvent();
        checkPermissionAndStartScan();
    }

    private void initView() {
        rvDevices = findViewById(R.id.rv_devices);
        tvDialogTitle = findViewById(R.id.tv_dialog_title);
        tvDeviceCount = findViewById(R.id.tv_device_count);
        tvScanProgress = findViewById(R.id.tv_scan_progress);
        btnCancel = findViewById(R.id.btn_cancel);
        layoutScanning = findViewById(R.id.layout_scanning);
        layoutEmpty = findViewById(R.id.layout_empty);
    }

    private void initAdapter() {
        mAdapter = new DeviceScanAdapter(getContext());
        rvDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDevices.setAdapter(mAdapter);
        
        mAdapter.setOnDeviceConnectListener(this::handleDeviceClick);
    }

    private void handleDeviceClick(DeviceInfo device) {
        if (isScanning && ipScanService != null) {
            ipScanService.stopScan();
        }
        
        dismiss();
        
        if (connectDirectly && mConnectedListener != null) {
            ADBConnectUtil.connect(device.getIpAddress(), device.getPort(), new ADBConnectUtil.OnConnectListener() {
                @Override
                public void onConnectSuccess() {
                    ToastUtil.show("连接成功");
                    if (mConnectedListener != null) {
                        mConnectedListener.onDeviceConnected(true);
                    }
                }

                @Override
                public void onConnectFailed(String error) {
                    ToastUtil.show("连接失败: " + error);
                    if (mConnectedListener != null) {
                        mConnectedListener.onDeviceConnected(false);
                    }
                }
            });
        } else if (mSelectedListener != null) {
            mSelectedListener.onDeviceSelected(device);
        }
    }

    private void initEvent() {
        btnCancel.setOnClickListener(v -> {
            if (isScanning && ipScanService != null) {
                ipScanService.stopScan();
            }
            dismiss();
        });
    }

    private void checkPermissionAndStartScan() {
        startScan();
    }

    private void startScan() {
        if (ipScanService == null) {
            ipScanService = new IpScanService();
        }

        isScanning = true;
        layoutScanning.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        tvDeviceCount.setVisibility(View.GONE);

        ipScanService.startScan(getContext(), new IpScanService.IpScanCallback() {
            @Override
            public void onScanStart() {
                tvScanProgress.setText("正在扫描...");
            }

            @Override
            public void onDeviceFound(DeviceInfo device) {
                mAdapter.addDevice(device);
                updateDeviceCount(mAdapter.getItemCount());
                checkEmptyState();
            }

            @Override
            public void onScanProgress(int current, int total) {
                tvScanProgress.setText(String.format("正在扫描：%d/%d", current, total));
            }

            @Override
            public void onScanComplete(List<DeviceInfo> devices) {
                isScanning = false;
                layoutScanning.setVisibility(View.GONE);
                updateDeviceCount(devices.size());
                checkEmptyState();
            }

            @Override
            public void onScanError(String error) {
                isScanning = false;
                layoutScanning.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScanCancelled() {
                isScanning = false;
                layoutScanning.setVisibility(View.GONE);
            }
        });
    }

    private void updateDeviceCount(int count) {
        if (count > 0) {
            tvDeviceCount.setText(String.format("发现 %d 台设备", count));
            tvDeviceCount.setVisibility(View.VISIBLE);
        }
    }

    private void checkEmptyState() {
        if (!isScanning && mAdapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismiss() {
        if (isScanning && ipScanService != null) {
            ipScanService.stopScan();
        }
        super.dismiss();
    }

    public void setOnDeviceSelectedListener(OnDeviceSelectedListener listener) {
        this.mSelectedListener = listener;
        this.connectDirectly = false;
    }

    public void setOnDeviceConnectedListener(OnDeviceConnectedListener listener) {
        this.mConnectedListener = listener;
        this.connectDirectly = true;
    }
}