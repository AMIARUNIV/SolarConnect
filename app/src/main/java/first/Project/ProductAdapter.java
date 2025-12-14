package first.Project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;
    private LayoutInflater inflater;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.inflater = LayoutInflater.from(context);
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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = productList.get(position);
        holder.image.setImageResource(product.getImageResId());
        holder.name.setText(product.getName());
        holder.desc.setText(product.getShortDesc());
        holder.price.setText(product.getPrice());

        return convertView;
    }

    private static class ViewHolder {
        ImageView image;
        TextView name;
        TextView desc;
        TextView price;
    }
}