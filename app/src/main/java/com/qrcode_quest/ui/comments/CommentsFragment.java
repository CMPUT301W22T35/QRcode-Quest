package com.qrcode_quest.ui.comments;

import static java.util.Objects.requireNonNull;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.databinding.FragmentCommentsBinding;
import com.qrcode_quest.entities.Comment;
import com.qrcode_quest.entities.QRCode;

import java.util.ArrayList;

/**
 * A comment viewing fragment. This is for displaying and adding comments to a specific QR Code.
 *
 * @author jdumouch
 * @version 1.0
 */
public class CommentsFragment extends Fragment {

    private FragmentCommentsBinding binding;
    private QRCode viewedCode;
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

        ArrayList<Comment> comments = new ArrayList<>();
        comments.add(new Comment("Dadman", "I like memes", qrHash));
        RecyclerView recyclerView = binding.commentsCommentList;
        recyclerView.setAdapter(new CommentViewAdapter(comments));
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        return binding.getRoot();
    }
}