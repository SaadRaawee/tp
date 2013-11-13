package com.phonehalo.itemtracker.db;

import android.net.Uri;

public class PeripheralSettings {
    private String peripheralAddress;
    private String peripheralName;
    private Uri peripheralImageUri;
    private String peripheralImageType;
    private int peripheralAlertDuration;
    private int phoneAlertDurationSeconds;
    private boolean phoneAudibleAlertOn;
    private boolean phoneAudibleAlertMuted;
    private int phoneAudibleAlertVolume;
    private boolean phoneVibrateAlertOn;
    private Uri phoneAudibleAlertUri;
    private boolean deviceConnectionState;

    private boolean peripheralNameDirty;
    private boolean peripheralImageUriDirty;
    private boolean peripheralAlertDurationDirty;
    private boolean phoneAlertDurationSecondsDirty;
    private boolean phoneAudibleAlertOnDirty;
    private boolean phoneAudibleAlertMutedDirty;
    private boolean phoneAudibleAlertVolumeDirty;
    private boolean phoneVibrateAlertOnDirty;
    private boolean phoneAudibleAlertUriDirty;
    private boolean deviceConnectionStateDirty;

    public void clearDirtyFlags() {
        peripheralNameDirty = false;
        peripheralImageUriDirty = false;
        peripheralAlertDurationDirty = false;
        phoneAlertDurationSecondsDirty = false;
        phoneAudibleAlertOnDirty = false;
        phoneVibrateAlertOnDirty = false;
        phoneAudibleAlertUriDirty = false;
        deviceConnectionStateDirty = false;
    }


    public String getPeripheralAddress() {
        return peripheralAddress;
    }

    public void setPeripheralAddress(String peripheralAddress) {
        this.peripheralAddress = peripheralAddress;
    }

    public String getPeripheralName() {
        return peripheralName;
    }

    public void setPeripheralName(String peripheralName) {
        if (this.peripheralName == null || !this.peripheralName.equals(peripheralName)) {
            peripheralNameDirty = true;
            this.peripheralName = peripheralName;
        }
    }

    public Uri getPeripheralImageUri() {
        return peripheralImageUri;
    }

    public void setPeripheralImageUri(Uri peripheralImageUri) {
        if (this.peripheralImageUri == null || this.peripheralImageUri != peripheralImageUri){
            peripheralImageUriDirty = true;
            this.peripheralImageUri = peripheralImageUri;
        }
    }

    public int getPeripheralAlertDuration() {
        return peripheralAlertDuration;
    }

    public void setPeripheralAlertDuration(int peripheralAlertDuration) {
        if (peripheralAlertDuration != this.peripheralAlertDuration) {
            peripheralAlertDurationDirty = true;
            this.peripheralAlertDuration = peripheralAlertDuration;
        }
    }

    public int getPhoneAlertDurationSeconds() {
        return phoneAlertDurationSeconds;
    }

    public void setPhoneAlertDurationSeconds(int phoneAlertDurationSeconds) {
        if (phoneAlertDurationSeconds != this.phoneAlertDurationSeconds) {
            phoneAlertDurationSecondsDirty = true;
            this.phoneAlertDurationSeconds = phoneAlertDurationSeconds;
        }
    }


    //audio has two settings... on the peripheral settings page, audio can be on or off
    //on the main item page, audio can be muted
    public boolean isPhoneAudibleAlertOn() {
        return phoneAudibleAlertOn;
    }

    public void setPhoneAudibleAlertOn(boolean phoneAudibleAlertOn) {
        if (phoneAudibleAlertOn != this.phoneAudibleAlertOn) {
            phoneAudibleAlertOnDirty = true;
            this.phoneAudibleAlertOn = phoneAudibleAlertOn;
        }
    }

    public boolean isPhoneAudibleAlertMuted() {
        return phoneAudibleAlertMuted;
    }

    public void setPhoneAudibleAlertMuted(boolean phoneAudibleAlertMuted) {
        if (phoneAudibleAlertMuted != this.phoneAudibleAlertMuted) {
            phoneAudibleAlertMutedDirty = true;
            this.phoneAudibleAlertMuted = phoneAudibleAlertMuted;
        }
    }

    public int getPhoneAudibleAlertVolume() {
        return phoneAudibleAlertVolume;
    }

