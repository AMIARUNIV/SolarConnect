// first/Project/OrderConfirmationActivity.java
package first.Project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OrderConfirmationActivity extends AppCompatActivity {

    private TextView orderIdText, totalAmountText, statusText, messageText;
    private Button viewOrdersButton, continueShoppingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        initializeViews();
        setupListeners();
        displayOrderDetails();
    }

    private void initializeViews() {
        orderIdText = findViewById(R.id.orderIdText);
        totalAmountText = findViewById(R.id.totalAmountText);
        statusText = findViewById(R.id.statusText);
        messageText = findViewById(R.id.messageText);
        viewOrdersButton = findViewById(R.id.viewOrdersButton);
        continueShoppingButton = findViewById(R.id.continueShoppingButton);
    }

    private void setupListeners() {
        viewOrdersButton.setOnClickListener(v -> {
            // Navigate to orders history (to be implemented)
            Intent intent = new Intent(this, ClientMainActivity.class);
            startActivity(intent);
            finish();
        });

        continueShoppingButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClientMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void displayOrderDetails() {
        Intent intent = getIntent();
        int orderId = intent.getIntExtra("orderId", 0);
        double totalAmount = intent.getDoubleExtra("totalAmount", 0.0);

        if (orderId > 0) {
            orderIdText.setText("Order #" + orderId);
            totalAmountText.setText(String.format("DZD %.2f", totalAmount));
            statusText.setText("PENDING");
            messageText.setText("Thank you for your order! Your order has been received and is being processed.");
        } else {
            Toast.makeText(this, "Order information not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}