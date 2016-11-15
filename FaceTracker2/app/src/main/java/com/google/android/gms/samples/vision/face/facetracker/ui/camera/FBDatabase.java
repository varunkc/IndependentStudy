/*
package com.google.android.gms.samples.vision.face.facetracker.ui.camera;

import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.samples.vision.face.facetracker.FaceTrackerActivity;
import com.google.android.gms.samples.vision.face.facetracker.R;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutionException;

*/
/**
 * Created by itsme on 10/27/2016.
 *//*


public class FBDatabase implements Runnable {


    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseID;
    private DatabaseReference content;
   // private TextView instruction;
    @Override
    public void run() {
        firebaseDatabase = FirebaseDatabase.getInstance();

        firebaseID = firebaseDatabase.getReference("ID");
        //firebaseID.setValue("1");


        */
/****Checking for internet connectivity in the app****//*

       */
/* ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            instruction.setText("True");
        }
        else
            instruction.setText("False");*//*


        */
/*firebaseID.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String IdValue = dataSnapshot.getValue(String.class);
                //FaceTrackerActivity.this.IdNum = Integer.parseInt(IdValue);
                //IdNum++;
                setIdNum(Integer.parseInt(IdValue));
                //Log.d(TAG, "Value is: " + Integer.toString(IdNum));
                instruction.setText(Integer.toString(IdNum[0]));
                firebaseID.setValue(Integer.toString(IdNum[0]));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });*//*


        final TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        com.google.android.gms.tasks.Task<String> tcsTask = tcs.getTask();


        class FbReader implements ValueEventListener {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String IdValue = dataSnapshot.getValue(String.class);

                if(IdValue != null){
                    tcs.setResult(IdValue);
                }


                FaceTrackerActivity.IdNum[0] = Integer.parseInt(IdValue);
                //IdNum[0]++;
                //setIdNum(Integer.parseInt(IdValue));
                //Log.d(TAG, "Value is: " + Integer.toString(IdNum));
                //instruction.setText(Integer.toString( FaceTrackerActivity.IdNum[0]));
                firebaseID.setValue(Integer.toString( FaceTrackerActivity.IdNum[0] + 1));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
               // Log.w(TAG, "Failed to read value.", error.toException());
            }
        }

        FbReader fd = new FbReader();
        firebaseID.addListenerForSingleValueEvent(fd);


        try {
            Tasks.await(tcsTask);
        }catch(ExecutionException e){
            //handle exception
        }catch (InterruptedException e){
            //handle exception
        }


       */
/* Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                click.setText(Integer.toString(IdNum[0]));
            }
        };
        mHandler.postDelayed(myRunnable, 5000);
        *//*


        FaceTrackerActivity.sIdNum = Integer.toString( FaceTrackerActivity.IdNum[0]);
        content = firebaseID.getParent().child( FaceTrackerActivity.sIdNum);

        content.child("1000").setValue("1");
        //click.setText(Integer.toString(IdNum[0]));
        //firebaseID.setValue(Integer.toString(IdNum));
    }
}
*/
