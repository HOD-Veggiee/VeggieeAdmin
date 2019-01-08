package com.veggiee.veggieeadmin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.veggiee.veggieeadmin.Common.Common;
import com.veggiee.veggieeadmin.Model.Staff;

public class StaffActivity extends AppCompatActivity {

    MaterialSpinner rollSpinner;
    TextInputLayout nameTIP, numberTIP, passwordTLP;
    AppCompatEditText edtName, edtPhone, edtPassword;
    AppCompatButton btnRegister;

    Staff newStaff;

    // Firebase
    FirebaseDatabase db;
    DatabaseReference staff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        // init Firebase
        db = FirebaseDatabase.getInstance();
        staff = db.getReference("Staff");

        edtName = findViewById(R.id.nameEditText);
        edtPhone = findViewById(R.id.phoneEditText);
        edtPassword = findViewById(R.id.passwordEditText);
        rollSpinner = findViewById(R.id.rollSpinner);
        btnRegister = findViewById(R.id.registerButton);
        rollSpinner.setItems("staff", "admin");
        nameTIP=findViewById(R.id.nameTextLayout);
        numberTIP=findViewById(R.id.phoneTextLayout);
        passwordTLP=findViewById(R.id.passwordTextLayout);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nameVerification(edtName.getText().toString());
                phoneNumberVerification(edtPhone.getText().toString());
                passwordVerification(edtPassword.getText().toString());

                if(isFormValidated())
                {
                    newStaff = new Staff(edtName.getText().toString(), edtPassword.getText().toString(), edtPhone.getText().toString(), Common.convertCodeToRoll(rollSpinner.getSelectedIndex()));

                    staff.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child(newStaff.getPhone()).exists())
                                Toast.makeText(StaffActivity.this, "Staff member with this number '" + edtPhone.getText().toString() + "' already exists!", Toast.LENGTH_LONG).show();
                            else
                            {
                                Toast.makeText(StaffActivity.this, "Successfully registered new staff member!", Toast.LENGTH_LONG).show();
                                staff.child(newStaff.getPhone()).setValue(newStaff);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        // Text Listeners

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                nameVerification(charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

                nameVerification(editable.toString());

            }
        });

        edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String number=charSequence.toString();
                phoneNumberVerification(number);
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String number=editable.toString();
                phoneNumberVerification(number);

            }
        });

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String number=charSequence.toString();
                passwordVerification(number);
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String number=editable.toString();
                passwordVerification(number);

            }
        });
    }

    private void nameVerification(String name)
    {
        if(name.isEmpty())
        {
            nameTIP.setErrorEnabled(true);
            nameTIP.setError("Name is required.");
        }
        else
        {
            nameTIP.setErrorEnabled(false);
        }
    }

    private void phoneNumberVerification(String number)
    {
        if ((number.length()>2)&&(!number.startsWith("+92")))
        {
            numberTIP.setErrorEnabled(true);
            numberTIP.setError("Write in format +923xxxxxxxxxx");
        }
        else if(number.isEmpty())
        {
            numberTIP.setErrorEnabled(true);
            numberTIP.setError("Number is required.");
        }
        else if(number.length()>13)
        {
            numberTIP.setErrorEnabled(true);
            numberTIP.setError("Number must be of 13 digits");
        }
        else
        {
            numberTIP.setErrorEnabled(false);
        }
    }

    private void passwordVerification(String pwd) {
        if(pwd.isEmpty())
        {
            passwordTLP.setErrorEnabled(true);
            passwordTLP.setError("Password is required.");
        }
        else if(pwd.length()<6)
        {
            passwordTLP.setErrorEnabled(true);
            passwordTLP.setError("Password must be of at least 6 characters");
        }
        else
        {
            passwordTLP.setErrorEnabled(false);
        }
    }

    private boolean isFormValidated()
    {
        return !numberTIP.isErrorEnabled()
                &&
                !passwordTLP.isErrorEnabled();
    }
}
