package com.bratuha.pwkeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class AuthenticationActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String PIN_KEY = "PIN";
    private static final String USE_BIOMETRIC_KEY = "UseBiometric";

    private SharedPreferences prefs;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set up biometric authentication
        setupBiometricAuth();

        // Start authentication process
        startAuthentication();
    }

    private void startAuthentication() {
        // Check if PIN is set up
        if (!isPinSetUp()) {
            showSetupPinDialog();
        } else if (isBiometricEnabled()) {
            // If biometric is enabled, show biometric prompt
            showBiometricPrompt();
        } else {
            // Otherwise, show PIN authentication
            showPinAuthDialog();
        }
    }

    private boolean isPinSetUp() {
        return prefs.contains(PIN_KEY);
    }

    private boolean isBiometricEnabled() {
        return prefs.getBoolean(USE_BIOMETRIC_KEY, false);
    }

    private void setupBiometricAuth() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(AuthenticationActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                // Show PIN authentication on error
                showPinAuthDialog();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(AuthenticationActivity.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                startMainActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AuthenticationActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use PIN")
                .build();
    }

    private void showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo);
    }

    private void showSetupPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set up PIN");
        final EditText input = new EditText(this);
        input.setHint("Enter a 6-digit PIN");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String pin = input.getText().toString();
            if (pin.length() == 6) {
                savePin(pin);
                checkBiometricAvailability();
            } else {
                Toast.makeText(AuthenticationActivity.this, "PIN must be 6 digits", Toast.LENGTH_SHORT).show();
                showSetupPinDialog();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> finish());

        builder.setCancelable(false);
        builder.show();
    }

    private void savePin(String pin) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PIN_KEY, pin);
        editor.apply();
    }

    private void checkBiometricAvailability() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                showBiometricEnableDialog();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Biometric features are not available
                startMainActivity();
                break;
        }
    }

    private void showBiometricEnableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable Biometric Login")
                .setMessage("Do you want to enable biometric authentication?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(USE_BIOMETRIC_KEY, true);
                    editor.apply();
                    startMainActivity();
                })
                .setNegativeButton("No", (dialog, which) -> startMainActivity())
                .setCancelable(false)
                .show();
    }

    private void showPinAuthDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PIN");
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String enteredPin = input.getText().toString();
            if (validatePin(enteredPin)) {
                Toast.makeText(AuthenticationActivity.this, "PIN authentication successful", Toast.LENGTH_SHORT).show();
                startMainActivity();
            } else {
                Toast.makeText(AuthenticationActivity.this, "Invalid PIN", Toast.LENGTH_SHORT).show();
                showPinAuthDialog();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> finish());

        builder.setCancelable(false);
        builder.show();
    }

    private boolean validatePin(String enteredPin) {
        String storedPin = prefs.getString(PIN_KEY, "");
        return storedPin.equals(enteredPin);
    }

    private void startMainActivity() {
        Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}