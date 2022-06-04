package com.example.depthdefinedshoppinglist.domain;

import java.util.ArrayList;
import java.util.Stack;

public class ShoppingItem implements Comparable<ShoppingItem> {
    public static final int MAX_DEPTH = 1;
    public static final int MAX_NAME_LENGTH = 14;

    private ShoppingItem parentCategory;
    private final ArrayList<ShoppingItem> categoryItems = new ArrayList<>();
    private String name;
    private final int depth;


    public ShoppingItem(ShoppingItem parentCategory, String name) {
        this.parentCategory = parentCategory;
        this.name = name;
        if (parentCategory == null) {
            depth = 0;
        }
        else {
            depth = parentCategory.depth + 1;

            if (depth > MAX_DEPTH)
                throw new AssertionError("New item " + this.name +
                        " exceeded max depth with depth " + depth);
        }

    }

    //for the displayed cat root item
    public ShoppingItem(String name) {
        this.name = name;
        this.depth = -1;
    }

    /**
     * Search for the catalog items among the descendants of 'this'.
     * @param cats - catalog items to be searched for
     * @return - arraylist of all catalog items among those in the cats arg that were found
     */
    public ArrayList<ShoppingItem> findCats(ArrayList<ShoppingItem> cats) {
        if (cats == null || cats.isEmpty())
            return null;

        //check if 'this' is a cat to be found
        ArrayList<ShoppingItem> catsFound = new ArrayList<>();
        for (int i = 0; i < cats.size(); i++) {
            if (this.equals(cats.get(i))) {
                catsFound.add(cats.get(i));
                cats.remove(cats.get(i));
                break;
            }
        }

        //check tree rooted at 'this' to see if other cats to be found are present
        ShoppingItem item = this;
        Stack<Integer> tierIndex = new Stack<>();
        tierIndex.push(0);
        boolean searchIncomplete = true;
        while(searchIncomplete) {
            if (item.getCategoryItems().size() == tierIndex.peek()) {
                if (item.equals(this)) {
                    searchIncomplete = false;
                }
                else {
                    item = item.getParentCategory();
                    tierIndex.pop();
                    tierIndex.push(tierIndex.pop() + 1);
                }

            }
            else {
                item = item.getCategoryItems().get(tierIndex.peek());
                for (int i = 0; i < cats.size(); i++) {
                    if (item.equals(cats.get(i))) {
                        catsFound.add(cats.get(i));
                        cats.remove(cats.get(i));
                        if (cats.size() == 0)
                            searchIncomplete = false;
                        break;
                    }
                }
                tierIndex.push(0);
            }
        }

        return catsFound;
    }

    /**
     *
     * @param item - any ShoppingItem object
     * @return - a negative number if 'this' object comes before object 'o' alphabetically
     */
    @Override
    public int compareTo(ShoppingItem item) {
        return name.compareTo(item.getName());
    }

    @Override
    public boolean equals(Object o) {
        return o != null
                && this.getClass() == o.getClass()
                && this.equivAncestors((ShoppingItem) o);
    }

    /**
     * @param item - assumed to be non-null
     * @return true if each pair of ancestors (including item and 'this') at each height are equiv
     */
    private boolean equivAncestors(ShoppingItem item) {
        ShoppingItem item1 = item;
        ShoppingItem item2 = this;
        while (item1.getName().equals(item2.getName())) {
            item1 = item1.getParentCategory();
            item2 = item2.getParentCategory();
            if (item1 == null && item2 == null)
                return true;
            if (item1 == null || item2 == null)
                break;
        }

        return false;
    }

    public ShoppingItem getParentCategory() {
        return parentCategory;
    }

    public ArrayList<ShoppingItem> getCategoryItems() {
        return categoryItems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepth() {
        return depth;
    }
}
