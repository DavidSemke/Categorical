package com.example.depthdefinedshoppinglist.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.data.TextFileManager;
import com.example.depthdefinedshoppinglist.domain.ShoppingItem;
import com.example.depthdefinedshoppinglist.ui.recViewAdapter.SelectedItemsRecViewAdapter;
import com.example.depthdefinedshoppinglist.ui.util.OnItemClickListener;
import com.example.depthdefinedshoppinglist.ui.viewModel.MainViewModel;

import java.util.ArrayList;

public class SelectedItemsFragment extends Fragment implements OnItemClickListener {

    private MainViewModel mainViewModel;

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

        mainViewModel =
                new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        TextView selectedItemsEmptyRecViewMsg = view.findViewById(R.id.selected_items_empty_rec_view_text);

        if (mainViewModel.getSelectedItems().isEmpty())
            selectedItemsEmptyRecViewMsg.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = view.findViewById(R.id.selected_items_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mainViewModel.setSelectedItemsAdapter(
                new SelectedItemsRecViewAdapter(new ArrayList<>(),
                this));
        recyclerView.setAdapter(mainViewModel.getSelectedItemsAdapter());

        mainViewModel.buildSelectedItemsView();
    }

    public void onItemClick(int position) {
        ShoppingItem item = mainViewModel.getSelectedItemsAdapter().getItem(position);

        if (TextFileManager.isExternalModify()) {
            if (mainViewModel.updateLists(requireActivity())) {

                mainViewModel.buildSelectedItemsView();

                int newPosition = mainViewModel.findIndexOfPersistingSelectedItem(item);
                if (newPosition != -1) {

                    mainViewModel.removeFromSelected(newPosition);
                    TextFileManager.unobservedWriteToFile(mainViewModel.getCatItems(),
                            mainViewModel.getSelectedItems(), requireActivity());
                }

                Toast.makeText(
                        requireActivity(), R.string.toast_external_cat_update,
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(
                        requireActivity(), R.string.toast_failed_refresh,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            mainViewModel.removeFromSelected(position);
            TextFileManager.unobservedWriteToFile(mainViewModel.getCatItems(),
                    mainViewModel.getSelectedItems(), requireActivity());
        }

    }
}
