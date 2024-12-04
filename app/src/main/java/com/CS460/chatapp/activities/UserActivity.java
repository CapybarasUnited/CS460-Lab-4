package com.CS460.chatapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.CS460.chatapp.adapters.UserAdapter;
import com.CS460.chatapp.databinding.ActivityUserBinding;
import com.CS460.chatapp.models.User;
import com.CS460.chatapp.utilities.Constants;
import com.CS460.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {
    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        setListeners();
        getUsers();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot snapshot : task.getResult()) {
                            if(currentUserId.equals(snapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.firstName = snapshot.getString(Constants.KEY_FIRST_NAME);
                            user.lastName = snapshot.getString(Constants.KEY_LAST_NAME);
                            user.email = snapshot.getString(Constants.KEY_EMAIL);
                            user.image = snapshot.getString(Constants.KEY_IMAGE);
                            user.token = snapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = snapshot.getString(Constants.KEY_USER_ID);
                            users.add(user);
                        }

                        if(!users.isEmpty()) {
                            UserAdapter userAdapter = new UserAdapter(users);
                            binding.usersRecyclerView.setAdapter(userAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else {
                            showErrorMessage("You have no friends :(");
                        }
                    }
                    else {
                        showErrorMessage("Error retrieving data from database");
                    }
                });
    }

    private void showErrorMessage(String message) {
        binding.textErrorMessage.setText(message);
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(boolean isLoading) {
        if(isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}