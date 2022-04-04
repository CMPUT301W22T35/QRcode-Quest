package com.qrcode_quest.ui.comments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.CommentManager;
import com.qrcode_quest.databinding.FragmentCommentsBinding;
import com.qrcode_quest.entities.Comment;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A comment viewing fragment. This is for displaying and adding comments to a specific QR Code.
 *
 * @author jdumouch
 * @version 1.0
 */
public class CommentsFragment extends Fragment {
    private CommentsViewModel viewModel;
    private MainViewModel mainViewModel;
    private FragmentCommentsBinding binding;
    private CommentViewAdapter viewAdapter;
    private String qrHash;

    /** Required empty constructor */
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

        // Create the comments view model
        AppContainer appContainer = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        ViewModelProvider.Factory viewModelFactory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                return Objects.requireNonNull(aClass.cast(new CommentsViewModel(
                        requireActivity().getApplication(), appContainer.getDb())));
            }
        };

        // Grab the view models
        viewModel = new ViewModelProvider(this, viewModelFactory).get(CommentsViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Load the recycler view
        RecyclerView recyclerView = binding.commentsCommentList;
        viewAdapter = new CommentViewAdapter(new ArrayList<>());
        recyclerView.setAdapter(viewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        // Hide the content views and show the progress spinner
        setLoadingState(true);

        // Load and observe reloads to comments
        viewModel.getComments(qrHash).observe(getViewLifecycleOwner(), comments->{
            viewAdapter = new CommentViewAdapter(comments);
            recyclerView.setAdapter(viewAdapter);

            setLoadingState(false);
        });

        // Hook up listeners to post comments
        binding.commentsPostButton.setOnClickListener(v->onPostClicked());
        binding.commentsInput.setOnEditorActionListener((textView, i, keyEvent) -> {
            onPostClicked();
            return false;
        });
        return binding.getRoot();
    }

    /**
     * Sets visibility of components depending on the loading state of the view.
     * @param loading A boolean denoting if the view is loading or not
     */
    private void setLoadingState(boolean loading){
        if (loading) {
            binding.commentsPostButton.setEnabled(false);
            binding.commentsCommentList.setVisibility(View.GONE);
            binding.commentsNocommentsLabel.setVisibility(View.GONE);
            binding.commentsProgress.setVisibility(View.VISIBLE);
        }
        else{
            binding.commentsPostButton.setEnabled(true);
            binding.commentsCommentList.setVisibility(viewAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            binding.commentsNocommentsLabel.setVisibility(viewAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            binding.commentsProgress.setVisibility(View.GONE);
        }
    }

    /**
     * Handles the post button being clicked.
     * Validation is performed on the comment and on success will be added to the database
     *
     * Closing the soft keyboard:
     * https://stackoverflow.com/questions/3400028/close-virtual-keyboard-on-button-press
     * Author: Paul Maserrat, Nov 21, 2011
     * Accessed: March 26, 2022
     */
    private void onPostClicked(){
        // Close their keyboard
        AppCompatActivity parent = (AppCompatActivity) this.requireActivity();
        if (parent.getCurrentFocus() != null) {
            InputMethodManager inputManager =
                    (InputMethodManager) parent.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(
                    parent.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // Grab the trimmed text entered by the user
        String msg = binding.commentsInput.getText().toString().trim();

        // Prevent empty messages
        if (msg.isEmpty()) { return; }
        setLoadingState(true);

        // Add the comment to the database
        AppContainer appContainer = ((QRCodeQuestApp) requireActivity().getApplication()).getContainer();
        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), player-> {
            new CommentManager(appContainer.getDb()).addComment(
                    new Comment( player.getUsername(), msg, qrHash),
                    result -> {
                        // Handle comment addition failure
                        if (!result.isSuccess()){
                            Toast.makeText(this.getContext(),
                                    "Failed to add comment.",
                                    Toast.LENGTH_SHORT).show();
                            setLoadingState(false);
                            return;
                        }

                        // Handle addition success
                        viewModel.loadComments();
                        binding.commentsInput.setText("");
                    }
            );
        });
    }
}
