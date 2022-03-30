package com.qrcode_quest.ui.leaderboard;

/**
 * A data structure for calculating/storing player stats
 */
class PlayerStats {
    public final String username;
    public int highestCode;
    public int totalScore;
    public int totalCodes;

    /**
     * Creates an empty PlayerStats object with the user's name
     * @param user Username of the PlayerAccount
     */
    public PlayerStats(String user) {
        this.username = user;
        this.highestCode = 0;
        this.totalCodes = 0;
        this.totalScore = 0;
    }
}
