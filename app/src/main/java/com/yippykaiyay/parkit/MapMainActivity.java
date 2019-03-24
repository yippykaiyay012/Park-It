package com.yippykaiyay.parkit;


import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

//import com.google.android.gms.location.LocationListener;


import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MapMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener {


    UserLocalStore userLocalStore;
    ParkedLocalStore parkedLocalStore;
    TimerLocalStore timerLocalStore;


    private GoogleMap mMap;

    Context context = this;


    //permission activity?
    public Activity activity2;

    //permission context?
    private Context context2;


    //permission code variable
    private static final int PERMISSION_REQUEST_CODE = 1;


    SupportMapFragment SupMapFragment;




    LocationManager mng;
    //LocationListener locationListener;
    String bestProvider;
    Criteria criteria;
    public double latitude;
    public double longitude;




    String timeString;






    //Check if a user is logged in before displaying main activity
    //if not, start log in screen
    @Override
    protected void onStart() {

        super.onStart();
        if (authenticate() == false) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }


    }


    @Override
    protected void onResume(){
        super.onResume();
        if (authenticate() == false) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        getLocation();


    }

    @Override
    protected void onPause(){

        //Stopping GPS when user switches out of app, save battery.
        if (checkPermission()) {
            mng.removeUpdates(this);
        }

        super.onPause();

    }


    //checking if user exists logged in
    private boolean authenticate() {

        if (userLocalStore.getLoggedInUser() == null) {
            return false;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        userLocalStore = new UserLocalStore(this);
        parkedLocalStore = new ParkedLocalStore(this);
        timerLocalStore = new TimerLocalStore(this);




        //permission activity?
        activity2 = this;

        //permission context?
        context2 = getApplicationContext();



        mng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkPermission()) {
            getLocation();
            //trying to get better accuracy of gps locations. "getLastKnownLocation" is not accurate. seems to default to house when at uni.
            //locationListener = new MyLocationListener();
            mng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            bestProvider = String.valueOf(mng.getBestProvider(criteria, true));
            Location location = mng.getLastKnownLocation(bestProvider);


            mng.requestLocationUpdates(bestProvider, 0, 0, this);

            if (location == null) {

                mng.requestLocationUpdates(mng.GPS_PROVIDER,0,0,this);

            }else{
                getLocation();
            }




            //mng.requestSingleUpdate(new Criteria(),locationListener, Looper.myLooper());

        }else{
            requestPermission();
        }


        SupMapFragment = SupportMapFragment.newInstance();


        setContentView(R.layout.map_activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        SupMapFragment.getMapAsync(this);



        getLocation();

        //setting normal map option to be selected by default
        navigationView.setCheckedItem(R.id.nav_normal_map);


        if (authenticate() == false) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }


        showMap();


    }





    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            AlertDialog.Builder dialogBuilderLogOut = new AlertDialog.Builder(MapMainActivity.this);
            dialogBuilderLogOut.setTitle("Sign Out?");
            dialogBuilderLogOut.setMessage("Are You Sure You Want To Sign Out?");
            dialogBuilderLogOut.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logOut();
                }
            });
            dialogBuilderLogOut.setNegativeButton("Not Now", null);
            dialogBuilderLogOut.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_main, menu);


        //if there is a timer currently active. display the expiry time in the menu.
        if (isTimerRunning() == true) {

            TextView textViewMenuTime = (TextView) findViewById(R.id.textViewMenuTime);
            ImageView alarmSetImage = (ImageView) findViewById(R.id.alarmSetImage);

            textViewMenuTime.setVisibility(View.VISIBLE);
            alarmSetImage.setVisibility(View.VISIBLE);


            String menuTime = timerLocalStore.getTimerInfo();

            textViewMenuTime.setText(menuTime);

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.


        int id = item.getItemId();


        if (id == R.id.nav_search_location) {

            onSearch();

        } else if (id == R.id.nav_park_car) {

            saveCurrentLocation();


        } else if (id == R.id.parking_timer){

            parkingTimer();

        } else if (id == R.id.nav_show_parking) {

            populateMap();

        } else if (id == R.id.nav_normal_map) {

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        } else if (id == R.id.nav_satellite_map) {

            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        } else if (id == R.id.nav_sign_out) {

            AlertDialog.Builder dialogBuilderLogOut = new AlertDialog.Builder(MapMainActivity.this);
            dialogBuilderLogOut.setTitle("Sign Out?");
            dialogBuilderLogOut.setMessage("Are You Sure You Want To Sign Out?");
            dialogBuilderLogOut.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logOut();
                }
            });
            dialogBuilderLogOut.setNegativeButton("Not Now", null);
            dialogBuilderLogOut.show();

        } else if (id == R.id.nav_help_about) {

            informationScreen();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }



    //inflates information screen
    public void informationScreen(){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View v = layoutInflater.inflate(R.layout.how_to_popup, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);
        builder.setCancelable(false);
        builder.setPositiveButton("Done", null);
        builder.show();
    }








    //method for allowing user to set parking time limit. possible notification when time is up.
    public void parkingTimer() {



        //SHOULD ADD A BOOLEAN TO BE SET TRUE AFTER TIMER SET
        //IF BOOLEAN TRUE OPEN DIALOGUE TO SEE TIME REMAINING AND OFFER CANCEL TIMER.
        //implemented




        if (isTimerRunning() == true) {

            AlertDialog.Builder dialogBuilderTimerRunning = new AlertDialog.Builder(MapMainActivity.this);
            dialogBuilderTimerRunning.setIcon(R.mipmap.ic_parking_timer);
            dialogBuilderTimerRunning.setTitle("Timer is Currently Running");
            dialogBuilderTimerRunning.setMessage("Cancel This Timer?");
            dialogBuilderTimerRunning.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //cancel ongoing timer/alarm service
                    cancelAlarm();
                }

            });
            dialogBuilderTimerRunning.setNegativeButton("No", null);
            dialogBuilderTimerRunning.show();
        } else {


            LayoutInflater layoutInflater = LayoutInflater.from(context);
            final View v = layoutInflater.inflate(R.layout.map_timer, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(v);
            builder.setCancelable(false);

            final SeekBar seekBar = (SeekBar) v.findViewById(R.id.seekBar);
            final TextView textTime = (TextView) v.findViewById(R.id.textTime);
            final Vibrator vibe = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
            final Button buttonPlus = (Button) v.findViewById(R.id.buttonPlus);
            final Button buttonMinus = (Button) v.findViewById(R.id.buttonMinus);

            //buttons for fine control of time setting
            buttonMinus.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    seekBar.setProgress(seekBar.getProgress() - 1);
                }
            });
            buttonPlus.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    seekBar.setProgress(seekBar.getProgress() + 1);
                }
            });


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    //Making seekbar update in 5 min increments with "if divisible by 5"
                    //if(progress % 5 == 0) {

                    //taking the progress int and converting it to a time formatted HH:MM string to display.
                    int min = progress;
                    long hours = TimeUnit.MINUTES.toHours(progress);
                    long remainMinute = min - TimeUnit.HOURS.toMinutes(hours);
                    String resultTime = String.format("%02d", hours) + ":"
                            + String.format("%02d", remainMinute);

                    textTime.setText(resultTime);


                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


            builder.setPositiveButton("Set Timer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    final int mins = seekBar.getProgress();

                    if (mins == 0) {
                        AlertDialog.Builder noTimeChosen = new AlertDialog.Builder(MapMainActivity.this);
                        noTimeChosen.setTitle("No Time Limit Selected!");
                        noTimeChosen.setIcon(R.mipmap.ic_menu_about);
                        noTimeChosen.setMessage("Please Choose A Valid Time Limit");
                        noTimeChosen.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                parkingTimer();
                            }
                        });
                        noTimeChosen.show();

                    } else {


                        setAlarm(mins);


                        vibe.vibrate(500);
                        AlertDialog.Builder dialogBuilderTimerInMenu = new AlertDialog.Builder(MapMainActivity.this);
                        dialogBuilderTimerInMenu.setTitle("Timer Set!");
                        dialogBuilderTimerInMenu.setIcon(R.mipmap.ic_parking_timer);
                        dialogBuilderTimerInMenu.setMessage("Expiry Time Is Displayed In The Menu Drawer");
                        dialogBuilderTimerInMenu.setPositiveButton("Ok", null);
                        dialogBuilderTimerInMenu.show();

                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

        }
    }



    //Starting the alarm service with the chosen time from user.
    public void setAlarm(int mins){


        Long alertTime = new GregorianCalendar().getTimeInMillis()+mins*60000;

        Intent alertIntent = new Intent(this, AlertReceiver.class);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime, PendingIntent.getBroadcast(this, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));


        final TextView textViewMenuTime = (TextView) findViewById(R.id.textViewMenuTime);
        final ImageView alarmSetImage = (ImageView) findViewById(R.id.alarmSetImage);

        textViewMenuTime.setVisibility(View.VISIBLE);
        alarmSetImage.setVisibility(View.VISIBLE);


        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeString = formatter.format(new Date(alertTime));

        textViewMenuTime.setText(timeString);

        timerLocalStore.storeTimerData(timeString);
        timerLocalStore.isTimerRunning(true);

    }




    //cancelling the alarm service and offering to start a new timer.
    public void cancelAlarm(){

        Intent alertIntent = new Intent(this, AlertReceiver.class);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(PendingIntent.getBroadcast(this, 1, alertIntent, PendingIntent.FLAG_CANCEL_CURRENT));


        timerLocalStore.clearTimerData();
        timerLocalStore.isTimerRunning(false);


        final TextView textViewMenuTime = (TextView) findViewById(R.id.textViewMenuTime);;
        final ImageView alarmSetImage = (ImageView) findViewById(R.id.alarmSetImage);

        textViewMenuTime.setVisibility(View.INVISIBLE);
        alarmSetImage.setVisibility(View.INVISIBLE);


        AlertDialog.Builder dialogTimerCancelled = new AlertDialog.Builder(MapMainActivity.this);
        dialogTimerCancelled.setIcon(R.mipmap.ic_menu_about);
        dialogTimerCancelled.setTitle("Timer Cancelled!");
        dialogTimerCancelled.setMessage("Set A New Timer?");
        dialogTimerCancelled.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                parkingTimer();
            }
        });
        dialogTimerCancelled.setNegativeButton("No", null);
        dialogTimerCancelled.show();

    }



    //attempting to clear all parking timer variables after timer is complete.
    //unable to call this method from alertReciever class without crashing.
    public void alarmFinished(){

        timerLocalStore.isTimerRunning(false);
        timerLocalStore.clearTimerData();

        final TextView textViewMenuTime = (TextView) findViewById(R.id.textViewMenuTime);
        final ImageView alarmSetImage = (ImageView) findViewById(R.id.alarmSetImage);

        textViewMenuTime.setVisibility(View.INVISIBLE);
        alarmSetImage.setVisibility(View.INVISIBLE);

    }


    private boolean isTimerRunning() {
        if (timerLocalStore.getTimerInfo() == null) {
            return false;
        }
        return true;
    }





    //No longer completely necessary since no more fragments are implemented
    public void showMap() {

        android.support.v4.app.FragmentManager sFm = getSupportFragmentManager();


        //Stopping always visible map
        if (SupMapFragment.isAdded())
            sFm.beginTransaction().hide(SupMapFragment).commit();

        //if not added --- add   if added then show
        if (!SupMapFragment.isAdded())
            sFm.beginTransaction().add(R.id.map, SupMapFragment).commit();
        else
            sFm.beginTransaction().show(SupMapFragment).commit();
    }






    //clears local store of user details and parked car details, now also clears alarm data ------------------------------------------------------------------------------------
    public void logOut() {
        userLocalStore.clearUserData();
        parkedLocalStore.clearParkedData();

        userLocalStore.setUserLoggedIn(false);
        parkedLocalStore.setUserParked(false);

        cancelAlarm();
        Intent loginIntent = new Intent(this, Login.class);
        startActivity(loginIntent);
    }




    //Method takes marker info from longpress on map and sends it to server database. shows alert once successful ------------------
    private void addMarker(Marker marker) {
        ServerRequests serverRequest = new ServerRequests(this);
        serverRequest.storeMarkerDataInBackground(marker, new GetMarkerCallback() {
            @Override
            public void done(Marker returnedMarker) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MapMainActivity.this);
                dialogBuilder.setMessage("Pin Submitted");
                dialogBuilder.setPositiveButton("Ok", null);
                dialogBuilder.show();
            }
        });
    }




    //method for "park-it" feature --- saving current location ---------------------------------------------------------------------
    private void saveCurrentLocation() {




        //current time stamp formatted to HH MM
        final Long tsLong = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date resultdate = new Date(tsLong);
        final String TS = (sdf.format(resultdate));




        //Checking if car is already parked. If so offer to remove previous location. else carry on with saving location
        if (isParked() == true) {

            String setOn = parkedLocalStore.getParkedLocation().snippet;
            String setOn1 = setOn.replaceAll("parked at: ", "");


            AlertDialog.Builder dialogBuilderDelete = new AlertDialog.Builder(MapMainActivity.this);
            dialogBuilderDelete.setTitle("Parked Location Already Exists");
            dialogBuilderDelete.setMessage("Remove Location Set On: " + setOn1 + "?");
            dialogBuilderDelete.setPositiveButton("Remove", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    parkedLocalStore.clearParkedData();
                    parkedLocalStore.setUserParked(false);

                    mMap.clear();
                    populateMap();
                    saveCurrentLocation();

                    Toast.makeText(context, "Removed. You can now Park-it! again.", Toast.LENGTH_LONG).show();


                }
            });
            dialogBuilderDelete.setNegativeButton("Not now", null);
            dialogBuilderDelete.show();
        }else {



            final Vibrator vibe = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);



            //final LocationManager mng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (checkPermission()) {
                if (!mng.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MapMainActivity.this);
                    dialogBuilder.setIcon(R.mipmap.ic_mascot);
                    dialogBuilder.setTitle("Park-It!");
                    dialogBuilder.setMessage("Save your current location?");
                    dialogBuilder.setPositiveButton("Park-It!", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            //This needs to be changed to add location to a local database.
                            //the position (if exists) should then be added to the map at the same time as other parking pins.
                            //Stops the user losing parked location if application is cleared from memory.
                            //*********NOW IMPLEMENTED********

                            vibe.vibrate(100);

                            if (checkPermission()) {

                               // LocationManager mng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                Location location = mng.getLastKnownLocation(mng.getBestProvider(new Criteria(), false));



                                //prevents the location getting saved as 0,0.
                                if (location == null) {

                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MapMainActivity.this);
                                    dialogBuilder.setTitle("GPS Accuracy");
                                    dialogBuilder.setMessage("GPS service can not get accurate location. Please wait for GPS lock or try again outdoors.");
                                    dialogBuilder.setPositiveButton("OK", null);
                                    dialogBuilder.show();




                                }else {
                                    getLocation();
                                    LatLng me = new LatLng(latitude, longitude);


                                    //creating a marker object to put into local database
                                    // title / snippet / location
                                    Marker marker = new Marker("My Parked Car", "Parked at: " + TS, me.toString());
                                    parkedLocalStore.storeParkedData(marker);
                                    //sets user parked to true
                                    parkedLocalStore.setUserParked(true);

                                    //converting the string in database and removing all characters that are no use. looks clunky.
                                    String[] latlngDB = parkedLocalStore.getParkedLocation().position.split(",");

                                    String lat = latlngDB[0].replaceFirst("lat/lng: ", "");
                                    String lat1 = lat.replace("(", "");
                                    String lon = latlngDB[1].replace(")", "");
                                    double latitude = Double.parseDouble(lat1);
                                    double longitude = Double.parseDouble(lon);
                                    LatLng parkedLatLng = new LatLng(latitude, longitude);


                                    mMap.addMarker(new MarkerOptions().title(parkedLocalStore.getParkedLocation().title).snippet(parkedLocalStore.getParkedLocation().snippet).position(parkedLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_mascot)));

                                    //CameraPosition cameraPositionAdded = new CameraPosition.Builder().target(me).zoom(17).build();
                                    CameraPosition cameraPositionAdded = new CameraPosition.Builder().target(parkedLatLng).zoom(17).build();
                                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPositionAdded));


                                    //also call parking timer method to offer timer setting.
                                    AlertDialog.Builder dialogBuilderOfferTimer = new AlertDialog.Builder(MapMainActivity.this);
                                    dialogBuilderOfferTimer.setTitle("Time Limit?");
                                    dialogBuilderOfferTimer.setIcon(R.mipmap.ic_parking_timer);
                                    dialogBuilderOfferTimer.setMessage("Would You Like To Set A Timer For This Location?");
                                    dialogBuilderOfferTimer.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            parkingTimer();
                                        }
                                    });
                                    dialogBuilderOfferTimer.setNegativeButton("No", null);
                                    dialogBuilderOfferTimer.show();

                                }
                               // }

                            }
                        }
                    });
                    dialogBuilder.setNegativeButton("Not now", null);
                    dialogBuilder.show();

                }
            } else {
                requestPermission();
            }
        }
    }



    private boolean isParked() {
        if (parkedLocalStore.getParkedLocation() == null) {
            return false;
        }
        return true;
    }





    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //mng.requestSingleUpdate(new Criteria(),locationListener, Looper.myLooper());

        //checking permissions to set users location on map
        if (checkPermission()) {
            //
            mMap.setMyLocationEnabled(true);

        } else if (!checkPermission()) {
            requestPermission();
        }


        //adding parking locations once map is ready.
        populateMap();


        final Vibrator vibe = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);


        // this checks is the user has a location saved in the local database/ if they are "parked".
        //if they are do string alterations and add pin to map.
        // solves the cleared from memory issue.
        //****************************************CRASHES ON FIRST RUN FOR SOME REASON FIND OUT WHY, NULL POINTER ON POSITION? ************************************************************************************************************************************************************
        //if (parkedLocalStore.parkedLocalDatabase.getBoolean("parked",true) == true) {
        if (isParked() == true) {

            //converting the string in database and removing all characters that are no use. looks insane.
            String[] latlngDB = parkedLocalStore.getParkedLocation().position.split(",");

            String lat = latlngDB[0].replaceFirst("lat/lng: ", "");
            String lat1 = lat.replace("(", "");
            String lon = latlngDB[1].replace(")", "");
            double latitude = Double.parseDouble(lat1);
            double longitude = Double.parseDouble(lon);
            LatLng parkedLatLng = new LatLng(latitude, longitude);

            mMap.addMarker(new MarkerOptions().title(parkedLocalStore.getParkedLocation().title).snippet(parkedLocalStore.getParkedLocation().snippet).position(parkedLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_mascot)));


        }


