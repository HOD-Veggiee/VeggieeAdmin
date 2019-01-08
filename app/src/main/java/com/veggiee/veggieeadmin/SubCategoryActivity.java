package com.veggiee.veggieeadmin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.veggiee.veggieeadmin.Common.Common;
import com.veggiee.veggieeadmin.Interface.ItemClickListener;
import com.veggiee.veggieeadmin.Model.Food;
import com.veggiee.veggieeadmin.Model.SubCategory;
import com.veggiee.veggieeadmin.ViewHolder.FoodViewHolder;
import com.veggiee.veggieeadmin.ViewHolder.SubCategoryViewHolder;

import java.util.UUID;

import io.paperdb.Paper;

public class SubCategoryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recycler_sub_category;
    TextView emptySubCategoryText;
    LinearLayoutManager mLayoutManager;
    FloatingActionButton fab;

    RelativeLayout rootLayout;
    NavigationView navigationView;
    DrawerLayout drawer;
    TextView txtFullName;

    // Firebase
    FirebaseDatabase db;
    DatabaseReference subCategoryList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId = "";

    FirebaseRecyclerAdapter<SubCategory, SubCategoryViewHolder> adapter;

    AppCompatEditText edtName;
    AppCompatButton btnSelect, btnUpload;

    SubCategory newSubCategory;

    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);

        // init Paper
        Paper.init(this);

        // Navigation
        navigationView = findViewById(R.id.nav_view);
        if (Common.currentStaff.getRoll().equals("admin"))
            showItemAddStaff();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("categoryName").toUpperCase());
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Set Name for Staff

        View headerView = navigationView.getHeaderView(0);
        txtFullName = headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentStaff.getName());


        // Firebase

        db = FirebaseDatabase.getInstance();
        subCategoryList = db.getReference("SubCategory");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Init

        recycler_sub_category= findViewById(R.id.SubCategoryListRecyclerView);
        emptySubCategoryText = findViewById(R.id.emptySubCategoryText);
        mLayoutManager=new LinearLayoutManager(this);
        recycler_sub_category.setLayoutManager(new GridLayoutManager(this,2));

        rootLayout = findViewById(R.id.rootLayout);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddSubCategoryDialog();
            }
        });

        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("categoryId");

        if(!categoryId.isEmpty())
            loadSubCategoryList(categoryId);
    }

    private void showAddSubCategoryDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SubCategoryActivity.this);
        alertDialog.setTitle("Add new Sub Category");
        alertDialog.setMessage("Please enter new Sub Category details!");

        LayoutInflater layoutInflater = this.getLayoutInflater();
        View add_menu_layout = layoutInflater.inflate(R.layout.add_new_menu_layout, null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        // Event for Button

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); // Let user choose image from gallery , save ui in DB
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_cart);

        // Set Button
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if(newSubCategory != null)
                {
                    subCategoryList.push().setValue(newSubCategory);

                    subCategoryList.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren())
                                emptySubCategoryText.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Snackbar.make(rootLayout, "New Sub Category " + newSubCategory.getName() + " was added", Snackbar.LENGTH_SHORT).show();
                }
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

    private void loadSubCategoryList(String categoryId) {


        FirebaseRecyclerOptions<SubCategory> options = new FirebaseRecyclerOptions.Builder<SubCategory>()
                .setQuery(subCategoryList.orderByChild("categoryId").equalTo(categoryId), SubCategory.class).build();

        //getting data from Firebase
        adapter=new FirebaseRecyclerAdapter<SubCategory, SubCategoryViewHolder>(options) {
            @NonNull
            @Override
            public SubCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sub_category_item,viewGroup,false);
                return new SubCategoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull SubCategoryViewHolder holder, int position, @NonNull final SubCategory model) {
                holder.sub_category_name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.sub_category_image);

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //get sub_category id and send it to foodlist activity to get foodlist of specific sub_category
                        Intent foodListIntent=new Intent(SubCategoryActivity.this, FoodListActivity.class);
                        foodListIntent.putExtra("subCategoryId",adapter.getRef(position).getKey());
                        foodListIntent.putExtra("subCategoryName",adapter.getItem(position).getName());
                        startActivity(foodListIntent);
                    }
                });
            }
        };

        recycler_sub_category.setAdapter(adapter);

        subCategoryList.orderByChild("categoryId").equalTo(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    emptySubCategoryText.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void uploadImage() {
        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(SubCategoryActivity.this, "Uploaded!", Toast.LENGTH_LONG).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            newSubCategory = new SubCategory();
                            newSubCategory.setName(edtName.getText().toString());
                            newSubCategory.setCategoryId(categoryId);
                            newSubCategory.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(SubCategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded "+progress+"%");
                }
            });
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    private void changeImage(final SubCategory item) {
        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(SubCategoryActivity.this, "Uploaded!", Toast.LENGTH_LONG).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            item.setImage(uri.toString());

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(SubCategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded "+progress+"%");
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            saveUri = data.getData();
            btnSelect.setText("Image Selected!");
        }
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE))
        {
            showUpdateSubCategoryDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.DELETE))
        {
            deleteSubCategory(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void showUpdateSubCategoryDialog(final String key, final SubCategory item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SubCategoryActivity.this);
        alertDialog.setTitle("Update new Sub Category");
        alertDialog.setMessage("Please enter Sub Category details!");

        LayoutInflater layoutInflater = this.getLayoutInflater();
        View add_menu_layout = layoutInflater.inflate(R.layout.add_new_menu_layout, null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        // Set default values

        edtName.setText(item.getName());

        // Event for Button

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); // Let user choose image from gallery , save ui in DB
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_cart);

        // Set Button
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                // Update Information

                item.setName(edtName.getText().toString());

                subCategoryList.child(key).setValue(item);

                Snackbar.make(rootLayout, "Sub Category " + item.getName() + " was updated", Snackbar.LENGTH_SHORT).show();
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

    private void deleteSubCategory(String key) {

        // Get all foods in sub category
        DatabaseReference foods = db.getReference("Food");

        Query foodInCategory = foods.orderByChild("menuId").equalTo(key);

        foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    postSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        subCategoryList.child(key).removeValue();

        subCategoryList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren())
                    emptySubCategoryText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toast.makeText(SubCategoryActivity.this, "Sub Category Deleted !", Toast.LENGTH_LONG).show();
    }


    // Navigation
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent orders = new Intent(SubCategoryActivity.this, OrderStatusActivity.class);

        switch (id)
        {
            case R.id.nav_pending_orders:
                orders.putExtra("order_status_id", "0");
                startActivity(orders);
                break;
            case R.id.nav_preparing_orders:
                orders.putExtra("order_status_id", "1");
                startActivity(orders);
                break;
            case R.id.nav_on_its_way_orders:
                orders.putExtra("order_status_id", "2");
                startActivity(orders);
                break;
            case R.id.nav_completed_orders:
                orders.putExtra("order_status_id", "3");
                startActivity(orders);
                break;
            case R.id.nav_planners:
                Intent plannerIntent=new Intent(SubCategoryActivity.this,PlannerActivity.class);
                startActivity(plannerIntent);
                break;
            case R.id.nav_add_banners:
                Intent addBannerIntent=new Intent(SubCategoryActivity.this, AddBannerActivity.class);
                startActivity(addBannerIntent);
                break;
            case R.id.nav_add_staff:
                Intent addStaffIntent=new Intent(SubCategoryActivity.this, StaffActivity.class);
                startActivity(addStaffIntent);
                break;
            case R.id.nav_logout:
                // Delete Remember user
                Paper.book().destroy();

                Intent loginIntent=new Intent(SubCategoryActivity.this,LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showItemAddStaff()
    {
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_add_staff).setVisible(true);
    }
}