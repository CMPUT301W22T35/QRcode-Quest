package com.qrcode_quest.ui.map;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.qrcode_quest.R;

import java.util.ArrayList;

/**
 * A view to display nearby QR codes in a list
 *
 * @author ageolleg
 * @version 1.0
 */
public class MapListFragment extends Fragment {
    private int mColumnCount = 1;
    private static final String ARG_COLUMN_COUNT = "column-count";

    public static MapListFragment newInstance(int mColumnCount) {
        MapListFragment fragment = new MapListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, mColumnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MapListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MapListViewAdapter(MapListContent.ITEMS));
        }
        return view;
    }
}