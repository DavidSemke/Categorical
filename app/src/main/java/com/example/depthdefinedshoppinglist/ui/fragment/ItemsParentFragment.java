package com.example.depthdefinedshoppinglist.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.ui.viewModel.MainViewModel;

public class ItemsParentFragment extends Fragment {

    private MainViewModel mainViewModel;
    private Fragment navHostFragment;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.items_parent_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        FragmentManager fragmentManager = getChildFragmentManager();
        navHostFragment = fragmentManager.findFragmentById(R.id.nav_host_items_parent_fragment);

        //tab buttons for selected items and cat items lists
        Button selectedItemsTab = view.findViewById(R.id.selected_items_tab);
        Button catalogTab = view.findViewById(R.id.catalog_tab);

        selectedItemsTab.setOnClickListener(v -> {
            if (!mainViewModel.isSelectedItemsFragmentActive()) {
                NavHostFragment.findNavController(navHostFragment)
                        .navigate(R.id.action_catalog_to_selected_items);
                mainViewModel.setSelectedItemsFragmentActive(true);
            }
        });

        catalogTab.setOnClickListener(v -> {
            if (mainViewModel.isSelectedItemsFragmentActive()) {
                NavHostFragment.findNavController(navHostFragment)
                        .navigate(R.id.action_selected_items_to_catalog);
                mainViewModel.setSelectedItemsFragmentActive(false);
            }
        });
    }

}
