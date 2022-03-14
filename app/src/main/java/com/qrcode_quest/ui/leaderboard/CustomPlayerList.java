package com.qrcode_quest.ui.leaderboard;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.qrcode_quest.R;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author ageolleg, tianming
 * @version 0.2
 */
public class CustomPlayerList extends ArrayAdapter<CustomPlayerList.PlayerScore> {

    /**
     * contains information for an item in the player leaderboard
     */
    public static class PlayerScore {
        public PlayerScore(PlayerAccount account, int score) {
            this.account = account;
            this.score = score;
        }

        public PlayerAccount getAccount() {
            return account;
        }

        public void setAccount(PlayerAccount account) {
            this.account = account;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        PlayerAccount account;
        int score;
    }

    private ArrayList<PlayerScore> players;
    private Context context;

    public CustomPlayerList(Context context, ArrayList<PlayerScore> players) {
        super(context, 0, players);
        this.players = players;
        this.context = context;
    }

    static public CustomPlayerList getInstanceWithPlaceholderData(Context context) {
        ArrayList<PlayerScore> mockPlayers = new ArrayList<>();
        int curScore = 1;
        for(int i = 0; i < 10; i++) {
            String indexStr = Integer.toString(i);
            PlayerAccount account = new PlayerAccount(
                    "player" + i, "email" + i, "phone" + i,
                    false, true);
            PlayerScore player = new PlayerScore(account, curScore);
            curScore = (curScore * 7) % 10;
            mockPlayers.add(player);
        }
        return new CustomPlayerList(context, mockPlayers);
    }

    public void setPlayers(ArrayList<PlayerScore> players) {
        // TODO: implement sorting by cloning the players and then call .sort with a defined comparator
        this.players.clear();
        this.players.addAll(players);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.player_item_view, parent,false);
        }

        PlayerScore ps = players.get(position);

        TextView playerName = view.findViewById(R.id.player_text);
        TextView score_text = view.findViewById(R.id.score_text);

        PlayerAccount player = ps.getAccount();
        playerName.setText(player.getUsername());
        score_text.setText(Integer.toString(ps.getScore()));

        return view;
    }
}
