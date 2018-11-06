package com.veggiee.veggieeadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.veggiee.veggieeadmin.Common.Common;
import com.veggiee.veggieeadmin.Model.Staff;

public class LoginActivity extends AppCompatActivity {

    AppCompatEditText edtPhone, edtPassword;
    AppCompatButton btnLogin;

    FirebaseDatabase db;
    DatabaseReference staff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.loginButton);
        edtPhone = findViewById(R.id.phoneEditText);
        edtPassword = findViewById(R.id.passwordEditText);

        // Init Firebase

        db = FirebaseDatabase.getInstance();
        staff = db.getReference("Staff");



        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginStaff(edtPhone.getText().toString(), edtPassword.getText().toString());
            }
        });
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
                    if(staff.getRoll().equals("admin"))
                    {
                        if (staff.getPassword().equals(localPassword))
                        {
                            Intent logIn = new Intent(LoginActivity.this, HomeActivity.class);
                            Common.currentStaff = staff;
                            startActivity(logIn);
                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(LoginActivity.this, "Wrong Password", Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(LoginActivity.this, "Please login with Staff Account", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Staff don't exist in Database", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
