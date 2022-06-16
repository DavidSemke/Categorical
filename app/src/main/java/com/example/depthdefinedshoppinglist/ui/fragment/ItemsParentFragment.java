package com.example.depthdefinedshoppinglist.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.ui.viewModel.MainViewModel;

public class ItemsParentFragment extends Fragment {

    private MainViewModel mainViewModel;
    private Fragment navHostFragment;
    public static final String tabColor = "#FF6200EE";

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

        GestureDetectorCompat gestureDetector = new GestureDetectorCompat(this.getContext(),
                new MyGestureListener());


        //tab buttons for selected items and cat items lists
        Button selectedItemsTab = view.findViewById(R.id.selected_items_tab);
        Button catalogTab = view.findViewById(R.id.catalog_tab);

        //initially non-selected tab is initially white with colored letters
//        selectedItemsTab.setBackgroundColor(Color.parseColor(tabColor));
//        catalogTab.setBackgroundColor(Color.TRANSPARENT);
//        catalogTab.setTextColor(Color.parseColor(tabColor));

        selectedItemsTab.setOnClickListener(v -> {
            if (!mainViewModel.isSelectedItemsFragmentActive()) {
                NavHostFragment.findNavController(navHostFragment)
                        .navigate(R.id.action_catalog_to_selected_items);
                mainViewModel.setSelectedItemsFragmentActive(true);
//                selectedItemsTab.setBackgroundColor(Color.parseColor(tabColor));
//                selectedItemsTab.setTextColor(Color.TRANSPARENT);
//                catalogTab.setBackgroundColor(Color.TRANSPARENT);
//                catalogTab.setTextColor(Color.parseColor(tabColor));
            }
        });

        catalogTab.setOnClickListener(v -> {
            if (mainViewModel.isSelectedItemsFragmentActive()) {
                NavHostFragment.findNavController(navHostFragment)
                        .navigate(R.id.action_selected_items_to_catalog);
                mainViewModel.setSelectedItemsFragmentActive(false);
//                catalogTab.setBackgroundColor(Color.parseColor(tabColor));
//                catalogTab.setTextColor(Color.TRANSPARENT);
//                selectedItemsTab.setBackgroundColor(Color.TRANSPARENT);
//                selectedItemsTab.setTextColor(Color.parseColor(tabColor));
            }
        });
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());

            //swipe right
            if (velocityX > 0) {
                if (mainViewModel.isSelectedItemsFragmentActive()) {
                    NavHostFragment.findNavController(navHostFragment)
                            .navigate(R.id.action_selected_items_to_catalog);
                    mainViewModel.setSelectedItemsFragmentActive(false);
                }
            }
            //swipe left
            else if (velocityX < 0) {
                if (!mainViewModel.isSelectedItemsFragmentActive()) {
                    NavHostFragment.findNavController(navHostFragment)
                            .navigate(R.id.action_catalog_to_selected_items);
                    mainViewModel.setSelectedItemsFragmentActive(true);
                }
            }

            return true;
        }
    }


}
