package first.Project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment {

    private ListView productList;
    private List<Product> products;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_products_list, container, false);

        productList = view.findViewById(R.id.productList);
        products = loadProductsFromJson();

        ProductAdapter adapter = new ProductAdapter(getActivity(), products);
        productList.setAdapter(adapter);

        productList.setOnItemClickListener((parent, view1, position, id) -> {
            Product p = products.get(position);

            // AlertDialog (boîte de dialogue)
            new AlertDialog.Builder(getActivity())
                    .setTitle(p.getName())
                    .setMessage(p.getLongDesc() + "\n\nPrice: " + p.getPrice())
                    .setPositiveButton("OK", null)
                    .show();
        });

        return view;
    }

    private List<Product> loadProductsFromJson() {
        List<Product> productList = new ArrayList<>();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.products);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONArray jsonArray = new JSONArray(jsonBuilder.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("name");
                String shortDesc = obj.getString("shortDesc");
                String longDesc = obj.getString("longDesc");
                String price = obj.getString("price");
                String imageName = obj.getString("image");

                int imageResId = getResources().getIdentifier(imageName, "drawable", getActivity().getPackageName());
                productList.add(new Product(name, shortDesc, longDesc, imageResId, price));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return productList;
    }
}