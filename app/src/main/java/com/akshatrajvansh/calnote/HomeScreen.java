package com.akshatrajvansh.calnote;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akshatrajvansh.calnote.Adapters.LockableViewPager;
import com.akshatrajvansh.calnote.Adapters.SubRecAdapter;
import com.akshatrajvansh.calnote.Adapters.SwipeToDeleteCallback;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class HomeScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private FirebaseFirestore firebaseFirestore;
    private GoogleSignInAccount googleSignIn;
    private GoogleSignInClient client;
    private Button showDrawer;
    private GoogleSignInOptions gso;
    private LockableViewPager viewPager;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton addSubjects;
    private ArrayList<String> SubjectCode = new ArrayList<String>();
    private ArrayList<String> AttendedClasses = new ArrayList<String>();
    private ArrayList<String> BunkedClasses = new ArrayList<String>();
    private ArrayList<String> SubjectName = new ArrayList<String>();
    private EditText subjectName, subjectCode, subjectAtt, subjectBun;
    private String subName, subCode, subAtt, subBun;
    RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private ConstraintLayout AttendanceScreen, UdhariyaanScreen;

    private TextView userName, userEmailID;
    private CircularImageView userProfilePic;
    private ActionBarDrawerToggle toggle;

    private DrawerLayout drawer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        firebaseFirestore = FirebaseFirestore.getInstance();
        googleSignIn = GoogleSignIn.getLastSignedInAccount(this);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, gso);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        AttendanceScreen = findViewById(R.id.attendance_screen);
        UdhariyaanScreen = findViewById(R.id.udhariyaan_screen);
        View headerView = navigationView.getHeaderView(0);

        //Navigation Header will try to access data from the Google Account
        userEmailID = (TextView) headerView.findViewById(R.id.all_sub_email);
        userName = (TextView) headerView.findViewById(R.id.all_sub_username);
        userProfilePic = (CircularImageView) headerView.findViewById(R.id.profilePhoto);
        userEmailID.setText(googleSignIn.getEmail());
        userName.setText(googleSignIn.getDisplayName());
        assert googleSignIn != null;
        Uri photo_url = googleSignIn.getPhotoUrl();
        if (photo_url != null) {
            Glide.with(HomeScreen.this).load(photo_url)
                    .into(userProfilePic);
        }
        //Navigation Header now contains Username, EmailID, and the Profile Photo of the user

        showDrawer = findViewById(R.id.more_button);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //sets the default opening screen of attendance in home screen
        navigationView.setCheckedItem(R.id.nav_attendance);
        recyclerView = findViewById(R.id.recycler_view);
        addSubjects = findViewById(R.id.add_subjects);
        addSubjects.setOnClickListener(this);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        firebaseFirestore = FirebaseFirestore.getInstance();
        DataItems();

        showDrawer.setOnClickListener(this);

    }

    public void addSubjectsPrompt() {
        LayoutInflater layoutInflater = LayoutInflater.from(HomeScreen.this);
        View promptsView = layoutInflater.inflate(R.layout.subject_adding_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeScreen.this);
        alertDialogBuilder.setView(promptsView);
        subjectName = (EditText) promptsView.findViewById(R.id.subject_name);
        subjectCode = (EditText) promptsView.findViewById(R.id.subject_code);
        subjectAtt = (EditText) promptsView.findViewById(R.id.subject_att);
        subjectBun = (EditText) promptsView.findViewById(R.id.subject_bun);
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                subName = subjectName.getText().toString();
                subCode = subjectCode.getText().toString();
                subAtt = subjectAtt.getText().toString();
                subBun = subjectBun.getText().toString();
                addNewSubject(subCode, subName, subAtt, subBun);
            }
        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void addNewSubject(String code, String name, String att, String bun) {
        Map<String, Object> subjectDetails = new HashMap<>();
        subjectDetails.put("Subject Name", name);
        subjectDetails.put("Subject Code", code);
        subjectDetails.put("Attended", att);
        subjectDetails.put("Bunked", bun);
        // Add a new document with a generated ID
        firebaseFirestore.collection("Users").document(googleSignIn.getId()).collection("Attendance").document(code)
                .set(subjectDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FireStore", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FireStore", "Error writing document", e);
                    }
                });
        DataItems();
    }

    public void DataItems() {
        CollectionReference collectionReference = firebaseFirestore.collection("Users")
                .document(googleSignIn.getId()).collection("Attendance");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    clear();
                    Log.d("Firestore", "Current data: " + queryDocumentSnapshots.getDocuments());
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d("TAG", "onSuccess: LIST EMPTY");

                        } else {

                            SubjectName.add(documentSnapshot.getString("Subject Name"));
                            SubjectCode.add(documentSnapshot.getString("Subject Code"));
                            AttendedClasses.add(documentSnapshot.getString("Attended"));
                            BunkedClasses.add(documentSnapshot.getString("Bunked"));
                            Log.d("HomeScreen", documentSnapshot.getData().toString());
                            Log.d("HomeScreen", documentSnapshot.getString("Attended"));
                            mAdapter = new SubRecAdapter(HomeScreen.this, SubjectCode, SubjectName, AttendedClasses, BunkedClasses);
                            recyclerView.setAdapter(mAdapter);
                            ItemTouchHelper itemTouchHelper = new
                                    ItemTouchHelper(new SwipeToDeleteCallback((SubRecAdapter) mAdapter));
                            itemTouchHelper.attachToRecyclerView(recyclerView);
                        }
                    }
                } else {
                    Log.d("Firestore", "Current data: null");
                }
            }
        });
    }

    public void clear() {
        SubjectCode.clear();
        SubjectName.clear();
        AttendedClasses.clear();
        BunkedClasses.clear();
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //(findViewById(R.id.nav_all_subjects)).setClickable(false);
        if (id == R.id.nav_profile) {
            Toast.makeText(HomeScreen.this, "Profile Button Clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_attendance) {
            Toast.makeText(HomeScreen.this, "Attendance Button Clicked", Toast.LENGTH_SHORT).show();
            AttendanceScreen.setVisibility(View.VISIBLE);
            UdhariyaanScreen.setVisibility(View.GONE);
        } else if (id == R.id.nav_udhariyaan) {
            Toast.makeText(HomeScreen.this, "Udhariyaan Button Clicked", Toast.LENGTH_SHORT).show();
            AttendanceScreen.setVisibility(View.GONE);
            UdhariyaanScreen.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_introduction) {
            Toast.makeText(this, "Introduction Clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
            client.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> history) {
                    startActivity(new Intent(HomeScreen.this, SplashScreen.class));
                    finish();
                }
            });
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_subjects:
                addSubjectsPrompt();
                break;
            case R.id.more_button:
                drawer.openDrawer(Gravity.LEFT);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                    .setMessage("Are you sure?")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }).setNegativeButton("no", null).show();
        }
    }


}
