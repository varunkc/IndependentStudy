/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.Task;
//import com.google.android.gms.samples.vision.face.facetracker.ui.camera.FBDatabase;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Landmark;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static android.R.attr.value;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static com.google.android.gms.vision.face.Landmark.NOSE_BASE;
import static java.lang.Boolean.TRUE;
import static java.sql.Types.NULL;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */


public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private RelativeLayout doIT;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private Handler mHandler = new Handler();
    //extra additions
    private Button click;
    private TextView instruction;
    private EditText userData;
    private TextView fixed;
    private int countClicks, nextIndex;

    private int[] alreadyGen;
    private String userName;

    private Random genIndex;

    //database

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseID;
    private DatabaseReference content;
    public String sIdNum;
    public final int[] IdNum = {0, 0};



    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */

    //TextWatcher added function
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
        {
            //checkFieldForEmptyValues();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            checkFieldForEmptyValues();
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //checkFieldForEmptyValues();
        }
    };


    protected View.OnKeyListener detectEnter = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if ((event.getAction() == KeyEvent.ACTION_DOWN) && click.isEnabled() && (keyCode == KEYCODE_ENTER)) {
                doNext(v);

            }
            return false;
        }

    };

    //check user input for empty values
    protected void checkFieldForEmptyValues(){
        String inputValue = userData.getText().toString();

        if(inputValue.equals(""))
            click.setEnabled(false);
        else
            click.setEnabled(true);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);


        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        //mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }


        //FirebaseDatabase database = FirebaseDatabase.getInstance("https://console.firebase.google.com/project/facetracker2/database/data");
        //DatabaseReference myRef = database.getReference("message");

        //myRef.setValue("Hello, World!");


        //*******added code******


        click =(Button) findViewById(R.id.nextClick);
        instruction = (TextView) findViewById(R.id.instruct);
        fixed = (TextView) findViewById(R.id.fixedInstruction);
        userData = (EditText) findViewById(R.id.userInput);
        userName="";
        countClicks = 0;
        alreadyGen = new int[31];
        genIndex = new Random();

        //listener to check the text entered in that field
        userData.setText("");
        userData.addTextChangedListener(textWatcher);
        userData.setOnKeyListener(detectEnter);
        checkFieldForEmptyValues();

        //FBDatabase fb = new FBDatabase();

       // fb.run();
        //*******added code******


        firebaseDatabase = FirebaseDatabase.getInstance();

        firebaseID = firebaseDatabase.getReference("ID");
        content = firebaseDatabase.getReference("ID");
        //firebaseID.setValue("1");


        /***Checking for internet connectivity in the app***/
        /*ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            instruction.setText("True");
        }
        else
            instruction.setText("False");*/

       /* firebaseID.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String IdValue = dataSnapshot.getValue(String.class);
                //FaceTrackerActivity.this.IdNum = Integer.parseInt(IdValue);
                //IdNum++;
                //setIdNum(Integer.parseInt(IdValue));
                //Log.d(TAG, "Value is: " + Integer.toString(IdNum));
                instruction.setText(Integer.toString(IdNum[0]));
                firebaseID.setValue(Integer.toString(IdNum[0]));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });*/

        /*final TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        com.google.android.gms.tasks.Task<String> tcsTask = tcs.getTask();*/
        //unable to synchronize data

        class FbReader implements ValueEventListener {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String IdValue = dataSnapshot.getValue(String.class);

                /*if(IdValue != null){
                    tcs.setResult(IdValue);
                }*/ //unable to synchronize data


                FaceTrackerActivity.this.IdNum[0] = Integer.parseInt(IdValue);
                //IdNum[0]++;
                //setIdNum(Integer.parseInt(IdValue));
                //Log.d(TAG, "Value is: " + Integer.toString(IdNum));
                //instruction.setText(Integer.toString(IdNum[0]));
                content = firebaseID.getParent().child(String.valueOf(IdNum[0]));
                firebaseID.setValue(Integer.toString(IdNum[0] + 1));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        }

        FbReader fd = new FbReader();
        firebaseID.addListenerForSingleValueEvent(fd);


        /*try {
            Tasks.await(tcsTask);
        }catch(ExecutionException e){
            //handle exception
        }catch (InterruptedException e){
            //handle exception
        }*/ //unable to synchronize data

        sIdNum = Integer.toString(IdNum[0]+1);
      /*  Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                //click.setText(Integer.toString(IdNum[0]));

                //firebaseID.setValue(sIdNum);
                //content = content.child(sIdNum);
                //click.setText(sIdNum);
                //  content = firebaseID.getParent().child(sIdNum);
//                content.child("1000").child("x").setValue("1");
//                content.child("1000").child("y").setValue("1");
                //content.child("1000").child("2").setValue("1");

            }
        };
        mHandler.postDelayed(myRunnable, 5000);*/




        //click.setText(Integer.toString(IdNum[0]));


    }


    public void doNext(View v) {


        if (userName.equals("")) {
            userName = userData.getText().toString();
            String textToDisplay = "";
            textToDisplay = textToDisplay + getResources().getString(R.string.greet);
            textToDisplay += userName;
            textToDisplay = textToDisplay + (getResources().getString(R.string.beginContext));
            userData.setText("");
            instruction.setText(textToDisplay);

            /*Storing username into Firebase*/

            content.child("Username").setValue(userName);

            // pause p = new pause(5);

            final String finalTextToDisplay = textToDisplay;
            final String finalTextToDisplay1 = textToDisplay;

            final CountDownTimer timer = new CountDownTimer(6000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if (millisUntilFinished > 4000)
                        instruction.setText(finalTextToDisplay1);
                    else {
                        instruction.setText("");
                        instruction.setText(Long.toString((int) (millisUntilFinished / 1000)));
                    }
                }


                @Override
                public void onFinish() {
                   /* instruction.setText("");
                    instruction.append(getResources().getString(R.string.toStart));
                    this.cancel();
                    */
                    alreadyGen[0] = genIndex.nextInt(getResources().getStringArray(R.array.commonTexts).length);
                    //instruction.setText("Test 1";);
                    userData.clearComposingText();
                    instruction.setText(getResources().getStringArray(R.array.commonTexts)[alreadyGen[0]]);
                    fixed.setVisibility(View.VISIBLE);


                    alreadyGen[1] = -1;
                    countClicks++;

                    Date tCompleted = new Date(NULL);
                    SimpleDateFormat simpleDF = new SimpleDateFormat();
                    final TextView _tv = (TextView) findViewById(R.id.timer);
                    _tv.setVisibility(View.VISIBLE);
                   /* new CountDownTimer(300000, 100) {
                        int sec = 0;
                        public void onTick(long millisUntilFinished) {
                            long timeCompleted = 300000 - millisUntilFinished;

                            _tv.setText("" +simpleDF("mm:ss").format(tCompleted( timeCompleted )));

                            content.child(Integer.toString(sec)).child("x").setValue((int)FaceGraphic.x);
                            content.child(Integer.toString(sec++)).child("y").setValue((int)FaceGraphic.y);
                        }

                        public void onFinish() {
                            _tv.setText("done!");
                        }
                    }.start();


                }


            };*/

                    new CountDownTimer(200000, 100) {
                        int sec = 0;
                        @Override
                        public void onTick(long millisUntilFinished) {
                            long timeCompleted = 200000 - millisUntilFinished;
                            _tv.setText(String.format("%02d : %02d",
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),

                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));

                                    /*TimeUnit.MILLISECONDS.toMillis(timeCompleted) -
                                            TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(timeCompleted) -
                                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeCompleted)))*/


                                    content.child(Integer.toString(sec)).child("x").setValue((int)FaceGraphic.x);
                                    content.child(Integer.toString(sec++)).child("y").setValue((int)FaceGraphic.y);

                        }

                        @Override
                        public void onFinish() {

                        }
                    }.start();

                }
            };
            timer.start();


        }
        else{
            if(!instruction.getText().toString().equals(userData.getText().toString())){
                AlertDialog alertDialog = new AlertDialog.Builder(FaceTrackerActivity.this).create();
                alertDialog.setTitle("Error\n");
                alertDialog.setMessage("Oops! Concentrate more and try again!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                userData.setText("");
                return;
            }
            else{
               /* Random rand = new Random();
                int n= rand.nextInt(30);
                String message = userData.getText().toString();
                String faceid = Integer.toString(n);
                timeToStore(faceid ,message);   //store in database */

                /**Alert box on successful entry by the user**/


                /*AlertDialog alertDialog = new AlertDialog.Builder(FaceTrackerActivity.this).create();

                alertDialog.setTitle("Success" );
                alertDialog.setMessage("Click 'OK' for next quote");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();*/
                //userData.clearComposingText();
                Toast.makeText(getApplicationContext(), "Go Ahead :)",
                        Toast.LENGTH_SHORT).show();
                userData.setText("");
            }

//            if(countClicks >= 30)
//                finish();

            while(true){

                nextIndex = (genIndex.nextInt(getResources().getStringArray(R.array.commonTexts).length));
                boolean found = false;
                for(int i = 0; i < alreadyGen.length && alreadyGen[i] != -1; i++) {
                    if (alreadyGen[i] == nextIndex) {
                        found = true;
                        break;
                    }
                }
                if(found)
                    continue;
                else {
                    countClicks++;
                    break;
                }
            }
            /*if (nextIndex > 30)
                nextIndex = 0;*/
            instruction.setText(getResources().getStringArray(R.array.commonTexts)[nextIndex]);
            userData.clearComposingText();


        }
    }
    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
   /* public void timeToStore(String faceid,String message)
    {
        faceInfoHelper = new FaceInfoHelper(getApplicationContext());
        sqlitedatabase = openOrCreateDatabase("FaceInfo.db",context.MODE_PRIVATE,null);
        faceInfoHelper.InsertInfo(faceid ,message,sqlitedatabase );

        Toast.makeText(getBaseContext(),"Data inserted and saved!!",Toast.LENGTH_LONG).show();
        faceInfoHelper.close();
    }*/
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(TRUE)   //extra code added , adds overhead
                .setLandmarkType(FaceDetector.NO_LANDMARKS) //extra code added, adds overhead
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT) //VALUE CHANGED FROM BACK TO FRONT
                .setRequestedFps(30.0f)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }








}
