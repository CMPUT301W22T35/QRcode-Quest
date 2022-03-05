package com.qrcode_quest;

public class PlayerManager {
    public interface PlayerResult {
        void handlePlayerResult(PlayerAccount player);
    }

    public static void getPlayer(String id, PlayerResult callback){
        PlayerAccount account = new PlayerAccount(id, "some contact info", true);

        // fetch player from db
        callback.handlePlayerResult(account);
    }
}