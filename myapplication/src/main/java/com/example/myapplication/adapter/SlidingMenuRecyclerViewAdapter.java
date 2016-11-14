package com.example.myapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.entity.SlidingMenuItem;

import java.util.List;


public class SlidingMenuRecyclerViewAdapter extends RecyclerView.Adapter<SlidingMenuRecyclerViewAdapter.ViewHolder>
{

    List<SlidingMenuItem> menuList;
    Context context;
    LayoutInflater layoutInflater;

    public SlidingMenuRecyclerViewAdapter(List<SlidingMenuItem> menuList, Context context)
    {
        this.menuList = menuList;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View row = layoutInflater.inflate(R.layout.item_slidingmenu, parent, false);
        ViewHolder viewHolder = new ViewHolder(row);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position)
    {
        SlidingMenuItem item = menuList.get(position);

        holder.title.setText(item.title);
        holder.icon.setImageResource(item.icon);
    }

    @Override
    public int getItemCount()
    {
        return menuList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        ImageView icon;
        TextView title;

        public ViewHolder(View itemView)
        {
            super(itemView);

            icon = (ImageView) itemView.findViewById(R.id.item_slidingmenu_icon);
            title = (TextView) itemView.findViewById(R.id.item_slidingmenu_title);
        }
    }
}
