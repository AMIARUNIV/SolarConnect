package first.Project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.MapView;

public class WorkerMapFragment extends ProductMapFragment {

    private static final String ARG_LOCATION = "location";
    private String taskLocation;
    private TextView taskInfoView;
    private Button btnStartNavigation;
    private Marker taskMarker;

    public static WorkerMapFragment newInstance(String location) {
        WorkerMapFragment fragment = new WorkerMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION, location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskLocation = getArguments().getString(ARG_LOCATION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Add worker-specific UI elements
        addWorkerUI(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add task marker if location is provided
        if (taskLocation != null && !taskLocation.isEmpty()) {
            addTaskMarker(taskLocation);
        }
    }

    private void addWorkerUI(View view) {
        try {
            // Inflate additional UI for worker
            View workerOverlay = LayoutInflater.from(getContext())
                    .inflate(R.layout.worker_map_overlay, (ViewGroup) view, false);

            taskInfoView = workerOverlay.findViewById(R.id.taskInfo);
            btnStartNavigation = workerOverlay.findViewById(R.id.btnStartNavigation);

            if (taskLocation != null) {
                taskInfoView.setText("Task Location: " + taskLocation);
            } else {
                taskInfoView.setText("No task location specified");
                btnStartNavigation.setEnabled(false);
            }

            btnStartNavigation.setOnClickListener(v -> {
                if (taskLocation != null) {
                    startNavigationToTask();
                } else {
                    Toast.makeText(getContext(), "No task location available", Toast.LENGTH_SHORT).show();
                }
            });

            // Add to map view
            if (mapView != null && view instanceof ViewGroup) {
                ((ViewGroup) view).addView(workerOverlay);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading worker UI", Toast.LENGTH_SHORT).show();
        }
    }

    private void addTaskMarker(String location) {
        try {
            String[] coords = location.split(",");
            double lat = Double.parseDouble(coords[0].trim());
            double lon = Double.parseDouble(coords[1].trim());

            GeoPoint taskPoint = new GeoPoint(lat, lon);

            // Create marker for task location
            taskMarker = new Marker(mapView);
            taskMarker.setPosition(taskPoint);
            taskMarker.setTitle("Task Location");
            taskMarker.setSnippet("Click for navigation");
            taskMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Use a different color for task marker
            // You can set a custom icon here if you have one
            // taskMarker.setIcon(getResources().getDrawable(R.drawable.ic_task_marker));

            mapView.getOverlays().add(taskMarker);
            mapView.invalidate();

            // Center map on task location
            if (mapController != null) {
                mapController.animateTo(taskPoint);
                mapController.setZoom(15.0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Invalid task location format", Toast.LENGTH_SHORT).show();
        }
    }

    private void startNavigationToTask() {
        if (taskLocation != null) {
            try {
                String[] coords = taskLocation.split(",");
                double lat = Double.parseDouble(coords[0].trim());
                double lon = Double.parseDouble(coords[1].trim());

                GeoPoint taskPoint = new GeoPoint(lat, lon);

                // Check if we have current location
                if (myLocationOverlay != null && myLocationOverlay.getLastFix() != null) {
                    setSelectedLocation(taskPoint);
                    getDirections();
                } else {
                    // No current location, just center on task
                    Toast.makeText(getContext(),
                            "Waiting for your location...\nMap centered on task location",
                            Toast.LENGTH_SHORT).show();

                    if (mapController != null) {
                        mapController.animateTo(taskPoint);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Invalid task location", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No task location available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void cleanupResources() {
        super.cleanupResources();
        // Remove task marker if exists
        if (taskMarker != null && mapView != null) {
            mapView.getOverlays().remove(taskMarker);
            taskMarker = null;
        }
    }
}