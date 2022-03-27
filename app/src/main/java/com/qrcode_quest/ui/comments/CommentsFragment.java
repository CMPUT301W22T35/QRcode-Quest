package com.qrcode_quest.ui.comments;

import static java.util.Objects.requireNonNull;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.databinding.FragmentCommentsBinding;
import com.qrcode_quest.entities.QRCode;

import java.util.ArrayList;

/**
 * A comment viewing fragment. This is for displaying and adding comments to a specific QR Code.
 *
 * @author jdumouch
 * @version 1.0
 */
public class CommentsFragment extends Fragment {

    private CommentsViewModel viewModel;
    private FragmentCommentsBinding binding;
    private CommentViewAdapter viewAdapter;
    private String qrHash;

    public CommentsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the arguments into the fragment
        Bundle args = requireArguments();
        qrHash = args.getString("qr_hash");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommentsBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(CommentsViewModel.class);

        // Load the recycler view
        RecyclerView recyclerView = binding.commentsCommentList;
        viewAdapter = new CommentViewAdapter(new ArrayList<>());
        recyclerView.setAdapter(viewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        // Hide the content views and show the progress spinner
        binding.commentsCommentList.setVisibility(View.GONE);
        binding.commentsNocommentsLabel.setVisibility(View.GONE);
        binding.commentsProgress.setVisibility(View.VISIBLE);

        // Load and observe reloads to comments
        viewModel.getComments(qrHash).observe(getViewLifecycleOwner(), comments->{
            viewAdapter = new CommentViewAdapter(comments);
            recyclerView.setAdapter(viewAdapter);

            // Display the appropriate view
            binding.commentsCommentList.setVisibility(comments.size() == 0 ? View.GONE : View.VISIBLE);
            binding.commentsNocommentsLabel.setVisibility(comments.size() == 0 ? View.VISIBLE : View.GONE);
            binding.commentsProgress.setVisibility(View.GONE);
        });

        binding.commentsPostButton.setOnClickListener(this::onPostClicked);

        return binding.getRoot();
    }

    private void onPostClicked(View view){

    }
}