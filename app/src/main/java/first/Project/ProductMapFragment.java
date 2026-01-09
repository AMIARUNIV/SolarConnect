package first.Project;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProductMapFragment extends Fragment {

    // UI Components
    MapView mapView;
    IMapController mapController;
    MyLocationNewOverlay myLocationOverlay;
    private TextView locationInfo, addressInfo;
    private LinearLayout infoPanel;
    private FloatingActionButton fabMyLocation;
    private Button btnGetDirections, btnClearRoute;

    // State Management
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private boolean locationPermissionGranted = false;
    private boolean isMapVisible = false;

    // Marker Management
    private Marker currentMarker = null;
    private GeoPoint selectedLocation = null;

    // Route Management
    private Polyline currentRoute = null;
    private boolean isRouteDisplayed = false;
    private final String OSRM_BASE_URL = "https://router.project-osrm.org/route/v1/driving/";

    // HTTP Client for API calls
    private OkHttpClient httpClient = new OkHttpClient();
    private Gson gson = new Gson();

    // ==================== LIFECYCLE METHODS ====================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        initializeOSMdroid();
        initializeViews(view);
        setupMap();
        setupClickListeners();
        checkLocationPermission();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isMapVisible = isVisibleToUser;

        if (mapView != null) {
            if (isVisibleToUser) {
                mapView.onResume();
            } else {
                mapView.onPause();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null && isMapVisible) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        cleanupResources();
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                setupLocationOverlay();
                showToast("✅ Location permission granted");
            } else {
                showToast("❌ Location permission denied");
            }
        }
    }

    // ==================== INITIALIZATION METHODS ====================

    private void initializeOSMdroid() {
        Configuration.getInstance().load(requireContext(),
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()));
    }

    private void initializeViews(View view) {
        mapView = view.findViewById(R.id.mapView);
        fabMyLocation = view.findViewById(R.id.fab_my_location);
        infoPanel = view.findViewById(R.id.info_panel);
        locationInfo = view.findViewById(R.id.location_info);
        addressInfo = view.findViewById(R.id.address_info);
        btnGetDirections = view.findViewById(R.id.btn_get_directions);
        btnClearRoute = view.findViewById(R.id.btn_clear_route);
    }

    // ==================== MAP SETUP METHODS ====================

    private void setupMap() {
        configureMapView();
        setupMapController();
        addMapClickListener();
    }

    private void configureMapView() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        mapView.setMinZoomLevel(3.0);
        mapView.setMaxZoomLevel(19.0);
    }

    private void setupMapController() {
        mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(36.3650, 6.6147); // Constantine, Algeria
        mapController.setCenter(startPoint);
    }

    private void addMapClickListener() {
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                setSelectedLocation(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        mapView.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));
    }

    // ==================== LOCATION METHODS ====================

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            testLocationServices();
            setupLocationOverlay();
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        showToast("🔍 Requesting location permission...");
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void testLocationServices() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        String status = "GPS: " + (gpsEnabled ? "✅" : "❌") + ", Network: " + (networkEnabled ? "✅" : "❌");
        showToast("Location services: " + status);
    }

    private void setupLocationOverlay() {
        if (!locationPermissionGranted || mapView == null) {
            showToast("❌ Location permission not granted or map null");
            return;
        }

        if (!isLocationServiceEnabled()) {
            showToast("📍 Please enable location services in phone settings");
            return;
        }

        cleanupExistingOverlay();
        createNewLocationOverlay();
        addLocationFoundListener();
    }

    private boolean isLocationServiceEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void cleanupExistingOverlay() {
        if (myLocationOverlay != null) {
            mapView.getOverlays().remove(myLocationOverlay);
            myLocationOverlay.disableMyLocation();
        }
    }

    private void createNewLocationOverlay() {
        GpsMyLocationProvider provider = new GpsMyLocationProvider(getContext());
        provider.setLocationUpdateMinTime(500);
        provider.setLocationUpdateMinDistance(0);

        myLocationOverlay = new MyLocationNewOverlay(provider, mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        myLocationOverlay.setPersonHotspot(0.5f, 1.0f);

        setLocationIcon();
        mapView.getOverlays().add(myLocationOverlay);
        mapView.invalidate();

        showToast("📍 Location tracking enabled. Waiting for fix...");
    }

    private void setLocationIcon() {
        try {
            Drawable locationIcon = createBlueCircleDrawable();
            if (locationIcon != null) {
                Bitmap bitmap = drawableToBitmap(locationIcon);
                myLocationOverlay.setPersonIcon(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLocationFoundListener() {
        new Handler().postDelayed(() -> {
            if (myLocationOverlay != null && myLocationOverlay.getLastFix() != null) {
                Location loc = myLocationOverlay.getLastFix();
                showToast(String.format("✅ Location found!\nLat: %.6f\nLon: %.6f",
                        loc.getLatitude(), loc.getLongitude()));
            } else {
                showToast("⏳ Still waiting for location... Try moving outside");
            }
        }, 3000);
    }

    // ==================== MARKER METHODS ====================

    void setSelectedLocation(GeoPoint location) {
        removePreviousMarker();
        selectedLocation = location;
        createNewMarker(location);
        showLocationSelection(location);

        // Enable directions button
        if (btnGetDirections != null) {
            btnGetDirections.setEnabled(true);
        }
    }

    private void removePreviousMarker() {
        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
        }
    }

    private void createNewMarker(GeoPoint location) {
        currentMarker = new Marker(mapView);
        currentMarker.setPosition(location);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        currentMarker.setTitle("Selected Destination");

        setMarkerIcon(currentMarker);
        setMarkerClickListener(currentMarker, location);

        mapView.getOverlays().add(currentMarker);
    }

    private void setMarkerIcon(Marker marker) {
        try {
            Drawable icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_compass);
            if (icon != null) {
                Drawable tintedIcon = DrawableCompat.wrap(icon).mutate();
                DrawableCompat.setTint(tintedIcon, Color.RED);
                marker.setIcon(tintedIcon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMarkerClickListener(Marker marker, GeoPoint location) {
        marker.setOnMarkerClickListener((marker1, mapView) -> {
            showLocationSelection(location);
            return true;
        });
    }

    // ==================== ROUTE/DIRECTIONS METHODS ====================

    private void setupClickListeners() {
        fabMyLocation.setOnClickListener(v -> {
            v.setPressed(true);
            new Handler().postDelayed(() -> v.setPressed(false), 100);
            handleMyLocationClick();
        });

        btnGetDirections.setOnClickListener(v -> {
            if (selectedLocation != null) {
                getDirections();
            } else {
                showToast("Please select a destination first by tapping on the map");
            }
        });

        btnClearRoute.setOnClickListener(v -> {
            clearRoute();
        });
    }

    void getDirections() {
        if (myLocationOverlay == null || myLocationOverlay.getLastFix() == null) {
            showToast("📍 Need your current location first");
            return;
        }

        Location myLocation = myLocationOverlay.getLastFix();
        GeoPoint start = new GeoPoint(myLocation.getLatitude(), myLocation.getLongitude());
        GeoPoint end = selectedLocation;

        // Show loading message
        showToast("Calculating route...");

        // Fetch route from OSRM API
        new FetchRouteTask().execute(start, end);
    }

    private class FetchRouteTask extends AsyncTask<GeoPoint, Void, List<GeoPoint>> {
        @Override
        protected List<GeoPoint> doInBackground(GeoPoint... geoPoints) {
            if (geoPoints.length < 2) return null;

            GeoPoint start = geoPoints[0];
            GeoPoint end = geoPoints[1];

            // Build OSRM API URL
            String url = OSRM_BASE_URL +
                    start.getLongitude() + "," + start.getLatitude() + ";" +
                    end.getLongitude() + "," + end.getLatitude() +
                    "?overview=full&geometries=geojson";

            try {
                Request request = new Request.Builder().url(url).build();
                Response response = httpClient.newCall(request).execute();

                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    return parseRouteFromJson(jsonResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<GeoPoint> routePoints) {
            if (routePoints != null && !routePoints.isEmpty()) {
                drawRoute(routePoints);
                showRouteInfo(routePoints);
            } else {
                showToast("Failed to calculate route. Please try again.");
            }
        }
    }

    private List<GeoPoint> parseRouteFromJson(String json) {
        try {
            RouteResponse response = gson.fromJson(json, RouteResponse.class);
            if (response != null && response.routes != null && !response.routes.isEmpty()) {
                Route route = response.routes.get(0);
                if (route.geometry != null && route.geometry.coordinates != null) {
                    List<GeoPoint> points = new ArrayList<>();
                    for (double[] coord : route.geometry.coordinates) {
                        // GeoJSON coordinates are [lon, lat]
                        points.add(new GeoPoint(coord[1], coord[0]));
                    }
                    return points;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void drawRoute(List<GeoPoint> routePoints) {
        // Clear previous route
        clearRoute();

        // Create new polyline for the route
        currentRoute = new Polyline();
        currentRoute.setPoints(routePoints);

        // Style the route - Use Color.argb to include alpha
        int routeColor = Color.argb(180, 30, 136, 229); // #1E88E5 with 70% opacity
        currentRoute.setColor(routeColor);
        currentRoute.setWidth(12.0f);

        // Make the line geodesic (follows Earth's curvature)
        currentRoute.setGeodesic(true);

        // Add to map
        mapView.getOverlays().add(currentRoute);
        mapView.invalidate();

        isRouteDisplayed = true;

        // Enable clear route button
        if (btnClearRoute != null) {
            btnClearRoute.setEnabled(true);
        }

        // Zoom to fit the route
        if (routePoints != null && !routePoints.isEmpty()) {
            zoomToRoute(routePoints);
        }
    }

    // Helper method to zoom map to show the entire route
    private void zoomToRoute(List<GeoPoint> routePoints) {
        if (routePoints == null || routePoints.isEmpty() || mapController == null) {
            return;
        }

        double minLat = routePoints.get(0).getLatitude();
        double maxLat = routePoints.get(0).getLatitude();
        double minLon = routePoints.get(0).getLongitude();
        double maxLon = routePoints.get(0).getLongitude();

        for (GeoPoint point : routePoints) {
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLon = Math.min(minLon, point.getLongitude());
            maxLon = Math.max(maxLon, point.getLongitude());
        }

        // Add padding
        double latPadding = (maxLat - minLat) * 0.1;
        double lonPadding = (maxLon - minLon) * 0.1;

        GeoPoint center = new GeoPoint(
                (minLat + maxLat) / 2,
                (minLon + maxLon) / 2
        );

        // Calculate zoom level based on bounding box
        double latSpan = (maxLat - minLat) + latPadding * 2;
        double lonSpan = (maxLon - minLon) + lonPadding * 2;

        // Simple zoom calculation - adjust as needed
        double zoom = 15.0;
        if (latSpan > 0.1 || lonSpan > 0.1) {
            zoom = 12.0;
        }
        if (latSpan > 0.5 || lonSpan > 0.5) {
            zoom = 10.0;
        }

        mapController.setCenter(center);
        mapController.setZoom(zoom);
    }

    private void showRouteInfo(List<GeoPoint> routePoints) {
        if (routePoints.size() >= 2) {
            GeoPoint start = routePoints.get(0);
            GeoPoint end = routePoints.get(routePoints.size() - 1);

            String info = String.format(Locale.getDefault(),
                    "🚗 Route Created\nStart: %.6f, %.6f\nEnd: %.6f, %.6f\nPoints: %d",
                    start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude(),
                    routePoints.size());

            locationInfo.setText(info);
            addressInfo.setText("Route displayed on map. Tap 'Clear Route' to remove.");
            showToast("✅ Route calculated successfully!");
        }
    }

    private void clearRoute() {
        if (currentRoute != null) {
            mapView.getOverlays().remove(currentRoute);
            currentRoute = null;
            mapView.invalidate();
            isRouteDisplayed = false;

            // Disable clear route button
            if (btnClearRoute != null) {
                btnClearRoute.setEnabled(false);
            }

            showToast("Route cleared");
        }
    }

    // ==================== UI INTERACTION METHODS ====================

    private void handleMyLocationClick() {
        if (!locationPermissionGranted) {
            showToast("🔍 Requesting location permission...");
            requestLocationPermission();
            return;
        }

        if (myLocationOverlay == null) {
            setupLocationOverlay();
            return;
        }

        Location location = getCurrentLocation();

        if (location != null) {
            centerMapOnLocation(location);
            showMyLocationInfo(location);
            addMyLocationMarker(new GeoPoint(location.getLatitude(), location.getLongitude()));
        } else {
            showToast("❌ No location available. Showing Constantine...");
            showConstantineCenter();
        }
    }

    // FIXED METHOD: Changed from showConstantineFallback to showConstantineCenter
    private void showConstantineCenter() {
        GeoPoint constantine = new GeoPoint(36.3650, 6.6147);
        if (mapController != null) {
            mapController.animateTo(constantine);
            showToast("📍 Showing Constantine center");

            // Add a marker at Constantine center
            if (currentMarker != null) {
                mapView.getOverlays().remove(currentMarker);
            }

            currentMarker = new Marker(mapView);
            currentMarker.setPosition(constantine);
            currentMarker.setTitle("Constantine Center");

            try {
                Drawable icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_compass);
                if (icon != null) {
                    Drawable tintedIcon = DrawableCompat.wrap(icon).mutate();
                    DrawableCompat.setTint(tintedIcon, Color.parseColor("#FFC400"));
                    currentMarker.setIcon(tintedIcon);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mapView.getOverlays().add(currentMarker);
            mapView.invalidate();
        }
    }

    private Location getCurrentLocation() {
        if (myLocationOverlay != null) {
            Location overlayLocation = myLocationOverlay.getLastFix();
            if (overlayLocation != null) {
                return overlayLocation;
            }
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (gpsLocation != null) return gpsLocation;

            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (networkLocation != null) return networkLocation;

            Location passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (passiveLocation != null) return passiveLocation;
        }

        return null;
    }

    private void centerMapOnLocation(Location location) {
        GeoPoint myLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (mapController != null) {
            mapController.animateTo(myLocation);
        }
    }

    private void addMyLocationMarker(GeoPoint location) {
        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("You are here");

        try {
            Drawable icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_mylocation);
            if (icon != null) {
                Drawable tintedIcon = DrawableCompat.wrap(icon).mutate();
                DrawableCompat.setTint(tintedIcon, Color.BLUE);
                marker.setIcon(tintedIcon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getOverlays().add(marker);
    }

    // ==================== INFO DISPLAY METHODS ====================

    private void showLocationSelection(GeoPoint location) {
        String info = String.format(Locale.getDefault(),
                "📍 Selected Destination:\nLat: %.6f\nLon: %.6f",
                location.getLatitude(),
                location.getLongitude());

        locationInfo.setText(info);
        addressInfo.setText("Tap 'Get Directions' for route");
        infoPanel.setVisibility(View.VISIBLE);
    }

    private void showMyLocationInfo(Location location) {
        String info = String.format(Locale.getDefault(),
                "📍 Your Location:\nLat: %.6f\nLon: %.6f",
                location.getLatitude(),
                location.getLongitude());

        locationInfo.setText(info);
        addressInfo.setText("Tap map to select destination");
        infoPanel.setVisibility(View.VISIBLE);
    }

    // ==================== HELPER METHODS ====================

    private Drawable createBlueCircleDrawable() {
        Drawable circle = ContextCompat.getDrawable(requireContext(), android.R.drawable.presence_online);
        if (circle != null) {
            circle = DrawableCompat.wrap(circle).mutate();
            DrawableCompat.setTint(circle, Color.BLUE);
        }
        return circle;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) return null;

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    void cleanupResources() {
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
            if (mapView != null) {
                mapView.getOverlays().remove(myLocationOverlay);
            }
            myLocationOverlay = null;
        }
    }

    // ==================== JSON RESPONSE CLASSES ====================

    class RouteResponse {
        List<Route> routes;
    }

    class Route {
        Geometry geometry;
        double distance; // in meters
        double duration; // in seconds
    }

    class Geometry {
        String type;
        List<double[]> coordinates;
    }
}