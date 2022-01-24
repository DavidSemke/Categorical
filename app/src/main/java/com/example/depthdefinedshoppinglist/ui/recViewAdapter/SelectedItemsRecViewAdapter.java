package com.example.depthdefinedshoppinglist.ui.recViewAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.depthdefinedshoppinglist.R;
import com.example.depthdefinedshoppinglist.domain.ShoppingItem;
import com.example.depthdefinedshoppinglist.ui.util.OnItemClickListener;

import java.util.List;
import java.util.Objects;

public class SelectedItemsRecViewAdapter extends RecyclerView.Adapter<SelectedItemsRecViewAdapter.ViewHolder> {

    private final OnItemClickListener onItemClickListener;
    private final List<ShoppingItem> selectedItems;

    public SelectedItemsRecViewAdapter(List<ShoppingItem> shoppingList,
                                       OnItemClickListener onItemClickListener) {
        this.selectedItems = shoppingList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public SelectedItemsRecViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rec_view_selected_item, parent, false);

        return new ViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedItemsRecViewAdapter.ViewHolder holder, int position) {
        ShoppingItem shoppingItem = Objects.requireNonNull(selectedItems).get(position);
        holder.itemName.setText(shoppingItem.getName());
    }

    @Override
    public int getItemCount() {
        return Objects.requireNonNull(selectedItems).size();
    }

    public void remove(int itemIndex) {
        selectedItems.remove(itemIndex);
        notifyItemRemoved(itemIndex);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemName;

        public ViewHolder(@NonNull View itemView, OnItemClickListener onSelectedItemClickListener) {
            super(itemView);
            itemName = itemView.findViewById(R.id.selected_item_name);

            itemView.setOnClickListener((View v) -> {
                onSelectedItemClickListener.onItemClick(getAdapterPosition());
            });
        }



    }
}
