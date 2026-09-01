package com.example.mediguard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etContact;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private MaterialButton btnRegister;
    private ImageButton btnBack;
    private ImageButton btnTogglePassword;
    private ImageButton btnToggleConfirmPassword;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ==========================
        // Initialize Database
        // ==========================

        databaseHelper = new DatabaseHelper(this);

        // ==========================
        // Initialize Views
        // ==========================

        etUsername = findViewById(R.id.etUsername);
        etContact = findViewById(R.id.etContact);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        // ==========================
        // Password Visibility
        // ==========================

        btnTogglePassword.setOnClickListener(
                v -> togglePasswordVisibility(
                        etPassword,
                        btnTogglePassword
                )
        );

        btnToggleConfirmPassword.setOnClickListener(
                v -> togglePasswordVisibility(
                        etConfirmPassword,
                        btnToggleConfirmPassword
                )
        );

        // ==========================
        // Back Button
        // ==========================

        btnBack.setOnClickListener(v -> {

            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
            finish();
        });

        // ==========================
        // Register Button
        // ==========================

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void togglePasswordVisibility(
            EditText passwordField,
            ImageButton toggleButton
    ) {

        if (passwordField.getTransformationMethod()
                instanceof PasswordTransformationMethod) {

            passwordField.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            toggleButton.setImageResource(
                    R.drawable.ic_visibility_off
            );

            toggleButton.setContentDescription(
                    "Hide password"
            );

        } else {

            passwordField.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            toggleButton.setImageResource(
                    R.drawable.ic_visibility
            );

            toggleButton.setContentDescription(
                    "Show password"
            );
        }

        passwordField.setSelection(
                passwordField.length()
        );
    }

    private void registerUser() {


        String username =
                etUsername.getText()
                        .toString()
                        .trim();

        String contact =
                etContact.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString()
                        .trim();

        String confirmPassword =
                etConfirmPassword.getText()
                        .toString()
                        .trim();

        // ==========================
        // Empty Fields
        // ==========================

        if (TextUtils.isEmpty(username)
                || TextUtils.isEmpty(contact)
                || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword)) {

            Toast.makeText(
                    this,
                    "Please fill in all fields.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ==========================
        // Contact Number Validation
        // ==========================

        if (contact.length() != 11) {

            Toast.makeText(
                    this,
                    "Contact number must be 11 digits.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ==========================
        // Password Match
        // ==========================

        if (!password.equals(confirmPassword)) {

            Toast.makeText(
                    this,
                    "Passwords do not match.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ==========================
        // Check Username
        // ==========================

        if (databaseHelper.checkUsername(username)) {

            Toast.makeText(
                    this,
                    "Username already exists!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ==========================
        // Register User
        // ==========================

        boolean inserted =
                databaseHelper.registerUser(
                        username,
                        contact,
                        password
                );

        // ==========================
        // Registration Successful
        // ==========================

        if (inserted) {

            Toast.makeText(
                    this,
                    "Registration Successful!",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
            finish();

        } else {

            Toast.makeText(
                    this,
                    "Registration Failed!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}