/*
    Page to view RSVP'd events
 */

package com.example.eventappprod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RSVPEventsActivity extends AppCompatActivity {
    //Recycler View needed for Event Feed
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    //Variable to add events to feed
    private CardAdapter mAdapter;

    //Reference to events in database
    DatabaseReference eventRef;

    //ArrayLists of events and cards of those events
    ArrayList<Event> evenList = new ArrayList<>();
    ArrayList<Card> exampleList = new ArrayList<>();

    //get the current user
    User currUser  = User.getInstance();

    ////ArrayLists to retrieve and display data from database
    ArrayList<String> images_Firestore = new ArrayList<>();
    ArrayList<String> eventNames_Screenshow = new ArrayList<>();
    ArrayList<String> eventStartTime_Screenshow=new ArrayList<>();
    ArrayList<String> eventEndTime_Screenshow=new ArrayList<>();
    ArrayList<String> eventDate_Screenshow=new ArrayList<>();
    ArrayList<String> creator = new ArrayList<>();

    //Array of rsvp'd events from database
    String[] rsvp = new String[currUser.getRSVPEvents().length()];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_events);

        //Set recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewArchive);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        //Get reference to events in database
        eventRef = FirebaseDatabase.getInstance().getReference("/EVENT");

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Retrieve current user's rsvp'd events into array
                rsvp = currUser.getRSVPEvents().split(",");

                evenList.clear();

                //Add user's rsvp'd events to evenList arraylist
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for(String rsvpEvent : rsvp) {
                        if(ds.child("name").getValue().equals(rsvpEvent)) {
                            evenList.add(ds.getValue(Event.class));
                        }
                    }
                }

                retrieveData();

            }

            //Called when data is not retrieved properly
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RSVPEventsActivity.this, "Error loading RSVP list", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //select the correct menu layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

        //utilizes the search action
        MenuItem searchItem =  menu.findItem(R.id.action_search);
        final androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    //Adds fields of each event into respective arraylists
    public void retrieveData(){
        eventNames_Screenshow.clear();
        eventStartTime_Screenshow.clear();
        eventEndTime_Screenshow.clear();
        eventDate_Screenshow.clear();
        images_Firestore.clear();
        creator.clear();

        // fetching data to particular array
        for (int i=0; i<evenList.size();i++) {
            eventNames_Screenshow.add(i, evenList.get(i).getName());
            eventStartTime_Screenshow.add(evenList.get(i).getStartTime());
            eventEndTime_Screenshow.add(evenList.get(i).getEndTime());
            eventDate_Screenshow.add(evenList.get(i).getDate());
            images_Firestore.add(evenList.get(i).getImage());
            creator.add(evenList.get(i).getOwner());
        }

        LoadDatatoRSVPEvents();

    }

    public void LoadDatatoRSVPEvents(){
        //Load events to card views to be displayed
        for (int i = 0; i < evenList.size(); i++) {
            exampleList.add(new Card(eventNames_Screenshow.get(i), eventStartTime_Screenshow.get(i), eventEndTime_Screenshow.get(i), eventDate_Screenshow.get(i), creator.get(i), images_Firestore.get(i)));
        }

        //Logic for displaying the events using recycler view
        mRecyclerView = findViewById(R.id.recyclerViewArchive);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new CardAdapter(this, exampleList, "RSVP");
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
