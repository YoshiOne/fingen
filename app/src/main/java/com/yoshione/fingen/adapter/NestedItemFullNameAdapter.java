package com.yoshione.fingen.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.yoshione.fingen.model.AutocompleteItem;

import java.util.ArrayList;
import java.util.List;

public class NestedItemFullNameAdapter extends ArrayAdapter<AutocompleteItem> {
    private List<AutocompleteItem> suggestions;
    private List<AutocompleteItem> itemsAll;
    private Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            return ((AutocompleteItem) (resultValue)).getFullName();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (AutocompleteItem item : itemsAll) {
                    if (item.getFullName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(item);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            List<AutocompleteItem> filteredList = (List<AutocompleteItem>) results.values;
            if (results.count > 0) {
                clear();
                for (AutocompleteItem c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };

    public NestedItemFullNameAdapter(Context context, int textViewResourceId, List<AutocompleteItem> items) {
        super(context, textViewResourceId, items);
        itemsAll = new ArrayList<>();
        itemsAll.addAll(items);
        suggestions = new ArrayList<>();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return nameFilter;
    }
}
