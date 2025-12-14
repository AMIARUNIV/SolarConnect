package first.Project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText nameInput = findViewById(R.id.nameInput);
        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        EditText phoneInput = findViewById(R.id.phoneInput);
        EditText addressInput = findViewById(R.id.addressInput);
        RadioGroup roleGroup = findViewById(R.id.roleGroup);
        RadioButton radioClient = findViewById(R.id.radioClient);
        RadioButton radioWorker = findViewById(R.id.radioWorker);
        Button registerButton = findViewById(R.id.registerButton);
        TextView loginText = findViewById(R.id.loginText);

        registerButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String role = radioClient.isChecked() ? "Client" : "Worker";

            // Simple validation
            if (name.isEmpty()) {
                nameInput.setError("Enter your name");
                nameInput.requestFocus();
                return;
            }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Enter a valid email address");
                emailInput.requestFocus();
                return;
            }

            if (password.isEmpty() || password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                passwordInput.requestFocus();
                return;
            }

            if (phone.isEmpty() || phone.length() != 10 || !phone.startsWith("0")) {
                phoneInput.setError("Enter a valid phone number (must start with 0 and have 10 digits)");
                phoneInput.requestFocus();
                return;
            }

            if (address.isEmpty()) {
                addressInput.setError("Enter your address");
                addressInput.requestFocus();
                return;
            }

            Toast.makeText(this, "Registered as " + role, Toast.LENGTH_SHORT).show();

            // Continue to MainActivity
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
