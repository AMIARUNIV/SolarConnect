package first.Project.network;

import java.util.List;

import first.Project.Product;
import first.Project.models.ApiResponse;
import first.Project.models.LoginRequest;
import first.Project.models.OrderRequest;
import first.Project.models.OrderResponse;
import first.Project.models.RegistrationRequest;
import first.Project.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("mobile/auth/login")
    Call<ApiResponse<User>> login(@Body LoginRequest loginRequest);

    @POST("mobile/auth/register")
    Call<ApiResponse<User>> register(@Body RegistrationRequest registrationRequest);

    @GET("mobile/products")  // CHANGED: Remove "api/" from here
    Call<ApiResponse<List<Product>>> getProducts();

    @GET("mobile/products/available")
    Call<ApiResponse<List<Product>>> getAvailableProducts();

    @GET("mobile/products/{id}")
    Call<ApiResponse<Product>> getProductById(@Path("id") Integer productId);

    // Order endpoints
    @POST("mobile/orders")
    Call<ApiResponse<OrderResponse>> createOrder(@Body OrderRequest orderRequest);

    @GET("mobile/orders/client/{clientId}")
    Call<ApiResponse<List<OrderResponse>>> getClientOrders(@Path("clientId") Integer clientId);

    @GET("mobile/orders/{orderId}")
    Call<ApiResponse<OrderResponse>> getOrderById(@Path("orderId") Integer orderId);
}