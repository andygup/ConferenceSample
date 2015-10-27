package com.esri.conferencesample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.android.map.MapView;
import com.esri.core.geodatabase.Geodatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    ViewController viewController;
    RoomViewer roomViewer;
    MapView map;
    Geodatabase geodatabase;
    static String roomNameField;
    static final double mapRotation = -90.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inflateMap();
        copyGeodatabase();
        initializeViews();
    }

    private void initializeViews(){
        try {

            // Since we've copied over the geodatabase file we can now instantiate an instance of it
            geodatabase = new Geodatabase(getApplicationContext().getFilesDir() + "/palmspringslg_harn.geodatabase");
            roomNameField = getString(R.string.room_name);

            viewController = new ViewController(getApplicationContext(), map, geodatabase);
            roomViewer = new RoomViewer(getApplicationContext(), roomNameField, this, map, viewController.roomNamesFeatureTable);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void inflateMap(){
        map = (MapView)findViewById(R.id.map);
        map.setAllowRotationByPinch(true);
        map.setRotationAngle(mapRotation);
        map.setMapBackground(Color.WHITE, Color.WHITE, 0, 0);
    }

    /**
     * Copy the geodatabase from R.raw to the application runtime filesystem
     */
    private void copyGeodatabase(){
        try {
            // Copy our file geodatabase from /res to the applications runtime filesystem
            final File file = new File(getApplicationContext().getFilesDir(), "palmspringslg_harn.geodatabase");

            InputStream is = getApplicationContext().getResources().openRawResource(R.raw.palmspringslg_harn2);
            OutputStream os;
            os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        map.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.unpause();
    }

}
