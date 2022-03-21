package com.qrcode_quest.ui.leaderboard;

import com.qrcode_quest.entities.PlayerAccount;

/**
 * contains information for an item in the player leaderboard
 * @author tianming
 * @version 1.0
 */
public class PlayerScore {
    public PlayerScore(PlayerAccount account, int score) {
        this.m_account = account;
        this.m_score = score;
    }
    public PlayerAccount m_account;
    public int m_score;
}