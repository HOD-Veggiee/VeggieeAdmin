package com.veggiee.veggieeadmin;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.veggiee.veggieeadmin.Common.Common;
import com.veggiee.veggieeadmin.Model.Staff;

import io.paperdb.Paper;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;
    FirebaseDatabase db;
    DatabaseReference staff;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //ini Firebase
        db = FirebaseDatabase.getInstance();
        staff = db.getReference("Staff");

        // init Paper
        Paper.init(this);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run()
            {
                // Check Remember
                String user = Paper.book().read(Common.USER_KEY);
                String pwd = Paper.book().read(Common.PWD_KEY);

                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

                if (user != null && pwd != null)
                {
                    if (!user.isEmpty() && !pwd.isEmpty())
                        loginStaff(user,pwd);
                    else
                    {
                        startActivity(intent);
                        finish();
                    }
                }
                else
                {
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void loginStaff(String phone, final String password) {

        final String localPhone = phone;
        final String localPassword = password;

        staff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(localPhone).exists())
                {
                    Staff staff = dataSnapshot.child(localPhone).getValue(Staff.class);
                    staff.setPhone(localPhone);
                    if(staff.getRoll().equals("admin") || staff.getRoll().equals("staff"))
                    {
                        if (staff.getPassword().equals(localPassword))
                        {
                            Intent logIn = new Intent(SplashActivity.this, HomeActivity.class);
                            Common.currentStaff = staff;
                            startActivity(logIn);
                            finish();
                            Toast.makeText(SplashActivity.this, "Login Successfully", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                        Toast.makeText(SplashActivity.this, "Please login with valid Account", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(SplashActivity.this, "Staff don't exist in Database", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
