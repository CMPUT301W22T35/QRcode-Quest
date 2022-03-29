package com.qrcode_quest.ui.map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qrcode_quest.BuildConfig;
import com.qrcode_quest.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MapController mapController;
    LocationManager locationManager;
    ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
    private int MAP_DEFAULT_ZOOM = 15;


    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Context ctx = getActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        // getPermissions();

        View view = inflater.inflate(R.layout.map_fragment, container, false);

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);

        mapController = (MapController) mapView.getController();
        mapController.setZoom(MAP_DEFAULT_ZOOM);

        // Add a scale onto Map
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        mapView.getOverlays().add(scaleBarOverlay);

        // Mark current location -example
        markCurrentLocation(53.52682, -113.524493735076);  // location: Edmonton-UofA

        // Mark QR codes locations - example
        markQRLocation(100, 100, 53.526221, -113.520771);
        markQRLocation(155, 150, 53.523402, -113.52782);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void markCurrentLocation(double lat, double lon){
        // Mark the user's current Location and set to center of the map
        GeoPoint startPoint = new GeoPoint( lat, lon);
        mapController.setCenter(startPoint);
        mapController.animateTo(startPoint);

        Marker startMarker = new Marker(mapView);
        Drawable myLocationIcon = getResources().getDrawable(R.drawable.ic_mylocation_marker);
        startMarker.setIcon(myLocationIcon);

        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Current Location");

        mapView.getOverlays().add(startMarker);
    }

    private void markQRLocation(int points, int distance, double lat, double lon){
        // Mark a QR code location
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        Marker qrMarker = new Marker(mapView);
        Drawable qrLocationIcon = getResources().getDrawable(R.drawable.ic_qr_map_marker);
        qrMarker.setIcon(qrLocationIcon);

        qrMarker.setPosition(geoPoint);
        qrMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        qrMarker.setTitle(points + " points");
        qrMarker.setSnippet(distance + "m away");

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

