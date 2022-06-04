package com.example.depthdefinedshoppinglist.ui.viewModel;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.example.depthdefinedshoppinglist.data.TextFileManager;
import com.example.depthdefinedshoppinglist.domain.ShoppingItem;
import com.example.depthdefinedshoppinglist.ui.fragment.CatalogFragment;
import com.example.depthdefinedshoppinglist.ui.recViewAdapter.CatalogRecViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

/**
 * View model shared by MainActivity instance and all its fragments.
 * Let 'catItems' be ShoppingItem objects in the 'catItems' array.
 * Let 'selectedItems' be ShoppingItem objects in the 'selectedItems' array.
 */
public class MainViewModel extends ViewModel {
    private ArrayList<ShoppingItem> catItems = new ArrayList<>();
    private ArrayList<ShoppingItem> selectedItems = new ArrayList<>();
    //index i is the expanded item at depth i (null if no item is expanded at that depth)
    private final ShoppingItem[] expandedCats = new ShoppingItem[ShoppingItem.MAX_DEPTH+1];

    private CatalogRecViewAdapter catAdapter;

    //two fragment hosts; one for SelectedItemsFragment (default) and CatalogFragment, the other
    //for the ItemsParentFragment (default) and the SettingsFragment
    //the following booleans track which of each set of two fragments is active
    private boolean selectedItemsFragmentActive = true;
    private boolean itemsParentFragmentActive = true;

    public enum Mode {SELECT, ADD, EDIT, DELETE}
    private Mode mode = Mode.SELECT;

    public MainViewModel() { }

    /**
     * Update the items of the catItems and the selectedItems. This method sets 'fileModified' field
     * of TextFileManager to false as it is called whenever 'fileModified' is true.
     * @param context - activity or fragment
     * @return true if update occurs, false otherwise
     */
    public boolean updateLists(Context context) {
        TextFileManager.setExternalModify(false);
        ArrayList<ArrayList<ShoppingItem>> catAndSelectedItemsLists
                = TextFileManager.getCatalogAndSelectedItems(context);

        if (catAndSelectedItemsLists == null)
            return false;

        //index 0 holds catList, index 1 holds selectedItemsList
        setCatItems(catAndSelectedItemsLists.get(0));
        setSelectedItems(catAndSelectedItemsLists.get(1));

        //also update values of expandedCats
        for (int i = 0; i < expandedCats.length && expandedCats[i] != null; i++) {
            ShoppingItem expanded = expandedCats[i];
            ShoppingItem persistingCat = findPersistingCat(expanded);
            if (persistingCat != null) {
                expandedCats[i] = persistingCat;
            } else
                clearExpandedCatsRange(i);
        }

        return true;
    }

    public void buildCatalogView() {
        catAdapter.clear();
        ArrayList<ShoppingItem> permanentCats = new ArrayList<>();
        permanentCats.add(CatalogFragment.CAT_ROOT);
        permanentCats.addAll(catItems);
        catAdapter.addAll(0, permanentCats);

        int depth = 0;
        int position = 1;
        ArrayList<ShoppingItem> sameDepthCats = catItems;

        while (depth < expandedCats.length && expandedCats[depth] != null) {
            ShoppingItem nextExpanded = expandedCats[depth];
            ArrayList<ShoppingItem> children = nextExpanded.getCategoryItems();
            boolean expandedCatFound = false;
            for (int i = 0; i < sameDepthCats.size(); i++, position++) {
                if (sameDepthCats.get(i).equals(nextExpanded)) {
                    catAdapter.addRange(++position, children);
                    expandedCatFound = true;
                    break;
                }
            }
            if (expandedCatFound) {
                depth++;
                sameDepthCats = children;
            }
            else
                throw new AssertionError("buildCatalogView(): expandedCat does not exist!");


        }
    }

