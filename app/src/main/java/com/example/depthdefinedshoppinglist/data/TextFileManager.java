package com.example.depthdefinedshoppinglist.data;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.domain.ShoppingItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class TextFileManager {
    private static Uri catFileUri;

    //set to true whenever app detects changes to file not performed by app
    private static boolean externalModify = false;
    //if a FileObserver object is made local, it stops observing file (do not make local)
    private static FileObserver observer;

    private TextFileManager() { }

    /**
     *
     * @param catList - must be sorted (including children of all child categories)
     * @param selectedItemsList - must be sorted
     * @param context - MainActivity instance
     * @return true if write successful, else false
     */
    public static boolean writeToCatalogTextFile(ArrayList<ShoppingItem> catList,
                                              ArrayList<ShoppingItem> selectedItemsList,
                                              Context context) {

        if (fileUnavailable(context))
            return false;

        try {
            //mode 'wt' sets mode of pfd to write and truncate (i.e. overwrite)
            ParcelFileDescriptor pfd = context.getContentResolver().
                    openFileDescriptor(catFileUri, "wt");
            FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            ShoppingItem currentCat = null;
            String stringOfAncestors = null;
            Stack<Integer> tierIndex = new Stack<>();
            //0th index of tierIndex corresponds to 0th tier (contains undeclared parent of all
            //category list items); value of 0th index corresponds to index of child of 1st tier
            tierIndex.push(0);

            boolean catalogNotFullyDocumented = true;
            while (catalogNotFullyDocumented) {
                if (currentCat == null) {
                    if (catList.size() == tierIndex.peek()) {
                        catalogNotFullyDocumented = false;
                    }
                    else {
                        currentCat = catList.get(tierIndex.peek());
                        stringOfAncestors = currentCat.getName();
                        tierIndex.push(0);
                    }
                }
                else if (currentCat.getCategoryItems().isEmpty()) {
                    if (selectedItemsList.contains(currentCat)) {
                        bw.write("<" + stringOfAncestors + ">");
                    } else
                        bw.write(stringOfAncestors);
                    bw.newLine();
                    currentCat = currentCat.getParentCategory();
                    tierIndex.pop();
                    tierIndex.push(tierIndex.pop()+1);

                    int finalIndex = stringOfAncestors.lastIndexOf(".");
                    if (finalIndex != -1)
                        stringOfAncestors = stringOfAncestors.substring(0, finalIndex);
                    else
                        stringOfAncestors = "";
                }
                else if (currentCat.getCategoryItems().size() == tierIndex.peek()) {
                    currentCat = currentCat.getParentCategory();
                    tierIndex.pop();
                    tierIndex.push(tierIndex.pop()+1);

                    int finalIndex = stringOfAncestors.lastIndexOf(".");
                    if (finalIndex != -1)
                        stringOfAncestors = stringOfAncestors.substring(0, finalIndex);
                    else
                        stringOfAncestors = "";
                }
                else {
                    currentCat = currentCat.getCategoryItems().get(tierIndex.peek());
                    stringOfAncestors += "." + currentCat.getName();
                    tierIndex.push(0);
                }
            }
            bw.close();
            fos.close();
            pfd.close();
        }
        catch(IOException | SecurityException e){
            e.printStackTrace();

            return false;
        }

        return true;
    }

    /**
     *
     * @return list of cat items, followed by the list of items selected from those cat items
     * by the user (both lists are ordered alphabetically); assumes file exists
     */
    public static ArrayList<ArrayList<ShoppingItem>> getCatalogAndSelectedItems(Context context) {

        if (fileUnavailable(context)) {
            return null;
        }

        ArrayList<ShoppingItem> catList = new ArrayList<>();
        ArrayList<ShoppingItem> selectedItemsList = new ArrayList<>();

        try {
            ParcelFileDescriptor pfd = context.getContentResolver().
                    openFileDescriptor(catFileUri, "r");

            FileInputStream fileInputStream =
                    new FileInputStream(pfd.getFileDescriptor());
            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals(""))
                    continue;

                boolean hierarchyIncomplete = false;
                boolean isSelectedItem = false;
                if (line.indexOf("<") == 0 && line.indexOf(">") == line.length()-1) {
                    line = line.substring(1, line.length() - 1);
                    isSelectedItem = true;
                }

                // categories must not contain illegal char's
                if (line.contains(">") || line.contains("<"))
                    continue;

                String cat;
                if (line.contains(".")) {
                    hierarchyIncomplete = true;
                    cat = line.substring(0, line.indexOf(".")).trim();
                } else
                    cat = line.trim();

                //make sure all names conform to max and min length
                if (cat.length() > ShoppingItem.MAX_NAME_LENGTH
                    || cat.length() == 0)
                    continue;


                ShoppingItem item = null;
                for (int i = 0; i < catList.size(); i++) {
                    if (cat.equals(catList.get(i).getName())) {
                        item = catList.get(i);
                    }
                }

                if (item == null) {
                    item = new ShoppingItem(null, cat);
                    catList.add(item);
                    if (!hierarchyIncomplete && isSelectedItem) {
                        selectedItemsList.add(item);
                    }
                }
                ShoppingItem parentCat = item;
                ArrayList<ShoppingItem> children;
                ShoppingItem currentCat = null;
                int depth = 0;
                while (hierarchyIncomplete && ++depth <= ShoppingItem.MAX_DEPTH) {
                    line = line.substring(line.indexOf(".") + 1);

                    children = parentCat.getCategoryItems();

                    if (!line.contains(".")) {
                        cat = line.trim();
                        hierarchyIncomplete = false;
                    } else {
                        cat = line.substring(0, line.indexOf(".")).trim();
                    }

                    //make sure all names conform to max and min length
                    if (cat.length() > ShoppingItem.MAX_NAME_LENGTH
                            || cat.length() == 0)
                       break;

                    for (int i = 0; i < children.size(); i++) {
                        if (cat.equals(children.get(i).getName())) {
                            currentCat = children.get(i);
                        }
                    }

                    if (currentCat == null) {
                        currentCat = new ShoppingItem(parentCat, cat);
                        children.add(currentCat);
                        Collections.sort(children);

                        if (!hierarchyIncomplete && isSelectedItem)
                            selectedItemsList.add(currentCat);
                    }
                    parentCat = currentCat;
                    currentCat = null;
                }

            }

            pfd.close();
            fileInputStream.close();
            br.close();
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }

        ArrayList<ArrayList<ShoppingItem>> listOfLists = new ArrayList<>();
        Collections.sort(catList);
        Collections.sort(selectedItemsList);
        listOfLists.add(catList);
        listOfLists.add(selectedItemsList);

        return listOfLists;
    }

    private static boolean fileUnavailable(Context context) {
        boolean unavailable = false;
        String path = FileUtil.getPath(catFileUri, context);
        if (path != null &&
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(path);
            if (!file.exists())
                unavailable = true;
        } else
            unavailable = true;

        return unavailable;
    }

    public static void unobservedWriteToFile(ArrayList<ShoppingItem> catList,
                                       ArrayList<ShoppingItem> selectedItemsList,
                                             Context context) {
        observer.stopWatching();
        if (!writeToCatalogTextFile(catList,
                selectedItemsList, context))
            Toast.makeText(
                    context, R.string.failed_refresh_msg,
                    Toast.LENGTH_SHORT).show();
        startObserving(FileUtil.getPath(catFileUri, context));
    }

    /**
     * Initialize a FileObserver object tied to a text file, and start observing it.
     * @param filePath - path of text file to be observed for modifications; assumed to be non-null.
     */
    public static void startObserving(String filePath) {
        observer = new FileObserver(filePath) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                if (event == CLOSE_WRITE) {
                    externalModify = true;
                }
            }
        };

        observer.startWatching();
    }

    public static void setCatFileUri(Uri catFileUri) {
        TextFileManager.catFileUri = catFileUri;
    }

    public static boolean isExternalModify() {
        return externalModify;
    }

    public static void setExternalModify(boolean externalModify) {
        TextFileManager.externalModify = externalModify;
    }
}
