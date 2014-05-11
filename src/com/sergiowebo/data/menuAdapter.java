package com.sergiowebo.data;

import com.sergiowebo.R;
import com.sergiowebo.R.id;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class menuAdapter extends ArrayAdapter<menuItem>{

    Context context; 
    int layoutResourceId;    
    menuItem data[] = null;
    
    public menuAdapter(Context context, int layoutResourceId, menuItem[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        menuHolder holder = null;
        
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new menuHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            
            row.setTag(holder);
        } else {
            holder = (menuHolder)row.getTag();
        }
        
        menuItem mItem = data[position];
        holder.txtTitle.setText(mItem.title);
        holder.imgIcon.setImageResource(mItem.icon);
        
        return row;
    }
    
    static class menuHolder {
        ImageView imgIcon;
        TextView txtTitle;
    }
}