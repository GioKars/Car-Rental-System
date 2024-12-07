package com.example.car_rent;

import android.Manifest;
import android.database.Cursor;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyAccountActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private static final String UPLOAD_URL = "http://10.0.2.2/upload_image.php"; // Update with your XAMPP server URL

    private Button buttonUploadDriverLicense, buttonUploadId, buttonSaveAccountInfo;
    private ImageView imageViewDriverLicenseFront, imageViewDriverLicenseBack;
    private ImageView imageViewIdFront, imageViewIdBack;
    private TextView uploadProgressCounter, uploadProgressCounterID, editTextEmail, editTextFirstName, editTextLastName, editTextPhone;

    private Uri imageUri;
    private boolean isFrontImage = true;
    private String currentDocumentType;

    private int uploadedDriverLicenseCount = 0;
    private int uploadedIdCardCount = 0;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        // Firebase Realtime Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Ensure permission for accessing storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextFirstName = findViewById(R.id.editTextName); // Assuming you have this field in your layout
        editTextLastName = findViewById(R.id.editTextSurname);   // Assuming you have this field in your layout
        editTextPhone = findViewById(R.id.editTextPhoneNumber);

        imageViewDriverLicenseFront = findViewById(R.id.driverLicenseFront);
        imageViewDriverLicenseBack = findViewById(R.id.driverLicenseBack);
        imageViewIdFront = findViewById(R.id.idCardFront);
        imageViewIdBack = findViewById(R.id.idCardBack);

        buttonUploadDriverLicense = findViewById(R.id.buttonUploadDriverLicense);
        buttonUploadId = findViewById(R.id.buttonUploadId);
        buttonSaveAccountInfo = findViewById(R.id.buttonSaveAccountInfo); // Button to save user data
        uploadProgressCounter = findViewById(R.id.uploadProgressCounterDL); // For driver license
        uploadProgressCounterID = findViewById(R.id.uploadProgressCounterID); // For ID card

        buttonUploadDriverLicense.setOnClickListener(v -> startUploadProcess("driver_license", uploadProgressCounter));
        buttonUploadId.setOnClickListener(v -> startUploadProcess("id", uploadProgressCounterID));

        buttonSaveAccountInfo.setOnClickListener(v -> saveUserData());

        displayUserEmail();

        // Check if user data already exists
        checkAndPopulateUserData();
    }

    private void displayUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            editTextEmail.setText(email);
            editTextEmail.setEnabled(false);
        } else {
            editTextEmail.setText("No user logged in");
        }
    }

    private void startUploadProcess(String documentType, TextView progressCounter) {
        isFrontImage = true; // Start with the front image
        currentDocumentType = documentType;
        selectOrCaptureImage(documentType + "_front");
    }

    private void selectOrCaptureImage(String imageType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option");
        builder.setItems(new CharSequence[]{"Choose from Gallery", "Take Photo"}, (dialog, which) -> {
            if (which == 0) {
                pickImageFromGallery(imageType);
            } else {
                capturePhoto(imageType);
            }
        });
        builder.show();
    }

    private void pickImageFromGallery(String imageType) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void capturePhoto(String imageType) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        }
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    uploadImageToServer(imageUri, currentDocumentType + (isFrontImage ? "_front" : "_back"));
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    Uri photoUri = getImageUri(bitmap);
                    uploadImageToServer(photoUri, currentDocumentType + (isFrontImage ? "_front" : "_back"));
                }
            });

    private Uri getImageUri(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void uploadImageToServer(Uri uri, String imageType) {
        // Get the user ID from Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "unknown";

        File file = new File(getRealPathFromURI(uri));
        if (file.exists()) {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", file.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), file))
                    .addFormDataPart("userId", userId) // Include userId
                    .build();

            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MyAccountActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            runOnUiThread(() -> {
                                if (currentDocumentType.equals("driver_license")) {
                                    // Update the image view based on whether it's the front or back
                                    if (isFrontImage) {
                                        imageViewDriverLicenseFront.setImageURI(uri);
                                        uploadedDriverLicenseCount++; // Increment the count
                                    } else {
                                        imageViewDriverLicenseBack.setImageURI(uri);
                                        uploadedDriverLicenseCount++; // Increment the count
                                    }

                                    // Update the progress counter
                                    uploadProgressCounter.setText(uploadedDriverLicenseCount + "/2");

                                    // Disable button if both sides of the driver's license are uploaded
                                    if (uploadedDriverLicenseCount == 2) {
                                        buttonUploadDriverLicense.setEnabled(false);
                                    }

                                } else if (currentDocumentType.equals("id")) {
                                    // Similar logic for the ID card
                                    if (isFrontImage) {
                                        imageViewIdFront.setImageURI(uri);
                                        uploadedIdCardCount++;
                                    } else {
                                        imageViewIdBack.setImageURI(uri);
                                        uploadedIdCardCount++;
                                    }

                                    // Update the progress counter for the ID card
                                    uploadProgressCounterID.setText(uploadedIdCardCount + "/2");

                                    // Disable button if both sides of the ID card are uploaded
                                    if (uploadedIdCardCount == 2) {
                                        buttonUploadId.setEnabled(false);
                                    }
                                }

                                // Handle front/back image switching for the next upload
                                if (isFrontImage) {
                                    isFrontImage = false;
                                    selectOrCaptureImage(currentDocumentType + "_back"); // Prepare for back image
                                } else {
                                    isFrontImage = true; // Toggle to upload front next
                                }
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(MyAccountActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show());
                        }
                    } finally {
                        // Always close the response to avoid connection leaks
                        response.close();
                    }
                }

            });
        } else {
            Toast.makeText(MyAccountActivity.this, "File not found.", Toast.LENGTH_SHORT).show();
        }
    }


    private String getRealPathFromURI(Uri uri) {
        if (uri.getScheme().equals("content")) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                return filePath;
            }
        }
        return uri.getPath(); // Default handling
    }

    private void saveUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            Log.d("SaveUserData", "Firebase UID: " + uid);
