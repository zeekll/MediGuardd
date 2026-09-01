package com.example.mediguard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private ImageButton btnBack;
    private ImageButton btnTogglePassword;
    private TextView txtRegister;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Database
        databaseHelper = new DatabaseHelper(this);

        // Initialize Views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.btnBack);
        txtRegister = findViewById(R.id.txtRegister);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);

        // Password Visibility
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Back Button
        btnBack.setOnClickListener(v -> finish());

        // Register Link
        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        // Login Button
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void togglePasswordVisibility() {

        if (etPassword.getTransformationMethod()
                instanceof PasswordTransformationMethod) {

            etPassword.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            btnTogglePassword.setImageResource(
                    R.drawable.ic_visibility_off
            );

            btnTogglePassword.setContentDescription(
                    "Hide password"
            );

        } else {

            etPassword.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            btnTogglePassword.setImageResource(
                    R.drawable.ic_visibility
            );

            btnTogglePassword.setContentDescription(
                    "Show password"
            );
        }

        etPassword.setSelection(
                etPassword.length()
        );
    }

    private void loginUser() {


        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Check Empty Fields
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {

            Toast.makeText(this,
                    "Please enter username and password.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        // Check Login Credentials
        boolean checkLogin = databaseHelper.loginUser(username, password);

        if (checkLogin) {

            Toast.makeText(this,
                    "Login Successful!",
                    Toast.LENGTH_SHORT).show();

            int userId = databaseHelper.getUserId(username);

            getSharedPreferences(
                    "MediGuardPrefs",
                    MODE_PRIVATE
            ).edit()
                    .putInt("user_id", userId)
                    .putString("username", username)
                    .apply();

            Intent intent = new Intent(
                    LoginActivity.this,
                    MainActivity.class
            );

            intent.putExtra(
                    "username",
                    username
            );

            intent.putExtra(
                    "user_id",
                    userId
            );

            startActivity(intent);
            finish();



        } else {

            Toast.makeText(this,
                    "Login Unsuccessful!\nIncorrect username or password.",
                    Toast.LENGTH_LONG).show();

        }
    }
}