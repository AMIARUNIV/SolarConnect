package first.Project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import first.Project.models.LoginRequest;
import first.Project.network.ApiService;
import first.Project.network.NetworkUtils;
import first.Project.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        TextView signupText = findViewById(R.id.signupText);
        Button googleSignInButton = findViewById(R.id.googleSignInButton);

        // Normal login
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check network connection
            if (!NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading
            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

            // Call login API
            loginUser(email, password);
        });

        // Go to register
        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Fake Google sign-in
        googleSignInButton.setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show();
        });

        // Check if coming from registration
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("REGISTERED_EMAIL")) {
            String registeredEmail = intent.getStringExtra("REGISTERED_EMAIL");
            emailInput.setText(registeredEmail);
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginUser(String email, String password) {
        ApiService apiService = RetrofitClient.getApiService();
        LoginRequest loginRequest = new LoginRequest(email, password);

        Call<first.Project.models.ApiResponse<first.Project.models.User>> call = apiService.login(loginRequest);

        call.enqueue(new Callback<first.Project.models.ApiResponse<first.Project.models.User>>() {
            @Override
            public void onResponse(Call<first.Project.models.ApiResponse<first.Project.models.User>> call,
                                   Response<first.Project.models.ApiResponse<first.Project.models.User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    first.Project.models.ApiResponse<first.Project.models.User> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        first.Project.models.User user = apiResponse.getData();

                        // DEBUG: Log user info
                        Log.d("LoginDebug", "User ID received: " + user.getUserId());
                        Log.d("LoginDebug", "User Email: " + user.getEmail());
                        Log.d("LoginDebug", "User Role: " + user.getRole());

                        // Determine role based on user type
                        String role = determineUserRole(user);
                        user.setRole(role);

                        // ✅✅✅ CRITICAL: Save user info to SharedPreferences ✅✅✅
                        saveUserInfo(user);

                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Navigate based on role
                        navigateBasedOnRole(role);

                    } else {
                        Toast.makeText(LoginActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<first.Project.models.ApiResponse<first.Project.models.User>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void saveUserInfo(first.Project.models.User user) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save user ID - using getUserId() since your model has getUserId()
        if (user.getUserId() != null) {
            editor.putInt("userId", user.getUserId());
            editor.putInt("user_id", user.getUserId());
            editor.putInt("id", user.getUserId());
            Log.d("LoginActivity", "Saved userId: " + user.getUserId() + " to SharedPreferences");
        } else {
            Log.e("LoginActivity", "User ID is null! Cannot save to SharedPreferences");
        }

        // Save other user info
        if (user.getEmail() != null) {
            editor.putString("email", user.getEmail());
        }
        if (user.getFirstName() != null && user.getLastName() != null) {
            editor.putString("name", user.getFirstName() + " " + user.getLastName());
        }
        if (user.getRole() != null) {
            editor.putString("role", user.getRole());
        }

        editor.apply();
        Log.d("LoginActivity", "User info saved to SharedPreferences");
    }

    private String determineUserRole(first.Project.models.User user) {
        // The backend returns "CLIENT" or "WORKER" in the response
        if (user.getRole() != null) {
            return user.getRole();
        }
        return "CLIENT"; // Default fallback
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;

        if ("WORKER".equals(role)) {
            // Worker goes to WorkerMainActivity
            intent = new Intent(LoginActivity.this, WorkerMainActivity.class);
            intent.putExtra("USER_ROLE", "WORKER");
        } else {
            // Client goes to ClientMainActivity
            intent = new Intent(LoginActivity.this, ClientMainActivity.class);
            intent.putExtra("USER_ROLE", "CLIENT");
            intent.putExtra("SHOW_DASHBOARD_FIRST", true);
        }

        startActivity(intent);
        finish();
    }
}