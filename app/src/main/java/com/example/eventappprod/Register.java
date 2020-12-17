package com.example.eventappprod;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {
    //get the tag
    public static final String TAG = "TAG";
    //fields to get the name, email, and password
    EditText mFullName,mEmail,mPassword, confirmPassword;
    //register button
    Button mRegisterBtn;
    //take user to log in after
    TextView mLoginBtn;
    //get the authentification of the user
    FirebaseAuth fAuth;
    //variable to check the cycle rotation
    ProgressBar progressBar;
    //call to store the info in the firebase
    FirebaseFirestore fStore;
    //creating a user
    User u;
    //profile user name id
    private TextView profileName;

    //reference to the database
    FirebaseDatabase database;
    //get the ref
    DatabaseReference ref;

    //creating the page
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the view of the page
        setContentView(R.layout.activity_register);
        //get  the reference to the database
        ref =FirebaseDatabase.getInstance().getReference();

        //full name box
        mFullName   = findViewById(R.id.fullName);
        //email box
        mEmail      = findViewById(R.id.Email);
        //password box
        mPassword   = findViewById(R.id.password);
        //confirm password box
        confirmPassword   = findViewById(R.id.cfmPassword);

        //register button
        mRegisterBtn= findViewById(R.id.registerBtn);
        //login button
        mLoginBtn   = findViewById(R.id.createText);

        //get instance of the authentification
        fAuth = FirebaseAuth.getInstance();
        //get the firebase storage
        fStore = FirebaseFirestore.getInstance();
        //get the recycle view
        progressBar = findViewById(R.id.progressBar);

        //profile name button
        profileName = findViewById(R.id.profileName);

        //Create action of the register button
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the email
                final String email = mEmail.getText().toString().trim();
                //get password
                final String password = mPassword.getText().toString().trim();
                //get confirmation password
                String cfmPassword = confirmPassword.getText().toString().trim();
                //full name
                final String fullName = mFullName.getText().toString();

                //check if valid ucsd email
                String domain = email .substring(email .indexOf("@") + 1);
                if (domain.equals("ucsd.edu") == false) {
                    mEmail.setError("Email is invalid. Please use a UCSD email");
                    mPassword.setText("");
                    confirmPassword.setText("");
                    return;
                }

                //check if the passwords do not match
                if (password.equals(cfmPassword) == false) {
                    confirmPassword.setError("Passwords do not match. Please confirm again");
                    confirmPassword.setText("");
                    return;
                }

                //check if the email given is empty
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }

                //check if the password box is empty
                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }

                //check if length of the password is less than 7 characters
                if(password.length() < 8){
                    mPassword.setError("Password Must Be 8 or More Characters");
                    return;
                }

                //set the recycle view to visible
                progressBar.setVisibility(View.VISIBLE);

                // register the user in firebase
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //send authentifcation password to user email
                            fAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                                        mEmail.setText("");
                                        mPassword.setText("");
                                        //create the new user with default background and proile images
                                        u  = new User(fullName, email, "",password, "", "https://firebasestorage.googleapis.com/v0/b/event-b161b.appspot.com/o/EVENT%2Fraw%253A%252Fstorage%252Femulated%252F0%252FDownload%252Fdownload.png?alt=media&token=06256f12-4b6f-4099-84f0-8aa4a90f0159",
                                                "https://firebasestorage.googleapis.com/v0/b/event-b161b.appspot.com/o/EVENT%2F285871589?alt=media&token=0a2f3f7e-e6f8-4b44-ac9b-b15d0669ecd1","", "", "", "", "", "");

                                        //grab the userID that will define the user
                                        String userID = email.substring(0, email.indexOf("@"));
                                        //set the userID
                                        u.setUserId(userID);
                                        //set the value
                                        ref.child("/USER").child(userID).setValue(u);
                                        //go to the login page
                                        startActivity(new Intent(getApplicationContext(),Login.class));

                                    }
                                    //if there is an error
                                    else {
                                        Log.e(TAG, "Email hasn't been verified.", task.getException());
                                    }
                                    //if there is a crash
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Email not sent " + e.getMessage());
                                }
                            });

                        }
                        //if the recycle view crashes
                        else {
                            Toast.makeText(Register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });



        //go to login
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });



    }

}
