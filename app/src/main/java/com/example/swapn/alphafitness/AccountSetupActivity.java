package com.example.swapn.alphafitness;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swapn.alphafitness.common.Util;
import com.example.swapn.alphafitness.models.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AccountSetupActivity extends AppCompatActivity {

    EditText input_weight;
    EditText input_height;
    ImageView prof_pic;
    Button submit;
    private static final int CAMERA_REQUEST_IMAGE = 1;
    private StorageReference mStorage;
    private Uri selectedPic;
    private String gender = "";
    Double inputHeight;
    Double inputWeight;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUser;
    UserModel user;
    Util u;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        u = new Util();
        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase.addValueEventListener(postListener);
        mStorage = FirebaseStorage.getInstance().getReference();
        input_height = findViewById(R.id.input_height);
        input_weight = findViewById(R.id.input_weight);
        prof_pic = findViewById(R.id.profile_picture);
        submit = findViewById(R.id.btn_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
        prof_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, CAMERA_REQUEST_IMAGE);
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_male:
                if (checked)
                        gender = "male";
                    break;
            case R.id.radio_female:
                if (checked)
                        gender = "female";
                    break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == CAMERA_REQUEST_IMAGE){
            selectedPic = data.getData();
            Picasso.with(AccountSetupActivity.this).load(selectedPic).fit().centerCrop().into(prof_pic);
        }
    }

    ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI

            user = dataSnapshot.getValue(UserModel.class);
            // ...
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w("AccountSetup", "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    protected boolean validate() {
        boolean valid = false;
        if(selectedPic == null) {
            Toast.makeText(AccountSetupActivity.this,"Uplaod Profile Picture...", Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            inputHeight = Double.parseDouble(input_height.getText().toString());
            inputWeight = Double.parseDouble(input_weight.getText().toString());
        } catch(Exception e) {
            return false;
        }

        if (input_height.getText().toString().isEmpty()) {
            input_height.setError("Enter Height...");
            return false;
        }

        if (input_weight.getText().toString().isEmpty()) {
            input_height.setError("Enter Weight...");
            return false;
        }

        if(gender.isEmpty()) {
            Toast.makeText(this,"Select Gender...", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public void startRecordWorkOutActivity() {
        Intent loginIntent = new Intent(AccountSetupActivity.this, RecordWorkOutActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }

    private void saveUserData() {
        final ProgressDialog progressDialog = new ProgressDialog(AccountSetupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading Data...");
        progressDialog.show();
        if(validate()) {
            StorageReference filePath = mStorage.child("photos").child(selectedPic.getLastPathSegment());
            filePath.putFile(selectedPic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    user.setAccount_setuped(true);
                    user.setProf_pic(downloadUri.toString());
                    Picasso.with(AccountSetupActivity.this).load(Uri.parse(downloadUri.toString())).fit().centerCrop().into(prof_pic);
                    user.setHeight(input_height.getText().toString());
                    user.setWeight(input_weight.getText().toString());
                    user.setGender(gender);
                    mDatabaseUser.child(user.getUid()).setValue(user);
                    u.setSharedPreferences(getApplicationContext(), user);
                    progressDialog.hide();
                    startRecordWorkOutActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AccountSetupActivity.this,"Failed to Uplaod Data...", Toast.LENGTH_LONG);
                    progressDialog.hide();
                }
            });
        } else {
            Log.d("AccountSetup", "Failed to Validated");
            progressDialog.hide();
        }
    }
}
