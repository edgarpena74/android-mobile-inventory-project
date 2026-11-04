//-----------------------------
//-----------------------------
// INVENTORY GRID ADAPTER
//-----------------------------
//-----------------------------

// Binds ItemRecord rows to grid cells and exposes stable IDs for selection.
// Also computes a simple status color and label based on quantity.
// Extra display rule:
// - If amount == 0, we show item name as "Not Available" and status "Out of Stock".
//-----------------------------

package com.example.cs360projectthreeedgarpena.adapters;

//-----------------------------
// IMPORTS
//-----------------------------
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.cs360projectthreeedgarpena.R;
import com.example.cs360projectthreeedgarpena.repository.InventoryRepository.ItemRecord;

import java.util.List;

//-----------------------------
// CLASS DEFINITION
//-----------------------------
public class InventoryGridAdapter extends BaseAdapter {

    // Android context for inflating layouts
    private final Context context;

    // Data backing the grid
    private final List<ItemRecord> items;

    // Inflater for row layouts
    private final LayoutInflater inflater;

    //-----------------------------
    // CONSTRUCTOR
    //-----------------------------
    // Store references and prepare inflater
    public InventoryGridAdapter(Context context, List<ItemRecord> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    //-----------------------------
    // GET COUNT
    //-----------------------------
    // Total cells to render
    @Override
    public int getCount() {
        return (items == null) ? 0 : items.size();
    }

    //-----------------------------
    // GET ITEM
    //-----------------------------
    // Returns the ItemRecord for a given position (used by onItemClick in InventoryActivity)
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    //-----------------------------
    // GET ITEM ID
    //-----------------------------
    // Returns the database id for stable selection/animations
    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    //-----------------------------
    // HAS STABLE IDS
    //-----------------------------
    // Tell GridView that ids are stable to improve selection behavior
    @Override
    public boolean hasStableIds() {
        return true;
    }

    //-----------------------------
    // GET VIEW
    //-----------------------------
    // Inflates/binds each cell in the grid.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // ViewHolder pattern to reuse row views
        ViewHolder holder;
        if (convertView == null) {
            // Inflate the cell layout
            convertView = inflater.inflate(R.layout.cell_inventory_item, parent, false);

            // Create holder and bind views
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            // Reuse existing holder
            holder = (ViewHolder) convertView.getTag();
        }

        // Grab the item for this row
        ItemRecord item = items.get(position);

        // ----------------------------
        // DISPLAY NAME LOGIC
        // ----------------------------
        // If amount is 0, show "Not Available" instead of the actual item name.
        // Otherwise show the real item name.
        String displayName;
        if (item.amount == 0) {
            displayName = "Not Available";
        } else {
            displayName = item.name;
        }

        // Bind item name text
        holder.textViewName.setText(displayName);

        // Bind amount text (always show the numeric amount)
        holder.textViewAmount.setText("Amount: " + item.amount);

        // Bind price text (formatted to 2 decimals)
        holder.textViewPrice.setText("Price: $" + String.format("%.2f", item.price));

        // Bind location text with fallback to "Not Available"
        holder.textViewLocation.setText(
                "Location: " + (item.location != null && !item.location.isEmpty()
                        ? item.location
                        : "Not Available")
        );

        // ----------------------------
        // STATUS COLOR + LABEL
        // ----------------------------
        // amount == 0 -> gray dot, "Out of Stock"
        // amount <= 2 (but >0) -> red dot, "Low"
        // amount <= 5 -> yellow dot, "Medium"
        // else -> green dot, "In Stock"

        int colorResId;
        String statusText;

        if (item.amount == 0) {
            // Out of stock
            colorResId = R.color.status_gray;
            statusText = "Out of Stock";
        } else if (item.amount <= 2) {
            // Critically low
            colorResId = R.color.status_red;
            statusText = "Low";
        } else if (item.amount <= 5) {
            // Medium
            colorResId = R.color.status_yellow;
            statusText = "Medium";
        } else {
            // Plenty in stock
            colorResId = R.color.status_green;
            statusText = "In Stock";
        }

        // Apply dot color
        holder.viewStatusDot.setBackgroundResource(colorResId);

        // Apply text label under/next to dot
        holder.textViewStatusText.setText(statusText);

        // return the prepared row
        return convertView;
    }

    //-----------------------------
    // VIEW HOLDER
    //-----------------------------
    // Caches row subviews for smoother scrolling.
    private static class ViewHolder {
        final TextView textViewName;
        final TextView textViewAmount;
        final TextView textViewPrice;
        final TextView textViewLocation;
        final View     viewStatusDot;
        final TextView textViewStatusText;

        // Bind each subview by id so we don't keep calling findViewById in getView()
        ViewHolder(View root) {
            textViewName       = root.findViewById(R.id.textViewName);
            textViewAmount     = root.findViewById(R.id.textViewAmount);
            textViewPrice      = root.findViewById(R.id.textViewPrice);
            textViewLocation   = root.findViewById(R.id.textViewLocation);
            viewStatusDot      = root.findViewById(R.id.viewStatusDot);
            textViewStatusText = root.findViewById(R.id.textViewStatusText);
        }
    }
}