package com.example.depthdefinedshoppinglist.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.data.FileUtil;
import com.example.depthdefinedshoppinglist.data.TextFileManager;
import com.example.depthdefinedshoppinglist.ui.util.DialogManager;
import com.example.depthdefinedshoppinglist.ui.util.LauncherUtil;
import com.example.depthdefinedshoppinglist.ui.viewModel.MainViewModel;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String MESSAGE_ID = "messages_prefs";
    public static final String CAT_TEXT_FILE_URI_KEY = "catTextFileUri";

    private MainViewModel mainViewModel;

    ActivityResultLauncher<Intent> fileSelectionLauncher =
            LauncherUtil.createFileSelectionLauncher(this, this);

    ActivityResultLauncher<Intent> fileCreationLauncher =
            LauncherUtil.createFileCreationLauncher(this, this, fileSelectionLauncher);

    Fragment navHostFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setSupportActionBar(findViewById(R.id.toolbar));

        mainViewModel =
                new ViewModelProvider(this).get(MainViewModel.class);

        setUpCatalogTextFile();

        FragmentManager fragmentManager = getSupportFragmentManager();
        navHostFragment = fragmentManager.findFragmentById(R.id.nav_host_main_fragment);

        ImageView settingsIcon = findViewById(R.id.settings_icon);
        ImageView refreshIcon = findViewById(R.id.refresh_icon);

        settingsIcon.setOnClickListener(v -> {
            if (mainViewModel.isItemsParentFragmentActive()) {
                NavHostFragment.findNavController(navHostFragment)
                        .navigate(R.id.action_itemsParentFragment_to_settingsFragment);
                mainViewModel.setItemsParentFragmentActive(false);
            }
        });

        refreshIcon.setOnClickListener(v -> {
            if (TextFileManager.isExternalModify()) {
                if (mainViewModel.updateLists(this)) {

                    mainViewModel.buildCatalogView();

                    Toast.makeText(
                            this, R.string.refreshed_msg,
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(
                            this, R.string.failed_refresh_msg,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setUpCatalogTextFile() {
        //get data from shared pref
        SharedPreferences getShareData = getSharedPreferences(MESSAGE_ID, MODE_PRIVATE);

        String fileUri = getShareData.getString(CAT_TEXT_FILE_URI_KEY, null);

        if (fileUri == null) {
            DialogManager.createCreateTextFileDialog(this,
                    fileCreationLauncher, fileSelectionLauncher);
        }
        else {
            Uri catFileUri = Uri.parse(fileUri);
            String path = FileUtil.getPath(catFileUri, this);

            if (path != null) {
                File file = new File(path);

                if (!file.exists()) {
                    SharedPreferences prefs = getSharedPreferences(MESSAGE_ID, MODE_PRIVATE);
                    prefs.edit().remove(CAT_TEXT_FILE_URI_KEY).clear().apply();

                    DialogManager.createCreateTextFileDialog(this,
                            fileCreationLauncher, fileSelectionLauncher);
                }
                else {
                    TextFileManager.setCatFileUri(catFileUri);
                    //observe file; if modified, then on the next add, del, edit, or manual refresh,
                    //display will be updated
                    TextFileManager.startObserving(path);

                    mainViewModel.updateLists(this);
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (!mainViewModel.isItemsParentFragmentActive()) {
            mainViewModel.setItemsParentFragmentActive(true);
        }
        super.onBackPressed();
    }
}