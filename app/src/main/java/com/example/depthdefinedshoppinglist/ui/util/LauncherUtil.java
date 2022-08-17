package com.example.depthdefinedshoppinglist.ui.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.data.FileUtil;
import com.example.depthdefinedshoppinglist.data.TextFileManager;
import com.example.depthdefinedshoppinglist.ui.activity.MainActivity;

public class LauncherUtil {

    public static ActivityResultLauncher<Intent> createFileSelectionLauncher
            (Context context, ActivityResultCaller arc) {

        String prefsMsgID = MainActivity.MESSAGE_ID;
        String uriKey = MainActivity.CAT_TEXT_FILE_URI_KEY;

        return arc.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            int takeFlags = data.getFlags();
                            takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                            String path = FileUtil.getPath(uri, context);

                            if (path != null) {

                                String stringUri = uri.toString();
                                SharedPreferences sharedPreferences =
                                        context.getSharedPreferences(prefsMsgID, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(uriKey, stringUri);
                                editor.apply();


                                context.getContentResolver()
                                        .takePersistableUriPermission(uri,
                                                takeFlags);

                                TextFileManager.setCatFileUri(uri);
                                TextFileManager.startObserving(path);

                                MainActivity mainActivity = (MainActivity) context;
                                mainActivity.setUpCatalogTextFile();
                                mainActivity.recreate();

                                Toast.makeText(context.getApplicationContext(),
                                        R.string.toast_chosen_file_accepted,
                                        Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(context.getApplicationContext(),
                                        R.string.toast_chosen_file_rejected,
                                        Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public static ActivityResultLauncher<Intent> createFileCreationLauncher
            (Context context, ActivityResultCaller arc,
             ActivityResultLauncher<Intent> fileSelectionLauncher) {

        return arc.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Toast.makeText(context.getApplicationContext(),
                                    R.string.toast_successful_file_creation,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(context.getApplicationContext(),
                                    R.string.toast_failed_file_creation,
                                    Toast.LENGTH_SHORT).show();
                    }
                    DialogManager.createSelectTextFileDialog(context, fileSelectionLauncher);
                });
    }
}
