package com.example.eventappprod;

public class Card {
    private String mName;
    private String mStartTime;
    private String mEndTime;
    private String mDate;
    private String img_firestore;
    private String mCreator;

    // Contains the information of each card in the recycler views
    public Card(String cardName, String cardStartTime, String cardEndTime, String cardDate, String cardCreator, String imgfirestore){
        mName = cardName;
        mStartTime = cardStartTime;
        mEndTime = cardEndTime;
        mDate = cardDate;
        img_firestore=imgfirestore;
        mCreator = cardCreator;
    }

    public String getName(){
        return mName;
    }

    public String getStartTime(){
        return mStartTime;
    }

    public String getEndTime() { return mEndTime; }

    public String getDate() { return mDate; }

    public String getCreator() { return mCreator; }

    public String getImg_firestore(){
        return  img_firestore;
    }
}
