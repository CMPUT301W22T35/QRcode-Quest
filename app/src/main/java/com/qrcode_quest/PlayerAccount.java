package com.qrcode_quest;

public class PlayerAccount {
    private String playerName;
    private String contactInfo;
    private boolean isOwner;

    public PlayerAccount (String name, String info, boolean isOwner) {
        this.playerName = name;
        this.contactInfo = info;
        this.isOwner = isOwner;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}