    public void addToCatalog(ShoppingItem itemToAdd, int parentPosition) {
        ShoppingItem parent = catAdapter.getItem(parentPosition);
        ArrayList<ShoppingItem> children;

        if (parent.equals(CatalogFragment.CAT_ROOT))
            children = catItems;
        else
            children = parent.getCategoryItems();

        children.add(itemToAdd);
        Collections.sort(children);

        if (!parent.equals(CatalogFragment.CAT_ROOT) && parent.getCategoryItems().size() == 1) {
            for (int i = 0; i < selectedItems.size(); i++) {
                if (selectedItems.get(i).equals(parent)) {
                    selectedItems.remove(i);
                    catAdapter.notifyItemChanged(parentPosition);
                    break;
                }
            }
        }

        //add to view
        catAdapter.addToParent(itemToAdd, parentPosition);

    }

    public void deleteFromCatalog(int posOfItemToDelete) {
        ShoppingItem item = catAdapter.getItem(posOfItemToDelete);
        int descendantCount = 0;

        ShoppingItem parent = item.getParentCategory();
        if (parent == null)
            catItems.remove(item);
        else {
            parent.getCategoryItems().remove(item);

            if (parent.getCategoryItems().size() == 0)
                clearExpandedCatsRange(parent.getDepth());
                catAdapter.notifyItemChanged(posOfItemToDelete-1);
        }

        if (item.equals(expandedCats[item.getDepth()])) {
            descendantCount = totalDisplayedDescendantsOfDepth(item.getDepth());
            clearExpandedCatsRange(item.getDepth());
        }

        catAdapter.delete(posOfItemToDelete, descendantCount);
    }

    public void editCatalog(int itemIndex, String newName) {
        ShoppingItem itemToEdit = catAdapter.getItem(itemIndex);
        itemToEdit.setName(newName);

        ShoppingItem parent = itemToEdit.getParentCategory();
        if (parent == null)
            Collections.sort(catItems);
        else
            Collections.sort(parent.getCategoryItems());

        Collections.sort(selectedItems);

        buildCatalogView();
    }

    //the following methods manipulate the catalog and what catItems the user sees

    /**
     * Called when a collapse and/or a expand is needed in the view. A collapse hides children of
     * selected item, and a expand reveals children of selected item.
     * @param selectedPosition - position in adapter of item selected
     */
    public void collapseAndExpand(int selectedPosition) {
        ShoppingItem selected = catAdapter.getItem(selectedPosition);
        int selectedDepth = selected.getDepth();
        ShoppingItem originalExpanded = expandedCats[selectedDepth];
        if (originalExpanded != null) {
            if (originalExpanded.equals(selected)) {
                //collapse, no expand
                collapse(selectedPosition);
            }
            else {
                int originalExpandedPosition = catAdapter.getPosition(originalExpanded);

                //collapse if able
                if (originalExpandedPosition != -1) {

                    int displayedChildren = collapse(originalExpandedPosition);

                    if (originalExpandedPosition < selectedPosition)
                        selectedPosition -= displayedChildren;
                }

                //always expand
                expand(selectedPosition);
            }
        }
        else {
            //expand, no collapse
            expand(selectedPosition);
        }
    }

    /**
     * Logic for collapsing a category displaying its children.
     * @param position - position of item to collapse.
     * @return - total items that were collapsed (hidden) in display.
     */
    private int collapse(int position) {
        ShoppingItem toCollapse = catAdapter.getItem(position);
        int depth = toCollapse.getDepth();

        int numOfDescendantsToCollapse = totalDisplayedDescendantsOfDepth(depth);
        clearExpandedCatsRange(depth);
        catAdapter.removeRange(position+1, numOfDescendantsToCollapse);
        catAdapter.notifyItemChanged(position);

        return numOfDescendantsToCollapse;
    }

    private void expand(int position) {
        ShoppingItem toExpand = catAdapter.getItem(position);
        int depth = toExpand.getDepth();
        expandedCats[depth] = toExpand;

        ArrayList<ShoppingItem> children = toExpand.getCategoryItems();
        catAdapter.addRange(position+1, children);
        catAdapter.notifyItemChanged(position);
    }

