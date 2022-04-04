package com.qrcode_quest.ui.leaderboard;

/**
 * An immutable data model class for displaying a PlayerAccount in a list
 */
public class PlayerViewItem {
    public final String username;
    public final int score;

    /**
     * creates an item in the player view
     * @param username player name
     * @param score score (may be different under different context)
     */
    public PlayerViewItem(String username, int score) {
        this.username = username;
        this.score = score;
    }
}
