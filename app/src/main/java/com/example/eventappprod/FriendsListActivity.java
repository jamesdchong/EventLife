package com.example.eventappprod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsListActivity extends AppCompatActivity {

    //Start of Model in MVC (Variables containing the data structure used in the file)
    //Friend List Variables
    private String friendAdd;
    private String[] array;
    int added = 0;

    //currUser Stuff
    private String userID;

    //Firebase Variables
    private DatabaseReference ref;

    //Event Feed String Arrays
    String[] profileImages = new String[20];
    String[] friendsList = new String[20];
    String[] userName = new String[20];
    String[] userIDArr = new String[20];

    private ArrayList<User> userList;
    //End of Model in MVC

    //View
    //Recycler View Needed for Event Feed
    ArrayList<Card> exampleList;
    private RecyclerView mRecyclerView;
    private CardAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    User currUser = User.getInstance();


    //Controller
    private Context mContext;

    //Initializes all variables and creates connections between front-end and back-end
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        //View in MVC (updating visual aspects)
        //Set context at very top
        mContext = getApplicationContext();

        //Create list to make the cards
        exampleList = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.friendListRecycler);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CardAdapter(mContext, exampleList, "friend");
        mRecyclerView.setAdapter(mAdapter);

        //Model in MVC (data-structure to hold userID & Firebase reference to all users)
        ref = FirebaseDatabase.getInstance().getReference("/USER");
        userID = currUser.getEmail().substring(0, currUser.getEmail().indexOf("@"));

        //Create user list and update info inside current user from database (Controller in MVC)
        userList = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<User> newUserList = new ArrayList<>();

                //Iterate over database ref & pull all users to add to newUserList (Model in MVC)
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    newUserList.add(ds.getValue(User.class));
                }

                //Iterate over to find currentUser to add (Controller in MVC)
                for (int i = 0; i < newUserList.size();i++)
                {
                    if(newUserList.get(i).getUserId().equals(userID))
                    {
                       currUser = newUserList.get(i);
                    }
                }

                //Ensure that database information properly loaded (Model in MVC)
                if (newUserList.size()!=0) {
                    userList = newUserList;
                    retrieveData();
                }
            }


            // View in MVC (show errors to user visually)
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendsListActivity.this, "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Controller in MVC (update based on user's input)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_friend_button) {
            // Visually display this textbox (View in MVC)
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Want to follow a user? Add their username below!");

            // Set up the input (View in MVC)
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setPaddingRelative(40,20,20,20);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons (View in MVC)
            builder.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Update text (Controller in MVC)
                    friendAdd = input.getText().toString();
                    boolean flag = false;
                    array = currUser.getFriendList().split(",");

                    //Controller in MVC (used to process adding a friend)
                    //First check if userList is non-populated.
                    if(userList.size()!=0)
                    {
                        //Iterate across entire user database pulled from firebase reference
                        for (int i = 0; i < array.length; i++)
                        {
                            //checks if the potential friend is the user or already in the friendslist.
                            if (array[i].equals(friendAdd) || array[i].equals(currUser.getUserId()) || currUser.getUserId().equals(friendAdd))
                            {
                                flag = true;
                            }
                        }
                        //Iterate across userList to compare
                        for (int i = 0; i < userList.size();i++)
                        {
                            //check if the user is in the userlist, and is correct based off of the flag
                            if ((userList.get(i).getUserId().equals(friendAdd) && flag == false))
                            {
                                currUser.addFriend(friendAdd);
                                ref.child(userID).child("friendList").setValue(currUser.getFriendList());

                                //Create a new row for that friend (View in MVC)
                                exampleList.add(0, new Card(userList.get(i).getName(), userList.get(i).getUserId(), "", "", "", userList.get(i).getProfileImage()));
                                mAdapter.notifyItemRangeChanged(0, exampleList.size());
                                mAdapter.notifyItemInserted(0);
                                mRecyclerView.scrollToPosition(0);
                                mAdapter.resetFull();
                                //Set added = 1 so that correct message is displayed to user (i.e. successful add or not)
                                added = 1;
                                break;
                            }
                        }

                        //View in MVC (display to the user successful add or not)
                        if (added == 1) {
                            Toast.makeText(FriendsListActivity.this, "Added " + friendAdd, Toast.LENGTH_SHORT).show();
                            added = 0;
                        } else {
                            Toast.makeText(FriendsListActivity.this, friendAdd + " Does not exist or is already added", Toast.LENGTH_SHORT).show();
                        }
                    }
                    dialog.cancel();
                }
            });
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


    //View in MVC (display top bar w/ magnifying glass & add friend icon)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
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

    //Controller in MVC (Get all necessary user fields necessary for displaying in RecyclerView)
    public void retrieveData(){

        //Fetching data to particular array (Controller in MVC)
        for (int i=0; i<userList.size();i++) {
            userName[i] = userList.get(i).getName();
            userIDArr[i] = userList.get(i).getUserId();
            friendsList[i] = userList.get(i).getFriendList();
            profileImages[i] = userList.get(i).getProfileImage();
        }
        LoadDatatoFriendsList();
    }
    public void LoadDatatoFriendsList() {
        array = currUser.getFriendList().split(",");
        User user = new User();
        //Iterate through current user's friends list
        for (int i = 0; i < array.length; i++) {

            //Iterate through all user's in the database, and if user is part of current user's friend list, add to cardView to display in recyclerView
            for (int j = 0; j < userList.size(); j++) {
                if (userList.get(j).getUserId().equals(array[i])) {
                    user = userList.get(j);

                    //View in MVC (display information on the card in the cardView)
                    exampleList.add(new Card(user.getName(), user.getUserId(), "", "", "", user.getProfileImage()));
                    mAdapter.notifyItemInserted(0);
                    mAdapter.resetFull();
                    mRecyclerView.scrollToPosition(0);
                }
            }
        }
    }
}