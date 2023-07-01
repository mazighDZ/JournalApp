package com.section34.journalApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.section34.journalApp.util.JournalUser;


import java.util.HashMap;
import java.util.Map;

public class SingUpActivity extends AppCompatActivity {
    private static final String KEY_NAME = "userName";
    private static final String KEY_EMAIL = "userEmail";
    EditText password_create , email_create , userName_create;
    Button singUpBTN;
    //firebase auth
    private  FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //firebase connection
    private FirebaseFirestore db= FirebaseFirestore.getInstance();

    private CollectionReference collectionReference =db.collection("Users");
//    private Firestore db_store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);
        //Authentication init
        // [START initialize_auth]
        // Initialize FirebaseApp

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

// we must change rules  to true
//      allow read, write: if true; //
     /**firebase Auth require Google Account on the device to be run successfully**/
        // Initialize Firebase Auth

        singUpBTN= findViewById(R.id.btnSingUp);
        password_create= findViewById(R.id.etPasswordSignUP);
        email_create= findViewById(R.id.etEmailSignUP);
        userName_create= findViewById(R.id.etUserNameSingUP);



        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser!= null){
                    // user already login
                    Toast.makeText(SingUpActivity.this,"not sing in" ,Toast.LENGTH_SHORT).show();

                }else{
                    //no user yet
                    Toast.makeText(SingUpActivity.this,"not sing in" ,Toast.LENGTH_SHORT).show();

                }
            }
        };


        // [END initialize_auth]

        singUpBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(email_create.getText().toString())&&
                        !TextUtils.isEmpty(password_create.getText().toString())){
                    String email = email_create.getText().toString().trim();
                    String password = password_create.getText().toString().trim();
                    String userName = userName_create.getText().toString().trim();
                    Log.d("myTag" ,  "email:"+ email +" passowrd:"+ password );

                    createAccount(email,password,userName);

                }else {
                    Toast.makeText(SingUpActivity.this,"error input",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void createAccount(String email, String password ,String userName) {
        Log.d("myTag" ,  "email:"+ email +" passowrd:"+ password );
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SingUpActivity.this, "successfully created ", Toast.LENGTH_SHORT).show();
                        currentUser = mAuth.getCurrentUser();
                        assert currentUser != null; // means if user is null thrown AssertionError
                        saveDataToNewDocument();
                    }
                }
            });

    }
    private  void saveDataToNewDocument(){

        String name = userName_create .getText().toString().trim();
        Map<String, String> userMap = new HashMap<>();
        userMap.put("userId", currentUser.getUid());
        userMap.put("userName", name);
//        userMap.put("en", name);
        collectionReference.add(userMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.getResult().exists()){
                            String userName = task.getResult().getString("userName");

                            //create Global Journal User
                            //so that when moving to addjournalactivity we find AddJournal already exist
                            JournalUser journalUser = JournalUser.getInstance();
                            journalUser.setUserId(currentUser.getUid());
                            journalUser.setUserName(userName);

                            Intent i = new Intent(SingUpActivity.this,AddJournalActivity.class);
                            i.putExtra("userName",userName);
                            i.putExtra("userId",currentUser.getUid());
                            startActivity(i);
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //display Toast message
                Toast.makeText(SingUpActivity.this, "something went Wrong  ", Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(authStateListener);

    }
}