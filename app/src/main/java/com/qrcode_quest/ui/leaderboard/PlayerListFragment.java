package com.qrcode_quest.ui.leaderboard;

import static com.qrcode_quest.ui.leaderboard.PlayerListFragmentDirections.actionLeaderboardToPlayerqrs;
import static com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections.actionPlayerqrsToQrview;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.databinding.FragmentPlayerListBinding;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.ui.leaderboard.PlayerListFragmentDirections.ActionLeaderboardToPlayerqrs;
import com.qrcode_quest.ui.playerQR.PlayerQRListFragmentDirections;

import java.util.Arrays;

/**
 * A view to display or select an arbitrary list of players
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerListFragment extends Fragment {

    public enum ViewMode {
        LEADERBOARD,
    }

    private PlayerListViewModel viewModel;
    private FragmentPlayerListBinding binding;

    public static PlayerListFragment newInstance() {
        return new PlayerListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PlayerListViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlayerListBinding.inflate(inflater, container, false);


        RecyclerView recyclerView = binding.playerlistRecycler;
        PlayerViewAdapter viewAdapter = new PlayerViewAdapter(
                Arrays.asList(new PlayerAccount("bob")), p->{
                    NavController navController = NavHostFragment.findNavController(this);
                    ActionLeaderboardToPlayerqrs action =
                            actionLeaderboardToPlayerqrs(p);
                    navController.navigate(action);
                }
        );
        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(viewAdapter);
        return binding.getRoot();
    }

}