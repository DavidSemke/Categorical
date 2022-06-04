package com.example.depthdefinedshoppinglist.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.ui.recViewAdapter.SelectedItemsRecViewAdapter;
import com.example.depthdefinedshoppinglist.ui.util.OnItemClickListener;
import com.example.depthdefinedshoppinglist.ui.viewModel.MainViewModel;

public class SelectedItemsFragment extends Fragment implements OnItemClickListener {

    private SelectedItemsRecViewAdapter selectedItemsRecViewAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.selected_items_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainViewModel mainViewModel =
                new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        TextView selectedItemsEmptyRecViewMsg =
                view.findViewById(R.id.selected_items_empty_rec_view_text);

        if (mainViewModel.getSelectedItems().isEmpty())
            selectedItemsEmptyRecViewMsg.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = view.findViewById(R.id.selected_items_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        selectedItemsRecViewAdapter =
                new SelectedItemsRecViewAdapter(mainViewModel.getSelectedItems(),
                this);
        recyclerView.setAdapter(selectedItemsRecViewAdapter);
    }

    public void onItemClick(int position) {
        selectedItemsRecViewAdapter.remove(position);
    }

}
