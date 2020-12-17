package com.example.eventappprod;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CreateEventActivity extends AppCompatActivity {
    EditText mName, mLocation, mStartTime, mEndTime, mDate, mDescription;
    Button Create;
    String RealTimeImagePath = "";
    ImageButton chooseImage;
    Event event;
    Uri uri;
    StorageReference imagePath;
    FirebaseStorage storage;
    private DatabaseReference ref;
    User curruser = User.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Input texts for create an event.
        chooseImage = (ImageButton) findViewById(R.id.btnImage);
        mName = (EditText) findViewById(R.id.createEventTvName);
        mLocation = (EditText) findViewById(R.id.createEventTvLocation);
        mDate = (EditText) findViewById(R.id.createEventTvDate);
        mStartTime = (EditText) findViewById(R.id.createEventTvStartTime);
        mEndTime = (EditText) findViewById(R.id.createEventTvEndTime);
        Create = (Button) findViewById(R.id.btnCreateEvent);
        Create.setEnabled(false);
        mDescription =(EditText) findViewById(R.id.createEventTvDescription);

        // reference realtime Firebase
        ref =FirebaseDatabase.getInstance().getReference();

        // Select the Startime for an event
        mStartTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEventActivity.this, android.R.style.Theme_Holo_Light_Dialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String addZero = "";
                        if(selectedMinute < 10)
                            addZero = "0";

                        String _24HourTime = selectedHour + ":" + addZero + selectedMinute;
                        SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
                        String date = "";
                        try {
                            java.util.Date _24HourDt = _24HourSDF.parse(_24HourTime);
                            date = _12HourSDF.format(_24HourDt);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if(date.substring(0,1).equals("0"))
                            date = date.substring(1);

                        mStartTime.setText(date);
                    }
                }, hour, minute, false);
                mTimePicker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        // Select the Endtime for an event
        mEndTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEventActivity.this, android.R.style.Theme_Holo_Light_Dialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String addZero = "";
                        if(selectedMinute < 10)
                            addZero = "0";

                        String _24HourTime = selectedHour + ":" + addZero + selectedMinute;
                        SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
                        String date = "";
                        try {
                            java.util.Date _24HourDt = _24HourSDF.parse(_24HourTime);
                            date = _12HourSDF.format(_24HourDt);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if(date.substring(0,1).equals("0"))
                            date = date.substring(1);

                        mEndTime.setText(date);
                    }
                }, hour, minute, false);
                mTimePicker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        // Select the Date-Month-year for an event
        mDate.setOnClickListener(new View.OnClickListener() {
            Calendar myCalendar = Calendar.getInstance();
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(CreateEventActivity.this, android.R.style.Theme_Holo_Light_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String myFormat = "MM/dd/yy"; //In which you need put here
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        mDate.setText(sdf.format(myCalendar.getTime()));
                    }

                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)) {
                    @Override
                    public void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                }.show();
            }
        });
        // Select an image from local memory
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilechooser();
            }
        });
        // Check the valid Event data
        Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = mName.getText().toString().trim();
                String description = mDescription.getText().toString().trim();
                String startTime = mStartTime.getText().toString().trim();
                String endTime = mEndTime.getText().toString().trim();
                String location = mLocation.getText().toString().trim();
                String date = mDate.getText().toString().trim();
                Boolean create = true;
                if (TextUtils.isEmpty(name)) {
                    mName.setError("Name Required.");
                    create = false;
                }
                if (TextUtils.isEmpty(description)) {
                    mDescription.setError("Description Required.");
                    create = false;
                }
                if (TextUtils.isEmpty(startTime)) {
                    mStartTime.setError("Start Time Required.");
                    create = false;
                }
                if (TextUtils.isEmpty(endTime)) {
                    mEndTime.setError("End Time Required.");
                    create = false;
                }
                if (TextUtils.isEmpty(location)) {
                    mLocation.setError("Location Required.");
                    create = false;
                }
                if (TextUtils.isEmpty(date)) {
                    mDate.setError("Date Required.");
                    create = false;
                }
                if(!RealTimeImagePath.isEmpty() && create)
                {
                    addEvent();
                    Toast.makeText(CreateEventActivity.this, "Event created", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), DashBoard.class));
                }
            }
        });
    }

    // Write the Event data from local memory to realtime Firestore
    public void addEvent(){
        String EventName = mName.getText().toString();
        String Location = mLocation.getText().toString();
        String Day = mDate.getText().toString();
        String StartTime = mStartTime.getText().toString();
        String EndTime = mEndTime.getText().toString();
        String Des = mDescription.getText().toString();
        String id = ref.push().getKey();
        String rsvpevents;
        // check if all instances filled
        if (!TextUtils.isEmpty(EventName) && !TextUtils.isEmpty(Day) && !TextUtils.isEmpty(Location) &&
                !TextUtils.isEmpty(StartTime) && !TextUtils.isEmpty(EndTime) && !TextUtils.isEmpty(Des)) {
            // make an event object with designated contructor
            event = new Event(id, EventName,"", Day, Location, StartTime, EndTime, EventName, Des, "", "", "");
            // store image uploaded to the event object
            event.setImage(RealTimeImagePath);
            User curruser = User.getInstance();
            // to point to an user
            String userId = curruser.getEmail().substring(0, curruser.getEmail().indexOf("@"));
            event.setOwner(userId+",");
            // make an event with the event's name
            ref.child("/EVENT").child(event.getName()).setValue(event);
            // creator must go to the event UserGoing list since he is the host
            ref.child("/EVENT").child(EventName).child("userGoing").setValue(userId+ ",");
            //User curruser = User.getInstance();
            String c =curruser.getCreatedEvents();
            // also, the event will go to the user's createdEvent list
            ref.child("/USER").child(userId).child("createdEvents").setValue(EventName+ "," + c);
            // Get current user's rsvp'd events
            rsvpevents = curruser.getRSVPEvents();
            // the event will also go to rsvp'd events since the creator is going to their event
            ref.child("/USER").child(userId).child("rsvpevents").setValue(rsvpevents + EventName + ",");
            // back to Dashboard
            startActivity(new Intent(getApplicationContext(), DashBoard.class));
        }

        // if one of the field is empty, prompt the user to input data again
        else {
            Toast.makeText(CreateEventActivity.this, "All fields must be entered", Toast.LENGTH_LONG).show();
        }

    }


    // select an image in the user phone's Gallery
    public void openFilechooser(){
        // create an intent so user can jump to his phone's folder to select photo
        Intent intent = new Intent(Intent.ACTION_PICK);
        // only pick image
        intent.setType("image/*");
        // grab the photo
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }
    // get the result of choosing picture in Gallery
    // write image to the FireStore
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1 && resultCode == RESULT_OK && data!=null && data.getData()!=null)
        {
            // get the data for picture chosen
            uri = data.getData();
            // set the chooseImage by the picture chosen
            chooseImage.setImageURI(uri);
            // assign the imagePath by using uri
            String url = uri.toString();
            String filename = url.substring(url.lastIndexOf("/")+1);
            Date currentime= Calendar.getInstance().getTime();
            String unique_time = currentime.toString();
            imagePath = FirebaseStorage.getInstance().getReference().child("/EVENT").child(unique_time + filename);
            //  put the picture to put in Image box
            imagePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                // if upload success, print message
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(CreateEventActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    StorageMetadata snapshotMetadata = taskSnapshot.getMetadata();
                    Task<Uri> downloadUrl = imagePath.getDownloadUrl();
                    downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            RealTimeImagePath = uri.toString();
                            Create.setEnabled(true);
                        }
                    });
                }
                // if upload fails, print message
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateEventActivity.this, "Not Uploaded" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // display the pic
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double process = (120.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                }
            });
        }
    }

}
