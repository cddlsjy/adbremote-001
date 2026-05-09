package com.swx.adbremote.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swx.adbremote.R;
import com.swx.adbremote.entity.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class DeviceScanAdapter extends RecyclerView.Adapter<DeviceScanAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    private List<DeviceInfo> mData;
    private OnDeviceConnectListener mListener;

    public interface OnDeviceConnectListener {
        void onDeviceSelected(DeviceInfo device);
    }

    public DeviceScanAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_device_scan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceInfo device = mData.get(position);
        holder.tvDeviceIp.setText(device.getIpAddress());
        holder.tvDeviceStatus.setText(String.format("响应时间：%dms", device.getResponseTime()));
        
        holder.btnConnect.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDeviceSelected(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<DeviceInfo> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addDevice(DeviceInfo device) {
        this.mData.add(device);
        int position = mData.size() - 1;
        notifyItemInserted(position);
    }

    public DeviceInfo getDevice(int position) {
        if (position < 0 || position >= mData.size()) {
            return null;
        }
        return mData.get(position);
    }

    public List<DeviceInfo> getAllDevices() {
        return new ArrayList<>(mData);
    }

    public void setOnDeviceConnectListener(OnDeviceConnectListener listener) {
        this.mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDeviceIcon;
        TextView tvDeviceIp;
        TextView tvDeviceStatus;
        Button btnConnect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDeviceIcon = itemView.findViewById(R.id.iv_device_icon);
            tvDeviceIp = itemView.findViewById(R.id.tv_device_ip);
            tvDeviceStatus = itemView.findViewById(R.id.tv_device_status);
            btnConnect = itemView.findViewById(R.id.btn_connect);
        }
    }
}
