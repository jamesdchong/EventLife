package com.example.eventappprod;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nullable;

public class DashBoardFrag extends Fragment {

    //Event Feed String Arrays
    private static final String TAG = "PostDetailActivity";
    private static final String CHANNEL_ID = "Channel1";

    //ArrayLists to retrieve and display data from database (view)
    ArrayList<String> images_Firestore = new ArrayList<>();
    ArrayList<String> eventNames_Screenshow = new ArrayList<>();
    ArrayList<String> eventStartTime_Screenshow = new ArrayList<>();
    ArrayList<String> eventEndTime_Screenshow = new ArrayList<>();
    ArrayList<String> eventDate_Screenshow = new ArrayList<>();
    ArrayList<String> creator = new ArrayList<>();
    ArrayList<String> friendList = new ArrayList<>();
    ArrayList<String> rsvpEvents = new ArrayList<>();
    ArrayList<User> userList = new ArrayList<>();
    ArrayList<User> newUserList = new ArrayList<>();
    ArrayList<Event> evenList = new ArrayList<>();  //List of events to be displayed on dashboard

    //Get current user
    User currUser = User.getInstance();

    //Arrays to store current user's friend list and RSVP'd events from database
    String[] friends = new String[currUser.getFriendList().length()];
    String[] rsvp = new String[currUser.getRSVPEvents().length()];

    //Database references of events and users
    DatabaseReference eventref;
    DatabaseReference userref;

    //Variables used for following users
    String friendAdd;
    int added = 0;
    String userID = currUser.getUserId();

    //Recycler View Needed for Event Feed (View, Model)
    private RecyclerView mRecyclerView;
    private CardAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_dash_board_frag, container, false);
        setHasOptionsMenu(true);

        //Get reference to database users
        userref = FirebaseDatabase.getInstance().getReference("/USER");

        userref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Fetches user data from database
                newUserList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    newUserList.add(ds.getValue(User.class));
                }

                //Makes sure that user arraylist is populated
                if (newUserList.size() != 0) {
                    userList = newUserList;
                    retrieveData(view);
                }

                //Updates correct current user data
                for (int i = 0; i < newUserList.size(); i++) {
                    if (newUserList.get(i).getUserId().equals(userID)) {
                        currUser = newUserList.get(i);
                    }
                }

            }

            //Called when data is not retrieved properly
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DashBoardFrag.super.getContext(), "Error on Firebase", Toast.LENGTH_SHORT).show();
            }
        });

        //Gets current user's friend list
        friends = currUser.getFriendList().split(",");

        //Gets database reference to events
        eventref = FirebaseDatabase.getInstance().getReference("/EVENT");

        eventref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Populate friend list from database
                friendList.clear();
                friends = currUser.getFriendList().split(",");
                friendList.addAll(Arrays.asList(friends));

                //Populate rsvp'd events from database
                rsvpEvents.clear();
                rsvp = currUser.getRSVPEvents().split(",");
                Collections.addAll(rsvpEvents, rsvp);

                evenList.clear();

                //Loop through each event in database
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    //Adds every event a friend has created
                    for (String friend : friendList) {
                        if (ds.child("owner").getValue().equals(friend + ",")) {
                            evenList.add(ds.getValue(Event.class));
                        }
                    }

                    //Removes events that have been rsvp'd
                    for (String event : rsvpEvents) {
                        if (ds.child("name").getValue().equals(event)) {
                            Iterator<Event> it = evenList.iterator();
                            while (it.hasNext()) {
                                Event eventIteration = it.next();
                                if (eventIteration.getName().equals(ds.child("name").getValue())) {
                                    it.remove();
                                }
                            }
                        }
                    }

                    //Adds events the user creates
                    if (ds.child("owner").getValue().equals(currUser.getUserId() + ",")) {
                        evenList.add(ds.getValue(Event.class));
                    }
                }

                retrieveData(view);

            }

            //Called when data is not retrieved properly
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DashBoardFrag.super.getContext(), "Error loading events", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_friend_button) {
            //Create alert box
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Want to follow a user? Add their username below!");

            //Set up the input
            final EditText input = new EditText(getActivity());
            //Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setPaddingRelative(40,20,20,20);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            //Checking username
            builder.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Convert input to string
                    friendAdd = input.getText().toString();
                    boolean flag = false;
                    friends = currUser.getFriendList().split(",");
                    if(userList.size()!=0)
                    {
                        for (int i = 0; i < friends.length; i++)
                        {
                            if (friends[i].equals(friendAdd) || friends[i].equals(currUser.getUserId()) || currUser.getUserId().equals(friendAdd))
                            {
                                flag = true;
                            }
                        }
                        for (int i = 0; i < userList.size();i++)
                        {
                            if ((userList.get(i).getUserId().equals(friendAdd) && flag == false))
                            {
                                currUser.addFriend(friendAdd);
                                userref.child(userID).child("friendList").setValue(currUser.getFriendList());

                                //Set added = 1 so that correct message is displayed to user (i.e. successful add or not)
                                added = 1;
                                break;
                            }

                        }

                        if (added == 1) {
                            Toast.makeText(DashBoardFrag.super.getContext(), "Added friend " + friendAdd, Toast.LENGTH_SHORT).show();
                            added = 0;
                        } else {
                            Toast.makeText(DashBoardFrag.super.getContext(), friendAdd + " does not exist or is already added", Toast.LENGTH_SHORT).show();
                        }
                    }
                    dialog.cancel();
                }
            });

            //Called when data is not retrieved properly
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //select the correct menu layout
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
    }

    //Adds fields of each event into respective arrays
    public void retrieveData(View view){
        eventNames_Screenshow.clear();
        eventStartTime_Screenshow.clear();
        eventEndTime_Screenshow.clear();
        eventDate_Screenshow.clear();
        images_Firestore.clear();
        creator.clear();

        //Blank event for the button
        eventNames_Screenshow.add("");
        eventStartTime_Screenshow.add("");
        eventEndTime_Screenshow.add("");
        eventDate_Screenshow.add("");
        images_Firestore.add("");
        creator.add("");

        //Retrieves data from event list in database
        for (int i=1; i<evenList.size()+1;i++) {
            eventNames_Screenshow.add(i, evenList.get(i-1).getName());
            eventStartTime_Screenshow.add(i, evenList.get(i-1).getStartTime());
            eventEndTime_Screenshow.add(i, evenList.get(i-1).getEndTime());
            eventDate_Screenshow.add(i, evenList.get(i-1).getDate());
            images_Firestore.add(i, evenList.get(i-1).getImage());
            creator.add(i,evenList.get(i-1).getOwner());
        }

        LoadDatatoDashBoard(view);

    }

    //Populates display with events for current user to view
    public void LoadDatatoDashBoard(View view){

        //Takes info from events which creates the card for the user to see
        ArrayList<Card> exampleList = new ArrayList<>();
        for (int i = 0; i < evenList.size() + 1; i++) {
            exampleList.add(new Card(eventNames_Screenshow.get(i), eventStartTime_Screenshow.get(i), eventEndTime_Screenshow.get(i), eventDate_Screenshow.get(i), creator.get(i), images_Firestore.get(i)));
        }

        //Set recycler view
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());

        //Adjust mAdapter depending on eventlist size
        if(evenList.size() == 0) {
            mAdapter = new CardAdapter(this.getContext(), exampleList, "empty");
        } else {
            mAdapter = new CardAdapter(this.getContext(), exampleList, "event");
        }

        //Updates recycler view
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}