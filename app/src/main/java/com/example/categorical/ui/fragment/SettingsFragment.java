package com.example.categorical.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.categorical.R;
import com.example.categorical.ui.util.DialogManager;
import com.example.categorical.ui.util.LauncherUtil;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActivityResultLauncher<Intent> fileSelectionLauncher =
                LauncherUtil.createFileSelectionLauncher(requireActivity(), this);

        ActivityResultLauncher<Intent> fileCreationLauncher =
                LauncherUtil.createFileCreationLauncher(requireActivity(), this,
                        fileSelectionLauncher);

        Button select_file_button = view.findViewById(
                R.id.settings_file_management_select_file_button);
        select_file_button.setOnClickListener(v ->
                DialogManager.createCreateTextFileDialog(requireActivity(), fileCreationLauncher,
                fileSelectionLauncher));
    }

}
