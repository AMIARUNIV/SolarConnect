// first/Project/ProductAdapter.java
package first.Project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;
import first.Project.models.CartItem;
import first.Project.utils.CartManager;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;
    private LayoutInflater inflater;
    private CartManager cartManager;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.inflater = LayoutInflater.from(context);
        this.cartManager = CartManager.getInstance(context);
    }

    @Override
    public int getCount() { return productList.size(); }

    @Override
    public Object getItem(int position) { return productList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_product_list, parent, false);
            holder = new ViewHolder();
            holder.image = convertView.findViewById(R.id.productImage);
            holder.name = convertView.findViewById(R.id.productName);
            holder.desc = convertView.findViewById(R.id.productDesc);
            holder.price = convertView.findViewById(R.id.productPrice);
            holder.addToCartBtn = convertView.findViewById(R.id.addToCartBtn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = productList.get(position);

        // Load image using Glide
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .override(100, 100);

            Glide.with(context)
                    .load(product.getImageUrl())
                    .apply(requestOptions)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.logo);
        }

        holder.name.setText(product.getName());
        holder.desc.setText(product.getDescription());

        // Display price
        if (product.getPrice() != null) {
            holder.price.setText(String.format("DZD %.2f", product.getPrice()));
        } else {
            holder.price.setText("Price not set");
        }

        // Set up Add to Cart button
        holder.addToCartBtn.setOnClickListener(v -> {
            addProductToCart(product);
        });

        // Set up product item click to show seller info
        convertView.setOnClickListener(v -> {
            showSellerInfoDialog(product);
        });

        return convertView;
    }

    private void addProductToCart(Product product) {
        // Create cart item from product
        CartItem cartItem = new CartItem(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                1
        );

        // Add seller info to cart item
        cartItem.setSellerId(product.getSellerId());
        cartItem.setSellerName(product.getSellerName());

        // Add to cart
        cartManager.addToCart(cartItem);

        // Show success message
        String message = product.getName() + " added to cart!";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        // Update cart badge
        updateCartBadge();
    }

    private void showSellerInfoDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = inflater.inflate(R.layout.dialog_seller_info, null);

        TextView textSellerName = dialogView.findViewById(R.id.textSellerName);
        TextView textSellerPhone = dialogView.findViewById(R.id.textSellerPhone);
        TextView textSellerEmail = dialogView.findViewById(R.id.textSellerEmail);
        Button btnContactSeller = dialogView.findViewById(R.id.btnContactSeller);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        // Set seller info
        if (product.getSellerName() != null && !product.getSellerName().isEmpty()) {
            textSellerName.setText(product.getSellerName());
        } else {
            textSellerName.setText("No seller information");
        }

        if (product.getSellerPhone() != null && !product.getSellerPhone().isEmpty()) {
            textSellerPhone.setText(product.getSellerPhone());
        } else {
            textSellerPhone.setText("No phone available");
        }

        if (product.getSellerEmail() != null && !product.getSellerEmail().isEmpty()) {
            textSellerEmail.setText(product.getSellerEmail());
        } else {
            textSellerEmail.setText("No email available");
        }

        // Contact Seller button click
        btnContactSeller.setOnClickListener(v -> {
            if (product.getSellerPhone() != null && !product.getSellerPhone().isEmpty()) {
                // Open phone dialer
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + product.getSellerPhone()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnClose.setOnClickListener(v -> dialog.dismiss());
    }

    private void updateCartBadge() {
        // This method will be implemented later when we add cart badge
        // For now, just show toast
        int itemCount = cartManager.getItemCount();
        if (itemCount > 0) {
            Toast.makeText(context, "Cart: " + itemCount + " items", Toast.LENGTH_SHORT).show();
        }
    }

    private static class ViewHolder {
        ImageView image;
        TextView name;
        TextView desc;
        TextView price;
        Button addToCartBtn;
    }
}