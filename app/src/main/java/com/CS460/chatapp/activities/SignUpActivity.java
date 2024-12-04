package com.CS460.chatapp.activities;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.CS460.chatapp.R;
import com.CS460.chatapp.databinding.ActivitySignUpBinding;
import com.CS460.chatapp.utilities.Constants;
import com.CS460.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Controller class for the sign up activity
 */
public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String encodeImage;
    private PreferenceManager preferenceManager;

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        encodeImage = encodeImage(bitmap);
                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    /**
     * Initialization method
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    /**
     * Set listeners for the buttons in this activity
     */
    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                onBackPressed());

        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()) {
                signUp();
            }
        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
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
     * Create a new account given the data stored in the EditTexts and ImageView
     */
    private void signUp() {
        //check loading
        loading(true);

        //post to firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, String> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME, binding.inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME, binding.inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodeImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_FIRST_NAME, binding.inputFirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME, binding.inputLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodeImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }).addOnFailureListener(exception -> {
                    loading(false);
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Encode an image into a string to make storing in a text only database easy
     * @param bitmap Bitmap image to be encoded
     * @return String encoded image
     */
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Make sure the user has entered everything necessary to create a new account,
     * and send a toast message if there is something missing
     * @return boolean true/false depending on the state of the elements
     */
    private boolean isValidSignUpDetails() {
        if(encodeImage == null) {
            showToast("Please add a profile image");
            return false;
        }
        else if(binding.inputFirstName.getText().toString().trim().isEmpty()) {
            showToast("Please enter your first name");
            return false;
        }
        else if(binding.inputLastName.getText().toString().trim().isEmpty()) {
            showToast("Please enter your last name");
            return false;
        }
        else if(binding.inputEmail.getText().toString().trim().isEmpty()) {
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
        else if(binding.confirmInputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please confirm your password");
            return false;
        }

        //make sure the password is equal to the confirm password
        if(!binding.inputPassword.getText().toString().equals(binding.confirmInputPassword.getText().toString())) {
            showToast("the two passwords do not match!");
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
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}