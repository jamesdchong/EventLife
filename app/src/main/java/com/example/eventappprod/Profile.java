package com.example.eventappprod;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Profile extends AppCompatActivity {

    // Buttons
    private Button friendsListButton;
    private Button prevEventButton;
    private Button archivedEventButton;
    private Button LogoutButton;
    private ImageButton updatePicButton;
    private ImageButton updateBackgroundButton;

    String RealTimeImagePath;

    // ImageView and TextView objects to interact with
    private ImageView profilePic;
    private ImageView backgroundPic;
    private TextView profileName;
    private TextView profileUsername;

    // variables pertaining to users
    private String userID;
    private int update = 0;
    User currUser  = User.getInstance();
    private ArrayList<User> userList;


    // authorization
    private FirebaseAuth firebaseAuth;

    // event, image, and Firebase variables
    Event event;
    Uri uri;
    StorageReference imagePath;
    FirebaseStorage  storage;
    private DatabaseReference ref;

    // arrays of user profile images, friend lists, usernames, and user IDs
    String[] profileImages = new String[20];
    String[] friendsList = new String[20];
    String[] userName = new String[20];
    String[] userIDArr = new String[20];


    // all the intents
    Intent friendIntent = getIntent();

    // make phone select an image from their gallery
    public void openFilechooser(int i){
        // create an intent so user can jump to his phone's folder to select photo
        Intent intent = new Intent(Intent.ACTION_PICK);
        update = i;
        // only pick image
        intent.setType("image/*");
        // grab the photo
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    // method will create a profile page for all users in the database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // grab the reference to the USER section in the database
        ref = FirebaseDatabase.getInstance().getReference("/USER");

        // initialize array list to hold our users
        userList = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            // when users are added or removed update the profiles of all users
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // initialize array list to hold new users
                ArrayList<User> newUserList = new ArrayList<>();

                // add all current (and new) users to the array list
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    newUserList.add(ds.getValue(User.class));
                }

                // grab the instance of the user currently viewing the profile page
                for (int i = 0; i < newUserList.size();i++)
                {
                    // ID check to ensure they are the correct user
                    if(newUserList.get(i).getUserId().equals(userID))
                    {
                        currUser = newUserList.get(i);
                    }
                }
                // clear the old user list
                userList.clear();
                // if we even one user we must retrieve their data
                if (newUserList.size()!=0) {
                    userList = newUserList;
                    retrieveData();
                }
            }

            // error message to throw when error occurs
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Profile.this, "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });


        // user's profile
        // attach the corresponding views to the declared variables
        profilePic = findViewById(R.id.profilePicture);
        backgroundPic = findViewById(R.id.background);
        profileName = findViewById(R.id.profileName);
        profileName.setText(currUser.getName());
        profileUsername = findViewById(R.id.profileUser);
        userID = currUser.getUserId();
        profileUsername.setText(userID);

        //mainImageView.setImageURI(Uri.parse(image));
        Picasso.get().load(currUser.getProfileImage()).into(profilePic);
        Picasso.get().load(currUser.getBackgroundImage()).into(backgroundPic);
        //create reference to the user
        //ref = FirebaseDatabase.getInstance().getReference("/USER");

        // Create the update profile picture button, open the corresponding file chooser, and update
        // the user profile picture
        updatePicButton = findViewById(R.id.updatePicBtn);
        updatePicButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openFilechooser(0);
                currUser.setProfileImage(RealTimeImagePath);
            }
        });

        // Create the update background button, open the corresponding file chooser, and update
        // the background image
        updateBackgroundButton = findViewById(R.id.updateBackground);
        updateBackgroundButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openFilechooser(1);
                currUser.setBackgroundImage(RealTimeImagePath);
            }
        });

        // display the Friends List button and open the corresponding screen
        friendsListButton = findViewById(R.id.viewFriendsButton);
        friendsListButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FriendsListActivity.class);
                intent.putExtra("ProfileFriend", currUser);

                startActivity(intent);

            }

        });

        // display the Created Events button and open the corresponding screen
        prevEventButton = findViewById(R.id.viewHistoryButton);
        prevEventButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext()
                        , CreatedEventsActivity.class));

            }

        });

        // display the RSVP Events button and open the corresponding screen
        archivedEventButton = findViewById(R.id.viewArchiveButton);
        archivedEventButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext()
                        , RSVPEventsActivity.class));

            }

        });

        // display the logout button and open the corresponding screen
        firebaseAuth = FirebaseAuth.getInstance();
        LogoutButton = findViewById(R.id.btnLogout);
        LogoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext()
                        ,Login.class));
                Toast.makeText(Profile.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
            }
        });


        //Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // The navigation text/symbols will change color when you are on that page
        bottomNavigationView.setSelectedItemId(R.id.dashboard);
        int[] colors = new int[] {
                Color.LTGRAY,
                Color.WHITE,
        };

        int [][] states = new int [][]{
                new int[] { android.R.attr.state_enabled, -android.R.attr.state_checked},
                new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}
        };

        bottomNavigationView.setItemTextColor(new ColorStateList(states, colors));
        bottomNavigationView.setItemIconTintList(new ColorStateList(states, colors));

        //Set profile as selected
        bottomNavigationView.setSelectedItemId(R.id.profile);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.dashboard:
                        startActivity(new Intent(getApplicationContext()
                                ,DashBoard.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        return true;
                    case R.id.explore:
                        startActivity(new Intent(getApplicationContext()
                                , Explore.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    // method will update image fields and inform the user after they've clicked the image view
    // icons for the profile image and background image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode==1 && resultCode == RESULT_OK && data!=null && data.getData()!=null)
        {
            // get the data for picture chosen
            uri = data.getData();
            if(update == 0){
            // set the chooseImage by the picture chosen
            profilePic.setImageURI(uri);
            }

            else {
            backgroundPic.setImageURI(uri);
            }
            // assign the imagePath by using uri

            String url = uri.toString();
            String filename = url.substring(url.lastIndexOf("/")+1);

            imagePath = FirebaseStorage.getInstance().getReference().child("/EVENT").child(filename);

            //  put the picture to put in Image box
            imagePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                // if upload success, print message
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // inform user their profile picture has been uploaded
                    Toast.makeText(Profile.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    // maintainence details to grab image as a string
                    StorageMetadata snapshotMetadata = taskSnapshot.getMetadata();
                    Task<Uri> downloadUrl = imagePath.getDownloadUrl();
                    downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            RealTimeImagePath = uri.toString();
                            //save url into ref and upload to profile image if that is what user selected
                            if(update == 0) {
                                ref.child(userID).child("profileImage").setValue(RealTimeImagePath);
                            }
                            //otherwise make it the background picture
                            else {
                                ref.child(userID).child("backgroundImage").setValue(RealTimeImagePath);
                            }

                        }
                    });
            // message to warn user about an image not be uploaded
                }
                // if upload fails, print message
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Profile.this, "Not Uploaded" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // display the pic
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double process = (120.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                }
            });

        }
    }
    // method to populate our arrays with the necessary user information
    public void retrieveData(){

        // fetching data to particular arrays
        // essentially a mass getter call
        for (int i=0; i<userList.size();i++) {
            userName[i] = userList.get(i).getName();
            userIDArr[i] = userList.get(i).getUserId();
            friendsList[i] = userList.get(i).getFriendList();
            profileImages[i] = userList.get(i).getProfileImage();
        }
    }
}

