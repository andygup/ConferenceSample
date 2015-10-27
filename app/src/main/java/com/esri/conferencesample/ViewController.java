package com.esri.conferencesample;

import android.content.Context;
import android.util.Log;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;

import java.util.List;

public class ViewController {

    Context _context;
    MapView _map;
    Geodatabase _geodatabase;

    public GeodatabaseFeatureTable roomNamesFeatureTable;

    public ViewController(Context context, MapView map, Geodatabase geodatabase){
        _context = context;
        _map = map;
        _geodatabase = geodatabase;
        _populateMapFromGeodatabase();
    }

    private void _populateMapFromGeodatabase(){

        try {
            final List<GeodatabaseFeatureTable> list = _geodatabase.getGeodatabaseTables();

            // Load the layers in reverse order, otherwise the bottom most layer will
            // obscure the top most layer. They aren't semi-transparent.
            for(int i = list.size() - 1; i >= 0; i--) {
                final GeodatabaseFeatureTable geodatabaseFeatureTable = _geodatabase.getGeodatabaseFeatureTableByLayerId(i);

                Log.d("ArcGIS", "Loading feature layer ID: " + geodatabaseFeatureTable.getFeatureServiceLayerId() + ", NAME: "
                        + geodatabaseFeatureTable.getTableName());

                if(geodatabaseFeatureTable.getTableName().equals(_context.getString(R.string.room_name_feature_table_name))){
                    // Set this property so that we can share it publicly with RoomViewer Class
                    roomNamesFeatureTable = geodatabaseFeatureTable;
                }

                FeatureLayer featureLayer = new FeatureLayer(geodatabaseFeatureTable);
                featureLayer.setEnableLabels(true);

                // Add the layers to the map.
                // They will automatically display unless there was an error
                _map.addLayer(featureLayer);
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
