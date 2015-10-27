package com.esri.conferencesample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.query.QueryParameters;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class RoomViewer {

    Context _context;
    String _fieldName;
    Spinner _spinner;
    Activity _activity;
    MapView _map;
    GeodatabaseFeatureTable _roomNameFeatureTable;
    GraphicsLayer _graphicsLayer = new GraphicsLayer();
    static String _roomName;
    static final SimpleLineSymbol _simpleLineSymbol = new SimpleLineSymbol(Color.RED,3);

    public RoomViewer(Context context, String searchableFieldName, Activity activity, MapView map, GeodatabaseFeatureTable roomNameFeatureTable){
        _context = context;
        _fieldName = searchableFieldName;
        _activity = activity;
        _map = map;
        _map.addLayer(_graphicsLayer);
        _roomNameFeatureTable = roomNameFeatureTable;
        _roomName = _context.getString(R.string.room_name);
        _createRoomList();
    }

    private class RoomInfo {
        String roomName;
        Geometry geometry;
    }

    /**
     * Query the database for room names and then use those results to populate the Spinner
     */
    private void _createRoomList() {
        try {

            QueryParameters query = new QueryParameters();
            query.setOutFields(new String[]{_roomName});
            query.setWhere("\\\"SPACETYPE\\\" IN ('Classroom','Conference Room','Exhibit Hall')");

            Future<FeatureResult> resultFuture = _roomNameFeatureTable.queryFeatures(query, new CallbackListener<FeatureResult>() {
                @Override
                public void onCallback(FeatureResult featureResult) {
                    Log.d("ArcGIS", "featureResult back.");
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });

            final ArrayList<String> roomNames = new ArrayList<>();
            final ArrayList<RoomInfo> roomInfo = new ArrayList<>();

            roomNames.add("Select a room.");

            for (Object result : resultFuture.get()) {
                final Feature feature = (Feature) result;
                Log.d("ArcGIS",feature.getAttributes().toString());

                if(feature.getAttributeValue(_fieldName) != null){
                    roomNames.add(feature.getAttributeValue(_fieldName).toString());

                    final RoomInfo tempRoomInfoObject = new RoomInfo();
                    tempRoomInfoObject.roomName = feature.getAttributeValue(_fieldName).toString();
                    tempRoomInfoObject.geometry = feature.getGeometry();
                    roomInfo.add(tempRoomInfoObject);
                }
            }

            _populateSpinner(roomNames, roomInfo);

//            for(String value: roomNames) {
//                Log.d("ArcGIS attributes: ", value);
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _setRoomHighlighter(Geometry geometry){
        _graphicsLayer.removeAll();
        Graphic graphic = new Graphic(geometry,_simpleLineSymbol);
        _graphicsLayer.addGraphic(graphic);
    }

    private void _populateSpinner(final ArrayList<String> roomNames, final ArrayList<RoomInfo> roomInfo){

        try {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, R.layout.support_simple_spinner_dropdown_item,roomNames);

            _spinner = (Spinner) _activity.findViewById(R.id.spinner);

            // When an item is selected then zoom to it's location on the map.
            // We use the geographic Extent of the room's Polygon geometry
            _spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    try{
                        for (RoomInfo roomInfo1 : roomInfo) {

                            if(roomInfo1.roomName != null) {
                                if(roomInfo1.roomName.equals(roomNames.get(i))){
                                    final Geometry geometry = roomInfo1.geometry;

                                    // Verify that we are using a polygon to set the Extent
                                    if(geometry.getType().equals(Geometry.Type.POLYGON)) {
                                        _map.setExtent(geometry,10,true);
                                        _setRoomHighlighter(geometry);
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            _spinner.setAdapter(dataAdapter);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
