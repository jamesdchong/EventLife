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

import javax.annotation.Nullable;

public class FindEventsFrag extends Fragment {

    //arrays used to get the necessary items for the event card
    String[] images_Firestore = new String[20];
    String[] eventNames_Screenshow = new String[20];
    String[] eventStartTime_Screenshow=new String[20];
    String[] eventEndTime_Screenshow=new String[20];
    String[] eventDate_Screenshow=new String[20];

    //Recycler View Needed for Event Feed
    private RecyclerView mRecyclerView;
    private CardAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    DatabaseReference ref;
    DatabaseReference userRef;
    ArrayList<Event> evenList;

    //Variables used to be able to add friends
    //Current user information
    ArrayList<User> userList;
    User currUser  = User.getInstance();
    String userID = currUser.getUserId();
    String[] friendList;
    String[] userIDList = new String[20];

    //Current user's friendslist
    String[] friendArr;
    String friendAdd;
    int added = 0;


    @Nullable
    @Override
    //to create the page
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment2_find_events, container, false);
        setHasOptionsMenu(true);

        //declare the arraylist for the events
        evenList = new ArrayList<>();
        //get path to event database
        ref = FirebaseDatabase.getInstance().getReference("/EVENT");

        //declare the arraylist for the users
        userList = new ArrayList<>();
        //get the path for the user database
        userRef = FirebaseDatabase.getInstance().getReference("/USER");

        //to grab all the users
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //update the user list and clear to make sure there's no additional add ons
                userList.clear();
                //fetch data from snap shots from database to populate array
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    userList.add(ds.getValue(User.class));
                }
                //grab the current user information
                for (int i=0; i < userList.size() ;i++) {
                    if(userList.get(i).getUserId().equals(userID)){
                        currUser = userList.get(i);
                    }
                }
                //check if data is fully fetched
                if (userList.size()!=0) {
                    retrieveData(view);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindEventsFrag.super.getContext(), "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });

        //get the user friendlist and split into a string array
        friendList = currUser.getFriendList().split(",");

        //to add the event to the user
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //clear event list
                evenList.clear();
                //iterate through the friendslist
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    boolean flag = true;
                    //check to make sure you grab no events from the friendlist
                    for (String friend : friendList) {
                        if (ds.child("owner").getValue().equals(friend + ","))
                            flag = false;
                    }

                    //grab to make sure you grab no events from the user
                    if (ds.child("owner").getValue().equals(currUser.getUserId() + ",")) {
                        flag = false;
                    }
                    //split the events of the user into an array
                    String[] rsvpevents = currUser.getRSVPEvents().split(",");

                    //make sure you grab no events from rsvp list
                    for (String e : rsvpevents) {
                        if (ds.child("name").getValue().equals(e)) {
                            flag = false;
                        }
                    }
                    //grab the correct events
                    if (flag) {
                        evenList.add(ds.getValue(Event.class));
                    }
                }
                //load data to stall time
                if (evenList.size() != 0) {
                    retrieveData(view);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FindEventsFrag.super.getContext(), "Error loading events", Toast.LENGTH_SHORT).show();
            }
        });
        //return view
        return view;
    }

    //get the data for the user list
    public void retrieveData(View view){

        // fetching data to arrays relating to users
        for (int i=0; i < userList.size();i++) {
            userIDList[i] = userList.get(i).getUserId();
        }

        // fetching data to arrays relating to events
        for (int i=0; i<evenList.size();i++) {
            eventNames_Screenshow[i] = evenList.get(i).getName();
            eventStartTime_Screenshow[i] = evenList.get(i).getStartTime();
            eventEndTime_Screenshow[i] = evenList.get(i).getEndTime();
            eventDate_Screenshow[i] = evenList.get(i).getDate();
            images_Firestore[i] = evenList.get(i).getImage();
        }
        //loading data to page
        LoadDatatoDashBoard(view);

    }

    //loading the events to the page
    public void LoadDatatoDashBoard(View view){
        ArrayList<Card> exampleList = new ArrayList<>();
        for (int i = 0; i < evenList.size(); i++) {
            exampleList.add(new Card(eventNames_Screenshow[i], eventStartTime_Screenshow[i],
                    eventEndTime_Screenshow[i], eventDate_Screenshow[i], "", images_Firestore[i]));
        }

        //split the user's friend into a string array
        friendArr = currUser.getFriendList().split(",");

        //display the users
        mRecyclerView = view.findViewById(R.id.recyclerViewFindEvents);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mAdapter = new CardAdapter(this.getContext(), exampleList, "");
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    //the method for adding friends in friend fragment based on the button in the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_friend_button) {
            //create the alert box
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Want to follow a user? Add their username below!");

            // Set up the input
            final EditText input = new EditText(getActivity());
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setPaddingRelative(40,20,20,20);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Used when current user presses the add button in the alert box
            builder.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    friendAdd = input.getText().toString();
                    boolean flag = false;
                    if(userList.size()!=0)
                    {
                        //iterate to make sure that you are not adding a friend already in the friendlist of the user
                        //make sure you do not add the user itself
                        for (int i = 0; i < friendArr.length; i++)
                        {
                            if (friendArr[i].equals(friendAdd) || friendArr[i].equals(currUser.getUserId()) || currUser.getUserId().equals(friendAdd))
                            {
                                flag = true;
                            }
                        }
                        //if the flags above are still false and you grab the correct user then you add the friend
                        for (int i = 0; i < userList.size();i++)
                        {
                            if ((userList.get(i).getUserId().equals(friendAdd) && flag == false))
                            {
                                //add the friend
                                currUser.addFriend(friendAdd);
                                userRef.child(userID).child("friendList").setValue(currUser.getFriendList());

                                //Set added = 1 so that correct message is displayed to user (i.e. successful add or not)
                                added = 1;
                                break;
                            }
                        }
                        //displays the correct toast message depending on if the person was added or not
                        if (added == 1) {
                            Toast.makeText(getActivity(), "Added " + friendAdd, Toast.LENGTH_SHORT).show();
                            added = 0;
                        } else {
                            Toast.makeText(getActivity(), friendAdd + " Does not exist or is already added", Toast.LENGTH_SHORT).show();
                        }
                    }
                    dialog.cancel();
                }
            });
            //used when current user decides to stop the process of adding the friend
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

    //this method makes sure we are using the correct menu display on the top of the app
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
}