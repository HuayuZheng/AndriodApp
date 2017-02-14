package com.example.s162077.helloworld;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by s162077 on 13-02-2017.
 */

public class TaskAdapter extends BaseAdapter implements ListAdapter {

    private final Context context;
    private final List<Coordinate> coordinates;

    public TaskAdapter(Context context, List<Coordinate> coordinates) {
        if (context == null) {  //context空是指什么?
            throw new IllegalArgumentException("Context must not be null.");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("List of tasks must not be null.");
        }
        this.context = context;
        this.coordinates = coordinates;
    }

    @Override
    public int getCount() {
        return this.coordinates.size();
    }

    @Override
    public Object getItem(int position) {
        return this.coordinates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return Adapter.IGNORE_ITEM_VIEW_TYPE;//什么用???
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {//ListView 加载数据
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //LayoutInflater class is used to instantiate layout XML file into its corresponding View objects.
            convertView = inflater.inflate(R.layout.read_from_sticker, parent, false);//resource,root,attchToRoot
            //specify a root view and to prevent attachment to the root.
        }
        TextView s = (TextView) convertView.findViewById(R.id.show);

        Coordinate c = this.coordinates.get(position);
        s.setText(String.format("The coordinates read from the Sticker are ( x =%f, y=%f, z=%f )",
                c.getX(),c.getY(),c.getZ()));

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.coordinates.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    /**
     * Add the given Task at the end of the list
     */
    public void add(Coordinate c) {
        this.coordinates.add(c);
        this.notifyDataSetChanged();
    }

    /**
     * Put the give Task at specified position
     */
    public void set(int position, Coordinate c) {
        this.coordinates.set(position,c);
        this.notifyDataSetChanged();
    }

    /**
     * Remove the Task at specified position
     */
    public void remove(int position) {
        this.coordinates.remove(position);
    }
}



}
