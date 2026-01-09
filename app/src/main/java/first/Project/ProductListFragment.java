package first.Project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import first.Project.models.ApiResponse;
import first.Project.Product;
import first.Project.network.ApiService;
import first.Project.network.RetrofitClient;
import first.Project.utils.CartManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment {

    private ListView productList;
    private List<Product> products;
    private ProductAdapter adapter;
    private ProgressBar progressBar;
    private ImageView cartIcon;
    private TextView cartBadge;
    private CartManager cartManager;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_products_list, container, false);

        productList = view.findViewById(R.id.productList);
        progressBar = view.findViewById(R.id.progressBar);
        cartIcon = view.findViewById(R.id.cartIcon);
        cartBadge = view.findViewById(R.id.cartBadge);

        products = new ArrayList<>();
        cartManager = CartManager.getInstance(getActivity());

        adapter = new ProductAdapter(getActivity(), products);
        productList.setAdapter(adapter);

        // Set up cart icon click
        cartIcon.setOnClickListener(v -> {
            openCart();
        });

        // Update cart badge
        updateCartBadge();

        // Fetch products from API
        fetchProductsFromAPI();

        return view;
    }

    private void fetchProductsFromAPI() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<List<Product>>> call = apiService.getProducts();

        call.enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Product>> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        products.clear();
                        products.addAll(apiResponse.getData());
                        adapter.notifyDataSetChanged();

                        if (products.isEmpty()) {
                            Toast.makeText(getActivity(), "No products available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to fetch products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void openCart() {
        Intent intent = new Intent(getActivity(), CartActivity.class);
        startActivity(intent);
    }

    private void updateCartBadge() {
        int itemCount = cartManager.getItemCount();
        if (itemCount > 0) {
            cartBadge.setText(String.valueOf(itemCount));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update cart badge when returning from CartActivity
        updateCartBadge();
    }
}