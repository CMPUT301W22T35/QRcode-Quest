package com.qrcode_quest.ui.comments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.R;
import com.qrcode_quest.databinding.FragmentCommentsBinding;
import com.qrcode_quest.entities.QRCode;

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
        Bundle args = getArguments();
        assert args != null;

        qrHash = args.getString("qr_hash");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommentsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}