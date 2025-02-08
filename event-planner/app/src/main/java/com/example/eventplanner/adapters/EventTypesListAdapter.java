package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventplanner.R;
import com.example.eventplanner.model.EventType;

import java.util.List;

public class EventTypesListAdapter extends BaseAdapter {
    private List<EventType> itemList;
    private LayoutInflater inflater;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public EventTypesListAdapter(Context context, List<EventType> itemList) {
        this.itemList = itemList;
        inflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.event_types_layout, parent, false);
            holder = new ViewHolder();
            holder.itemName = convertView.findViewById(R.id.item_name);
            holder.editIcon = convertView.findViewById(R.id.edit_icon);
            holder.deleteIcon = convertView.findViewById(R.id.delete_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.itemName.setText(itemList.get(position).getTypeName());

        holder.editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEditClick(position);
                }
            }
        });

        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView itemName;
        ImageView editIcon;
        ImageView deleteIcon;
    }
}
