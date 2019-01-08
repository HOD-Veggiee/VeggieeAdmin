package com.veggiee.veggieeadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
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
import com.rey.material.widget.CheckBox;
import com.veggiee.veggieeadmin.Common.Common;
import com.veggiee.veggieeadmin.Model.Staff;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    AppCompatEditText edtPhone, edtPassword;
    AppCompatButton btnLogin;
    CheckBox checkBoxRemember;
    TextInputLayout numberTIP, passwordTLP;

    FirebaseDatabase db;
    DatabaseReference staff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.loginButton);
        edtPhone = findViewById(R.id.phoneEditText);
        edtPassword = findViewById(R.id.passwordEditText);
        checkBoxRemember = findViewById(R.id.checkBoxRemember);
        numberTIP=findViewById(R.id.phoneTextLayout);
        passwordTLP=findViewById(R.id.passwordTextLayout);

        // Init Firebase

        db = FirebaseDatabase.getInstance();
        staff = db.getReference("Staff");

        // init Paper
        Paper.init(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                phoneNumberVerification(edtPhone.getText().toString());
                passwordVerification(edtPassword.getText().toString());

                // Save User and Password
                if (checkBoxRemember.isChecked())
                {
                    Paper.book().write(Common.USER_KEY, edtPhone.getText().toString());
                    Paper.book().write(Common.PWD_KEY, edtPassword.getText().toString());
                }


                if(isFormValidated())
                    loginStaff(edtPhone.getText().toString(), edtPassword.getText().toString());
            }
        });

        // Text Listeners

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
                            Intent logIn = new Intent(LoginActivity.this, HomeActivity.class);
                            Common.currentStaff = staff;
                            startActivity(logIn);
                            finish();
                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            passwordTLP.setErrorEnabled(true);
                            passwordTLP.setError("Wrong Password");
                        }
                    }
                    else
                        Toast.makeText(LoginActivity.this, "Please login with Valid Account", Toast.LENGTH_LONG).show();
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
