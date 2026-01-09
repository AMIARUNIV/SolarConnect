package first.Project;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import first.Project.models.ApiResponse;
import first.Project.models.CartItem;
import first.Project.models.OrderRequest;
import first.Project.models.OrderResponse;
import first.Project.network.ApiService;
import first.Project.network.RetrofitClient;
import first.Project.utils.CartManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CheckoutActivity extends AppCompatActivity {

    private EditText addressEditText, dateEditText, notesEditText;
    private Button cancelButton, confirmButton;
    private LinearLayout orderSummaryContainer;
    private TextView totalAmountText;
    private CartManager cartManager;
    private List<CartItem> cartItems;
    private double totalAmount = 0.0;
    private int userId = 0;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initializeViews();
        setupListeners();
        loadCartItems();
        displayOrderSummary();
        loadUserInfo();

        // Debug: Print all SharedPreferences
        debugSharedPreferences();
    }

    private void initializeViews() {
        addressEditText = findViewById(R.id.addressEditText);
        dateEditText = findViewById(R.id.dateEditText);
        notesEditText = findViewById(R.id.notesEditText);
        cancelButton = findViewById(R.id.cancelButton);
        confirmButton = findViewById(R.id.confirmButton);
        orderSummaryContainer = findViewById(R.id.orderSummaryContainer);
        totalAmountText = findViewById(R.id.totalAmountText);

        cartManager = CartManager.getInstance(this);
        cartItems = cartManager.getCartItems();
        calendar = Calendar.getInstance();
    }

    private void setupListeners() {
        // Date picker
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());

        // Confirm order button
        confirmButton.setOnClickListener(v -> placeOrder());
    }

    private void loadCartItems() {
        cartItems = cartManager.getCartItems();
        totalAmount = cartManager.getCartTotal();
        totalAmountText.setText(String.format("DZD %.2f", totalAmount));
    }

    private void displayOrderSummary() {
        orderSummaryContainer.removeAllViews();

        for (CartItem item : cartItems) {
            View itemView = getLayoutInflater().inflate(R.layout.item_order_summary, orderSummaryContainer, false);

            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView nameTextView = itemView.findViewById(R.id.itemName);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView quantityTextView = itemView.findViewById(R.id.itemQuantity);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView priceTextView = itemView.findViewById(R.id.itemPrice);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView subtotalTextView = itemView.findViewById(R.id.itemSubtotal);

            nameTextView.setText(item.getName());
            quantityTextView.setText("x" + item.getQuantity());
            priceTextView.setText(String.format("DZD %.2f", item.getPrice()));
            subtotalTextView.setText(String.format("DZD %.2f", item.getTotalPrice()));

            orderSummaryContainer.addView(itemView);
        }
    }

    private void loadUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Try different possible keys (check what your login stores)
        userId = sharedPreferences.getInt("userId", 0);

        // If still 0, try other possible keys
        if (userId == 0) {
            userId = sharedPreferences.getInt("user_id", 0);
        }
        if (userId == 0) {
            userId = sharedPreferences.getInt("id", 0);
        }
        if (userId == 0) {
            userId = sharedPreferences.getInt("clientId", 0);
        }

        // Debug log
        Log.d("CheckoutActivity", "Loaded userId: " + userId);

        // If still 0, show warning
        if (userId == 0) {
            Log.e("CheckoutActivity", "WARNING: userId is still 0! User not logged in properly.");
            Toast.makeText(this, "Warning: User ID not found. Please login again.", Toast.LENGTH_LONG).show();
        }

        // You can load saved address if available
        String savedAddress = sharedPreferences.getString("savedAddress", "");
        if (!savedAddress.isEmpty()) {
            addressEditText.setText(savedAddress);
        }
    }

    private void debugSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        java.util.Map<String, ?> allEntries = sharedPreferences.getAll();

        Log.d("CheckoutDebug", "=== SharedPreferences Contents ===");
        for (java.util.Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("CheckoutDebug", entry.getKey() + ": " + entry.getValue().toString());
        }
        Log.d("CheckoutDebug", "=================================");
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateEditText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateDateEditText() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        dateEditText.setText(sdf.format(calendar.getTime()));
    }

    private void placeOrder() {
        // Validate inputs
        String address = addressEditText.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter delivery address", Toast.LENGTH_SHORT).show();
            addressEditText.requestFocus();
            return;
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == 0) {
            // Try to load user info again
            loadUserInfo();

            if (userId == 0) {
                Toast.makeText(this, "Please login again. User ID not found.", Toast.LENGTH_SHORT).show();

                // Optional: Redirect to login
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }

        Log.d("CheckoutActivity", "Placing order with userId: " + userId);

        // Create order request
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setClientId(userId);
        orderRequest.setDeliveryAddress(address);
        orderRequest.setNotes(notesEditText.getText().toString().trim());

        // Set installation date if provided
        // Set installation date if provided
        String dateStr = dateEditText.getText().toString().trim();
        if (!dateStr.isEmpty()) {
            try {
                // Parse the date and format it to ISO 8601
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);

                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String isoDate = outputFormat.format(date);

                orderRequest.setInstallationDate(isoDate);
            } catch (Exception e) {
                e.printStackTrace();
                // Send as null if parsing fails
                orderRequest.setInstallationDate(null);
            }
        }
        // Create order items
        List<OrderRequest.OrderItemRequest> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderRequest.OrderItemRequest itemRequest = new OrderRequest.OrderItemRequest();
            itemRequest.setProductId(cartItem.getProductId());
            itemRequest.setSellerId(cartItem.getSellerId());
            itemRequest.setQuantity(cartItem.getQuantity());
            orderItems.add(itemRequest);
        }
        orderRequest.setItems(orderItems);

        // Save address for future use
        saveAddress(address);

        // Show loading
        confirmButton.setEnabled(false);
        confirmButton.setText("Processing...");

        // Send order to server
        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<OrderResponse>> call = apiService.createOrder(orderRequest);

        call.enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderResponse>> call, Response<ApiResponse<OrderResponse>> response) {
                confirmButton.setEnabled(true);
                confirmButton.setText("Place Order");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OrderResponse> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        // Order created successfully
                        OrderResponse order = apiResponse.getData();

                        // Clear cart
                        cartManager.clearCart();

                        // Show success and go to order confirmation
                        Intent intent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
                        intent.putExtra("orderId", order.getOrderId());
                        intent.putExtra("totalAmount", order.getTotalAmount());
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(CheckoutActivity.this,
                                "Failed: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CheckoutActivity.this,
                            "Server error: " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderResponse>> call, Throwable t) {
                confirmButton.setEnabled(true);
                confirmButton.setText("Place Order");
                Toast.makeText(CheckoutActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void saveAddress(String address) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedAddress", address);
        editor.apply();
    }
}