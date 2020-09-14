package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.collection.LLRBNode;

import java.util.ArrayList;
import java.util.List;

public class SeatBooker extends AppCompatActivity {
    private GridView list;
    ArrayList<String> data = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seatbook);
        int limit=(Integer)this.getIntent().getSerializableExtra("SeatLimit");
        setTitle((String)this.getIntent().getSerializableExtra("Flight"));
        ArrayList<Boolean>boolList=new ArrayList<>();
        ArrayList<Boolean>boolList2=new ArrayList<>();
        for(int i=0;i<limit;i++){
            data.add("S-"+i);
            boolList.add(false);
            boolList2.add(false);
        }
        List<Integer> booked=(List<Integer>)this.getIntent().getSerializableExtra("Booked");
        for(int i=0;i<booked.size();i++){
            boolList.set(booked.get(i),true);
        }
        List<Integer> bookedMy=(List<Integer>)this.getIntent().getSerializableExtra("BookedMy");
        for(int i=0;i<bookedMy.size();i++){
            boolList2.set(bookedMy.get(i),true);
        }
        GridViewCustomAdapter adapter = new GridViewCustomAdapter(this, data,boolList,boolList2);

        list = (GridView) findViewById(R.id.grid_view);
        list.setAdapter(adapter);

    }


    public class GridViewCustomAdapter extends BaseAdapter {

        ArrayList<String> items;
        ArrayList<Boolean>booked;
        ArrayList<Boolean>bookedMy;
        Activity mActivity;

        private LayoutInflater inflater = null;

        public GridViewCustomAdapter(Activity activity, ArrayList<String> tempTitle,ArrayList<Boolean> book,ArrayList<Boolean> book2) {
            mActivity = activity;
            items = tempTitle;
            booked=book;
            bookedMy=book2;
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public final int getCount() {

            return items.size();

        }

        @Override
        public final Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public final long getItemId(int position) {

            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = null;

            v = inflater.inflate(R.layout.button, null);

            Button tv = (Button) v.findViewById(R.id.button);
            tv.setText(items.get(position));
            if(!booked.get(position)) {
                final Button clone = tv;
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String[] split = clone.getText().toString().split("-");
                            int result = Integer.parseInt(split[1]);
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("result", result);
                            setResult(RESULT_FIRST_USER, resultIntent);
                            finish();
                        } catch (Exception e) {

                        }
                    }
                });
            }else{
                if(bookedMy.get(position)){
                    tv.setBackgroundColor(Color.GREEN);
                    tv.setText("M-"+position);
                    final Button clone = tv;
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                String[] split = clone.getText().toString().split("-");
                                int result = Integer.parseInt(split[1]);
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("result", result);
                                setResult(RESULT_FIRST_USER, resultIntent);
                                finish();
                            } catch (Exception e) {

                            }
                        }
                    });
                }else {
                    tv.setBackgroundColor(Color.RED);
                    tv.setText("B-"+position);
                }
            }

            return v;
        }

    }
}
