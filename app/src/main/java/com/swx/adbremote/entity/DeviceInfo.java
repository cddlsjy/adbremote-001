package com.swx.adbremote.entity;

import java.io.Serializable;

public class DeviceInfo implements Serializable {
    private String ipAddress;
    private int port;
    private String deviceName;
    private long responseTime;
    private String macAddress;

    public DeviceInfo() {
        this.port = 5555;
    }

    public DeviceInfo(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public DeviceInfo(String ipAddress, int port, long responseTime) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.responseTime = responseTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DeviceInfo that = (DeviceInfo) obj;
        return port == that.port && 
               ipAddress != null && ipAddress.equals(that.ipAddress);
    }

    @Override
    public int hashCode() {
        int result = ipAddress != null ? ipAddress.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", responseTime=" + responseTime +
                '}';
    }
}
