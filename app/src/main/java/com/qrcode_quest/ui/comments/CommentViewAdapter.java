package com.qrcode_quest.ui.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qrcode_quest.R;
import com.qrcode_quest.entities.Comment;

import java.util.ArrayList;

/**
 * A view adapter for loading comments into a RecyclerView.
 *
 * @author jdumouch
 * @version 1.1
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
        // Load the View that the view holder is made from
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item_view, parent, false);

        return new CommentViewAdapter.ViewHolder(view);
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
        public ViewHolder(@NonNull View view) {
            super(view);
            usernameText = view.findViewById(R.id.comment_item_user);
            commentText = view.findViewById(R.id.comment_item_content);
        }
    }
}
