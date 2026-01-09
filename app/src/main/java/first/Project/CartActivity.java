package first.Project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import first.Project.models.CartItem;
import first.Project.utils.CartManager;

public class CartActivity extends AppCompatActivity {

    private ListView cartListView;
    private TextView totalTextView;
    private TextView emptyCartTextView;
    private TextView itemCountTextView;
    private Button checkoutButton;
    private Button continueShoppingButton;
    private Button clearCartButton;
    private CartManager cartManager;
    private CartAdapter cartAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize views
        cartListView = findViewById(R.id.cartListView);
        totalTextView = findViewById(R.id.totalTextView);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);
        itemCountTextView = findViewById(R.id.itemCountTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        continueShoppingButton = findViewById(R.id.continueShoppingButton);
        clearCartButton = findViewById(R.id.clearCartButton);

        // Initialize cart manager
        cartManager = CartManager.getInstance(this);

        // Setup adapter
        List<CartItem> cartItems = cartManager.getCartItems();
        cartAdapter = new CartAdapter(this, cartItems);
        cartListView.setAdapter(cartAdapter);

        // Update UI
        updateCartUI();

        // Setup button listeners
        checkoutButton.setOnClickListener(v -> {
            if (cartManager.getItemCount() > 0) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(CartActivity.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });        continueShoppingButton.setOnClickListener(v -> finish());
        clearCartButton.setOnClickListener(v -> clearCart());

        // Setup cart adapter callback for quantity changes
        cartAdapter.setOnQuantityChangeListener(new CartAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChanged() {
                updateCartUI();
            }
        });
    }

    private void updateCartUI() {
        List<CartItem> cartItems = cartManager.getCartItems();
        double total = cartManager.getCartTotal();
        int itemCount = cartManager.getItemCount();

        if (cartItems.isEmpty()) {
            // Empty cart state
            emptyCartTextView.setVisibility(View.VISIBLE);
            cartListView.setVisibility(View.GONE);
            checkoutButton.setEnabled(false);
            clearCartButton.setEnabled(false);
            totalTextView.setText("Cart is empty");
            itemCountTextView.setText("0 items");
        } else {
            // Cart has items
            emptyCartTextView.setVisibility(View.GONE);
            cartListView.setVisibility(View.VISIBLE);
            checkoutButton.setEnabled(true);
            clearCartButton.setEnabled(true);
            totalTextView.setText(String.format("Total: DZD %.2f", total));
            itemCountTextView.setText(itemCount + " item(s)");
        }

        cartAdapter.updateData(cartItems);
    }

    private void proceedToCheckout() {
        if (cartManager.getCartItems().isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // For now, just show a message
        Toast.makeText(this, "Checkout functionality coming soon!", Toast.LENGTH_SHORT).show();

        // TODO: Later we'll create CheckoutActivity
        // Intent intent = new Intent(this, CheckoutActivity.class);
        // startActivity(intent);
    }

    private void clearCart() {
        if (cartManager.getCartItems().isEmpty()) {
            Toast.makeText(this, "Cart is already empty", Toast.LENGTH_SHORT).show();
            return;
        }

        cartManager.clearCart();
        updateCartUI();
        Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartUI();
    }
}