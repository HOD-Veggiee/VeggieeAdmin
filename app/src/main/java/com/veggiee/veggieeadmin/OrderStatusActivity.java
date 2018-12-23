package com.veggiee.veggieeadmin;

import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.veggiee.veggieeadmin.Model.MyResponse;
import com.veggiee.veggieeadmin.Model.Notification;
import com.veggiee.veggieeadmin.Model.Request;
import com.veggiee.veggieeadmin.Model.Sender;
import com.veggiee.veggieeadmin.Model.Token;
import com.veggiee.veggieeadmin.Remote.APIService;
import com.veggiee.veggieeadmin.ViewHolder.OrderViewHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatusActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    TextView emptyOrderText;
    LinearLayoutManager mLinearLayout;

    //Firebase
    FirebaseDatabase mDatabase;
    DatabaseReference requests;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    //this list will be shown to user for food list when clicked on a specific order
    Map<String,Request> previousOrdersDetailList=new HashMap<>();

    MaterialSpinner spinner;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //init Service
        mService = Common.getFCMClient();

        //init views
        mRecyclerView=(RecyclerView) findViewById(R.id.ordersListRecyclerView);
        emptyOrderText = (TextView) findViewById(R.id.emptyOrderText);
        mLinearLayout=new LinearLayoutManager (this);
        mRecyclerView.setLayoutManager(mLinearLayout);

        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        requests=mDatabase.getReference("Request");
        
        loadOrders();        
    }

    private void loadOrders() {

        FirebaseRecyclerOptions<Request> options=new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(requests,Request.class).build();


        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final OrderViewHolder holder, int position, @NonNull final Request model) {

                holder.orderId.setText(adapter.getRef(position).getKey());
                holder.orderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                holder.orderPhoneNumber.setText(model.getPhone());
                holder.orderAddress.setText(model.getAddress());

                previousOrdersDetailList.put(
                        holder.orderId.getText().toString(),
                        new Request(
                                model.getPhone(),
                                model.getAddress(),
                                model.getTotal(),
                                model.getFoods()
                        )
                );

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Request currentOrder=new Request();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            currentOrder.setFoods(Objects.requireNonNull(previousOrdersDetailList.getOrDefault(holder.orderId.getText().toString(), null)).getFoods());
                        }
                        else
                        {
                            currentOrder.setFoods(previousOrdersDetailList.get(holder.orderId.getText().toString()).getFoods());
                        }

                        currentOrder.setTotal(model.getTotal());

                        AlertDialog.Builder foodListDialog=new AlertDialog.Builder(OrderStatusActivity.this);
                        foodListDialog.setTitle("Order Items List");
                        foodListDialog.setIcon(R.drawable.ic_cart);

                        StringBuilder foods= new StringBuilder();

                        for(int i=0; i<currentOrder.getFoods().size();i++)
                        {
                            foods.append(
                                    currentOrder.getFoods().get(i).getProductName()
                                            +" x "
                                            +currentOrder.getFoods().get(i).getQuantity()
                                            +"\t( Rs "+currentOrder.getFoods().get(i).getPrice()+" )").append("\n");
                        }



                        foods.append("\nTotal Amount: Rs ").append(currentOrder.getTotal()).append("-/");
                        foodListDialog.setMessage(foods.toString());


                        foodListDialog.setNeutralButton("Okay",null);


                        foodListDialog.show();
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_layout,viewGroup,false);
                return new OrderViewHolder(view);
            }
        };

        mRecyclerView.setAdapter(adapter);

        requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    emptyOrderText.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE))
        {
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
            Toast.makeText(OrderStatusActivity.this, "Update", Toast.LENGTH_LONG).show();
        }
        else if (item.getTitle().equals(Common.DELETE))
        {
            Toast.makeText(OrderStatusActivity.this, "Delete", Toast.LENGTH_LONG).show();
            deleteOrder(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void deleteOrder(String key) {
        requests.child(key).removeValue();

        requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren())
                    emptyOrderText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showUpdateDialog(String key, final Request item) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatusActivity.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Pleases choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout, null);

        spinner = (MaterialSpinner) view.findViewById(R.id.statusSpinner);
        spinner.setItems("Order placed.", "In Process.", "On way.");

        alertDialog.setView(view);

        final String localKey = key;

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                requests.child(localKey).setValue(item);

                sendOrderStatusToUser(localKey, item);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void sendOrderStatusToUser(final String key,final Request item) {
        DatabaseReference tokens = mDatabase.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Token token = postSnapshot.getValue(Token.class);

                            //Create raw payload to send
                            Notification notification = new Notification("Veggie", "You order # " + key + " is '" + Common.convertCodeToStatus(item.getStatus()) + "'");
                            Sender content = new Sender(token.getToken(), notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.body().success == 1)
                                            {
                                                Toast.makeText(OrderStatusActivity.this,"Order was updated",Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                            else
                                                Toast.makeText(getApplicationContext(),"Order was updated but Failed to send notification !!!",Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        adapter.stopListening();
    }
}
