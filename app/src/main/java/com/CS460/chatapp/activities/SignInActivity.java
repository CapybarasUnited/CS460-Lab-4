package com.CS460.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.CS460.chatapp.R;
import com.CS460.chatapp.databinding.ActivitySignInBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));

        binding.buttonSignIn.setOnClickListener(v -> addDataToFireStore());
    }

    /**
     * Test Case
     */
    private void addDataToFireStore() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, String> data = new HashMap<>();

        data.put("first_name", "Steve");
        data.put("last_name", "Summers");

        database.collection("User").add(data).addOnSuccessListener(
                documentReference -> {
                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                }
        ).addOnFailureListener(exception -> {
            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}