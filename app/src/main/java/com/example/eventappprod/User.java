package com.example.eventappprod;
import java.io.Serializable;

public class User implements Serializable{
    // mandatory fields for an user created

    private static User instance;

    private String name;
    private String email;
    private String password;
    private String biointro;
    private String userId;

    // optional field, user will go to profile to edit
    private String profileImage;
    private String backgroundImage;


    // filled during user's action on app
    private String rsvpevents;
    private String createdEvents;
    private String friendList;

    // Initializes a User object
    public User() {
        name = "";
        email =  "";
        password = "";
        biointro =  "";
        profileImage =  "";
        backgroundImage =  "";
        rsvpevents =  "";
        createdEvents =  "";
        friendList  =  "";
        userId = "";

    }

    // Initializes a User object with certain properties
    public User(String aname, String aemail, String userID, String apass, String abio,
                String aima, String back, String acol, String agroup,
                String arsvp, String acreated, String anotified, String afriendlist) {
        name = aname;
        email = aemail;
        userId = "";
        password = apass;
        biointro = abio;
        profileImage = aima;
        backgroundImage = back;
        rsvpevents = arsvp;
        createdEvents = acreated;
        friendList  = afriendlist;
    }

    // Returns user or creates a new user object if it does not exist
    public static synchronized User getInstance(){
        if(instance==null){
            instance=new User();
        }
        return instance;
    }

    // copy constructor for User object
    public User copy(User another){
        name = another.name;
        email = another.email;
        userId = another.userId;
        password = another.password;
        biointro = another.biointro;
        profileImage = another.profileImage;
        backgroundImage = another.backgroundImage;
        rsvpevents = another.rsvpevents;
        createdEvents = another.createdEvents;
        friendList  = another.friendList ;
        return instance;
    }


    // getters and setters
    public String getName(){
        return name;
    }
    public void setName(String n){
        name = n;
    }

    public String getEmail(){
        return email;
    }
    public void setEmail(String e){
        email = e;
    }

    public String getUserId(){return userId;}
    public void setUserId(String set){userId = set;}

    public String getPassword(){ return password; }
    public void setPassword(String p){ password = p; }

    public String getBiointro(){ return biointro; }
    public void setBiointro(String bio){ biointro = bio; }

    public String getProfileImage(){ return profileImage; }
    public void setProfileImage(String i){ profileImage = i; }

    public String getBackgroundImage(){ return backgroundImage; }
    public void setBackgroundImage(String i){ backgroundImage = i; }


    public String getCreatedEvents(){ return createdEvents; }
    public void addCreatedEvents(String favo){ createdEvents = createdEvents.concat(favo+ "$");
    }
    
    public String getRSVPEvents(){ return rsvpevents; }
    
    // adds an event to the list of RSVPd events
    public void addRSVPEvent(String past){
        rsvpevents = past + "," + rsvpevents;
    }
    
    // removes an RSVPd event from the RSVP list
    public void removeRSVPEvent(String past){
        rsvpevents = rsvpevents.replace(past+",", "");
    }

    public String getFriendList(){ return friendList; }
    
    // adds a friend to the friends list
    public void addFriend(String f){
       friendList = f + "," + friendList;
    }

    // removes a friend from the friends list
    public void removeFriend(String f){
       friendList = friendList.replace(f+",", "");
    }
}
