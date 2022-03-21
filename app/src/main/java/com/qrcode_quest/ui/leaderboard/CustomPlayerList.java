package com.qrcode_quest.ui.leaderboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.qrcode_quest.R;
import com.qrcode_quest.database.SchemaResultHelper;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author ageolleg, tianming
 * @version 0.2
 */
public class CustomPlayerList extends ArrayAdapter<PlayerScore> {

    private final ArrayList<PlayerScore> playerScores;
    private boolean isDataDummy;

    public CustomPlayerList(Context context, ArrayList<PlayerScore> playerScores) {
        super(context, 0, playerScores);
        this.playerScores = playerScores;
        this.isDataDummy = false;
    }

    static public CustomPlayerList getInstanceWithPlaceholderData(Context context) {
        ArrayList<PlayerScore> mockPlayers = new ArrayList<>();
        int curScore = 100;
        for(int i = 0; i < 10; i++) {
            String indexStr = Integer.toString(i);
            PlayerAccount account = new PlayerAccount(
                    "player" + i, "email" + i, "phone" + i,
                    false, true);
            PlayerScore player = new PlayerScore(account, curScore);
            curScore = curScore - 10;
            mockPlayers.add(player);
        }
        CustomPlayerList list = new CustomPlayerList(context, mockPlayers);
        list.isDataDummy = true;
        return list;
    }

    public boolean isDataPlaceHolder() { return isDataDummy; }

    /**
     * update the leaderboard data
     * @param playerScores a list of player account-score pairs
     */
    public void updatePlayerScores(ArrayList<PlayerScore> playerScores) {
        // TODO: implement sorting by cloning the players and then call .sort with a defined comparator
        this.playerScores.clear();
        this.playerScores.addAll(playerScores);
        notifyDataSetChanged();
        this.isDataDummy = false;
    }

    /**
     * recompute the leaderboard data when the source data changes
     */
    public void onSourceDataUpdate(ArrayList<PlayerAccount> accounts,
                                   ArrayList<QRShot> shots) {
        HashMap<String, ArrayList<QRShot>> shotMap =
                SchemaResultHelper.getOwnerNameToShotArrayMapFromJoin(accounts, shots);
        HashMap<String, QRCode> codeMap = SchemaResultHelper.getQrHashToCodeMapFromShots(shots);

        ArrayList<PlayerScore> playerScores = new ArrayList<>();
        for (PlayerAccount account: accounts) {
            int score = 0;
            for (QRShot shot: Objects.requireNonNull(shotMap.get(account.getUsername()))) {
                if (codeMap.containsKey(shot.getCodeHash())) {
                    score += Objects.requireNonNull(codeMap.get(shot.getCodeHash())).getScore();
                }
            }
            playerScores.add(new PlayerScore(account, score));
        }
        updatePlayerScores(playerScores);
    }

    public void setDataSources(LifecycleOwner owner,
                               LiveData<ArrayList<PlayerAccount>> accountsLiveData,
                               LiveData<ArrayList<QRShot>> shotsLiveData) {
        accountsLiveData.observe(owner, playerAccounts ->
                onSourceDataUpdate(playerAccounts, shotsLiveData.getValue()));
        shotsLiveData.observe(owner, shots ->
                onSourceDataUpdate(accountsLiveData.getValue(), shots));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        if(view == null){
            view = LayoutInflater.from(this.getContext()).inflate(R.layout.player_item_view, parent,false);
        }

        TextView playerName = view.findViewById(R.id.player_text);
        TextView score_text = view.findViewById(R.id.score_text);

        PlayerScore ps = playerScores.get(position);
        PlayerAccount player = ps.m_account;
        playerName.setText(player.getUsername());
        score_text.setText(Integer.toString(ps.m_score));

        return view;
    }
}
