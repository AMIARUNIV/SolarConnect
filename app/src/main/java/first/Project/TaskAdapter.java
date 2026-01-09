package first.Project;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.List;

public class TaskAdapter extends BaseAdapter {
    private Context context;
    private List<Task> taskList;
    private LayoutInflater inflater;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return taskList.size(); }

    @Override
    public Object getItem(int position) { return taskList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_task, parent, false);
            holder = new ViewHolder();
            holder.taskTitle = convertView.findViewById(R.id.taskTitle);
            holder.taskClient = convertView.findViewById(R.id.taskClient);
            holder.taskStatus = convertView.findViewById(R.id.taskStatus);
            holder.taskDate = convertView.findViewById(R.id.taskDate);
            holder.taskPriority = convertView.findViewById(R.id.taskPriority);
            holder.btnNavigate = convertView.findViewById(R.id.btnNavigate);
            holder.btnUpdateStatus = convertView.findViewById(R.id.btnUpdateStatus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskClient.setText("Client: " + task.getClientName());
        holder.taskStatus.setText("Status: " + task.getStatus());
        holder.taskDate.setText("Date: " + task.getDate());
        holder.taskPriority.setText("Priority: " + task.getPriority());

        // Color code based on priority
        if (task.isHighPriority()) {
            holder.taskPriority.setTextColor(Color.RED);
        } else if (task.isMediumPriority()) {
            holder.taskPriority.setTextColor(Color.parseColor("#FFA500")); // Orange
        } else {
            holder.taskPriority.setTextColor(Color.GREEN);
        }

        // Color code based on status
        if (task.isPending()) {
            holder.taskStatus.setTextColor(Color.RED);
        } else if (task.isInProgress()) {
            holder.taskStatus.setTextColor(Color.BLUE);
        } else {
            holder.taskStatus.setTextColor(Color.GREEN);
        }

        // Navigate button click
        holder.btnNavigate.setOnClickListener(v -> {
            // Navigate to map with task location
            navigateToTaskLocation(task);
        });

        // Update status button click
        holder.btnUpdateStatus.setOnClickListener(v -> {
            updateTaskStatus(task);
        });

        return convertView;
    }

    private void navigateToTaskLocation(Task task) {
        Toast.makeText(context,
                "Navigating to: " + task.getClientAddress(),
                Toast.LENGTH_SHORT).show();

        // Navigate to WorkerMapFragment with task location
        if (context instanceof WorkerMainActivity) {
            WorkerMainActivity activity = (WorkerMainActivity) context;
            Fragment fragment = WorkerMapFragment.newInstance(task.getLocation());

            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack("tasks")
                    .commit();
        }
    }

    private void updateTaskStatus(Task task) {
        // Simple status cycling: Pending -> In Progress -> Completed
        String newStatus;
        if (task.isPending()) {
            newStatus = "In Progress";
        } else if (task.isInProgress()) {
            newStatus = "Completed";
        } else {
            newStatus = "Pending";
        }

        // Update in adapter
        taskList.get(taskList.indexOf(task)).status = newStatus;
        notifyDataSetChanged();

        // In real app, update on server too
        Toast.makeText(context, "Status updated to: " + newStatus, Toast.LENGTH_SHORT).show();
    }

    private static class ViewHolder {
        TextView taskTitle;
        TextView taskClient;
        TextView taskStatus;
        TextView taskDate;
        TextView taskPriority;
        Button btnNavigate;
        Button btnUpdateStatus;
    }
}