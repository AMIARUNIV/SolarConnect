package first.Project;

import android.content.Context;
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

public class CartAdapter extends BaseAdapter {

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    private Context context;
    private List<CartItem> cartItems;
    private LayoutInflater inflater;
    private CartManager cartManager;
    private OnQuantityChangeListener listener;

    public CartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
        this.inflater = LayoutInflater.from(context);
        this.cartManager = CartManager.getInstance(context);
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.listener = listener;
    }

    public void updateData(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return cartItems.size(); }

    @Override
    public Object getItem(int position) { return cartItems.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_cart, parent, false);
            holder = new ViewHolder();
            holder.image = convertView.findViewById(R.id.cartItemImage);
            holder.name = convertView.findViewById(R.id.cartItemName);
            holder.price = convertView.findViewById(R.id.cartItemPrice);
            holder.quantity = convertView.findViewById(R.id.cartItemQuantity);
            holder.total = convertView.findViewById(R.id.cartItemTotal);
            holder.increaseBtn = convertView.findViewById(R.id.increaseBtn);
            holder.decreaseBtn = convertView.findViewById(R.id.decreaseBtn);
            holder.removeBtn = convertView.findViewById(R.id.removeBtn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CartItem item = cartItems.get(position);

        // Load image
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .override(80, 80);

            Glide.with(context)
                    .load(item.getImageUrl())
                    .apply(requestOptions)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.logo);
        }

        holder.name.setText(item.getName());
        holder.price.setText(String.format("DZD %.2f", item.getPrice()));
        holder.quantity.setText(String.valueOf(item.getQuantity()));
        holder.total.setText(String.format("DZD %.2f", item.getTotalPrice()));

        // Setup button listeners
        holder.increaseBtn.setOnClickListener(v -> {
            cartManager.updateQuantity(item.getProductId(), item.getQuantity() + 1);
            if (listener != null) {
                listener.onQuantityChanged();
            }
        });

        holder.decreaseBtn.setOnClickListener(v -> {
            cartManager.updateQuantity(item.getProductId(), item.getQuantity() - 1);
            if (listener != null) {
                listener.onQuantityChanged();
            }
        });

        holder.removeBtn.setOnClickListener(v -> {
            cartManager.removeFromCart(item.getProductId());
            Toast.makeText(context, item.getName() + " removed from cart", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onQuantityChanged();
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        ImageView image;
        TextView name;
        TextView price;
        TextView quantity;
        TextView total;
        Button increaseBtn;
        Button decreaseBtn;
        Button removeBtn;
    }
}