//*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*^&*^&*^&*&^*&^*&^*^&*&^*^&*^&*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^8
//*&^*&^*&^*&^*&^*&^*&^*&^*&^*^&*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*^&*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*&^*^&*&^*&^*&^*&^*&^*&^*

        //PROBLEM LIES HERE ------ java.lang.NullPointerException: Attempt to invoke virtual method 'double android.location.Location.getLatitude()' on a null object reference

        //Solved



        //SETTING LOCATION OF USER AND ANIMATING CAMERA TO THEM
        //LocationManager mng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkPermission()) {
            if (!mng.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            } else {


                mng.requestLocationUpdates(bestProvider, 10000, 15, this);

                Location location = mng.getLastKnownLocation(mng.getBestProvider(new Criteria(), false));



                    getLocation();

                if (location == null) {

                    mng.requestLocationUpdates(mng.GPS_PROVIDER, 0, 0, this);
                    mng.requestLocationUpdates(mng.NETWORK_PROVIDER, 0, 0, this);
                    getLocation();


                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MapMainActivity.this);
                    dialogBuilder.setTitle("GPS Accuracy");
                    dialogBuilder.setMessage("GPS service can not get accurate location. Please wait for GPS lock or try again outdoors.");
                    dialogBuilder.setPositiveButton("OK", null);
                    dialogBuilder.show();




                }else {
                    LatLng startMe = new LatLng(latitude, longitude);

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(startMe).zoom(17).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                }
                //}
            }

        }


        //ADDING LONG PRESS LISTENER FOR ADDING LOCATIONS TO DATABASE *********************************************
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {

                vibe.vibrate(100);
                if (mMap.getCameraPosition().zoom >= 18) {
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    final View v = layoutInflater.inflate(R.layout.add_alert, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(v);
                    builder.setCancelable(false);

                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {



                            EditText title = (EditText) v.findViewById(R.id.etParkingTitle);
                            EditText snippet = (EditText) v.findViewById(R.id.etSnippet);

                            if (title.getText().toString().equals("") || snippet.getText().toString().equals("")){

                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MapMainActivity.this);
                                dialogBuilder.setTitle("Fields Cannot Be Blank");
                                dialogBuilder.setMessage("You cannot submit a blank location.");
                                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                dialogBuilder.show();

                            }else{
                                mMap.addMarker(new MarkerOptions().title(title.getText().toString()).snippet(snippet.getText().toString()).position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.parkmapicon)));
                                Marker marker = new Marker(title.getText().toString(), snippet.getText().toString(), latLng.toString());

                                addMarker(marker);
                            }

                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MapMainActivity.this);
                    dialogBuilder.setTitle("Add New Location");
                    dialogBuilder.setMessage("For accuracy, you must be zoomed in. \n" +
                            "Zoom Now?");
                    dialogBuilder.setPositiveButton("Zoom Now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(18).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    });
                    dialogBuilder.setNegativeButton("Not Now", null);
                    dialogBuilder.show();
                }
            }
        });
