package com.veggiee.veggieeadmin;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.veggiee.veggieeadmin.Common.Common;
import com.veggiee.veggieeadmin.Interface.ItemClickListener;
import com.veggiee.veggieeadmin.Model.Planner;
import com.veggiee.veggieeadmin.Remote.APIService;
import com.veggiee.veggieeadmin.ViewHolder.ViewHolder_Planner;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlannerActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayout;

    //Firebase
    FirebaseDatabase mDatabase;
    DatabaseReference planners;

    TextView emptyPlannerText,itemPriceTxt,weeklyBillTxt, headingTxtView, foodNameTxtView;
    MaterialSpinner deliveryTimeSpinner;
    int[] weeklyBill = new int[8];

    FirebaseRecyclerAdapter<Planner,ViewHolder_Planner> adapter;

    //this list will be shown to user for Planner list when clicked on a specific planner
    Map<String,Planner> previousPlannersDetailList=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner);

        //init views
        emptyPlannerText = (TextView)findViewById(R.id.emptyPlannerText);
        mRecyclerView=(RecyclerView) findViewById(R.id.plannersListRecyclerView);
        mLinearLayout=new LinearLayoutManager (this);
        mRecyclerView.setLayoutManager(mLinearLayout);

        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        planners=mDatabase.getReference("Planner");


        if (Common.isConnectedToInternet(getBaseContext()))
            loadPlanners();
        else
            Toast.makeText(this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
    }

    private void loadPlanners() {

        Query query = planners.orderByChild("status").equalTo("subscribed");

        FirebaseRecyclerOptions<Planner> options=new FirebaseRecyclerOptions.Builder<Planner>()
                .setQuery(query,Planner.class).build();

        adapter=new FirebaseRecyclerAdapter<Planner, ViewHolder_Planner>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder_Planner holder, int position, @NonNull final Planner model) {

                holder.plannerId.setText(adapter.getRef(position).getKey());
                holder.plannerFoodName.setText("Name: " + model.getFoodName().toUpperCase());
                holder.plannerStatus.setText("Status: " + model.getStatus().toUpperCase());
                holder.plannerPhoneNumber.setText(model.getPhone());
                holder.plannerAddress.setText(model.getAddress());

                previousPlannersDetailList.put(
                        holder.plannerId.getText().toString(),
                        new Planner(
                                model.getAddress(),
                                model.getFoodId(),
                                model.getFoodName(),
                                model.getFoodPrice(),
                                model.getPhone(),
                                model.getTotalWeeklyBill(),
                                model.getStartDate(),
                                model.getCreatedDate(),
                                model.getDeliveryTimeSlot(),
                                model.getDays(),
                                model.getStatus(),
                                model.getPhone_status()
                        )
                );

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Planner currentPlanner=new Planner();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            currentPlanner.setDays(Objects.requireNonNull(previousPlannersDetailList.getOrDefault(holder.plannerId.getText().toString(), null)).getDays());
                        }
                        else
                        {
                            currentPlanner.setDays(previousPlannersDetailList.get(holder.plannerId.getText().toString()).getDays());
                        }

                        currentPlanner.setTotalWeeklyBill(model.getTotalWeeklyBill());

                        AlertDialog.Builder plannerListDialog=new AlertDialog.Builder(PlannerActivity.this);
                        plannerListDialog.setTitle("Planner Items List");
                        plannerListDialog.setIcon(R.drawable.ic_event_available_black_24dp);

                        StringBuilder planners= new StringBuilder();

                        if(!currentPlanner.getDays().isEmpty())
                            for(int i=0; i<currentPlanner.getDays().size();i++)
                            {
                                planners.append(
                                        currentPlanner.getDays().get(i).getName()
                                                +" x "
                                                +currentPlanner.getDays().get(i).getQuantity()
                                                +"\t( Rs "+currentPlanner.getDays().get(i).getPerDayBill()+" )").append("\n");
                            }
                        else
                            Toast.makeText(PlannerActivity.this, "This Planner is empty!!!", Toast.LENGTH_SHORT).show();



                        planners.append("\nTotal Weekly/Bill: Rs ").append(currentPlanner.getTotalWeeklyBill()).append("-/");
                        plannerListDialog.setMessage(planners.toString());


                        plannerListDialog.setNeutralButton("Okay",null);


                        plannerListDialog.show();
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolder_Planner onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.planner_item_view,viewGroup,false);
                return new ViewHolder_Planner(view);
            }
        };

        mRecyclerView.setAdapter(adapter);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    emptyPlannerText.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Common.isConnectedToInternet(getBaseContext()))
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Common.isConnectedToInternet(getBaseContext()))
            adapter.stopListening();
    }
}
