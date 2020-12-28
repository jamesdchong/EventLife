package com.example.eventappprod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EventActivity extends AppCompatActivity {

    //Variables for displaying (View)
    ImageView mainImageView;
    TextView title, description;
    TextView Date, StartTime, EndTime, Location, CreatedBy;

    //Variables for data (Model)
    String data1;
    String desc, sTime, eTime, loca, date, image, owner;
    Event myevent;

    //Variables for database references (Model)
    DatabaseReference ref;
    DatabaseReference userRef;
    FirebaseDatabase database;

    Button AttendeesButton, RSVPButton;

    private ArrayList<User> userList;
    User currUser  = User.getInstance();
    private String userID;

    //Variables to be used for displaying (Model)
    String[] profileImages = new String[20];
    String[] friendsList = new String[20];
    String[] userName = new String[20];
    String[] userIDArr = new String[20];



    //Create database reference, create link between database ref & current user (Model, Controller)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        ref = FirebaseDatabase.getInstance().getReference("/USER");
        userRef = FirebaseDatabase.getInstance().getReference("/EVENT");

        userID = currUser.getEmail().substring(0, currUser.getEmail().indexOf("@"));

        //Create user list and update info inside current user from database
        userList = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<User> newUserList = new ArrayList<>();

                //Iterate across database to grab users (Model)
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    newUserList.add(ds.getValue(User.class));
                }

                //Check against list of users to locate current user (Controller)
                for (int i = 0; i < newUserList.size(); i++) {
                    if (newUserList.get(i).getUserId().equals(userID)) {
                        currUser = newUserList.get(i);
                    }
                }

                if (newUserList.size() != 0) {
                    retrieveData();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventActivity.this, "Error updating users", Toast.LENGTH_SHORT).show();
            }
        });



        ref = database.getInstance().getReference("/EVENT");
        userRef = database.getInstance().getReference("/USER");


        //Make references that connect the XML <--> the Java variables (View)
        mainImageView = findViewById(R.id.eventFormImageView);
        title = findViewById(R.id.eventFormName);
        description = findViewById(R.id.eventFormDescription);

        description = findViewById(R.id.tvDescription);
        Date = findViewById(R.id.tvDate);
        StartTime = findViewById(R.id.tvStart);
        EndTime = findViewById(R.id.tvEnd);
        Location = findViewById(R.id.tvLocation);
        CreatedBy =findViewById(R.id.tvCreatedBy);

        AttendeesButton = findViewById(R.id.eventFormGuests);
        RSVPButton = findViewById(R.id.eventFormRSVP);

        //Call to method that pulls information (Controller)
        getData();
        AttendeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(EventActivity.this);
                String peopleGoing = myevent.getUserGoing();
                //People going to the event
                String array[] = peopleGoing.split(",");

                //Friends going to the event
                String friends_going = currUser.getFriendList();
                String userFriends[] = friends_going.split(",");
                String friends = "";

                //User object (Controller)
                User user = new User();
                for(int i = 0; i < userFriends.length; i++ ) {
                    for(int j = 0; j < array.length; j++) {
                        //If the user is the same as the array
                        if(userFriends[i].equals(array[j])){
                            friends = userFriends[i] + "," + friends;
                        }
                    }
                }

                String final_array[] = friends.split(",");

                builder.setTitle("Attendees:");
                builder.setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }

        });

        RSVPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //People going to the event
                String peopleGoing = myevent.getUserGoing();
                //Store in array since database only stores strings
                String array[] = peopleGoing.split(",");
                boolean alreadyGoing = false;
                for (int i = 0; i < array.length; i++) {
                    if (array[i].equals(currUser.getUserId())) {
                        alreadyGoing = true;
                    }
                }
                if (alreadyGoing == true) {
                    Toast.makeText(EventActivity.this, "You have already RSVP'd for this event!", Toast.LENGTH_SHORT).show();
                }
                else if (alreadyGoing == false) {
                    //Grab current user name
                    String personGoing = currUser.getUserId();
                    // Grab all their list of RSVP events
                    String userRSVP = currUser.getRSVPEvents();
                    // Add new event onto list
                    String rsvp = myevent.getName() + "," + currUser.getRSVPEvents();

                    // Add new user onto event's going list
                    String usersGoing = personGoing + "," + peopleGoing;
                    currUser.addRSVPEvent(rsvp + myevent.getName());
                    // Update info in database

                    //Ref is event path and eventRef is also event path
                    ref.child(myevent.getName()).child("userGoing").setValue(usersGoing);
                    userRef.child(currUser.getUserId()).child("rsvpevents").setValue(rsvp);
                    Toast.makeText(EventActivity.this, "RSVP'd to " + myevent.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //This is what will check and grab the information and load it in (Controller)
    private void getData(){
        if(getIntent().hasExtra("data1")){
            data1 = getIntent().getStringExtra("data1");

            // Make sure event name is not null
            if (data1=="" ) return;

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    myevent = (Event) dataSnapshot.child(data1).getValue(Event.class);
                    if (myevent!=null) {
                        desc = myevent.getDescription();
                        date = myevent.getDate();
                        sTime = myevent.getStartTime();
                        eTime = myevent.getEndTime();
                        loca = myevent.getLocation();
                        image = myevent.getImage();
                        owner = myevent.getOwner().replace(",","") ;
                    }

                    // After retrieving all data, then set data to the TextViews
                    setData();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        else{
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
        }

    }

    //This is actually what displays the information (pushing the data from the intent --> variables from XML)
    private void setData(){
        title.setText(data1);
        description.setText(desc);
        Glide.with(EventActivity.this).load(image).into(mainImageView);


        Date.setText(date);
        StartTime.setText(sTime);
        EndTime.setText(eTime);
        Location.setText(loca);
        CreatedBy.setText(owner);
    }
    public void retrieveData(){

        // Fetching data to particular array (Controller)

        //Actually iterating into the display fields (View)
        for (int i=0; i<userList.size();i++) {
            userName[i] = userList.get(i).getName();
            userIDArr[i] = userList.get(i).getUserId();
            friendsList[i] = userList.get(i).getFriendList();
            profileImages[i] = userList.get(i).getProfileImage();
            LoadDatatoFriendsList();
        }
    }
    public void LoadDatatoFriendsList() {
        String[] array = currUser.getFriendList().split(",");
        User user = new User();
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < userList.size(); j++) {
                if (userList.get(j).getUserId().equals(array[i])) {
                    user = userList.get(j);
                }
            }
        }

    }
}