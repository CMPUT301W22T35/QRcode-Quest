package com.qrcode_quest.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qrcode_quest.BuildConfig;
import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.R;
import com.qrcode_quest.entities.GPSLocationLiveData;
import com.qrcode_quest.entities.Geolocation;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.RawQRCode;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

/**
 * A view to display nearby QR codes on a map
 *
 * Uses OpenStreetMap (osmdroid) to create a MapView
 *  reference: https://osmdroid.github.io/osmdroid/How-to-use-the-osmdroid-library.html
 *
 * @author ageolleg
 * @version 1.0
 */
public class MapFragment extends Fragment {
    /** A tag used for logging */
    private static final String CLASS_TAG = "MAP_FRAGMENT";


    private MainViewModel mainViewModel;
    private GPSLocationLiveData currentLocation;
    private final int NEARBY_DISTANCE = 5000; // meters

    private MapView mapView;
    private MapController mapController;
    public LocationManager locationManager;

    private GeoPoint startPoint;
    private Marker startMarker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        setUpOSM();
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mainViewModel = new ViewModelProvider(this.requireActivity()).get(MainViewModel.class);

        mapView = view.findViewById(R.id.mapView);
        setMapView(mapView);

        // Get current location and mark it on the map
        startMarker = new Marker(mapView);
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        currentLocation = new GPSLocationLiveData(requireContext(), locationManager);
        currentLocation.observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                // Log.d(CLASS_TAG, String.valueOf(location));
                mapView.setVisibility(View.VISIBLE);
                view.findViewById(R.id.map_loading).setVisibility(View.GONE);
                view.findViewById(R.id.mapListActionButton).setVisibility(View.VISIBLE);
                startPoint = new GeoPoint( location.getLatitude(), location.getLongitude());
                markCurrentLocation(startPoint);

                // If current location is found, get nearby QR codes and mark them on the map
                mainViewModel.getQRShots().observe(getViewLifecycleOwner(), qrShots -> {
                    if (qrShots != null) {
                        setLocations(qrShots);
                    } else {
                        Log.d(CLASS_TAG, "Cannot get QR Shots");
                    }
                });

            } else {
                mapView.setVisibility(View.GONE);
                view.findViewById(R.id.mapListActionButton).setVisibility(View.GONE);
                view.findViewById(R.id.map_loading).setVisibility(View.VISIBLE);
                Log.d(CLASS_TAG, "Cannot get current location");
            }
        });
        mapView.getOverlays().add(startMarker);

        // Handle action to go to MapList (List of Nearby QR Codes)
        FloatingActionButton mapListActionButton = view.findViewById(R.id.mapListActionButton);
        mapListActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_mapFragment_to_mapListFragment);
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void setUpOSM(){
        Context context = requireActivity().getApplicationContext();
        //Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance()
                .setOsmdroidTileCache(
                        context.getExternalCacheDir()
                );
        Configuration.getInstance()
                .setOsmdroidBasePath(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                );

//        Log.d(CLASS_TAG, String.valueOf(Configuration.getInstance().getOsmdroidTileCache()));
//        Log.d(CLASS_TAG, String.valueOf(Configuration.getInstance().getOsmdroidBasePath()));
    }

    private void setMapView(MapView mapView){
        int MAP_DEFAULT_ZOOM = 14;

        // Handle zoom controls of the map
        // Use Ctrl + drag click to zoom in and out on emulator
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        mapView.setZoomRounding(true);

        // Set the map's zoom value
        mapController = (MapController) mapView.getController();
        mapController.setZoom(MAP_DEFAULT_ZOOM);

        // Add a scale onto Map
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        mapView.getOverlays().add(scaleBarOverlay);
    }

    private void setLocations(ArrayList<QRShot> qrShots){
        if (qrShots == null || qrShots.size() == 0){
            return;
        }

        MapListContent.clearItems();
        Geolocation playerGeolocation = locationToGeolocation(Objects.requireNonNull(currentLocation.getValue()));

        // Get a list of unique QRShots by their QRHash to remove duplicates
        HashSet<String> qrHashs = new HashSet<>(); // a list of unique QR Codes (their hash)
        ArrayList<QRShot> uniqueQRShots = new ArrayList<>(); // a list of unique QR shots
        for(QRShot qrShot: qrShots){
            if (qrHashs.add(qrShot.getCodeHash())){
                uniqueQRShots.add(qrShot);
            }
        }

        // Go through the list of unique QRShots and determine which to mark on map
        for (QRShot qrShot : uniqueQRShots) {
            double distance = qrShot.getLocation().getDistanceFrom(playerGeolocation);

            // only mark nearby QRShots
            if (distance <= NEARBY_DISTANCE ) {
                //geolocations.add(qrShot.getLocation());
                double lat = qrShot.getLocation().getLatitude();
                double lon = qrShot.getLocation().getLongitude();
                int score = RawQRCode.getScoreFromHash(qrShot.getCodeHash());

                MapListContent.addItem(new MapListContent.MapListItem(score, distance, lat, lon));
                markQRLocation(score, distance, lat, lon);
            }
        }

        // Sort QR code locations by distance (ascending), for Map List of nearby QR Codes
        MapListContent.sort();

    }

    /**
     * Converts a Location into Geolocation
     * @param location the Location to covert into Geolocation
     * @return a Geolocation
     */
    private Geolocation locationToGeolocation(Location location){
        return new Geolocation(location.getLatitude(), location.getLongitude());
    }

    private void markCurrentLocation(GeoPoint startPoint){
        // Mark the user's current Location and set to center of the map
        mapController.setCenter(startPoint);
        mapController.animateTo(startPoint);

        Drawable myLocationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_mylocation_marker);
        startMarker.setIcon(myLocationIcon);

        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Current Location");
    }

    private void markQRLocation(int score, double distance, double lat, double lon){
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        Marker qrMarker = new Marker(mapView);
        Drawable qrLocationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_qr_map_marker);
        qrMarker.setIcon(qrLocationIcon);

        // Mark a QR Shot Location and set information
        // Clicking on a marker shows the score and distance information
        // Clicking the information window hides the information
        qrMarker.setPosition(geoPoint);
        qrMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        qrMarker.setTitle(score + " points");
        qrMarker.setSnippet(String.format(Locale.getDefault(), "%.2fm away", distance));
        qrMarker.setSubDescription(String.format(Locale.getDefault(), "latitude: %.5f<br>longitude: %.5f", lat, lon));

        mapView.getOverlays().add(qrMarker);
    }



    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();
    }

}

