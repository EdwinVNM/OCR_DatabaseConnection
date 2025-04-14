package com.example.ocr_databaseconnection;

public class IpAddressSingleton {
    private static IpAddressSingleton instance;
    private String ipAddress;

    private IpAddressSingleton() {
        // Check if the app is running on an emulator or physical device
        if (isEmulator()) {
            // Default IP address for Android Emulator
            this.ipAddress = "http://10.0.2.2:8080";
        } else {
            // Set the IP to your laptop's IP address for physical devices
            this.ipAddress = "http://172.20.10.2:8080"; // Replace with your laptop's IP
        }//<YOUR_IPV4_ADDRESS>
    }

    public static synchronized IpAddressSingleton getInstance() {
        if (instance == null) {
            instance = new IpAddressSingleton();
        }
        return instance;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    // Helper method to detect if it's an emulator
    private boolean isEmulator() {
        return android.os.Build.FINGERPRINT.contains("generic");
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
