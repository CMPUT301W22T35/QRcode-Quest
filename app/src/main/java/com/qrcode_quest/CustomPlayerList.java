package com.qrcode_quest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qrcode_quest.entities.PlayerAccount;

import java.util.ArrayList;

public class CustomPlayerList extends ArrayAdapter<PlayerAccount> {
    private ArrayList<PlayerAccount> players;
    // private PlayerManager playerManager;
    private Boolean isLeaderboardList;
    private Context context;

    public CustomPlayerList(Context context, ArrayList<PlayerAccount> players) {
        super(context, 0, players);
        this.players = players;
        this.context = context;
        this.isLeaderboardList = isLeaderBoardList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        View view = convertView;if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.player_list_content, parent,false);
        }

        PlayerAccount player = players.get(position);

        TextView playerName = view.findViewById(R.id.player_text);
        TextView score_text = view.findViewById(R.id.score_text);

        playerName.setText(player.getUsername());

        // Set score for leaderboards...
        /*playerManager = new PlayerManager();
        if (isLeaderBoardList) {
            score_text.setText(playerManager.getScore(player));  ?
        }
        else {
            score_text.setVisibility(View.GONE);
        }*/


        return view;
    }
}