    public void setPhoneAudibleAlertVolume(int phoneAudibleAlertVolume) {
        if (phoneAudibleAlertVolume != this.phoneAudibleAlertVolume) {
            phoneAudibleAlertVolumeDirty = true;
            this.phoneAudibleAlertVolume = phoneAudibleAlertVolume;
        }
    }

    public boolean isPhoneVibrateAlertOn() {
        return phoneVibrateAlertOn;
    }

    public void setPhoneVibrateAlertOn(boolean phoneVibrateAlertOn) {
        if (phoneVibrateAlertOn != this.phoneVibrateAlertOn) {
            phoneVibrateAlertOnDirty = true;
            this.phoneVibrateAlertOn = phoneVibrateAlertOn;
        }
    }
    public boolean isDeviceConnectionState() {
        return deviceConnectionState;
    }

    public void setDeviceConnectionState(boolean deviceConnectionState) {
        deviceConnectionStateDirty = true;
        this.deviceConnectionState = deviceConnectionState;
    }



    public Uri getPhoneAudibleAlertUri() {
        return phoneAudibleAlertUri;
    }

    public void setPhoneAudibleAlertUri(Uri phoneAudibleAlertUri) {
        if (this.phoneAudibleAlertUri == null || !this.phoneAudibleAlertUri.equals(phoneAudibleAlertUri)) {
            phoneAudibleAlertUriDirty = true;
            this.phoneAudibleAlertUri = phoneAudibleAlertUri;
        }
    }

    public boolean isPeripheralNameDirty() {
        return peripheralNameDirty;
    }

    public boolean isPeripheralImageUriDirty() {
        return peripheralImageUriDirty;
    }

    public boolean isPeripheralAlertDurationDirty() {
        return peripheralAlertDurationDirty;
    }

    public boolean isPhoneAlertDurationSecondsDirty() {
        return phoneAlertDurationSecondsDirty;
    }

    public boolean isPhoneAudibleAlertOnDirty() {
        return phoneAudibleAlertOnDirty;
    }

    public boolean isPhoneAudibleAlertMutedDirty() {
        return phoneAudibleAlertMutedDirty;
    }

    public boolean isPhoneAudibleAlertVolumeDirty() {
        return phoneAudibleAlertVolumeDirty;
    }

    public boolean isPhoneVibrateAlertOnDirty() {
        return phoneVibrateAlertOnDirty;
    }

    public boolean isPhoneAudibleAlertUriDirty() {
        return phoneAudibleAlertUriDirty;
    }

    public boolean isDeviceConnectionStateDirty(){
        return deviceConnectionStateDirty;
    }

    @Override
    public String toString() {
        return "PeripheralSettings{" +
                "peripheralAddress='" + peripheralAddress + '\'' +
                ", peripheralName='" + peripheralName + '\'' +
                ", peripheralImageUri=" + peripheralImageUri +
                ", peripheralAlertDuration=" + peripheralAlertDuration +
                ", phoneAlertDurationSeconds=" + phoneAlertDurationSeconds +
                ", phoneAudibleAlertOn=" + phoneAudibleAlertOn +
                ", phoneAudibleAlertMuted=" + phoneAudibleAlertMuted +
                ", phoneVibrateAlertOn=" + phoneVibrateAlertOn +
                ", phoneAudibleAlertUri=" + phoneAudibleAlertUri +
                ", deviceConnectionState=" + deviceConnectionState +
                ", peripheralNameDirty=" + peripheralNameDirty +
                ", peripheralImageUriDirty=" + peripheralImageUriDirty +
                ", peripheralAlertDurationDirty=" + peripheralAlertDurationDirty +
                ", phoneAlertDurationSecondsDirty=" + phoneAlertDurationSecondsDirty +
                ", phoneAudibleAlertOnDirty=" + phoneAudibleAlertOnDirty +
                ", phoneVibrateAlertOnDirty=" + phoneVibrateAlertOnDirty +
                ", phoneAudibleAlertUriDirty=" + phoneAudibleAlertUriDirty +
                ", phoneAudibleAlertMutedDirty=" + phoneAudibleAlertMutedDirty +
                ", deviceConnectionStateDirty=" + deviceConnectionStateDirty +
                '}';
    }
}
