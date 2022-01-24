package com.example.depthdefinedshoppinglist.ui.util;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;

import com.example.depthdefinedshoppinglist.R;

public class DialogManager {

    //default name for file app stores catalog info in
    public static final String CAT_FILE_NAME = "catalog.txt";

    public static void createCreateTextFileDialog(Context context,
                                                  ActivityResultLauncher<Intent> fileCreationLauncher,
                                                  ActivityResultLauncher<Intent> fileSelectionLauncher) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(R.string.create_text_file_dialog_title);
        alertDialog.setMessage(R.string.create_text_file_dialog_msg);
        alertDialog.setPositiveButton(R.string.create_button_text,
                (dialog, which) -> letUserCreateTextFile(context, fileCreationLauncher));

        alertDialog.setNegativeButton(R.string.skip_button_text,
                (dialog, which) -> {
                    dialog.cancel();
                    createSelectTextFileDialog(context, fileSelectionLauncher);
                });

        alertDialog.show();
    }

    public static void createSelectTextFileDialog(Context context,
                                                  ActivityResultLauncher<Intent> fileSelectionLauncher) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(R.string.select_text_file_dialog_title);
        alertDialog.setMessage(R.string.select_text_file_dialog_msg);
        alertDialog.setPositiveButton(R.string.select_button_text,
                (dialog, which) ->
                        letUserSelectTextFile(context, fileSelectionLauncher));

        alertDialog.setNegativeButton(R.string.skip_button_text,
                (dialog, which) -> dialog.cancel());

        alertDialog.show();
    }

    private static void letUserCreateTextFile(Context context,
                                              ActivityResultLauncher<Intent> fileCreationLauncher) {
        Intent createIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        createIntent.addCategory(Intent.CATEGORY_OPENABLE);
        createIntent.setType("text/plain");
        createIntent.putExtra(Intent.EXTRA_TITLE, CAT_FILE_NAME);

        fileCreationLauncher.launch(Intent.createChooser(createIntent,
                context.getString(R.string.create_text_file_dialog_title)));
    }

    private static void letUserSelectTextFile(Context context,
                                              ActivityResultLauncher<Intent> fileSelectionLauncher) {
        Intent editAndReadPermissionsIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        editAndReadPermissionsIntent.addCategory(Intent.CATEGORY_OPENABLE);
        editAndReadPermissionsIntent.setType("text/plain");

        editAndReadPermissionsIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

        fileSelectionLauncher.launch(Intent.createChooser(editAndReadPermissionsIntent,
                context.getString(R.string.select_text_file_dialog_title)));
    }

}
