package com.CS460.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.CS460.chatapp.R;
import com.CS460.chatapp.databinding.ActivitySignInBinding;
import com.CS460.chatapp.utilities.Constants;
import com.CS460.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;

/**
 * Controller class for the sign in activity
 */
public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    /**
     * Initialization method
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    /**
     * Set listeners for the buttons in this activity
     */
    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));

        binding.buttonSignIn.setOnClickListener(v -> {
            if(isValidSignInDetails()) {
                signIn();
            }
        });

        //press enter while in password field to login
        binding.inputPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                binding.buttonSignIn.performClick();  // Simulate button click
                return true;
            }
            return false;
        });
    }

    /**
     * Show a toast message
     * @param message String message to show
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Sign in the user given the text the user has entered, checks to make sure the user provided
     * credentials are present in the database. As per the requirements this method will show a toast
     * for both successful and failed attempts
     */
    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                        preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                        System.out.println("User ID: " + preferenceManager.getString(Constants.KEY_USER_ID));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        showToast("Successful Sign-in!");
                    }
                    else {
                        loading(false);
                        showToast("Email or password are incorrect, or account doesn't exist");
                    }
                });
    }

    /**
     * Make sure the user has entered the right data to allow for a login attempt
     * @return boolean true/false depending on if the user can try logging in
     */
    private boolean isValidSignInDetails() {
        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email address");
            return false;
        }

        //make sure the email follows the valid email pattern
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please enter a valid email address");
            return false;
        }
        else if(binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter your password");
            return false;
        }

        return true;
    }

    /**
     * Hide/show the sign up button and progress bar depending on what the app is doing
     * @param isLoading boolean true/false depending on if the app is loading or not
     */
    private void loading (Boolean isLoading) {
        if(isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}