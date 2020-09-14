package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.room.Database;
import androidx.room.Room;


import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static android.content.ContentValues.TAG;


public class AppLogin extends Activity implements Serializable {
    EditText username,password,name,lastname;
    Spinner dropdown;
    LinearLayout mainLayout,formLayout,buttonLayout;
    ScrollView scrollView;
    DatabaseReference referenceRef;

    static AppDatabase room;
    User user;
    Cursor data;
    boolean change=false;


    public class RunDatabase implements Runnable{
        boolean register;
        AppLogin log;
        public RunDatabase(AppDatabase data,EditText first,EditText last,EditText user,EditText pass,Spinner spin,AppLogin log){
            room=data;
            username=user;
            lastname=last;
            name=first;
            password=pass;
            dropdown=spin;
            this.log=log;
        }
        @Override
        public void run() {
            try {
                User found = room.userDao().findByName(username.getText().toString(), password.getText().toString());

                if(found==null) {
                    room.userDao().insert(name.getText().toString(), lastname.getText().toString(), username.getText().toString(), password.getText().toString(),dropdown.getSelectedItem().toString());
                    log.saveUserFirebase(name.getText().toString(), lastname.getText().toString(), username.getText().toString(), password.getText().toString(),dropdown.getSelectedItem().toString());
                }
            }catch(NullPointerException e){

            }

        }
    }

    final AppLogin flightLogin=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        referenceRef = FirebaseDatabase
                .getInstance()
                .getReference(Constants.USERS);
        room = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "airport").build();
        mainLayout=(LinearLayout)findViewById(R.id.mainLayout);
        buttonLayout=(LinearLayout)findViewById(R.id.buttonLayout);
        formLayout=(LinearLayout)findViewById(R.id.formLayout);
        scrollView=(ScrollView)findViewById(R.id.scrollview);
        username = (EditText)findViewById(R.id.editText1);
        password = (EditText)findViewById(R.id.editText2);
        name = (EditText)findViewById(R.id.editText3);
        lastname = (EditText)findViewById(R.id.editText4);
        dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"admin", "user"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        LinearLayout found=formLayout.findViewById(R.id.buttonLayout);
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                if(found!=null){
                    formLayout.removeView(buttonLayout);
                    mainLayout.addView(buttonLayout);
                }
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if(found==null){
                    mainLayout.removeView(buttonLayout);
                    formLayout.addView(buttonLayout);
                }
                break;
        }

        name.setVisibility(View.GONE);
        lastname.setVisibility(View.GONE);
        dropdown.setVisibility(View.GONE);
        saveUserFirebase("admin", "admin", "admin", "admin","admin");
        Button register = (Button) findViewById(R.id.Button02);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getVisibility()==View.GONE){
                    name.setVisibility(View.VISIBLE);
                    lastname.setVisibility(View.VISIBLE);
                    dropdown.setVisibility(View.VISIBLE);
                }else{
                    if((dropdown.getSelectedItem().toString().equals("user") || dropdown.getSelectedItem().toString().equals("admin"))&&
                            !name.getText().toString().equals("") && !lastname.getText().toString().equals("") && !username.getText().toString().equals("") && !password.getText().toString().equals("")){
                        Thread t1 = new Thread(new AppLogin().new RunDatabase(room,name,lastname,username,password,dropdown,flightLogin));
                        t1.start();
                        name.setVisibility(View.GONE);
                        lastname.setVisibility(View.GONE);
                        dropdown.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Uspela registracija!",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Polja ne smeju biti prazna",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        Button login = (Button) findViewById(R.id.Button01);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(name.getVisibility()==View.VISIBLE){
                    name.setVisibility(View.GONE);
                    lastname.setVisibility(View.GONE);
                    dropdown.setVisibility(View.GONE);
                }
                if(username.getText().toString().equals("admin") &&
                        password.getText().toString().equals("admin")) {
                    final CountDownLatch latch = new CountDownLatch(1);
                    Thread uiThread = new HandlerThread("UIHandler"){
                        @Override
                        public void run(){
                            try {
                                user=room.userDao().findByName("admin","admin");
                                if(user==null) {
                                    room.userDao().insert("Admin", "Admin", "admin", "admin", "admin");
                                    user = room.userDao().findByName("admin", "admin");
                                }
                                latch.countDown(); // Release await() in the test thread.
                            }catch(NullPointerException e){
                                Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    uiThread.start();
                    try{
                        latch.await();
                    }catch(Exception e) {
                    }
                    Intent myIntent = new Intent(view.getContext(), FlightHome.class);
                    myIntent.putExtra("User",user);
                    startActivityForResult(myIntent, 0);
                }else{
                    final CountDownLatch latch = new CountDownLatch(1);
                    Thread uiThread = new HandlerThread("UIHandler"){
                        @Override
                        public void run(){
                            try {
                                user = room.userDao().findByName(username.getText().toString(), password.getText().toString());
                                if(user!=null) {
                                    change = true;
                                }
                                latch.countDown(); // Release await() in the test thread.
                            }catch(NullPointerException e){
                                Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    uiThread.start();
                    try{
                        latch.await();
                    }catch(Exception e) {
                    }
                    if(change) {
                        //Toast.makeText(getApplicationContext(), "Redirecting...", Toast.LENGTH_SHORT).show();
                        Intent myIntent = new Intent(view.getContext(), FlightHome.class);
                        myIntent.putExtra("User",user);
                        //myIntent.putExtra("UserDatabase",room);
                        //myIntent.putExtra("Home",flightLogin);
                        startActivityForResult(myIntent, 0);
                        change=false;
                    }else{
                        Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        fillBase();
    }

    /*@Override
    public void onResume(){
        super.onResume();
        fillBase();
    }*/

    String usernameT="";
    UserFirebase userFi;
    public void saveUserFirebase(String firstName,String lastName,String username,String password,String userrole){
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child(username);
        usernameT=username;
        UserFirebase userF=new UserFirebase(firstName,lastName,username,password,userrole);
        userFi=userF;
        referenceRef.child(usernameT).setValue(userFi);


    }
    List<UserFirebase>users=new ArrayList<>();
    public void fillBase(){
        referenceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    UserFirebase userFi=postSnapshot.getValue(UserFirebase.class);
                    users.add(userFi);
                }
                collectUsers();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }


        });

    }

    boolean roomEmpty=false;
    public boolean roomEmpty(){
        final CountDownLatch latch = new CountDownLatch(1);
        Thread uiThread = new HandlerThread("UIHandler"){
            @Override
            public void run(){
                try {
                    List<User> users=room.userDao().getAll();
                    roomEmpty=users==null;
                    latch.countDown(); // Release await() in the test thread.
                }catch(NullPointerException e){

                }
            }
        };
        uiThread.start();
        try{
            latch.await();
        }catch(Exception e) {
        }
        return roomEmpty;
    }

    private void collectUsers() {
        for(int i=0;i<users.size();i++){
            final int cur=i;
            final CountDownLatch latch = new CountDownLatch(1);
            Thread uiThread = new HandlerThread("UIHandler"){
                @Override
                public void run(){
                    try {
                        user=room.userDao().findByName(users.get(cur).username,users.get(cur).password);
                        if(user==null) {
                            room.userDao().insert(users.get(cur).firstname, users.get(cur).lastname, users.get(cur).username, users.get(cur).password, users.get(cur).userrole);
                        }
                        latch.countDown(); // Release await() in the test thread.
                    }catch(NullPointerException e){

                    }
                }
            };
            uiThread.start();
            try{
                latch.await();
            }catch(Exception e) {
            }
        }
    }
}
