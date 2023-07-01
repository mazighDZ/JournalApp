package com.section34.journalApp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.appcheck.AppCheckToken;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.section34.journalApp.util.JournalUser;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    EditText password;
    EditText email;
    Button loginBTN;
    Button createAccBTN;
    SignInButton signInButtonGoogle;
    //google signe in Api
    GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;

    // Firebase authentication
    private FirebaseAuth firebaseAuth;



    // Firebase Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        loginBTN = findViewById(R.id.email_sign_in_button);
        createAccBTN = findViewById(R.id.create_acct_BTN);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        signInButtonGoogle = findViewById(R.id.google_sing_in_btn);



        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(false);

        // Initialize Firebase App Check
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();



        //when using google play counsel
//        firebaseAppCheck.installAppCheckProviderFactory(
//                PlayIntegrityAppCheckProviderFactory.getInstance()
//        );
        //debuging-- you must add debug token that u will find in logcat by search (DebugAppCheckProvider) into firebase console
                firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());

              /**Google signing**/
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) //webAPI from Firebase
                .requestEmail()
                        .build();

        // Create a GoogleSignInClient object
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"google " ,Toast.LENGTH_SHORT).show();
                Log.d("myTag", " google intent sart " );

                // User is not signed in, show the sign-in UI
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });

        createAccBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SingUpActivity.class);
                startActivity(i);
            }
        });

        // Handle the back button press event
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(MainActivity.this, JournalListActivity.class);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(password.getText().toString()) && !TextUtils.isEmpty(email.getText().toString())) {
                    String userPassword = password.getText().toString().trim();
                    String userEmail = email.getText().toString().trim();
                    Log.d("myTag", "|" + userEmail + " " + userPassword);

                    firebaseAppCheck.getAppCheckToken(false).addOnCompleteListener(new OnCompleteListener<AppCheckToken>() {
                        @Override
                        public void onComplete(@NonNull Task<AppCheckToken> task) {
                            if (task.isSuccessful()) {
                                AppCheckToken token = task.getResult();
                                Log.d("myTag", "token: " + token);

                                signInWithEmailAndRecaptcha(userEmail, userPassword);
                            } else {
                                // Task not successful
                                Exception exception = task.getException();
                                if (exception instanceof FirebaseException) {
                                    FirebaseException firebaseException = (FirebaseException) exception;
                                    Log.d("myTag", "FirebaseException: " + firebaseException.getMessage());
                                    // Show error message to the user
                                    Toast.makeText(MainActivity.this, "FirebaseException: " + firebaseException.getMessage(), Toast.LENGTH_SHORT).show();
                                } else {
                                    // Other exceptions
                                    Log.d("myTag", "Task unsuccessful: " + exception.getMessage());
                                    // Show a generic error message to the user
                                    Toast.makeText(MainActivity.this, "Task unsuccessful", Toast.LENGTH_SHORT).show();

                                }
                        }
                        }
                    });
                }
            }
        });
    }

    private void signInWithEmailAndRecaptcha(String email, String password ) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.w("MyTAG", "Sign in success", task.getException());
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            // Update UI or perform further actions
                            if (user != null) {
                                String currentUserId = user.getUid();
                                // Create a singleton JournalUser
                                JournalUser journalUser = JournalUser.getInstance();
                                journalUser.setUserId(currentUserId);
                                   //display list of journal after login
                                Intent i = new Intent(MainActivity.this, JournalListActivity.class);
                                startActivity(i);
                            }
                        } else {
                            // Sign in failed
                            Log.w("MyTAG", "Sign in failed", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //adding menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_add){
            if( firebaseAuth != null){
                Intent i = new Intent(MainActivity.this , AddJournalActivity.class);
                startActivity(i);
            }


        } else if (item.getItemId() == R.id.action_signout) {
            if( firebaseAuth != null){
                firebaseAuth.signOut();
                Intent i = new Intent(MainActivity.this , MainActivity.class);
                startActivity(i);
            }
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            Log.d("myTag", "try getResult  GoogleSignInAccount : " );
            } catch (ApiException e) {
                // Google Sign-In failed, handle the error
                // TODO: Add your code here to handle the error
                Log.e("myTag", "Exception: " + e.getMessage(), e);

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // Get the ID token from the GoogleSignInAccount object
        String idToken = account.getIdToken();

        // Create a credential object
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // Authenticate with Firebase using the credential
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            // TODO: Add your code here to proceed to the main activity
                            String currentUserId = user.getUid();
                            // Create a singleton JournalUser
                            JournalUser journalUser = JournalUser.getInstance();
                            journalUser.setUserId(currentUserId);
                            //display list of journal after login
                            Intent i = new Intent(MainActivity.this, JournalListActivity.class);
                            // pass gso (google singin option ) in other acctivity that you can singout later
                            i.putExtra("gso", gso);
                            startActivity(i);

                        } else {
                            // Sign in failed, display a message to the user
                            // TODO: Add your code here to handle the sign-in failure
                            Log.w("MyTAG", "Sign in failed", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    }

