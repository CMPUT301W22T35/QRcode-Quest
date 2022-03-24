package com.qrcode_quest.ui.leaderboard;

class PlayerStats {
    public final String username;
    public int highestCode;
    public int totalScore;
    public int totalCodes;

    public PlayerStats(String user) {
        this.username = user;
        this.highestCode = 0;
        this.totalCodes = 0;
        this.totalScore = 0;
    }
}
