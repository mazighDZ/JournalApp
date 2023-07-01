package com.section34.journalApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.section34.journalApp.model.Journal;
import com.section34.journalApp.ui.JournalRecyclerAdapter;
import com.section34.journalApp.util.JournalUser;

import java.util.ArrayList;
import java.util.List;

public class JournalListActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private  FirebaseUser   user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private List<Journal> journalList;

    private RecyclerView recyclerView;
    private JournalRecyclerAdapter journalRecyclerAdapter;

    private CollectionReference collectionReference= db.collection("Journal");
    private TextView noPostsEntry;

    //google
    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);
        //Menu initailization
        //set toolbar so that Menu will show
        setSupportActionBar(findViewById(R.id.toolbar));


        //1- initialization variables and reference
        //firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        //set current  user
        user =firebaseAuth.getCurrentUser();

            //widgets
        noPostsEntry = findViewById(R.id.list_no_posts);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //posts Arraylist
        journalList = new ArrayList<>();

        //google signingOption that came from singActivity
        Intent i = getIntent();
        GoogleSignInOptions gso = i.getParcelableExtra("gso" );
        if(gso!= null){

         googleSignInClient = GoogleSignIn.getClient(this, gso);
        }

    }

    //adding menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu ,menu);
    return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_add){
            Toast.makeText(getApplicationContext() , "add selected" , Toast.LENGTH_SHORT).show();
            if( user!=null && firebaseAuth != null){
                Intent i = new Intent(JournalListActivity.this , AddJournalActivity.class);
                startActivity(i);
            }


        } else if (item.getItemId() == R.id.action_signout) {
            Toast.makeText(getApplicationContext() , "action_signout selected" , Toast.LENGTH_SHORT).show();

            if(user!=null && firebaseAuth != null){


                //if user sing in with google
                if (googleSignInClient!= null) {
                    googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Sign-out successful. Now the user will be prompted to choose a Google account again.
                            Intent i = new Intent(JournalListActivity.this , MainActivity.class);
                            startActivity(i);
                        }
                    });
                }
                // Call signOut() from firebase authentication
                firebaseAuth.signOut();
                Intent i = new Intent(JournalListActivity.this , MainActivity.class);
                startActivity(i);
            }
        }
        return super.onOptionsItemSelected(item);

    }

    //get all Posts
    @Override
    protected void onStart() {
        super.onStart();

        collectionReference.whereEqualTo("userId", JournalUser.getInstance().getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                   //Inside the loop, this code converts each QueryDocumentSnapshot object
        // (representing a document) to an instance of the Journal class using the toObject() method
                            for(QueryDocumentSnapshot journals : queryDocumentSnapshots){
                                Journal journal = journals.toObject(Journal.class);
                                journalList.add(journal);
                            }
                            //RecyclerView
                             journalRecyclerAdapter = new JournalRecyclerAdapter(JournalListActivity.this ,journalList);
                            recyclerView.setAdapter(journalRecyclerAdapter);
                            journalRecyclerAdapter.notifyDataSetChanged();


                        }else {
                            noPostsEntry.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // any failure
                        Toast.makeText(JournalListActivity.this,"somthing went wrong !" ,Toast.LENGTH_SHORT).show();
                    }
                });

    }
}