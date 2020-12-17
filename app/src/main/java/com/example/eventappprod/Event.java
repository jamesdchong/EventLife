package com.example.eventappprod;

public class Event {
    // strings for unique ID and given event name
    private String ID;
    private String name;

    // strings for creator of event, date, and location
    private String Owner;
    private String Date;
    private String Location;

    // strings for start and end times, tags, and event description
    private String StartTime;
    private String EndTime;
    private String Tag;
    private String Description;

    // picture of event as a string because Firebase only accepts strings
    private String Image;

    // strings delimited by commas to denote a list of users going, on the fence, or not going
    public String userGoing;

    // these ended up being unused because we did not have time
    public String userMaybeGoing;
    public String userNotGoing;

    public Event() {

    }

    // constructor for the event class, takes in 12 parameters that each match one of the above
    // variables
    public Event(String id,String name,String owner, String date, String loca,
                 String stime, String etime, String tag, String des,
                 String usergo, String usermaybe, String usernot) {
        // mandatory fields for an event object
        ID = id;
        this.name = name;
        Owner = owner;
        Date = date;
        Location = loca;
        Tag =tag;
        StartTime =stime;
        EndTime = etime;
        Description = des;

        // field filled by user's action
        userGoing = usergo;
        userMaybeGoing = usermaybe;
        userNotGoing = usernot;
    }

    // getter and setter methods for the event ID
    public String getId(){
        return ID;
    }
    public void setId(String id){ ID = id; }

    // getter and setter methods for the event name
    public String getName(){
        return name;
    }
    public void setName(String n){
        name = n;
    }


    // getter and setter methods for the event creator
    public String getOwner(){
        return Owner;
    }
    public void setOwner(String o){Owner= o; }

    // getter and setter methods for the event date
    public String getDate(){
        return Date;
    }
    public void setDate(String d){
        Date = d;
    }

    // getter and setter methods for the event location
    public String getLocation(){
        return Location;
    }
    public void setLocation(String l){ Location = l; }

    // getter and setter methods for the start time of the event
    public String getStartTime(){
        return StartTime;
    }
    public void setStartTime(String t){
        StartTime = t;
    }

    // getter and setter methods for the end time of the event
    public String getEndTime(){
        return EndTime;
    }
    public void setEndTime(String t){
        EndTime = t;
    }

    // getter and setter methods for the tag(s) associated with the event
    public String getTag(){
        return Tag;
    }
    public void setTag(String t){
        Tag =t;
    }

    // getter and setter methods for the event description
    public String getDescription(){
        return Description;
    }
    public void setDescription(String d){
        Description =d;
    }

    // getter and setter methods for the event picture
    public String getImage(){
        return Image;
    }
    public void setImage(String i){
        Image = i;
    }

    // getter method for the list of users going to the given event
    public String getUserGoing(){
        return userGoing;
    }
    // method adds to the list of users going
    // we use ',' as a delimiter since Firebase database can only store data in String objects
    public void addUserGoing(String g){
        userGoing = g + "," + userGoing;
    }

    // we ended up not having to use the below methods because we did not have enough time

    // getter method for the list of users maybe going to the given event
    public String getUserMaybeGoing(){
        return userMaybeGoing;
    }
    // method adds to the list of users maybve going
    // we use '$' as a delimiter since Firebase database can only store data in String objects
    public void addUserMaybeGoing(String m){
        userMaybeGoing = userMaybeGoing.concat(m+ "$");
    }

    // getter method for the list of users not going to the given event
    public String getUserNotGoing(){
        return userNotGoing;
    }
    // method adds to the list of users not going
    // we use '$' as a delimiter since Firebase database can only store data in String objects
    public void addUserNotGoing(String n){
        userNotGoing = userNotGoing.concat(n+ "$");
    }

}
