package com.swx.adbremote.utils;

import android.content.Context;
import android.util.Log;

import com.swx.adbremote.entity.DeviceInfo;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkScanner {
    private static final String TAG = "NetworkScanner";
    private static final int DEFAULT_PORT = 5555;
    private static final int THREAD_POOL_SIZE = 15;
    private static final int CONNECT_TIMEOUT_MS = 1000;
    private static final int SCAN_TIMEOUT_MS = 30000;
    private static final int MAX_DEVICES = 20;

    public interface ScanProgressCallback {
        void onProgress(int current, int total);
        void onDeviceFound(DeviceInfo device);
    }

    public List<DeviceInfo> scanNetwork(Context context) throws Exception {
        return scanNetwork(context, DEFAULT_PORT, null);
    }

    public List<DeviceInfo> scanNetwork(Context context, int port, ScanProgressCallback callback) throws Exception {
        String subnet = getLocalSubnet(context);
        Log.d(TAG, "Scanning subnet: " + subnet);

        List<DeviceInfo> devices = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CompletionService<DeviceInfo> completionService = new ExecutorCompletionService<>(executor);
        CountDownLatch latch = new CountDownLatch(254);
        AtomicInteger foundCount = new AtomicInteger(0);
        AtomicInteger scannedCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        try {
            for (int i = 1; i <= 254; i++) {
                final String ip = subnet + "." + i;
                final int finalPort = port;

                completionService.submit(() -> {
                    try {
                        if (isPortOpen(ip, finalPort, CONNECT_TIMEOUT_MS)) {
                            long responseTime = System.currentTimeMillis() - startTime;
                            DeviceInfo device = new DeviceInfo(ip, finalPort, responseTime);
                            Log.d(TAG, "Device found: " + ip + ":" + finalPort);

                            if (foundCount.incrementAndGet() <= MAX_DEVICES) {
                                if (callback != null) {
                                    callback.onDeviceFound(device);
                                }
                                return device;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error scanning " + ip, e);
                    } finally {
                        latch.countDown();
                        int current = scannedCount.incrementAndGet();
                        if (callback != null) {
                            callback.onProgress(current, 254);
                        }
                    }
                    return null;
                });
            }

            for (int i = 0; i < 254 && (System.currentTimeMillis() - startTime) < SCAN_TIMEOUT_MS; i++) {
                try {
                    Future<DeviceInfo> future = completionService.poll(500, TimeUnit.MILLISECONDS);
                    if (future != null) {
                        DeviceInfo device = future.get();
                        if (device != null && devices.size() < MAX_DEVICES) {
                            devices.add(device);
                        }
                    }

                    if (latch.getCount() == 0) {
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting scan result", e);
                }
            }

        } finally {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        Log.d(TAG, "Scan completed. Found " + devices.size() + " devices in " + 
              (System.currentTimeMillis() - startTime) + "ms");
        return devices;
    }

    private String getLocalSubnet(Context context) throws Exception {
        String ipAddress = getLocalIpAddress(context);
        if (ipAddress == null) {
            throw new Exception("无法获取本地IP地址");
        }
        
        Log.d(TAG, "Local IP Address: " + ipAddress);
        
        String subnet = calculateSubnet(ipAddress);
        Log.d(TAG, "Calculated subnet: " + subnet);
        
        return subnet;
    }

    private String getLocalIpAddress(Context context) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iface : interfaces) {
                if (iface.isLoopback()) continue;
                if (iface.isVirtual()) continue;
                
                List<InetAddress> addresses = Collections.list(iface.getInetAddresses());
                for (InetAddress addr : addresses) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting IP address", e);
        }
        
        return null;
    }

    private String calculateSubnet(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + "." + parts[2];
        }
        return "192.168.1";
    }

    private boolean isPortOpen(String ip, int port, int timeout) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close socket for " + ip + ":" + port, e);
                }
            }
        }
    }
}
