package com.phonehalo.itemtracker.db;

public class ExternalAlertSettings {
    private String emailAddress;
    private String emailCCAddress;
    private boolean emailAlertOn;
    private boolean twitterAlertOn;
    private boolean facebookAlertOn;
    private String oauthTokenTwitter;
    private String oauthSecretTwitter;

    private boolean emailAddressDirty;
    private boolean emailCCAddressDirty;
    private boolean emailAlertOnDirty;
    private boolean twitterAlertOnDirty;
    private boolean facebookAlertOnDirty;
    private boolean oauthTokenTwitterDirty;
    private boolean oauthSecretTwitterDirty;

    public ExternalAlertSettings() {
        emailAddress = "";
        emailCCAddress = "";
        emailAlertOn = false;
        twitterAlertOn = false;
        facebookAlertOn = false;
        oauthTokenTwitter = "";
        oauthSecretTwitter = "";
    }

    public void clearDirtyFlags() {
        emailAddressDirty = false;
        emailCCAddressDirty = false;
        emailAlertOnDirty = false;
        twitterAlertOnDirty = false;
        facebookAlertOnDirty = false;
        oauthTokenTwitterDirty = false;
        oauthSecretTwitterDirty = false;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        if (this.emailAddress == null || !this.emailAddress.equals(emailAddress)) {
            emailAddressDirty = true;
            this.emailAddress = emailAddress;
        }
    }

    public String getEmailCCAddress() {
        return emailCCAddress;
    }

    public void setEmailCCAddress(String emailCCAddress) {
        if (this.emailCCAddress == null || !this.emailCCAddress.equals(emailCCAddress)) {
            emailCCAddressDirty = true;
            this.emailCCAddress = emailCCAddress;
        }
    }

    public boolean isEmailAlertOn() {
        return emailAlertOn;
    }

    public void setEmailAlertOn(boolean emailAlertOn) {
        if (this.emailAlertOn != emailAlertOn) {
            emailAlertOnDirty = true;
            this.emailAlertOn = emailAlertOn;
        }
    }

    public boolean isTwitterAlertOn() {
        return twitterAlertOn;
    }

    public void setTwitterAlertOn(boolean twitterAlertOn) {
        if (this.twitterAlertOn != twitterAlertOn) {
            twitterAlertOnDirty = true;
            this.twitterAlertOn = twitterAlertOn;
        }
    }

    public boolean isFacebookAlertOn() {
        return facebookAlertOn;
    }

    public void setFacebookAlertOn(boolean facebookAlertOn) {
        if (this.facebookAlertOn != facebookAlertOn) {
            facebookAlertOnDirty = true;
            this.facebookAlertOn = facebookAlertOn;
        }
    }

    public void setTwitterTokenAndSecret(String oauthToken, String oauthSecret) {
        if (this.oauthTokenTwitter == null || this.oauthSecretTwitter == null ||
                !this.oauthTokenTwitter.equals(oauthToken) || !this.oauthSecretTwitter.equals(oauthSecret)) {
            this.oauthTokenTwitterDirty = true;
            this.oauthSecretTwitterDirty = true;
            this.oauthTokenTwitter = oauthToken;
            this.oauthSecretTwitter = oauthSecret;
        }
    }

    public String[] getTwitterTokenAndSecret() {
        String[] tokenAndSecret = {oauthTokenTwitter, oauthSecretTwitter};
        return tokenAndSecret;
    }

    public boolean isEmailAddressDirty() {
        return emailAddressDirty;
    }

    public boolean isEmailCCAddressDirty() {
        return emailCCAddressDirty;
    }

    public boolean isEmailAlertOnDirty() {
        return emailAlertOnDirty;
    }

    public boolean isTwitterAlertOnDirty() {
        return twitterAlertOnDirty;
    }

    public boolean isFacebookAlertOnDirty() {
        return facebookAlertOnDirty;
    }

    public boolean isOauthTokenTwitterDirty() {
        return oauthTokenTwitterDirty;
    }

    public boolean isOauthSecretTwitterDirty() {
        return oauthSecretTwitterDirty;
    }

    @Override
    public String toString() {
        return "ExternalAlertSettings{" +
                "emailAddress='" + emailAddress + '\'' +
                ", emailCCAddress='" + emailCCAddress + '\'' +
                ", emailAlertOn=" + emailAlertOn +
                ", twitterAlertOn=" + twitterAlertOn +
                ", facebookAlertOn=" + facebookAlertOn +
                ", oauthTokenTwitter='" + oauthTokenTwitter + '\'' +
                ", oauthSecretTwitter='" + oauthSecretTwitter + '\'' +
                ", emailAddressDirty=" + emailAddressDirty +
                ", emailCCAddressDirty=" + emailCCAddressDirty +
                ", emailAlertOnDirty=" + emailAlertOnDirty +
                ", twitterAlertOnDirty=" + twitterAlertOnDirty +
                ", facebookAlertOnDirty=" + facebookAlertOnDirty +
                ", oauthTokenTwitterDirty=" + oauthTokenTwitterDirty +
                ", oauthSecretTwitterDirty=" + oauthSecretTwitterDirty +
                '}';
    }
}
