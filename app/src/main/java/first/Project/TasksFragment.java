package first.Project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private ListView tasksListView;
    private List<Task> taskList;
    private TaskAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        tasksListView = view.findViewById(R.id.tasksListView);

        // Load tasks (for now, dummy data)
        taskList = loadDummyTasks();

        adapter = new TaskAdapter(getActivity(), taskList);
        tasksListView.setAdapter(adapter);

        tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = taskList.get(position);
                showTaskDetails(task);
            }
        });

        return view;
    }

    private List<Task> loadDummyTasks() {
        List<Task> tasks = new ArrayList<>();

        // Sample tasks for demonstration
        tasks.add(new Task("1", "Install Solar Panel",
                "Install 5KW solar panel system at client residence",
                "John Doe", "123 Main St, Constantine",
                "Pending", "36.3650,6.6147", "2024-03-15", "High"));

        tasks.add(new Task("2", "System Maintenance",
                "Routine maintenance check for existing solar system",
                "Jane Smith", "456 Oak Ave, Constantine",
                "In Progress", "36.3660,6.6150", "2024-03-14", "Medium"));

        tasks.add(new Task("3", "Battery Replacement",
                "Replace old batteries with new lithium-ion batteries",
                "Bob Wilson", "789 Pine Rd, Constantine",
                "Completed", "36.3670,6.6160", "2024-03-10", "High"));

        tasks.add(new Task("4", "Inverter Installation",
                "Install new inverter for solar system upgrade",
                "Alice Johnson", "321 Elm St, Constantine",
                "Pending", "36.3680,6.6170", "2024-03-20", "Medium"));

        tasks.add(new Task("5", "Site Inspection",
                "Initial site inspection for potential solar installation",
                "Charlie Brown", "654 Maple Dr, Constantine",
                "Pending", "36.3690,6.6180", "2024-03-18", "Low"));

        return tasks;
    }

    private void showTaskDetails(Task task) {
        Toast.makeText(getActivity(),
                "Task: " + task.getTitle() + "\n" +
                        "Client: " + task.getClientName() + "\n" +
                        "Status: " + task.getStatus() + "\n" +
                        "Click 'Navigate' to go to location",
                Toast.LENGTH_LONG).show();

        // You can later replace this with a dialog or new activity
        // For now, we'll just show a toast
    }

    public void updateTaskStatus(String taskId, String newStatus) {
        for (Task task : taskList) {
            if (task.getId().equals(taskId)) {
                // In real app, update on server too
                // For now, just update local list
                Toast.makeText(getActivity(),
                        "Task status updated to: " + newStatus,
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }
}