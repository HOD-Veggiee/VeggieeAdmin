package com.veggiee.veggieeadmin;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.veggiee.veggieeadmin.Common.Common;
import com.veggiee.veggieeadmin.Model.Staff;

import io.paperdb.Paper;

public class UpdatePasswordActivity extends AppCompatActivity {

    TextInputLayout currentPasswordTIP, newPasswordTIP, confirmPasswordTLP;
    AppCompatEditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    AppCompatButton btnUpdate;

    // Firebase
    FirebaseDatabase db;
    DatabaseReference staff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        // init Firebase
        db = FirebaseDatabase.getInstance();
        staff = db.getReference("Staff");

        edtCurrentPassword = findViewById(R.id.currentPasswordText);
        edtNewPassword = findViewById(R.id.newPasswordText);
        edtConfirmPassword = findViewById(R.id.confirmPasswordEditText);
        btnUpdate = findViewById(R.id.updateButton);
        btnUpdate.setEnabled(false);
        currentPasswordTIP =findViewById(R.id.currentPasswordTextLayout);
        newPasswordTIP =findViewById(R.id.newPasswordTextLayout);
        confirmPasswordTLP =findViewById(R.id.confirmPasswordTextLayout);

        // init Paper to update password there as well
        Paper.init(this);
                
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                passwordVerification(edtCurrentPassword.getText().toString(), currentPasswordTIP);
                passwordVerification(edtNewPassword.getText().toString(), newPasswordTIP);
                passwordVerification(edtConfirmPassword.getText().toString(), confirmPasswordTLP);

                Paper.book().write(Common.PWD_KEY, edtConfirmPassword.getText().toString());

                staff.orderByKey().equalTo(Common.currentStaff.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.child("password").getRef().setValue(edtConfirmPassword.getText().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Toast.makeText(UpdatePasswordActivity.this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        edtCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String number=charSequence.toString();
                passwordVerification(number, currentPasswordTIP);
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String number=editable.toString();
                passwordVerification(number, currentPasswordTIP);

                if (!Common.currentStaff.getPassword().equals(edtCurrentPassword.getText().toString()))
                {
                    currentPasswordTIP.setErrorEnabled(true);
                    currentPasswordTIP.setError("Invalid Password.");
                    btnUpdate.setEnabled(false);
                }
                else
                {
                    currentPasswordTIP.setErrorEnabled(false);
                    btnUpdate.setEnabled(true);
                }


            }
        });
        edtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String number=charSequence.toString();
                passwordVerification(number, newPasswordTIP);
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String number=editable.toString();
                passwordVerification(number, newPasswordTIP);

                if(!edtConfirmPassword.getText().toString().isEmpty() && !edtConfirmPassword.getText().toString().equals(null))
                {
                    if(!edtNewPassword.getText().toString().equals(edtConfirmPassword.getText().toString()))
                    {
                        btnUpdate.setEnabled(false);
                        confirmPasswordTLP.setErrorEnabled(true);
                        confirmPasswordTLP.setError("Password don't match.");
                    }
                    else
                    {
                        confirmPasswordTLP.setErrorEnabled(false);
                        btnUpdate.setEnabled(true);
                    }
                }
            }
        });
        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String number=charSequence.toString();
                passwordVerification(number, confirmPasswordTLP);
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String number=editable.toString();
                passwordVerification(number, confirmPasswordTLP);

                if(!edtNewPassword.getText().toString().equals(number))
                {
                    btnUpdate.setEnabled(false);
                    confirmPasswordTLP.setErrorEnabled(true);
                    confirmPasswordTLP.setError("Password don't match.");
                }
                else
                {
                    confirmPasswordTLP.setErrorEnabled(false);
                    btnUpdate.setEnabled(true);
                }

            }
        });
    }

    private void passwordVerification(String pwd, TextInputLayout TLP) {
        if(pwd.isEmpty())
        {
            TLP.setErrorEnabled(true);
            TLP.setError("Password is required.");
        }
        else if(pwd.length()<6)
        {
            TLP.setErrorEnabled(true);
            TLP.setError("Password must be of at least 6 characters");
        }
        else
        {
            TLP.setErrorEnabled(false);
        }
    }
}