//            String uniqueFolderName = generateUniqueFolderName();

            // Get Firestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Collect user data
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String email = currentUser.getEmail();
            String phone = editTextPhone.getText().toString().trim();

            // Validation (ensure fields are not empty)
            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // **Validation for uploaded images**
            if (imageViewDriverLicenseFront.getDrawable() == null) {
                Toast.makeText(this, "Please upload the front side of your Driver's License.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageViewDriverLicenseBack.getDrawable() == null) {
                Toast.makeText(this, "Please upload the back side of your Driver's License.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageViewIdFront.getDrawable() == null) {
                Toast.makeText(this, "Please upload the front side of your ID card.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageViewIdBack.getDrawable() == null) {
                Toast.makeText(this, "Please upload the back side of your ID card.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a Map to store user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("allowImageReupload", false); // or a default value
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("email", email);
            userData.put("phone", phone);
//            userData.put("folderName", uniqueFolderName); // Store the folder name

            // Save data to Firestore under the "Users" collection
            db.collection("Users").document(uid)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MyAccountActivity.this, "User data saved successfully.", Toast.LENGTH_SHORT).show();
                        disableInputs();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyAccountActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("SaveUserData", "Error saving user data", e);
                    });
        } else {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
        }
    }
    private void disableInputs() {
        editTextLastName.setEnabled(false);
        editTextFirstName.setEnabled(false);
        editTextPhone.setEnabled(false);
        editTextEmail.setEnabled(false);

        buttonUploadDriverLicense.setVisibility(TextView.GONE);
        buttonUploadId.setVisibility(TextView.GONE);

        buttonSaveAccountInfo.setVisibility(TextView.GONE);

        imageViewDriverLicenseFront.setVisibility(TextView.GONE);
        imageViewDriverLicenseBack.setVisibility(TextView.GONE);
        imageViewIdFront.setVisibility(TextView.GONE);
        imageViewIdBack.setVisibility(TextView.GONE);
        uploadProgressCounter.setVisibility(TextView.GONE);
        uploadProgressCounterID.setVisibility(TextView.GONE);
    }

    private void enableUploadButtons() {
        buttonUploadDriverLicense.setEnabled(true);
        buttonUploadId.setEnabled(true);

        imageViewDriverLicenseFront.setClickable(true);
        imageViewDriverLicenseBack.setClickable(true);
        imageViewIdFront.setClickable(true);
        imageViewIdBack.setClickable(true);
    }

    // Check if user data exists in Firestore
    private void checkAndPopulateUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String phone = documentSnapshot.getString("phone");
                            String email = documentSnapshot.getString("email");

                            boolean allowReupload = documentSnapshot.getBoolean("allowImageReupload");

                            editTextFirstName.setText(firstName);
                            editTextLastName.setText(lastName);
                            editTextPhone.setText(phone);
                            editTextEmail.setText(email);

                            if (allowReupload) {
                                enableUploadButtons();
                            } else {
                                disableInputs(); // Keep inputs disabled if re-upload is not allowed
                            }

                            Toast.makeText(MyAccountActivity.this, "Account information loaded.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyAccountActivity.this, "No account information found. Please fill in your details.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyAccountActivity.this, "Failed to fetch account information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("CheckUserData", "Error fetching user data", e);
                    });
        } else {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

}