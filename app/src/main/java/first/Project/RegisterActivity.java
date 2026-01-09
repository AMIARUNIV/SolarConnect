package first.Project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import first.Project.models.ApiResponse;
import first.Project.models.RegistrationRequest;
import first.Project.models.User;
import first.Project.network.ApiService;
import first.Project.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput, emailInput, passwordInput;
    private EditText phoneInput, birthDateInput, addressInput;
    private RadioGroup roleGroup;
    private RadioButton radioClient, radioWorker;
    private Button registerButton;
    private TextView loginText;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Retrofit
        apiService = RetrofitClient.getApiService();

        // Initialize views
        initializeViews();

        // Set up listeners
        setupListeners();
    }

    private void initializeViews() {
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneInput = findViewById(R.id.phoneInput);
        birthDateInput = findViewById(R.id.birthDateInput);
        addressInput = findViewById(R.id.addressInput);
        roleGroup = findViewById(R.id.roleGroup);
        radioClient = findViewById(R.id.radioClient);
        radioWorker = findViewById(R.id.radioWorker);
        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> attemptRegistration());

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegistration() {
        // Get input values
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String birthDate = birthDateInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String role = radioClient.isChecked() ? "CLIENT" : "WORKER";

        // Validate inputs
        if (!validateInputs(firstName, lastName, email, password, phone, address)) {
            return;
        }

        // Create registration request
        RegistrationRequest request = new RegistrationRequest(
                firstName, lastName, email, password, phone, role, birthDate, address
        );

        // Show loading
        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        // Call API
        apiService.register(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                registerButton.setEnabled(true);
                registerButton.setText("Register");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        // Registration successful
                        User registeredUser = apiResponse.getData();
                        Toast.makeText(RegisterActivity.this,
                                apiResponse.getMessage(), Toast.LENGTH_LONG).show();

                        // Go to login page
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("REGISTERED_EMAIL", email);
                        startActivity(intent);
                        finish();

                    } else {
                        // API returned error
                        Toast.makeText(RegisterActivity.this,
                                apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error
                    Toast.makeText(RegisterActivity.this,
                            "Registration failed: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                registerButton.setEnabled(true);
                registerButton.setText("Register");

                Toast.makeText(RegisterActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private boolean validateInputs(String firstName, String lastName, String email,
                                   String password, String phone, String address) {

        boolean isValid = true;

        // First name validation
        if (firstName.isEmpty()) {
            firstNameInput.setError("Enter your first name");
            firstNameInput.requestFocus();
            isValid = false;
        }

        // Last name validation
        if (lastName.isEmpty()) {
            lastNameInput.setError("Enter your last name");
            lastNameInput.requestFocus();
            isValid = false;
        }

        // Email validation
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            isValid = false;
        }

        // Password validation
        if (password.isEmpty() /*|| password.length() < 6*/) {
           /* passwordInput.setError("Password must be at least 6 characters");*/
            passwordInput.requestFocus();
            isValid = false;
        }

        // Phone validation
        if (phone.isEmpty() || phone.length() != 10 || !phone.startsWith("0")) {
            phoneInput.setError("Enter a valid phone number (must start with 0 and have 10 digits)");
            phoneInput.requestFocus();
            isValid = false;
        }

        // Address validation
        if (address.isEmpty()) {
            addressInput.setError("Enter your address");
            addressInput.requestFocus();
            isValid = false;
        }

        return isValid;
    }
}