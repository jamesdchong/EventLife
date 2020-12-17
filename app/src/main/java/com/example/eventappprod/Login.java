package com.example.eventappprod;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    //text grabber for email and password
    EditText mEmail, mPassword;
    //button used to login
    Button mLoginBtn;
    //viewer for the user login and if error in login
    TextView mCreateBtn, forgotTextLink;

    ProgressBar progressBar;
    //firebase authentication link
    FirebaseAuth fAuth;
    //firebase database
    FirebaseDatabase database;
    //database reference
    DatabaseReference ref;
    //textview to get profile name
    private TextView profileName;

    //variables used to grab the user input
    String email = "";
    String password = "";

    //Method used to create the page
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //email box
        mEmail = findViewById(R.id.Email);
        //password box
        mPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        //to get the an instance of the firebase authentification
        fAuth = FirebaseAuth.getInstance();
        //login button
        mLoginBtn = findViewById(R.id.loginBtn);
        //create account button
        mCreateBtn = findViewById(R.id.createText);
        //forgot password button
        forgotTextLink = findViewById(R.id.forgotPassword);

        //view the profile name
        profileName = findViewById(R.id.profileName);

        //to create the login
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //grab the email and password
                email = mEmail.getText().toString().trim();
                password = mPassword.getText().toString().trim();

                //get the ucsd domain and check if it is an ucsd.edu email
                String domain = email.substring(email.indexOf("@") + 1);
                if (domain.equals("ucsd.edu") == false) {
                    mEmail.setError("Email is invalid. Please Login with a UCSD email");
                    return;
                }

                //if the email text is empty
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required.");
                    return;
                }

                //if the password is empty
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required.");
                    return;
                }

                //if the length of the password is less than 8 characters
                if (password.length() < 8) {
                    mPassword.setError("Password Must be 8 or More Characters");
                    return;
                }

                // turn off the process bar
                progressBar.setVisibility(View.VISIBLE);
                // authenticate the user
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //if the task is successful
                        if (task.isSuccessful()) {
                            if (fAuth.getCurrentUser().isEmailVerified()) {
                                //message will be printed if the login is successful
                                Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                //grab the user database reference
                                ref = FirebaseDatabase.getInstance().getReference("/USER");

                                ref.addValueEventListener(new ValueEventListener() {
                                    @Override

                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //get everything before the @ from the email
                                        String userID = email.substring(0, email.indexOf("@"));

                                        //create an intent to bring the login user to the dashboard
                                        Intent intent = new Intent(Login.this, DashBoard.class);

                                        //get the correct user
                                        User user = dataSnapshot.child(userID).getValue(User.class);
                                        User globalcurrentUser = User.getInstance();
                                        globalcurrentUser.copy(user);

                                        //go to the dashboard
                                        startActivity(intent);
                                    }

                                    //if error in database
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(Login.this,"Error logging in", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                            //if authentification is not found then user cannot log in
                            else {
                                Toast.makeText(Login.this, "Please Verify your email address", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }


                        }
                        //show error if the task cannot be complete
                        else {
                            Toast.makeText(Login.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                        mPassword.setText("");

                    }
                });

            }
        });

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });

        forgotTextLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText resetMail = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Reset Link Has Been Sent To Your Email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error ! Reset Link Was Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });

                passwordResetDialog.create().show();

            }
        });


    }
}
