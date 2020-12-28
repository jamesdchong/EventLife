package com.example.eventappprod;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> implements Filterable {

    // Constants for the different card view types
    public static int BUTTON_VIEW = 0;
    public static int YOUR_EVENT_VIEW = 1;
    public static int EVENT_VIEW = 2;
    public static int FRIEND_SEARCH_VIEW = 3;
    public static int RSVP_VIEW = 4;
    public static int FRIEND_VIEW = 5;
    private String cardType;

    // Used for controlling the events that show in the view
    private ArrayList<Card> mCardList;
    private ArrayList<Card> cardListFull;
    User currUser = User.getInstance();
    Context context;


    // user and events connection to the database
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("/USER");
    private DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("/EVENT");

    // Event information temporary containers
    String[] userGoing = new String[20];
    String[] eventTitle = new String[20];
    String[] eventOwner = new String[20];
    String[] rsvpeventssplit = new String[20];
    ArrayList<Event> eventList = new ArrayList<>();
    ArrayList<Event> dEventList = new ArrayList<>();
    String[] rsvp = new String[currUser.getRSVPEvents().length()];

    String rsvpevents;
    String peopleGoing;

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout mRelativeLayout;
        public ConstraintLayout mainLayout;
        public ConstraintLayout deleteLayout;

        // Friend and event fields
        public ImageView mImageView;
        public TextView name, startTime, endTime, userId, date;
        public Button mUnfollowButton, mRSVPButton, mFollowButton, mCreatedDeleteButton, createEvent,
                    mDeleteButton;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            //Get references from events and friends
            mImageView = itemView.findViewById(R.id.myImageView);
            name = itemView.findViewById(R.id.cardName);
            startTime = itemView.findViewById(R.id.cardStartTime);
            userId = itemView.findViewById(R.id.friendCardUserId);
            endTime = itemView.findViewById(R.id.cardEndTime);
            date = itemView.findViewById(R.id.cardDate);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            createEvent = itemView.findViewById(R.id.create);
            mUnfollowButton = itemView.findViewById(R.id.unfollowButton);
            mRSVPButton = itemView.findViewById(R.id.RSVPButton);
            mFollowButton = itemView.findViewById(R.id.acceptButton);
            mRelativeLayout = itemView.findViewById(R.id.friendsRL);
            mCreatedDeleteButton = itemView.findViewById(R.id.DELETEButton);
            mDeleteButton = itemView.findViewById(R.id.unrsvpButton);
            deleteLayout = itemView.findViewById(R.id.deleteLayout);
        }
    }


    public CardAdapter(Context ct, ArrayList<Card> cardList, String type) {
        context = ct;
        mCardList = cardList;
        cardListFull = new ArrayList<>(cardList);
        cardType = type;
    }

    @NonNull
    @Override
    //Creates the View holder from our my_row layout
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == BUTTON_VIEW) { // The create event button view
            View v = LayoutInflater.from(context).inflate(R.layout.my_button_row, parent, false);
            CardViewHolder evh = new CardViewHolder(v);
            return evh;
        } else if (viewType == EVENT_VIEW) { // The regular event view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_row, parent, false);
            CardViewHolder evh = new CardViewHolder(v);
            return evh;
        } else if (viewType == FRIEND_SEARCH_VIEW) { // The friend card view in the explore page
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_friend_search_row, parent, false);
            CardViewHolder evh = new CardViewHolder(v);
            return evh;
        } else if (viewType == FRIEND_VIEW) { // The friend card view in the friend list
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_friend_row, parent, false);
            CardViewHolder evh = new CardViewHolder(v);
            return evh;
        } else if (viewType == RSVP_VIEW) { // The rsvp'd event card view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rsvp_event_row, parent, false);
            CardViewHolder evh = new CardViewHolder(v);
            return evh;
        } else { // The created event card view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_row_i_created, parent, false);
            CardViewHolder evh = new CardViewHolder(v);
            return evh;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, final int position) {

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                rsvp = currUser.getRSVPEvents().split(",");
                eventList.clear();
                dEventList.clear();

                // Grabs from the database the relevant events for the user
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for(String rsvpEvent : rsvp) {
                        if(ds.child("name").getValue().equals(rsvpEvent)) {
                            eventList.add(ds.getValue(Event.class));
                        }
                        dEventList.add(ds.getValue(Event.class));
                    }
                }
                retrieveData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        int viewType = getItemViewType(position);

        //The first card is always the create event button
        if (viewType == BUTTON_VIEW) {
            holder.createEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CreateEventActivity.class);
                    context.startActivity(intent);
                }
            });
        } else if (viewType == EVENT_VIEW) { // Event list

            final Card currItem = mCardList.get(position);

            Picasso.get().load(currItem.getImg_firestore()).into(holder.mImageView);

            holder.name.setText(currItem.getName());
            holder.startTime.setText(currItem.getStartTime());
            holder.endTime.setText(currItem.getEndTime());
            holder.date.setText(currItem.getDate());

            //This is what allows each card to be clicked and load up a new activity containing the information that goes with that card
            holder.mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, EventActivity.class);
                    //Extras are what we are passing from the adapter --> EventActivity (the event page)
                    //Inside EventActivity we will use these intents to pull information
                    intent.putExtra("data1", mCardList.get(position).getName());

                    context.startActivity(intent);
                }
            });
            holder.mRSVPButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Get the clicked item label
                    String personGoing = currUser.getUserId();

                    String itemLabel = mCardList.get(position).getName();
                    Event event = new Event();

                    for(int i = 0; i < dEventList.size(); i++) {
                        if(dEventList.get(i).getName().equals(itemLabel)) {
                            event = dEventList.get(i);
                        }
                    }

                    peopleGoing = event.getUserGoing();

                    String rsvp = itemLabel + "," + currUser.getRSVPEvents();

                    String usersGoing = personGoing + "," + peopleGoing;

                    currUser.addRSVPEvent(itemLabel);
                    userRef.child(currUser.getUserId()).child("rsvpevents").setValue(rsvp);

                    eventRef.child(itemLabel).child("userGoing").setValue(usersGoing);

                    // Remove the item on remove/button click
                    mCardList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mCardList.size());
                    Toast.makeText(context, "RSVP'd to " + itemLabel, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (viewType == RSVP_VIEW) { // Event list

            final Card currItem = mCardList.get(position);

            Picasso.get().load(currItem.getImg_firestore()).into(holder.mImageView);

            holder.name.setText(currItem.getName());
            holder.startTime.setText(currItem.getStartTime());
            holder.endTime.setText(currItem.getEndTime());
            holder.date.setText(currItem.getDate());

            //This is what allows each card to be clicked and load up a new activity containing the information that goes with that card
            holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, EventActivity.class);
                    //Extras are what we are passing from the adapter --> EventActivity (the event page)
                    //Inside EventActivity we will use these intents to pull information
                    intent.putExtra("data1", mCardList.get(position).getName());

                    context.startActivity(intent);
                }
            });
            holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Get the clicked item label
                    String personGoing = currUser.getUserId();
                    String itemLabel = mCardList.get(position).getName();
                    Event event = new Event();

                    for(int i = 0; i < eventList.size(); i++) {
                        if(eventList.get(i).getName().equals(itemLabel)) {
                            event = eventList.get(i);
                        }
                    }

                    //Event event = eventList.get(0);
                    peopleGoing = event.getUserGoing();

                    String removedPerson = peopleGoing.replace(personGoing+",", "");

                    String userRSVP = currUser.getRSVPEvents();
                    String removeEvent = userRSVP.replace(event.getName()+",", "");

                    currUser.removeRSVPEvent(event.getName());

                    userRef.child(currUser.getUserId()).child("rsvpevents").setValue(removeEvent);

                    eventRef.child(itemLabel).child("userGoing").setValue(removedPerson);

                    // Remove the item on remove/button click
                    mCardList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mCardList.size());
                    Toast.makeText(context, "UNRSVP'd to " + itemLabel, Toast.LENGTH_SHORT).show();

                }
            });
        } else if (viewType == FRIEND_SEARCH_VIEW) { // Friend Search
            Card currItem = mCardList.get(position);

            Picasso.get().load(currItem.getImg_firestore()).into(holder.mImageView);

            holder.name.setText(currItem.getName());
            holder.userId.setText(currItem.getStartTime());

            holder.mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Get the clicked item label
                    String itemLabel = mCardList.get(position).getName();
                    String userID = mCardList.get(position).getStartTime();

                    String friend_list = userID + "," + currUser.getFriendList();
                    userRef.child(currUser.getUserId()).child("friendList").setValue(friend_list);

                    // Add the item on accept/button click
                    mCardList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mCardList.size());
                    cardListFull = mCardList;
                    Toast.makeText(context, "Followed " + itemLabel, Toast.LENGTH_SHORT).show();
                }
            });

        } else if (viewType == FRIEND_VIEW) { // Friends List
            Card currItem = mCardList.get(position);

            Picasso.get().load(currItem.getImg_firestore()).into(holder.mImageView);

            holder.name.setText(currItem.getName());
            holder.userId.setText(currItem.getStartTime());

            //UnfollowButton used here
            holder.mUnfollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Get the clicked item label
                    String itemLabel = mCardList.get(position).getName();
                    //User ID
                    String userID = mCardList.get(position).getStartTime();


                    currUser.removeFriend(userID);
                    String userFriendlist = currUser.getFriendList();
                    userRef.child(currUser.getUserId()).child("friendList").setValue(userFriendlist);

                    // Remove the item on remove/button click
                    mCardList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mCardList.size());
                    Toast.makeText(context, "Unfollowed " + itemLabel, Toast.LENGTH_SHORT).show();
                }
            });
        } else if(viewType == YOUR_EVENT_VIEW) {
            Card currItem = mCardList.get(position);

            Picasso.get().load(currItem.getImg_firestore()).into(holder.mImageView);

            holder.name.setText(currItem.getName());
            holder.startTime.setText(currItem.getStartTime());
            holder.endTime.setText(currItem.getEndTime());
            holder.date.setText(currItem.getDate());

            //This is what allows each card to be clicked and load up a new activity containing the information that goes with that card
            holder.mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, EventActivity.class);
                    //Extras are what we are passing from the adapter --> EventActivity (the event page)
                    //Inside EventActivity we will use these intents to pull information
                    intent.putExtra("data1", mCardList.get(position).getName());

                    context.startActivity(intent);
                }


            });

            holder.mCreatedDeleteButton.setOnClickListener(new View.OnClickListener() {

                // Get the clicked item label

                @Override
                public void onClick(View v) {

                    String eventLabel = mCardList.get(position).getName();

                    mCardList.remove(position);
                    notifyItemRemoved(position);

                    String events = currUser.getCreatedEvents();
                    String delete = events.replace(eventLabel+",", "");

                    userRef.child(currUser.getUserId()).child("createdEvents").setValue(delete);

                    notifyItemRangeChanged(position, mCardList.size());
                    Toast.makeText(context, "Removed " + eventLabel, Toast.LENGTH_SHORT).show();

                    removeEvent(eventLabel);

                }
            });
        }
    }

    // Used for when you delete your own event
    public void removeEvent(final String s){
        eventRef.child(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Event e = dataSnapshot.getValue(Event.class);
                if (e != null) {
                    dataSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Makes sure when an event is deleted all the people rsvp'd are no longer rsvp'd
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    rsvpevents = ds.child("rsvpevents").getValue().toString();
                    rsvpeventssplit = rsvpevents.split(",");
                    for(int i = 0; i < rsvpeventssplit.length; i++) {
                        if(rsvpeventssplit[i].equals(s)) {
                            rsvpevents = rsvpevents.replace(s + ",", "");
                            userRef.child(ds.getKey()).child("rsvpevents").setValue(rsvpevents);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    @Override
    public int getItemViewType(int position) {
        // Depending on the position and card type the viewType will change
        if (position == 0 && this.cardType.equals("event")) {
            return BUTTON_VIEW;
        } else if(this.cardType.equals("empty")) {
            return BUTTON_VIEW;
        }else if (mCardList.get(position).getCreator().replace(",", "").equals(currUser.getUserId())) {
            return YOUR_EVENT_VIEW;
        } else if (this.cardType.equals("event") || this.cardType.equals("previous")) {
            return EVENT_VIEW;
        } else if (this.cardType.equals("friendSearch")) {
            return FRIEND_SEARCH_VIEW;
        } else if (this.cardType.equals("RSVP")) {
            return RSVP_VIEW;
        } else if (this.cardType.equals("friend")) {
            return FRIEND_VIEW;
        }
        return EVENT_VIEW;
    }

    @Override
    public int getItemCount() {
        return mCardList.size();
    }

    @Override
    public Filter getFilter() {
        return cardFilter;
    }

    private Filter cardFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Card> filteredList = new ArrayList<>();

            //Show all results bc we aren't filtering anything
            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(cardListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                //Searches for query in text1 and text2
                int i = 0;
                for (Card item : cardListFull) {
                    if (i == 0 && cardType.equals("event")) { // The item is the button and always should be added
                        filteredList.add(item);
                    } else {
                        if (item.getName().toLowerCase().contains(filterPattern)
                                || item.getStartTime().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                    i++;
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mCardList.clear();
            mCardList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    // Resets the events shown in the recycler view
    public void resetFull() {
        cardListFull = new ArrayList<>(mCardList);
    }

    // Records all the events that will be shown in the dashboard
    public void retrieveData() {
        for (int i = 0; i < eventList.size() ; i++) {
            userGoing[i] = eventList.get(i).getUserGoing();
            eventTitle[i] = eventList.get(i).getName();
            eventOwner[i] = eventList.get(i).getOwner();
        }
    }

}