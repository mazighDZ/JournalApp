package com.section34.journalApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

//import com.google.firebase.Timestamp;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.Timestamp;
import com.google.firebase.appcheck.AppCheckToken;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.section34.journalApp.model.Journal;
import com.section34.journalApp.util.JournalUser;

import java.util.Date;

public class AddJournalActivity extends AppCompatActivity {

    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton , imageView;
    private EditText titleEditText, descriptionEditText;
    private TextView currentUserTextView;

// User ID and userName
    private  String currentUserID;
    private  String currentUserName;

    //firebase
    private FirebaseAuth firebaseAuth;
    private  FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //connection to firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
   private StorageReference storageReference;
   private CollectionReference collectionReference =db.collection("Journal");
   private Uri imageUri;
    private static int GALLERY_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.post_progressBar);
        titleEditText = findViewById(R.id.edPost_title);
        descriptionEditText = findViewById(R.id.edPostDescription);
        currentUserTextView = findViewById(R.id.post_username_textview);

        imageView = findViewById(R.id.ivPostImage);
        saveButton = findViewById(R.id.post_save_journal_BTN);
        addPhotoButton = findViewById(R.id.postCameraBtn);

        progressBar.setVisibility(View.INVISIBLE);

        //         Initialize Firebase App Check
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());

        //debug app check



        if(JournalUser.getInstance()!=null){

            currentUserID = JournalUser.getInstance().getUserId();
            currentUserName = JournalUser.getInstance().getUserName();

            currentUserTextView.setText(currentUserName);
        }

        authStateListener  = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user !=null){

                }else {


                }
            }
        };

saveButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        firebaseAppCheck.getAppCheckToken(false).addOnCompleteListener(new OnCompleteListener<AppCheckToken>() {
            @Override
            public void onComplete(@NonNull Task<AppCheckToken> task) {
                if(task.isSuccessful()){
                    saveJournal();
                }else {
                    Exception exception = task.getException();
                    if (exception instanceof FirebaseException) {
                        FirebaseException firebaseException = (FirebaseException) exception;
                        Log.d("myTag", "FirebaseException: " + firebaseException.getMessage());
                        // Show error message to the user
                        Toast.makeText(AddJournalActivity.this, "FirebaseException: " + firebaseException.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        // Other exceptions
                        Log.d("myTag", "Task unsuccessful: " + exception.getMessage());
                        // Show a generic error message to the user
                        Toast.makeText(AddJournalActivity.this, "Task unsuccessful", Toast.LENGTH_SHORT).show();

                    }
                }

            }
        });


    }
});

addPhotoButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //getting Image from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT  );
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_CODE);

    }
});

    }

    private void saveJournal() {

   final String title = titleEditText.getText().toString().trim();
   final String description  = descriptionEditText.getText().toString().trim();
    progressBar.setVisibility(View.VISIBLE);
    if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && imageUri !=null){

    // the saving path of the image in Storage firebase
        //.../journal_images/imageName.png
        final  StorageReference filepath = storageReference.child("journal_images")
                .child("my_image"+ Timestamp.now().getSeconds());// all image it will save in format my_image(time second)

        //upload img
        filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                       String imageUri = uri.toString();
                       //creating object of journal
                        Journal journal = new Journal();

                        journal.setTitle(title);
                        journal.setDescription(description);
                        journal.setImageUri(imageUri);
                        journal.setTimeAdded(new Timestamp(new Date()));
                        journal.setUserName(currentUserName);
                        journal.setUserId(currentUserID);

                       // invoking collection Reference
                        collectionReference.add(journal)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        startActivity(new Intent(AddJournalActivity.this , JournalListActivity.class));
                                        finish();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext() , "Failed"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }else {
      Toast.makeText(getApplicationContext(),"Field Empty ",Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.INVISIBLE);

    }
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_CODE && resultCode== RESULT_OK){
            imageUri= data.getData();  //getting the actual path
            imageView.setImageURI(imageUri); //showing the image

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        //attach it
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //detach  it
        if (firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}