//**************************************************************************************************************************



    }


    //PERMISSION METHODS BELOW
    // This code is sampled from https://www.learn2crack.com/2015/10/android-marshmallow-permissions.html

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context2, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity2, Manifest.permission.ACCESS_FINE_LOCATION)) {

            Toast.makeText(context2, "GPS permission is needed for functionality. Allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(activity2, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {



        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(context2, "Permission Granted, location data available.", Toast.LENGTH_LONG).show();



                    //ONCE PERMISSION IS GRANTED DO THE ENABLE MY LOCATION AND CAMERA TO "MY" LOCATION

                    if (checkPermission()) {
                        mMap.setMyLocationEnabled(true);
                        //LocationManager mng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                        if (!mng.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                            buildAlertMessageNoGps();
                        } else {
                            Location location = mng.getLastKnownLocation(mng.getBestProvider(new Criteria(), false));



                            getLocation();
                            LatLng me = new LatLng(latitude, longitude);

                            //LatLng me = new LatLng(location.getLatitude(), location.getLongitude());


                            CameraPosition cameraPosition = new CameraPosition.Builder().target(me).zoom(17).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        }

                    }

                } else {

                    //Toast.makeText(context2, "Permission Denied, location data unavailable.", Toast.LENGTH_LONG).show();

                }
                break;

        }

    }

    //end of code sampled from https://www.learn2crack.com/2015/10/android-marshmallow-permissions.html



    //when called offers to take user to GPS settings
    public void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS is disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }





    //class for connecting to server, taking the JSON and adding it to the map
    public class GetMarkers extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(MapMainActivity.this);
        InputStream inputStream = null;
        String result = "";



        protected void  onPreExecute() {

            progressDialog.setMessage("Processing...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    GetMarkers.this.cancel(true);
                }
            });

        }


        @Override
        protected String doInBackground(String... params) {
            String url_select = "http://park-it.net23.net/FetchPinsDataJson.php";

            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

            try {

                HttpClient httpClient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(url_select);
                httpPost.setEntity(new UrlEncodedFormEntity(param));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();

                // Read content & Log
                inputStream = httpEntity.getContent();
            } catch (IOException e1) {
                Log.e("IOException", e1.toString());
                e1.printStackTrace();
            }
            // Convert response to string using String Builder
            try {
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line + "\n");
                }

                inputStream.close();
                result = sBuilder.toString();

            } catch (Exception e) {
                Log.e("Error Converting", e.toString());
            }


            return result;
        }




        //Taking strings from the JSON object and placing them into a marker
        protected void onPostExecute(String result) {
            int i;

            //parse JSON data
            try {
                JSONArray jArray = new JSONArray(result);
                for(i=0; i < jArray.length(); i++) {

                    JSONObject jObject = jArray.getJSONObject(i);

                    String title = jObject.getString("title");
                    String snippet = jObject.getString("snippet");
                    String position = jObject.getString("position");



                    //More string alterations like before.
                    String[] latlng = position.split(",");

                    String lat = latlng[0].replaceFirst("lat/lng: ", "");
                    String lat1 = lat.replace("(", "");
                    String lon = latlng[1].replace(")", "");
                    double latitude = Double.parseDouble(lat1);
                    double longitude = Double.parseDouble(lon);
                    LatLng markersLatLng = new LatLng(latitude, longitude);


                    mMap.addMarker(new MarkerOptions().title(title).snippet(snippet).position(markersLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.parkmapicon)));


                } // End Loop


                this.progressDialog.dismiss();
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)
        }
    }


    //Method starts getMarkers procedure from database ------------------
    private void populateMap() {
        new GetMarkers().execute();
    }





    //tring to get an accurate location
    protected void getLocation() {
        if (checkPermission()) {

            if (mng.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mng = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                criteria = new Criteria();
                bestProvider = String.valueOf(mng.getBestProvider(criteria, true)).toString();


                if (checkPermission()) {
                    Location location = mng.getLastKnownLocation(bestProvider);
                    if (location != null) {
                        mng.requestLocationUpdates(bestProvider, 0, 0, this);
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                       // while (location.getLatitude() == 0 && location.getLongitude() == 0){
                       //     getLocation();
                       // }
                    } else {
                        mng.requestLocationUpdates(bestProvider, 0, 0, this);
                    }
                } else {
                    requestPermission();
                }
            } else {
               // buildAlertMessageNoGps();
            }
        }else{
            requestPermission();
        }
    }





    @Override
    public void onLocationChanged(Location location) {


        if(location != null) {
            getLocation();

            mng.requestLocationUpdates(bestProvider, 0, 0, this);

            latitude = location.getLatitude();
            longitude = location.getLongitude();
            mng.removeUpdates(this);

            if (checkPermission()) {
                mng.removeUpdates(this);


                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

        Toast.makeText(this, "GPS has been disabled, please re-enable", Toast.LENGTH_LONG).show();
        buildAlertMessageNoGps();



    }




    //opens search pop up edit text. gets latlng from geocoder and animates camera
    public void onSearch(){

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View v = layoutInflater.inflate(R.layout.search_popup, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);
        builder.setCancelable(false);

        final EditText searchQuery = (EditText) v.findViewById(R.id.etSearchBox);





        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (searchQuery.getText().toString().equals("")) {
                    searchError();
                } else {

                    String searchQueryString = searchQuery.getText().toString();
                    List<Address> addressList = null;
                    if (searchQueryString != null || !searchQueryString.equals("")) {

                        Geocoder geocoder = new Geocoder(context);

                        try {
                            addressList = geocoder.getFromLocationName(searchQueryString, 1);

                        } catch (IOException e) {
                            searchError();
                            e.printStackTrace();
                        }

                        //if (addressList != null || !addressList.isEmpty() || addressList.size() !=0) {
                        if (addressList != null && addressList.size() != 0) {

                            Address address = addressList.get(0);
                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                            //clearing map, of previous search result pin. re adding parking pins then adding new pin and animating.
                            mMap.clear();
                            populateMap();
                            if (isParked() == true) {

                                //converting the string in database and removing all characters that are no use. looks insane.
                                String[] latlngDB = parkedLocalStore.getParkedLocation().position.split(",");

                                String lat = latlngDB[0].replaceFirst("lat/lng: ", "");
                                String lat1 = lat.replace("(", "");
                                String lon = latlngDB[1].replace(")", "");
                                double latitude = Double.parseDouble(lat1);
                                double longitude = Double.parseDouble(lon);
                                LatLng parkedLatLng = new LatLng(latitude, longitude);

                                mMap.addMarker(new MarkerOptions().title(parkedLocalStore.getParkedLocation().title).snippet(parkedLocalStore.getParkedLocation().snippet).position(parkedLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_mascot)));

                            }

                            mMap.addMarker(new MarkerOptions().title("Search Result").snippet(searchQueryString).position(latLng));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        } else {
                            searchError();
                        }
                    } else {
                        searchError();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();


    }



    public void searchError(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error With Search");
        builder.setIcon(R.mipmap.ic_menu_about);
        builder.setMessage("Please Search A Valid Location")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        onSearch();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();

    }


}
















//***************************************************************************************             NOW UNUSED CODE.              *********************************************************************************************
















                    /*new CountDownTimer(mins * 60000, 1000) {

                        public void onTick(long millisUntilFinished) {

                            long seconds = millisUntilFinished / 1000;
                            long hr = (seconds / 3600);
                            long rem = (seconds % 3600);
                            long mn = rem / 60;
                            long sec = rem % 60;

                            String hrStr = (hr < 10 ? "0" : "") + hr;
                            String mnStr = (mn < 10 ? "0" : "") + mn;
                            String secStr = (sec < 10 ? "0" : "") + sec;

                            textViewMenuTime.setText(hrStr + ":" + mnStr + ":" + secStr);

                            isTimerRunning = true;

                            //textViewMenuTime.setText("" + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            //textViewMenuTime.setText("Expired!");
                            vibe.vibrate(5000);
                            textViewMenuTime.setVisibility(v.INVISIBLE);

                            //SHOULD PUSH A NOTIFICATION/PLAY ALARM SOUND
                            //IMPLEMENT WARNINGS BEFORE ? ? ? ?
                            //ALSO SET A BOOLEAN TO FALSE SO TIMER CAN BE REACTIVATED


                            //Setting up a notification. on click leads back to main activity and clears itself.
                            Intent notificationIntent = new Intent(context, MapMainActivity.class);
                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            PendingIntent intent = PendingIntent.getActivity(context, 0,
                                    notificationIntent, 0);

                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                            mBuilder.setSmallIcon(R.mipmap.ic_mascot);
                            mBuilder.setContentTitle("Park-it! Timer Has Expired!");
                            mBuilder.setContentText("Your Allocated Parking Time Has Ended. Avoid Those Tickets!");
                            mBuilder.setContentIntent(intent);
                            mBuilder.setAutoCancel(true);
                            mBuilder.setSound(alarmSound);

                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            // in notification id for reference later?
                            mNotificationManager.notify(1, mBuilder.build());


                            isTimerRunning = false;


                        }
                    }.start();*/


//CHECKING IF IS ACCURATE - - - - - - - -TEST

        /*LocationManager mngTest = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location locationTest = mngTest.getLastKnownLocation(mngTest.getBestProvider(new Criteria(), false));
        if (isAccurate(locationTest) > 10){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MapMainActivity.this);
            dialogBuilder.setMessage("GPS location accuracy is insufficient at this time.");
            dialogBuilder.setPositiveButton("Ok", null);
            dialogBuilder.show();
        }*/







//TESTING COUNTDOWN TIMER IN DIFFERENT LOCATION TO TRY ACCESS ONFINISH() METHOD TO CANCEL CURRENT TIMER BEFORE SETTING NEW.


    /*public void countDownTimer(long minsChosen) {

        CountDownTimer countDownTimer = new CountDownTimer(minsChosen * 60000, 1000){
        @Override
        public void onTick(long millisUntilFinished) {

            TextView textViewMenuTime = (TextView) findViewById(R.id.textViewMenuTime);
            textViewMenuTime.setVisibility(View.VISIBLE);


            long seconds = millisUntilFinished / 1000;
            long hr = (seconds / 3600);
            long rem = (seconds % 3600);
            long mn = rem / 60;
            long sec = rem % 60;

            String hrStr = (hr < 10 ? "0" : "") + hr;
            String mnStr = (mn < 10 ? "0" : "") + mn;
            String secStr = (sec < 10 ? "0" : "") + sec;

            textViewMenuTime.setText(hrStr + ":" + mnStr + ":" + secStr);

            isTimerRunning = true;


        }

        @Override
        public void onFinish() {

            TextView textViewMenuTime = (TextView) findViewById(R.id.textViewMenuTime);

            vibe.vibrate(5000);
            textViewMenuTime.setVisibility(View.INVISIBLE);

            //SHOULD PUSH A NOTIFICATION/PLAY ALARM SOUND
            //IMPLEMENT WARNINGS BEFORE ? ? ? ?
            //ALSO SET A BOOLEAN TO FALSE SO TIMER CAN BE REACTIVATED


            //Setting up a notification. on click leads back to main activity and clears itself.
            Intent notificationIntent = new Intent(context, MapMainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setSmallIcon(R.mipmap.ic_mascot);
            mBuilder.setContentTitle("Park-it! Timer Has Expired!");
            mBuilder.setContentText("Your Allocated Parking Time Has Ended. Avoid Those Tickets!");
            mBuilder.setContentIntent(intent);
            mBuilder.setAutoCancel(true);
            mBuilder.setSound(alarmSound);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // in notification id for reference later?
            mNotificationManager.notify(1, mBuilder.build());


            isTimerRunning = false;


        }
    }.start();}*/


