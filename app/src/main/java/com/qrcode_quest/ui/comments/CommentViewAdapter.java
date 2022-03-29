package com.qrcode_quest.ui.comments;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qrcode_quest.databinding.CommentItemViewBinding;
import com.qrcode_quest.entities.Comment;

import java.util.ArrayList;

/**
 * A view adapter for loading comments into a RecyclerView.
 *
 * @author jdumouch
 * @version 1.0
 */
public class CommentViewAdapter extends RecyclerView.Adapter<CommentViewAdapter.ViewHolder> {

    private ArrayList<Comment> items;

    /**
     * Creates a new view adapter using the passed items.
     * @param items The initial list of comments to build
     */
    public CommentViewAdapter(ArrayList<Comment> items){
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(CommentItemViewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment item = items.get(position);

        holder.comment = item;
        holder.usernameText.setText(item.getUid());
        holder.commentText.setText(item.getContent());
    }

    /** Gets the size of the displayed item list */
    @Override
    public int getItemCount() { return items.size(); }

    /** ViewHolders serve as the binder between the View and the data. */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView usernameText;
        public final TextView commentText;
        public Comment comment;

        /** Constructs a ViewHolder and bind the members to the view */
        public ViewHolder(@NonNull CommentItemViewBinding binding) {
            super(binding.getRoot());
            usernameText = binding.commentItemUser;
            commentText = binding.commentItemContent;
        }
    }
}
