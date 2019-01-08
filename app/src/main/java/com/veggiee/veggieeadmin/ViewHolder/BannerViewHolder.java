package com.veggiee.veggieeadmin.ViewHolder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.veggiee.veggieeadmin.Common.Common;
import com.veggiee.veggieeadmin.Interface.ItemClickListener;
import com.veggiee.veggieeadmin.R;
import com.veggiee.veggieeadmin.Utility.SquareImage;

public class BannerViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener
{

    public ImageView banner_image;
    public TextView banner_name;

    public ItemClickListener itemClickListener;


    public BannerViewHolder(@NonNull View itemView) {
        super(itemView);

        banner_image = itemView.findViewById(R.id.banner_image);
        banner_name = itemView.findViewById(R.id.banner_name);

        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select the action");
        contextMenu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0, 1, getAdapterPosition(), Common.DELETE);
    }
}
