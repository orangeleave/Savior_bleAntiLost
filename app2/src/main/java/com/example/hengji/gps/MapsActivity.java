package com.example.hengji.gps;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.hengji.gps.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    final private String TAG = MapsActivity.class.getName();
    final static String IDENTITY_POOL_ID = "*******";
    final static String ACCOUNT_ID = "******";
    final static int MARKER_UPDATE_INTERVAL = 5000; /* milliseconds */
    final static String ROLE_ARN = "*****";
    private GoogleMap mMap;
    private geoData g;
    private boolean updatelocation = false;
    Marker marker;
    Handler handler = new Handler();
    AmazonDynamoDBClient ddbClient;
    CognitoCachingCredentialsProvider credentialsProvider;
    DynamoDBMapper mapper;
    Runnable[] runnableArray = new Runnable[2];

    //    Runnable updateMarker = new Runnable() {
//        @Override
//        public void run() {
//            marker.remove();
//            Log.d(TAG,g+"???");
//            if(g != null) {
//                LatLng pos = new LatLng(Double.valueOf(g.getLatitude()), Double.valueOf(g.getLongitude()));
//                marker = mMap.addMarker(new MarkerOptions().position(pos).title("gps"));
//
//                handler.postDelayed(this, MARKER_UPDATE_INTERVAL);
//            }
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Log.d(TAG, "hhh");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        BasicAWSCredentials b = new BasicAWSCredentials("***", "***");
        ddbClient = new AmazonDynamoDBClient(b);
        ddbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        mapper = new DynamoDBMapper(ddbClient);
        Runnable runnable = new Runnable() {
            public void run() {
                //DynamoDB calls go here
                DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                PaginatedScanList<geoData> result = mapper.scan(geoData.class, scanExpression);
                List<geoData> list = new ArrayList<geoData>();
                for(geoData geo:result) {
                    list.add(geo);
                }
                // sort the list in order to get the latest data
                Collections.sort(list, new Comparator<geoData>() {
                    @Override
                    public int compare(geoData g1, geoData g2) {
                        Double d = (Double.valueOf(g2.getTime()) - Double.valueOf(g1.getTime()));
                        return d.intValue();
                    }
                });
//                for(int i = 1; i < list.size(); i++) {
//                    geoData gg = list.get(i);
//                    mapper.delete(gg);
//                }
                g = new geoData();
                g.setDate(result.get(0).getDate());
                g.setTime(result.get(0).getTime());
                g.setLatitude(result.get(0).getLatitude());
                g.setLongitude(result.get(0).getLongitude());
                Log.d(TAG, "???" + g);

            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();

        new Thread() {
            public void run() {
                while(true) {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (g != null) {
                                    LatLng pos = new LatLng(Double.valueOf(g.getLatitude()), Double.valueOf(g.getLongitude()));
                                    marker.remove();
                                    mMap.addMarker(new MarkerOptions().position(pos));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                                    mMap.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
                                }
                            }
                        });
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }


//        handler.postDelayed(updateMarker, MARKER_UPDATE_INTERVAL);




//    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d(TAG, g + "woooooooo");
//        geoData temp = g.get(0);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-73, 40);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
}
