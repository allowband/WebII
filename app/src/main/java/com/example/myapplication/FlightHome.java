package com.example.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.HandlerThread;
import android.provider.CalendarContract;
import android.text.InputType;
import android.text.Layout;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

public class FlightHome extends AppCompatActivity {
    User user;
    TextView header,photoAdd;
    List<CheckBox>checks;
    AppDatabase userDb;
    FlightDatabase flightDb;
    DatabaseReference flightRef,reserveRef;
    ReserveDatabase reserveDb;
    List<Flight> flightovi;
    LinearLayout linearLayout,inputLayout,childLayout;
    List<Button>listAdd,listDel,listCalendar;
    List<TextView>listTitle,listCount;
    List<Integer>listLimit;
    ScrollView sw,inputScroll;
    EditText input,sLimit,datePick;
    Button dodaj,photo;
    int br;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        br=0;


        flightRef = FirebaseDatabase
                .getInstance()
                .getReference(Constants.FLIGHT);

        reserveRef = FirebaseDatabase
                .getInstance()
                .getReference(Constants.RESERVES);

        user=(User)this.getIntent().getSerializableExtra("User");
        userDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "aerodrom").build();
        //userDb=new BioskopLogin().room;
        header= (TextView)findViewById(R.id.textBox1);
        String hText=user.firstName+" "+user.lastName+"("+user.userrole+")";
        if(user.userrole.equals("admin")) {
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputLayout.getVisibility() == View.VISIBLE) {
                        inputScroll.setVisibility(View.GONE);
                        inputLayout.setVisibility(View.GONE);
                    } else {
                        inputScroll.setVisibility(View.VISIBLE);
                        inputLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        header.setText(hText);
        flightDb = Room.databaseBuilder(getApplicationContext(), FlightDatabase.class, "flightovi").build();
        reserveDb = Room.databaseBuilder(getApplicationContext(), ReserveDatabase.class, "reservation").build();

        linearLayout = (LinearLayout) findViewById(R.id.home);
        //linearLayout.removeView(header);
        if(user.userrole.equals("admin")) {
            inputScroll=new ScrollView(this);
            inputScroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            inputLayout = new LinearLayout(this);
            inputLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            inputLayout.setOrientation(LinearLayout.VERTICAL);
            inputScroll.addView(inputLayout);
            linearLayout.addView(inputScroll);
        }
        sw=new ScrollView(this);
        childLayout=new LinearLayout(this);
        childLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        childLayout.setOrientation(LinearLayout.VERTICAL);
        sw=new ScrollView(this);
        sw.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        sw.addView(childLayout);

        listAdd=new ArrayList<>();
        listDel=new ArrayList<>();
        listCount=new ArrayList<>();
        listTitle=new ArrayList<>();
        listLimit=new ArrayList<>();
        listCalendar=new ArrayList<>();
        checks=new ArrayList<>();



        fillFlight();
        fillReserve();
        final CountDownLatch latch = new CountDownLatch(1);
        Thread uiThread = new HandlerThread("UIHandler"){
            @Override
            public void run(){
                try {
                    flightovi = flightDb.flightDao().loadAll();
                    popuniRep();
                    if(user.userrole.equals("admin")) {
                        input();
                    }
                    linearLayout.addView(sw);
                    latch.countDown(); // Release await() in the test thread.
                }catch(Exception e){

                }
            }
        };
        uiThread.start();
        try{
            latch.await();
        }catch(Exception e) {
        }




    }

    public String bytesToString(byte[]bytes){
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public byte[] stringToByte(String bText){
        return Base64.decode(bText,Base64.DEFAULT);
    }

    FlightFirebase flightFi;
    public void saveFlightFirebase(String title,int seatLimit,String places,String datum,String bytes){
        FlightFirebase userF=new FlightFirebase(title,seatLimit,places,datum,bytes);
        flightFi=userF;
        flightRef.child(title).setValue(flightFi);
    }

    public void changeUserFirebase(){
        DatabaseReference userRef= FirebaseDatabase
                .getInstance()
                .getReference(Constants.USERS);
        userRef.child(user.username).setValue(new UserFirebase(user.firstName,user.lastName,user.username,user.password,user.userrole));
    }

    public void deleteFlightFirebase(String title){
        flightRef.child(title).removeValue();
    }

    List<FlightFirebase>flights=new ArrayList<>();

    public void fillFlight(){
        flightRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    FlightFirebase flightFi=postSnapshot.getValue(FlightFirebase.class);
                    flights.add(flightFi);
                }
                collectFlight();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }


        });

    }

    private void collectFlight() {
        for(int i=0;i<flights.size();i++){
            final int cur=i;
            final CountDownLatch latch = new CountDownLatch(1);
            Thread uiThread = new HandlerThread("UIHandler"){
                @Override
                public void run(){
                    try {
                        Flight found=flightDb.flightDao().findByTitle(flights.get(cur).title);
                        if(found==null) {
                            flightDb.flightDao().insert(flights.get(cur).title,stringToByte(flights.get(cur).bytes),flights.get(cur).seatLimit,flights.get(cur).places,flights.get(cur).datum);
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
        flights=new ArrayList<>();
    }

    public void saveReserveFirebase(String flight,String user,int seat){
        ReserveFirebase reserveF=new ReserveFirebase(flight,user,seat);
        reserveRef.child(flight+""+user+""+seat).setValue(reserveF);
    }

    public void deleteReserveFirebase(String flight,String user,int seat){
        reserveRef.child(flight+""+user+""+seat).removeValue();
    }

    List<ReserveFirebase>reserves=new ArrayList<>();

    public void fillReserve(){
        reserveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    ReserveFirebase reserveFi=postSnapshot.getValue(ReserveFirebase.class);
                    reserves.add(reserveFi);
                }
                collectReserve();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }


        });

    }

    private void collectReserve() {
        for(int i=0;i<reserves.size();i++){
            final int cur=i;
            final CountDownLatch latch = new CountDownLatch(1);
            Thread uiThread = new HandlerThread("UIHandler"){
                @Override
                public void run(){
                    try {
                        Reservation found=reserveDb.reserveDao().findMySeat(reserves.get(cur).flight,reserves.get(cur).user,reserves.get(cur).seat);
                        if(found==null) {
                            reserveDb.reserveDao().insert(reserves.get(cur).flight,reserves.get(cur).user,reserves.get(cur).seat);
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
        reserves=new ArrayList<>();
    }

    boolean canToast=false;
    String toastMessage="";
    public void getToasty(String toast){
        Toast.makeText(getApplicationContext(), toast,Toast.LENGTH_SHORT).show();
        canToast=false;
    }

    public void popuniRep(){
        childLayout.removeAllViews();
        for (Flight f:flightovi) {
            bitmap = BitmapFactory.decodeByteArray(f.bitmap, 0, f.bitmap.length);
            bitchanged=true;
            addNew(f.title,false,f.seatLimit,f.datum);
        }
    }
    public void addToCalendar(String title,String desc,String location,long start){
        ContentResolver cr=this.getContentResolver();
        ContentValues cv=new ContentValues();
        cv.put(CalendarContract.Events.TITLE,title);
        cv.put(CalendarContract.Events.DESCRIPTION,desc);
        cv.put(CalendarContract.Events.EVENT_LOCATION,location);
        cv.put(CalendarContract.Events.DTSTART, start);
        cv.put(CalendarContract.Events.DTEND,start+60*60*1000);
        cv.put(CalendarContract.Events.CALENDAR_ID,1);
        cv.put(CalendarContract.Events.EVENT_TIMEZONE,Calendar.getInstance().getTimeZone().getID());
        try {
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, cv);
            getToasty("Dogadjaj je dodat u Kalendar");
        }catch(Exception e){

        }
    }


    public void input(){
        input=new EditText(this);
        input.setWidth(200);
        input.setHeight(100);
        input.setHint("Unesite destinacije");
        input.setPadding(20,20,20,20);
        inputLayout.addView(input);
        sLimit=new EditText(this);
        sLimit.setWidth(200);
        sLimit.setHeight(100);
        sLimit.setHint("Unesite broj sedista");
        sLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
        sLimit.setPadding(20,20,20,20);
        inputLayout.addView(sLimit);

        datePick=new EditText(this);
        datePick.setWidth(200);
        datePick.setHeight(100);
        datePick.clearFocus();
        datePick.setHint("Unesite datum leta");
        datePick.setPadding(20,20,20,20);
        datePick.setOnFocusChangeListener(new DateOnClickListener(this,datePick));
        inputLayout.addView(datePick);

        photoAdd=new TextView(this);
        photoAdd.setWidth(200);
        photoAdd.setHeight(100);
        photoAdd.setText("Image Loaded");
        photoAdd.setGravity(Gravity.CENTER);
        photoAdd.setPadding(20,20,20,20);
        photoAdd.setVisibility(View.GONE);
        inputLayout.addView(photoAdd);
        photo=new Button(this);
        photo.setText("Add Photo");
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });
        inputLayout.addView(photo);

        CheckBox presedanje=new CheckBox(this);
        presedanje.setText("Sa presedanjem");
        checks.add(presedanje);
        inputLayout.addView(presedanje);
        CheckBox bPresedanja=new CheckBox(this);
        bPresedanja.setText("Bez presedanja");
        checks.add(bPresedanja);
        inputLayout.addView(bPresedanja);


        dodaj=new Button(this);
        dodaj.setText("Dodaj");
        dodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!input.getText().toString().equals("") && bitchanged && hasCheck() && !datePick.getText().toString().equals("")){
                    Flight flight=new Flight();
                    flight.title=input.getText().toString();
                    flight.datum=datePick.getText().toString();
                    br++;
                    flight.fid=br;
                    //flightDb.flightDao().insert(flight.title);
                    flightovi.add(flight);
                    try {
                        int limit = Integer.parseInt(sLimit.getText().toString());
                        addNew(flight.title, true, limit,flight.datum+"09:00:00");
                        photo.setVisibility(View.VISIBLE);
                        photoAdd.setVisibility(View.GONE);
                        getToasty("Dodat let (Pritisnite ime za Pregled)");
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    //linearLayout.removeView(input);
                    //linearLayout.removeView(dodaj);
                    //input();
                }else{
                    getToasty("Moraju sva polja biti popunjena");
                }
            }
        });
        inputLayout.addView(dodaj);
    }

    boolean bitchanged=false;
    Bitmap bitmap;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                bitchanged = true;
                photoAdd.setVisibility(View.VISIBLE);
                photo.setVisibility(View.GONE);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else if(resultCode==RESULT_FIRST_USER){
            retrievedSeat=data.getIntExtra("result",0);
            retrieved=true;
        }
    }







    public boolean hasCheck(){
        for(int i=0;i<checks.size();i++){
            if(checks.get(i).isChecked()){
                return true;
            }
        }
        return false;
    }
    public String getPlaces(){
        ArrayList<String>checked=new ArrayList<>();
        String places="";
        for(int i=0;i<checks.size();i++){
            if(checks.get(i).isChecked()){
                checked.add(checks.get(i).getText().toString());
                places+=checks.get(i).getText().toString()+",";
            }
        }
        if (places != null && places.length() > 0 && places.charAt(places.length() - 1) == ',') {
            places = places.substring(0, places.length() - 1);
        }
        return places;
    }

    boolean nexist=false;
    String num;
    List<Reservation>resList;
    public void addNew(String title, boolean nov,int seatLimit,String dateMov){

        LinearLayout myLayout=new LinearLayout(this);
        myLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        myLayout.setOrientation(LinearLayout.VERTICAL);

        br++;
        TextView textView1 = new TextView(this);
        textView1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        textView1.setText(title);
        textView1.setId(br);
        textView1.setTextSize(40);
        textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)

        br++;
        ImageView poster=new ImageView(this);
        poster.setId(br);
        if(bitchanged && bitmap!=null){
            poster.setImageBitmap(bitmap);
        }
        poster.setMaxWidth(20);
        poster.setMaxHeight(10);


        br++;
        TextView resText = new TextView(this);
        resText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        resText.setId(br);
        resText.setTextSize(15);
        if(user.userrole.equals("user")){
            resText.setVisibility(View.GONE);
        }
        resText.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
        num=0+"/"+seatLimit;

        LinearLayout reserveLayout=new LinearLayout(this);
        reserveLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        reserveLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button add=new Button(this);
        add.setText("Reserve");
        br++;
        add.setId(br);
        add.setOnClickListener(new ReserveOnClickListener(title,user.username,reserveDb,this,seatLimit));
        add.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT));

        Button map=new Button(this);
        map.setText("Map");
        br++;
        map.setId(br);
        map.setOnClickListener(new MapOnClickListener(this,flightDb,title));
        map.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT));

        Button calendar=new Button(this);
        calendar.setVisibility(View.GONE);
        br++;
        calendar.setId(br);
        calendar.setOnClickListener(new AddToCalendarListener(resText,this,title,flightDb,reserveDb,user.username));
        calendar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT));

        Button delete=new Button(this);
        br++;
        delete.setId(br);
        delete.setText("Delete");
        delete.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT));
        delete.setOnClickListener(new DeleteOnClickListener(childLayout,add,delete,listAdd,listDel,title,flightDb,reserveDb,myLayout,
                textView1,listTitle,listCount,resText,listLimit,listCalendar,this));
        if(user.userrole.equals("user")){
            delete.setVisibility(View.GONE);
            add.setVisibility(View.VISIBLE);
        }else if(user.userrole.equals("admin")){
            add.setVisibility(View.GONE);
            delete.setVisibility(View.VISIBLE);
        }

        if(nov) {
            final String threadString = title;
            final String dateFil=dateMov;
            final int limit=seatLimit;
            final CountDownLatch latch = new CountDownLatch(1);
            Thread uiThread = new HandlerThread("UIHandler") {
                @Override
                public void run() {
                    try {
                        Flight found = flightDb.flightDao().findByTitle(threadString);
                        if(found==null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            bitmap.recycle();
                            flightDb.flightDao().insert(threadString,byteArray,limit,getPlaces(),dateFil);
                            saveFlightFirebase(threadString,limit,getPlaces(),dateFil,bytesToString(byteArray));
                            nexist=true;
                        }
                        resList=reserveDb.reserveDao().reservationFlight(threadString);
                        if(resList!=null){
                            num=resList.size()+"/"+limit;
                        }
                        latch.countDown(); // Release await() in the test thread.
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            };
            uiThread.start();
            try {
                latch.await();
            } catch (Exception e) {
            }
        }
        resText.setText(num);
        if(nexist || !nov){
            listTitle.add(textView1);
            listAdd.add(add);
            listDel.add(delete);
            listCount.add(resText);
            listLimit.add(seatLimit);
            listCalendar.add(calendar);
            if(bitchanged && bitmap!=null) {
                myLayout.addView(poster);
            }
            myLayout.addView(textView1);
            myLayout.addView(resText);
            reserveLayout.addView(add);
            reserveLayout.addView(delete);
            reserveLayout.addView(map);
            reserveLayout.addView(calendar);
            myLayout.addView(reserveLayout);
            childLayout.addView(myLayout);
            nexist=false;
            updateRes();
        }
        bitchanged=false;
    }

    String changes,dateCh;

    boolean hasReservation;
    public void updateRes(){
        for(int i=0;i<listCount.size();i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            final int set=i;
            hasReservation=false;
            final FlightHome home=this;
            Thread uiThread = new HandlerThread("UIHandler") {
                @Override
                public void run() {
                    try {
                        resList = reserveDb.reserveDao().reservationFlight(listTitle.get(set).getText().toString());
                        List<Reservation> myRes=reserveDb.reserveDao().findByMatch(listTitle.get(set).getText().toString(),user.username);
                        if (resList != null) {
                            changes="";
                            if(user.userrole.equals("admin")) {
                                changes = resList.size() + "/" + listLimit.get(set);
                            }
                            if(myRes!=null){
                                if(myRes.size()!=0 && user.userrole.equals("user")) {
                                    if(myRes.size()==1) {
                                        changes += " Your seat:";
                                    }else{
                                        changes += " Your seats:";
                                    }
                                    hasReservation=true;
                                }
                                for(int j=0;j<myRes.size();j++){
                                    if(j!=myRes.size()-1){
                                        changes+=myRes.get(j).seat+",";
                                    }else{
                                        changes+=myRes.get(j).seat;
                                    }
                                }
                            }
                            resList=reserveDb.reserveDao().reservationUser(user.username);

                        }else{
                            changes=0+"/"+listLimit.get(set);
                        }
                        Flight flight=flightDb.flightDao().findByTitle(listTitle.get(set).getText().toString());
                        if(flight!=null){
                            dateCh=flight.datum;
                            dateCh=dateCh.split(" ")[0];
                        }
                        latch.countDown();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            };
            uiThread.start();
            try {
                latch.await();
            } catch (Exception e) {
            }
            listCount.get(i).setText(changes);
            if(hasReservation){
                listCount.get(i).setVisibility(View.VISIBLE);
                listCalendar.get(i).setVisibility(View.VISIBLE);
                if(dateCh!=null){
                    listCalendar.get(i).setText("Add to Calendar ("+dateCh+")");
                }
            }else{
                listCount.get(i).setVisibility(View.GONE);
                listCalendar.get(i).setVisibility(View.GONE);
            }


        }
    }
    int current=-1;
    int retrievedSeat=0;
    boolean retrieved=false;
    public void getSeatNumber(String title,String cUser,int limit){
        Intent myIntent = new Intent(this, SeatBooker.class);
        current=findIndexTitle(title);
        myIntent.putExtra("User",cUser);

            myIntent.putExtra("Flight", title);

        Serializable zauzete=(Serializable)booked(title,false);
        myIntent.putExtra("Booked",zauzete);
        Serializable zauzeteMy=(Serializable)booked(title,true);
        myIntent.putExtra("BookedMy",zauzeteMy);
        myIntent.putExtra("SeatLimit",limit);
        startActivityForResult(myIntent, 1);
    }

    public int findIndexTitle(String title){
        for(int i=0;i<listTitle.size();i++){
            if(listTitle.get(i).getText().toString().equals(title)){
                return i;
            }
        }
        return -1;
    }

    public List<Integer> booked(String title,boolean users){
        List<Integer>zauzeta=new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        final String threadString=title;
        final boolean pass=users;
        Thread uiThread = new HandlerThread("UIHandler") {
            @Override
            public void run() {
                try {
                    if(pass){
                        resList = reserveDb.reserveDao().findByMatch(threadString,user.username);
                    }else {
                        resList = reserveDb.reserveDao().reservationFlight(threadString);
                    }
                    latch.countDown();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };
        uiThread.start();
        try {
            latch.await();
        } catch (Exception e) {
        }
        if(resList!=null){
            for(int i=0;i<resList.size();i++){
                zauzeta.add(resList.get(i).seat);
            }
        }
        return zauzeta;
    }

    boolean resume=false;
    @Override
    public void onResume(){
        super.onResume();
        if(retrieved && current!=-1) {
            final CountDownLatch latch = new CountDownLatch(1);
            Thread uiThread = new HandlerThread("UIHandler") {
                @Override
                public void run() {
                    try {
                        Reservation mySeat=reserveDb.reserveDao().findMySeat(listTitle.get(current).getText().toString(),user.username,retrievedSeat);
                        if(mySeat!=null){
                            reserveDb.reserveDao().deleteByUser(listTitle.get(current).getText().toString(),user.username,retrievedSeat);
                            deleteReserveFirebase(listTitle.get(current).getText().toString(),user.username,retrievedSeat);
                            toastMessage="Obrisali ste sediste ";
                            resume=true;
                        }else {
                            Reservation found = reserveDb.reserveDao().findBySeat(listTitle.get(current).getText().toString(), retrievedSeat);
                            if (found == null) {
                                reserveDb.reserveDao().insert(listTitle.get(current).getText().toString(), user.username, retrievedSeat);
                                saveReserveFirebase(listTitle.get(current).getText().toString(),user.username,retrievedSeat);
                                toastMessage="Rezervisali ste sediste "+retrievedSeat;
                                resume=true;
                            }else{
                                resume=false;
                            }
                        }
                        latch.countDown();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            };
            uiThread.start();
            try {
                latch.await();
            } catch (Exception e) {
            }
            retrieved=false;
            updateRes();
            if(resume){
                getToasty(toastMessage);
            }
        }
    }

    public class ReserveOnClickListener implements View.OnClickListener{

        String title,user;
        ReserveDatabase resDb;
        FlightHome home;
        int seatLim;

        public ReserveOnClickListener(String tit,String us,ReserveDatabase db,FlightHome bh,int limit){
            title=tit;
            user=us;
            resDb=db;
            home=bh;
            seatLim=limit;

        }

        @Override
        public void onClick(View v){
            home.getSeatNumber(title,user,seatLim);
        }
    }

    public class DeleteOnClickListener implements View.OnClickListener
    {
        FlightHome home;
        LinearLayout ll,myLay;
        Button add,delete;
        List<Button>listAdd,listDel,listCal;
        String title;
        FlightDatabase db;
        ReserveDatabase resDb;
        List<TextView>listTitl,listCont;
        List<Integer> seatLimit;
        TextView titl,cont;
        public DeleteOnClickListener(LinearLayout lin,Button dod,Button del,List<Button>lA,List<Button>lD,String ti,
                                     FlightDatabase flight,ReserveDatabase rDb,LinearLayout myL,TextView tit,List<TextView>liTe,
                                     List<TextView>con,TextView cou,List<Integer>listLim,List<Button>listCal,FlightHome home) {
            this.ll = lin;
            delete=del;
            add=dod;
            listAdd=lA;
            listDel=lD;
            title=ti;
            db=flight;
            resDb=rDb;
            myLay=myL;
            titl=tit;
            cont=cou;
            listTitl=liTe;
            listCont=con;
            seatLimit=listLim;
            this.listCal=listCal;
            this.home=home;
        }

        @Override
        public void onClick(View v)
        {
            seatLimit.remove(seatLimit.get(listTitl.indexOf(titl)));
            listCal.remove(listCal.get(listTitl.indexOf(titl)));
            listTitl.remove(titl);
            listCont.remove(cont);
            listAdd.remove(add);
            listDel.remove(delete);
            ll.removeView(myLay);
            final CountDownLatch latch = new CountDownLatch(1);
            Thread uiThread = new HandlerThread("UIHandler") {
                @Override
                public void run() {
                    try {
                        flightDb.flightDao().deleteByTitle(title);
                        List<Reservation> resFlight=resDb.reserveDao().reservationFlight(title);
                        if(resFlight!=null) {
                            for (Reservation r :resFlight){
                                home.deleteReserveFirebase(r.flight,r.user,r.seat);
                            }
                        }
                        resDb.reserveDao().deleteByFlight(title);
                        home.deleteFlightFirebase(title);
                        latch.countDown(); // Release await() in the test thread.
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            };
            uiThread.start();
            try {
                latch.await();
            } catch (Exception e) {
            }
        }

    }
    public class MapOnClickListener implements View.OnClickListener{

        FlightHome home;
        FlightDatabase flightDb;
        String title;
        String places;
        public MapOnClickListener(FlightHome fh,FlightDatabase flight,String naslov){
            home=fh;
            flightDb=flight;
            title=naslov;
        }

        @Override
        public void onClick(View v){
            final CountDownLatch latch = new CountDownLatch(1);
            Thread uiThread = new HandlerThread("UIHandler") {
                @Override
                public void run() {
                    try {
                        Flight flight=flightDb.flightDao().findByTitle(title);
                        if(flight!=null){
                            places=flight.places;
                        }
                        latch.countDown(); // Release await() in the test thread.
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            };
            uiThread.start();
            try {
                latch.await();
            } catch (Exception e) {
            }
            if(places!=null){
                Intent myIntent = new Intent(home, MapsActivity.class);
                myIntent.putExtra("Places",places);
                myIntent.putExtra("Flight",title);
                startActivityForResult(myIntent, 1);
            }
        }
    }
    public class DateOnClickListener implements View.OnFocusChangeListener, DatePickerDialog.OnDateSetListener{
        EditText _editText;
        private int _day;
        private int _month;
        private int _year;
        FlightHome flight;
        public DateOnClickListener(FlightHome bh, EditText et)
        {
            flight=bh;
            this._editText = et;
            this._editText.setOnFocusChangeListener(this);
        }

        @Override
        public void onFocusChange(View v,boolean hasFocus){
            if(hasFocus) {
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

                DatePickerDialog dialog = new DatePickerDialog(flight, this,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            _year = year;
            _month = monthOfYear;
            _day = dayOfMonth;
            updateDisplay();
        }

        public void updateDisplay() {
            _editText.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(_day).append("-").append(_month + 1).append("-").append(_year).append(" "));
        }

    }

    public class AddToCalendarListener implements View.OnClickListener{
        TextView count;
        FlightHome fh;
        String title,dateSt="",userN,seats="Seats: ",location="";
        FlightDatabase fdb;
        ReserveDatabase rdb;
        public AddToCalendarListener(TextView resText,FlightHome fh,String title,FlightDatabase fdb,ReserveDatabase rdb,String userN){
            count=resText;
            this.fh=fh;
            this.title=title;
            this.fdb=fdb;
            this.rdb=rdb;
            this.userN=userN;
        }

        @Override
        public void onClick(View v){
            if(count.getText().toString().split(":").length>1){
                final CountDownLatch latch = new CountDownLatch(1);
                Thread uiThread = new HandlerThread("UIHandler") {
                    @Override
                    public void run() {
                        try {
                            Flight flight=fdb.flightDao().findByTitle(title);
                            if(flight!=null){
                                dateSt=flight.datum;
                                location=flight.places;
                            }
                            List<Reservation> listRe=rdb.reserveDao().reservationUser(userN);
                            if(listRe!=null){
                                for(int i=0;i<listRe.size();i++){
                                    if(i!=listRe.size()-1){
                                        seats+=listRe.get(i).seat+",";
                                    }else{
                                        seats+=listRe.get(i).seat;
                                    }
                                }
                            }
                            latch.countDown(); // Release await() in the test thread.
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                };
                uiThread.start();
                Date datum=new Date();
                try {
                    latch.await();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                    datum=sdf.parse(dateSt);
                } catch (Exception e) {
                }
                fh.addToCalendar(title,seats,location,datum.getTime());
            }
        }
    }
}
