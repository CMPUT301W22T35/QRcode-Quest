package com.qrcode_quest.ui.leaderboard;

/**
 * A data model class for displaying a PlayerAccount in the list
 */
public class PlayerViewItem {
    public final String username;
    public final int score;

    public PlayerViewItem(String username, int score) {
        this.username = username;
        this.score = score;
    }
}
