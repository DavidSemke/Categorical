package com.example.depthdefinedshoppinglist.ui.fragment;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.data.TextFileManager;
import com.example.depthdefinedshoppinglist.domain.ShoppingItem;
import com.example.depthdefinedshoppinglist.ui.recViewAdapter.CatalogRecViewAdapter;
import com.example.depthdefinedshoppinglist.ui.util.OnItemClickListener;
import com.example.depthdefinedshoppinglist.ui.viewModel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;

public class CatalogFragment extends Fragment implements OnItemClickListener {
    //all ShoppingItem objects have null as parent if parent is root; this is just for the sake of
    //displaying the root as an item
    public static final ShoppingItem CAT_ROOT = new ShoppingItem("CATALOG ROOT");

    private MainViewModel mainViewModel;

    private TextView catEmptyRecViewMsg;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.catalog_fragment, container, false);


    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        catEmptyRecViewMsg = view.findViewById(R.id.catalog_empty_rec_view_text);
        if (mainViewModel.getCatItems().isEmpty())
            catEmptyRecViewMsg.setVisibility(View.VISIBLE);

        RecyclerView catRecyclerView = view.findViewById(R.id.catalog_recyclerview);
        catRecyclerView.setHasFixedSize(true);
        catRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mainViewModel.setCatAdapter(new CatalogRecViewAdapter(new ArrayList<>(),
                this, mainViewModel));
        catRecyclerView.setAdapter(mainViewModel.getCatAdapter());

        mainViewModel.buildCatalogView();

        //set up bottom nav bar, which does NOT aid navigation but switching between app modes
        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_nav_bar);
        MainViewModel.Mode mode = mainViewModel.getMode();
        switch (mode) {
            case SELECT:
                bottomNav.setSelectedItemId(R.id.select_mode_indicator);
                break;
            case ADD:
                bottomNav.setSelectedItemId(R.id.add_mode_indicator);
                break;
            case EDIT:
                bottomNav.setSelectedItemId(R.id.edit_mode_indicator);
                break;
            case DELETE:
                bottomNav.setSelectedItemId(R.id.delete_mode_indicator);
                break;
        }

        bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.select_mode_indicator) {
                        mainViewModel.setMode(MainViewModel.Mode.SELECT);
                    }
                    else if (id == R.id.add_mode_indicator) {
                        mainViewModel.setMode(MainViewModel.Mode.ADD);
                    }
                    else if (id == R.id.edit_mode_indicator) {
                        mainViewModel.setMode(MainViewModel.Mode.EDIT);
                    }
                    else {
                        mainViewModel.setMode(MainViewModel.Mode.DELETE);
                    }

                    return true;
                }
        );

    }

    /**
     * Dictates the action to perform upon 'clicking' an item of the catalog displayed in the
     * catalog recycler view. Depth is a concern when in mode 'ADD'.
     * @param position - index of item selected with respect to the cat adapter
     */
    public void onItemClick(int position) {
        ShoppingItem item = mainViewModel.getCatAdapter().getItem(position);

        switch (mainViewModel.getMode()) {
            case SELECT:
                if (!item.equals(CAT_ROOT)) {
                    if (TextFileManager.isExternalModify()) {
                        if (mainViewModel.updateLists(requireActivity())) {

                            mainViewModel.buildCatalogView();

                            ShoppingItem newCat = mainViewModel.findPersistingCat(item);
                            if (newCat == null) {

                                Toast.makeText(
                                        requireActivity(), R.string.failed_select_msg,
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                //the result of getPosition(newCat) is assumed to not be -1
                                rawSelect(newCat, mainViewModel.getCatAdapter().getPosition(newCat));

                                TextFileManager.unobservedWriteToFile(mainViewModel.getCatItems(),
                                        mainViewModel.getSelectedItems(), requireActivity());
                            }

                            Toast.makeText(
                                    requireActivity(), R.string.external_cat_update_msg,
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            rawSelect(item, position);

                            Toast.makeText(
                                    requireActivity(), R.string.failed_refresh_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        rawSelect(item, position);

                        TextFileManager.unobservedWriteToFile(mainViewModel.getCatItems(),
                                mainViewModel.getSelectedItems(), requireActivity());
                    }

                }
                break;

            case ADD:
                if (item.getDepth() + 1 > ShoppingItem.MAX_DEPTH)
                    Toast.makeText(
                            requireActivity(), R.string.exceed_max_depth_msg,
                            Toast.LENGTH_SHORT).show();
                else {
                    //item to be added to becomes expanded if (add successful and not already
                    // expanded)
                    createAddCategoryDialog(position, item);
                }


                break;

            case EDIT:
                if (!item.equals(CAT_ROOT))
                    createEditCategoryDialog(position, item);
                break;

            case DELETE:
                if (!item.equals(CAT_ROOT)) {
                    if (TextFileManager.isExternalModify()) {
                        if (mainViewModel.updateLists(requireActivity())) {

                            mainViewModel.buildCatalogView();

                            ShoppingItem newCat = mainViewModel.findPersistingCat(item);
                            if (newCat == null) {
                                Toast.makeText(
                                        requireActivity(), R.string.failed_delete_msg,
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                //the result of getPosition(newCat) is assumed to not be -1
                                rawDelete(item, mainViewModel.getCatAdapter().getPosition(newCat));

                                TextFileManager.unobservedWriteToFile(mainViewModel.getCatItems(),
                                        mainViewModel.getSelectedItems(), requireActivity());
                            }
                            Toast.makeText(
                                    requireActivity(), R.string.external_cat_update_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            rawDelete(item, position);

                            Toast.makeText(
                                    requireActivity(), R.string.failed_refresh_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        rawDelete(item, position);

                        TextFileManager.unobservedWriteToFile(mainViewModel.getCatItems(),
                                mainViewModel.getSelectedItems(), requireActivity());
                    }
                }
                break;
        }
    }

    /**
     * Select cat without checking for file modifications.
     * @param item - Item selected.
     * @param position - Index of item selected.
     */
    private void rawSelect(ShoppingItem item, int position) {

        if (item.getCategoryItems().isEmpty()) { //no collapse, no expand

            ArrayList<ShoppingItem> selectedItems = mainViewModel.getSelectedItems();
            if (selectedItems.contains(item)) {
                selectedItems.remove(item);
            }
            else {
                selectedItems.add(item);
                Collections.sort(selectedItems);
            }
            //notify item changed in order to register checkmark
            mainViewModel.getCatAdapter().notifyItemChanged(position);
        }
        else {
            mainViewModel.collapseAndExpand(position);
        }


    }

    /**
     * Delete cat without checking for file modifications.
     * @param item - Item selected for deletion.
     * @param position - Index of item selected for deletion.
     */
    private void rawDelete(ShoppingItem item, int position) {
        ArrayList<ShoppingItem> selectedItems = mainViewModel.getSelectedItems();
        ArrayList<ShoppingItem> dependentSelectedItems = item.findCats(selectedItems);
        if (dependentSelectedItems == null) {
            selectedItems.remove(item); //item may or may not need removing
        }
        else {
            for (int i = 0; i < dependentSelectedItems.size(); i++) {
                selectedItems.remove(dependentSelectedItems.get(i));
            }

        }
        mainViewModel.deleteFromCatalog(position);

        Toast.makeText(
                requireActivity(), R.string.on_cat_delete_msg, Toast.LENGTH_SHORT).show();
    }

    private void createAddCategoryDialog(int position, ShoppingItem item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
        alertDialog.setTitle(getString(R.string.add_cat_dialog_title));
        alertDialog.setMessage(getString(R.string.add_cat_dialog_description, item.getName()));
        EditText input = new EditText(requireActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setSingleLine();
        input.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(ShoppingItem.MAX_NAME_LENGTH)});
        alertDialog.setView(input);
        alertDialog.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_add,
                null));

        alertDialog.setPositiveButton(R.string.ok_button_text,
                (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(
                                requireActivity(), R.string.field_empty,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else if (newName.contains(".")) {
                        Toast.makeText(
                                requireActivity(), R.string.category_name_contains_period,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //array to contain the to-be-siblings of the cat to be added
                    ArrayList<ShoppingItem> siblings = null;
                    ShoppingItem itemToAddTo = item;
                    //position may be adjusted due to external update; this var allows editing of position
                    int finalPosition = position;

                    if (TextFileManager.isExternalModify()) {
                        if (mainViewModel.updateLists(requireActivity())) {

                            mainViewModel.buildCatalogView();
                            //index 0 being null means no item was selected for expansion
                            //therefore, cat root was selected, which will always persist
                            if (itemToAddTo.equals(CAT_ROOT))
                                siblings = mainViewModel.getCatItems();
                            else {
                                itemToAddTo =
                                        mainViewModel.findPersistingCat(item);

                                if (itemToAddTo == null) {
                                    Toast.makeText(
                                            requireActivity(), R.string.failed_add_msg,
                                            Toast.LENGTH_SHORT).show();
                                }

                                else {
                                    finalPosition = mainViewModel.getCatAdapter()
                                            .getPosition(itemToAddTo);
                                    siblings = itemToAddTo.getCategoryItems();
                                }
                            }

                            Toast.makeText(
                                    requireActivity(), R.string.external_cat_update_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            siblings = rawAddSiblings(itemToAddTo);

                            Toast.makeText(
                                    requireActivity(), R.string.failed_refresh_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                        siblings = rawAddSiblings(itemToAddTo);

                    //if 'siblings != null', add is allowed
                    if (siblings != null) {
                        boolean notADupName = true;
                        for (int i=0; i<siblings.size(); i++) {
                            if (siblings.get(i).getName().equals(newName))
                                notADupName = false;
                        }
                        if (notADupName) {
                            ShoppingItem[] expandedCats = mainViewModel.getExpandedCats();

                            int parentPos = 0;
                            if (itemToAddTo.equals(CAT_ROOT))
                                itemToAddTo = null;
                            else if (!itemToAddTo.equals(expandedCats[itemToAddTo.getDepth()])) {
                                mainViewModel.collapseAndExpand(finalPosition);
                                parentPos = mainViewModel.getCatAdapter().getPosition(itemToAddTo);
                            }
                            else
                                parentPos = finalPosition;

                            mainViewModel.addToCatalog(new ShoppingItem(itemToAddTo, newName),
                                    parentPos);

                            catEmptyRecViewMsg.setVisibility(View.GONE);

                            //write to file
                            TextFileManager.unobservedWriteToFile(mainViewModel.getCatItems(),
                                    mainViewModel.getSelectedItems(), requireActivity());
                        }
                        else {
                            Toast.makeText(requireActivity(), R.string.duplicate_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });

        alertDialog.setNegativeButton(R.string.cancel_button_text,
                (dialog, which) -> dialog.cancel());

        alertDialog.show();
    }

    /**
     * Helper method for adding a child to parent. Determines the items the item to be added
     * will be compared to to avoid duplicates.
     * @param parent - parent of item to be added
     * @return - the items to be compared to the item to be added
     */
    private ArrayList<ShoppingItem> rawAddSiblings(ShoppingItem parent) {
        ArrayList<ShoppingItem> siblings;
        if (parent.equals(CAT_ROOT))
            siblings = mainViewModel.getCatItems();
        else
            siblings = parent.getCategoryItems();

        return siblings;
    }


    private void createEditCategoryDialog(int position, ShoppingItem item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
        alertDialog.setTitle(getString(R.string.edit_cat_dialog_title));
        alertDialog.setMessage(getString(R.string.edit_cat_dialog_description, item.getName()));
        EditText input = new EditText(requireActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_edit, null));
        alertDialog.setPositiveButton(R.string.ok_button_text,
                (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(
                                requireActivity(), R.string.field_empty,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else if (newName.contains(".")) {
                        Toast.makeText(
                                requireActivity(), R.string.category_name_contains_period,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<ShoppingItem> siblings = null;
                    //position may be adjusted due to external update; this var allows editing of position
                    int finalPosition = position;

                    if (TextFileManager.isExternalModify()) {
                        if (mainViewModel.updateLists(requireActivity())) {

                            mainViewModel.buildCatalogView();
                            //item is guaranteed to be non-null
                            ShoppingItem newCat =
                                    mainViewModel.findPersistingCat(item);

                            if (newCat == null) {
                                Toast.makeText(
                                        requireActivity(), R.string.failed_edit_msg,
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                finalPosition = mainViewModel.getCatAdapter().getPosition(newCat);
                                siblings = rawEditSiblings(newCat.getParentCategory());
                            }

                            Toast.makeText(
                                    requireActivity(), R.string.external_cat_update_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {

                            siblings = rawEditSiblings(item.getParentCategory());
                            Toast.makeText(
                                    requireActivity(), R.string.failed_refresh_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                        siblings = rawEditSiblings(item.getParentCategory());

                    if (siblings != null) {
                        boolean notADupName = true;
                        for (int i=0; i<siblings.size(); i++) {
                            if (siblings.get(i).getName().equals(newName))
                                notADupName = false;
                        }
                        if (notADupName) {
                            mainViewModel.editCatalog(finalPosition, newName);

                            //write to file
                            TextFileManager.unobservedWriteToFile(mainViewModel.getCatItems(),
                                    mainViewModel.getSelectedItems(), requireActivity());
                        }
                        else {
                            Toast.makeText(requireActivity(), R.string.duplicate_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });

        alertDialog.setNegativeButton(R.string.cancel_button_text,
                (dialog, which) -> dialog.cancel());

        alertDialog.show();
    }

    /**
     * Finds the siblings (other children with the same parent as cat to be edited)
     * of the cat to be edited without checking for file modifications.
     * @param parent - parent cat of item to be edited
     * @return - siblings of item to be edited
     */
    private ArrayList<ShoppingItem> rawEditSiblings(ShoppingItem parent) {
        ArrayList<ShoppingItem> siblings;

        if (parent == null)
            siblings = mainViewModel.getCatItems();
        else
            siblings = parent.getCategoryItems();

        return siblings;

    }
}
