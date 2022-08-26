package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    EditText mFullName, mEmail, mPassword, mPhone, mIDA, mID6, mID1, mDOB, mWID;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mIDA = findViewById(R.id.idAlphabet);
        mID6 = findViewById(R.id.idNumeric6);
        mID1 = findViewById(R.id.idNumeric1);
        mDOB = findViewById(R.id.dob);
        mWID = findViewById(R.id.wristbandID);

        mRegisterBtn = findViewById(R.id.register);
        mLoginBtn = findViewById(R.id.goToLogin);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //check if user login

        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        Log.i("CurrentUser: ", String.valueOf(fAuth.getCurrentUser()));

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String fullName = mFullName.getText().toString();
                final String phone = mPhone.getText().toString();
                final String id6 = mID6.getText().toString();
                final String id1 = mID1.getText().toString();
                final String idA = mIDA.getText().toString();
                final String idNum = idA + id6 + id1;
                final String dob = mDOB.getText().toString();
                final String wID = mWID.getText().toString();



                if(TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required.");
                    return;
                }

                if(TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(fullName)) {
                    mFullName.setError("Full Name is Required.");
                }

                if(TextUtils.isEmpty(phone)) {
                    mPhone.setError("Phone Number is Required.");
                }

                if(TextUtils.isEmpty(idA)) {
                    mFullName.setError("Alphabet is Required.");
                }

                if(TextUtils.isEmpty(id6)) {
                    mID6.setError("HKID is Required.");
                }

                if(TextUtils.isEmpty(id1)) {
                    mID1.setError("HKID is Required.");
                }

                if(TextUtils.isEmpty(dob)) {
                    mDOB.setError("Date of birth is Required.");
                }

                if(TextUtils.isEmpty(wID)) {
                    mWID.setError("Wristband ID is Required.");
                }

                if(password.length() < 6) {
                    mPassword.setError("Please set a password more than 6 characters.");
                    return;
                }



                //Start Firebase
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,"User Created.", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(idNum);
                            Map<String,Object> user  = new HashMap<>();
                            user.put("hkid",idNum);
                            user.put("fName",fullName);
                            user.put("email",email);
                            user.put("phone",phone);
                            user.put("DOB",dob);
                            user.put("wristband ID",wID);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                     Log.i("User profile added: ", userID);
                                }
                            });

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else {
                            Toast.makeText(RegisterActivity.this,"Error." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });




    }
}