    /**
     * Determines how many descendants there are of expanded cat at given depth.
     * @param depth - cannot equal MAX_DEPTH of class ShoppingList
     * @return - total descendants to be removed from display
     */
    public int totalDisplayedDescendantsOfDepth(int depth) {
        ShoppingItem ancestor = expandedCats[depth];
        int descendantCount = 0;
        while(ancestor != null) {
            descendantCount += ancestor.getCategoryItems().size();
            depth++;
            if (depth == expandedCats.length)
                break;
            ancestor = expandedCats[depth];
        }

        return descendantCount;
    }

    /**
     * Used to find a cat in catItems field equiv to the cat argument. Mainly used to determine if
     * there is a cat equiv to the old cat in catItems after an update.
     * @param oldCat - The old cat; it is assumed to be non-null.
     * @return - Old cat equivalent in catList (so it is a persisting cat); if it does not exists,
     * the return is null
     */
    public ShoppingItem findPersistingCat(ShoppingItem oldCat) {
        ShoppingItem persistingCat = null;
        ShoppingItem ancestorCat = oldCat;
        Stack<ShoppingItem> ancestors = new Stack<>();
        ancestors.push(ancestorCat);


        while (ancestorCat.getParentCategory() != null) {
            ancestorCat = ancestorCat.getParentCategory();
            ancestors.push(ancestorCat);
        }

        ShoppingItem cat = null;
        for (int i = 0; i < catItems.size(); i++) {
            if (catItems.get(i).equals(ancestors.peek())) {
                cat = catItems.get(i);
                ancestors.pop();
                break;
            }
        }
        if (cat != null) {
            boolean tierCleared = true;
            while (tierCleared && !ancestors.isEmpty()) {
                tierCleared = false;
                ArrayList<ShoppingItem> catItems = cat.getCategoryItems();
                for (int i=0; i < catItems.size(); i++) {
                    if (ancestors.peek().getName()
                            .equals(catItems.get(i).getName())) {
                        tierCleared = true;
                        ancestors.pop();
                        cat = cat.getCategoryItems().get(i);
                        break;
                    }
                }
            }
            //old and new cat are only equiv if the following is true
            if (ancestors.isEmpty() && cat.getName().equals(oldCat.getName())) {
                persistingCat = cat;
            }
        }


        return persistingCat;
    }

    private void clearExpandedCatsRange(int start) {
        for (int i = start; i < expandedCats.length; i++) {
            expandedCats[i] = null;
        }
    }

    public boolean isExpandedCat(ShoppingItem item) {
        for (ShoppingItem expandedCat : expandedCats) {
            if (item.equals(expandedCat))
                return true;
        }

        return false;

    }

    // Getters and setters

    public ArrayList<ShoppingItem> getCatItems() {
        return catItems;
    }

    public void setCatItems(ArrayList<ShoppingItem> catItems) {
        this.catItems = catItems;
    }

    public ArrayList<ShoppingItem> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(ArrayList<ShoppingItem> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean isSelectedItemsFragmentActive() {
        return selectedItemsFragmentActive;
    }

    public void setSelectedItemsFragmentActive(boolean selectedItemsFragmentActive) {
        this.selectedItemsFragmentActive = selectedItemsFragmentActive;
    }

    public boolean isItemsParentFragmentActive() {
        return itemsParentFragmentActive;
    }

    public void setItemsParentFragmentActive(boolean itemsParentFragmentActive) {
        this.itemsParentFragmentActive = itemsParentFragmentActive;
    }

    public CatalogRecViewAdapter getCatAdapter() {
        return catAdapter;
    }

    public void setCatAdapter(CatalogRecViewAdapter catalogRecViewAdapter) {
        this.catAdapter = catalogRecViewAdapter;
    }

    public ShoppingItem[] getExpandedCats() {
        return expandedCats;
    }
}
