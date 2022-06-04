package com.example.depthdefinedshoppinglist.ui.recViewAdapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.domain.ShoppingItem;
import com.example.depthdefinedshoppinglist.ui.fragment.CatalogFragment;
import com.example.depthdefinedshoppinglist.ui.util.OnItemClickListener;
import com.example.depthdefinedshoppinglist.ui.viewModel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CatalogRecViewAdapter extends RecyclerView.Adapter<CatalogRecViewAdapter.ViewHolder> {
    //measured in dp (density-independent pixels)
    public static final int indentationSize = 50;

    private final OnItemClickListener onItemClickListener;
    private final List<ShoppingItem> displayedCatItems;
    private final MainViewModel mainViewModel;
    private final Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
    private final Typeface normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);

    public CatalogRecViewAdapter(List<ShoppingItem> catList,
                                 OnItemClickListener onItemClickListener,
                                 MainViewModel mainViewModel) {
        this.displayedCatItems = catList;
        this.onItemClickListener = onItemClickListener;
        this.mainViewModel = mainViewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rec_view_cat_item, parent, false);

        return new ViewHolder(view, onItemClickListener);
    }

    public void onBindViewHolder(@NonNull CatalogRecViewAdapter.ViewHolder holder, int position) {
        ShoppingItem shoppingItem = Objects.requireNonNull(displayedCatItems).get(position);

        holder.expandSymbol.setVisibility(View.GONE);
        holder.checkmark.setVisibility(View.GONE);
        holder.collapseSymbol.setVisibility(View.GONE);
        holder.itemName.setTypeface(normalTypeface);
        holder.itemView.setPadding(0, 0, 0, 0);

        //exclude cat root item
        if (position != 0) {
            //determine the number of indents an item bears
            int itemIndent = (shoppingItem.getDepth()) * indentationSize;
            holder.itemView.setPadding(itemIndent, 0, 0, 0);

            if (mainViewModel.isExpandedCat(shoppingItem)) {
                holder.itemName.setTypeface(boldTypeface);
                holder.collapseSymbol.setVisibility(View.VISIBLE);
            }
            else if (!shoppingItem.getCategoryItems().isEmpty()) {
                holder.expandSymbol.setVisibility(View.VISIBLE);
            }
            else if (mainViewModel.getSelectedItems().contains(shoppingItem)) {
                holder.checkmark.setVisibility(View.VISIBLE);
            }

        }
        else
            holder.itemName.setTypeface(boldTypeface);

        holder.itemName.setText(shoppingItem.getName());


    }

    @Override
    public int getItemCount() {
        return Objects.requireNonNull(displayedCatItems).size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        displayedCatItems.clear();
        notifyDataSetChanged();
    }

    public void removeRange(int start, int itemCount) {
        displayedCatItems.subList(start, start+itemCount).clear();
        notifyItemRangeRemoved(start, itemCount);
    }

    /**
     * Add an array of items to displayedCatItems starting at index start.
     * @param start - starting index for insertions
     * @param items - items to insert - assumed to be alphabetically sorted.
     */
    public void addRange(int start, ArrayList<ShoppingItem> items) {
        for (int i = items.size()-1; i >= 0; i--) {
            displayedCatItems.add(start, items.get(i));
        }
        notifyItemRangeInserted(start, items.size());
    }

    public void addToParent(ShoppingItem item, int parentPosition) {
        ShoppingItem parent = getItem(parentPosition);
        int firstPosition = parentPosition + 1;

        //childCount should always be >=1 if this method is called
        int childCount;
        if (parent.equals(CatalogFragment.CAT_ROOT))
            childCount = mainViewModel.getCatItems().size();
        else
            childCount = parent.getCategoryItems().size();

        if (childCount == 1) {
            displayedCatItems.add(firstPosition, item);
            notifyItemInserted(firstPosition);
        }
        else {
            ShoppingItem next = displayedCatItems.get(firstPosition);
            int depthOfAdd = item.getDepth();
            int currPos = firstPosition;
            while (item.compareTo(next) > 0 && depthOfAdd == next.getDepth()) {
                if (mainViewModel.isExpandedCat(next)) {
                    int displayedDescendants =
                            mainViewModel.totalDisplayedDescendantsOfDepth(next.getDepth());
                    currPos += displayedDescendants;
                }
                currPos++;
                if (currPos == displayedCatItems.size()) {
                    break;
                }
                next = displayedCatItems.get(currPos);
            }

            displayedCatItems.add(currPos, item);
            notifyItemInserted(currPos);
        }
    }

    public void addAll(int start, ArrayList<ShoppingItem> itemList) {
        displayedCatItems.addAll(itemList);
        notifyItemRangeInserted(start, itemList.size());
    }

    public void delete(int itemIndex, int totalDisplayedDescendants) {
        ShoppingItem itemToDelete = getItem(itemIndex);
        removeRange(itemIndex, totalDisplayedDescendants+1);

        ShoppingItem parent = itemToDelete.getParentCategory();
        if (parent != null && parent.getCategoryItems().size() == 0)
            //parent has no children, and must not be viewed as a selectable item
            notifyItemChanged(itemIndex-1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemName;
        private final ImageView checkmark;
        private final ImageView expandSymbol;
        private final ImageView collapseSymbol;

        public ViewHolder(@NonNull View itemView, OnItemClickListener onCatItemClickListener) {
            super(itemView);
            itemName = itemView.findViewById(R.id.cat_item_name);
            checkmark = itemView.findViewById(R.id.cat_item_checkmark);
            expandSymbol = itemView.findViewById(R.id.cat_item_expand_symbol);
            collapseSymbol = itemView.findViewById(R.id.cat_item_collapse_symbol);

            itemName.setOnClickListener((View v) ->
                    onCatItemClickListener.onItemClick(getAdapterPosition()));
        }

    }

    public ShoppingItem getItem(int position) {
        return displayedCatItems.get(position);
    }

    public int getPosition(ShoppingItem item) {
        final int fail = -1;
        int soughtDepth = item.getDepth();
        if (soughtDepth == -1)
            return 0;
        else if (getItemCount() == 1)
            return fail;

        //parent initially cat root
        ShoppingItem next;
        int currDepth;
        int position = 1;
        for (; position < displayedCatItems.size(); position++) {
            next = displayedCatItems.get(position);
            currDepth = next.getDepth();
            if (currDepth == soughtDepth)
                break;
        }

        while (position < displayedCatItems.size()) {
            ShoppingItem possMatch = displayedCatItems.get(position);
            if (possMatch.getDepth() < soughtDepth)
                return fail;
            else if (possMatch.getDepth() > soughtDepth) {
                ShoppingItem expandedCat = possMatch.getParentCategory();
                int skip = mainViewModel.totalDisplayedDescendantsOfDepth(expandedCat.getDepth());
                position += skip;
                continue;
            }
            else if (item.equals(possMatch))
                return position;

            position++;
        }

        return fail;
    }

    public List<ShoppingItem> getDisplayedCatItems() {
        return displayedCatItems;
    }
}
