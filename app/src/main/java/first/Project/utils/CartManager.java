package first.Project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import first.Project.models.CartItem;

public class CartManager {
    private static final String PREF_NAME = "solar_cart_prefs";
    private static final String CART_ITEMS_KEY = "cart_items";
    private static CartManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    private CartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    // Add item to cart
    public void addToCart(CartItem item) {
        List<CartItem> cartItems = getCartItems();

        // Check if item already exists
        boolean found = false;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId().equals(item.getProductId())) {
                cartItem.increaseQuantity();
                found = true;
                break;
            }
        }

        // If not found, add new item with quantity 1
        if (!found) {
            item.setQuantity(1);
            cartItems.add(item);
        }

        saveCartItems(cartItems);
    }

    // Remove item from cart
    public void removeFromCart(Integer productId) {
        List<CartItem> cartItems = getCartItems();
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getProductId().equals(productId)) {
                cartItems.remove(i);
                break;
            }
        }
        saveCartItems(cartItems);
    }

    // Update quantity
    public void updateQuantity(Integer productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(productId);
            return;
        }

        List<CartItem> cartItems = getCartItems();
        for (CartItem item : cartItems) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                break;
            }
        }
        saveCartItems(cartItems);
    }

    // Get all cart items
    public List<CartItem> getCartItems() {
        String json = sharedPreferences.getString(CART_ITEMS_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<CartItem>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Save cart items
    private void saveCartItems(List<CartItem> cartItems) {
        String json = gson.toJson(cartItems);
        sharedPreferences.edit().putString(CART_ITEMS_KEY, json).apply();
    }

    // Clear cart
    public void clearCart() {
        sharedPreferences.edit().remove(CART_ITEMS_KEY).apply();
    }

    // Get cart total
    public double getCartTotal() {
        List<CartItem> cartItems = getCartItems();
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    // Get total item count
    public int getItemCount() {
        List<CartItem> cartItems = getCartItems();
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    // Get unique item count
    public int getUniqueItemCount() {
        return getCartItems().size();
    }

    // Check if product is in cart
    public boolean isInCart(Integer productId) {
        List<CartItem> cartItems = getCartItems();
        for (CartItem item : cartItems) {
            if (item.getProductId().equals(productId)) {
                return true;
            }
        }
        return false;
    }

    // Get cart item by productId
    public CartItem getCartItem(Integer productId) {
        List<CartItem> cartItems = getCartItems();
        for (CartItem item : cartItems) {
            if (item.getProductId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    // Get cart item quantity
    public int getCartItemQuantity(Integer productId) {
        CartItem item = getCartItem(productId);
        return item != null ? item.getQuantity() : 0;
    }
}