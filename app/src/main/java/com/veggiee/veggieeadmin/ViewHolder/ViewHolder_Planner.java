package com.veggiee.veggieeadmin.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.veggiee.veggieeadmin.Interface.ItemClickListener;
import com.veggiee.veggieeadmin.R;

public class ViewHolder_Planner extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView plannerId,plannerStatus,plannerPhoneNumber,plannerAddress, plannerFoodName;


    private ItemClickListener itemClickListener;


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ViewHolder_Planner(@NonNull View itemView) {
        super(itemView);

        plannerId=(TextView) itemView.findViewById(R.id.plannerId);
        plannerFoodName= itemView.findViewById(R.id.plannerFoodName);
        plannerStatus=(TextView) itemView.findViewById(R.id.plannerStatus);
        plannerPhoneNumber=(TextView) itemView.findViewById(R.id.plannerPhoneNumber);
        plannerAddress=(TextView) itemView.findViewById(R.id.plannerAddress);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}