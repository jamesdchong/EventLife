/*
    CREATED EVENTS FOR THE USER
 */
package com.example.eventappprod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CreatedEventsActivity extends AppCompatActivity {
    //Event Feed String Arrays
    String eventNames[];
    String eventDescriptions[];

    //Recycler View Needed for Event Feed (View, Model)
    RecyclerView recyclerView;
    private RecyclerView mRecyclerView;
    private CardAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DatabaseReference ref;
    ArrayList<Event> evenList;

    //Get the current user
    User currUser  = User.getInstance();
    String[] array = new String[20];

    //Variables used for displaying information from database (View)
    String[] images_Firestore = new String[20];
    String[] eventNames_Screenshow = new String[20];
    String[] eventStartTime_Screenshow=new String[20];
    String[] eventEndTime_Screenshow=new String[20];
    String[] eventDate_Screenshow=new String[20];
    ArrayList<Card> exampleList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_events);

        //Set recycler view (Model)
        mRecyclerView = (RecyclerView) findViewById(R.id.prevEventRecycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CardAdapter(this, exampleList, "previous");
        mRecyclerView.setAdapter(mAdapter);


        evenList= new ArrayList<Event>();
        ref = FirebaseDatabase.getInstance().getReference("/EVENT");
        ref.addValueEventListener(new ValueEventListener() {

            //Updating values (Controller)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                evenList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    evenList.add(ds.getValue(Event.class));
                }
              if (evenList.size()!=0) retrieveData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CreatedEventsActivity.this, "Error loading events", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void retrieveData(){

        // Fetching data to particular array (Controller)
        for (int i=0; i<evenList.size();i++) {
            eventNames_Screenshow[i] = evenList.get(i).getName();
            eventStartTime_Screenshow[i] = evenList.get(i).getStartTime();
            eventEndTime_Screenshow[i] = evenList.get(i).getEndTime();
            eventDate_Screenshow[i] = evenList.get(i).getDate();
            images_Firestore[i] = evenList.get(i).getImage();
        }
        LoadDatatoCreatedEvents();
    }



    //Pushing information into view variables (View)
    public void LoadDatatoCreatedEvents(){
        Event event = new Event();
        array = currUser.getCreatedEvents().split(",");
        for (int i = 0; i<array.length; i++) {
            for (int j = 0; j < evenList.size(); j++) {
                if (evenList.get(j).getName().equals(array[i])) {
                    event = evenList.get(j);
                    exampleList.add(0,new Card(event.getName(), event.getStartTime(), event.getEndTime(),
                            event.getDate(), event.getOwner() ,event.getImage()));
                }
            }
        }
        mAdapter.notifyItemRangeChanged(0,exampleList.size());
        mRecyclerView.setAdapter(mAdapter);

    }

}