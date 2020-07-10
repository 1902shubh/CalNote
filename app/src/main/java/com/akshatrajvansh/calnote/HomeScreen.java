package com.akshatrajvansh.calnote;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.akshatrajvansh.calnote.Adapters.LockableViewPager;
import com.akshatrajvansh.calnote.Adapters.SectionsPagerAdapter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONObject;

public class HomeScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private GoogleSignInAccount googleSignIn;
    private GoogleSignInClient client;
    private Button showDrawer;
    private GoogleSignInOptions gso;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private LockableViewPager viewPager;

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
        googleSignIn = GoogleSignIn.getLastSignedInAccount(this);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, gso);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

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
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.lockableViewPager);
        viewPager.setSwipeable(false);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        showDrawer.setOnClickListener(this);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //(findViewById(R.id.nav_all_subjects)).setClickable(false);
        if (id == R.id.nav_profile) {
            Toast.makeText(HomeScreen.this, "Profile Button Clicked", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(4);
        } else if (id == R.id.nav_attendance) {
            Toast.makeText(HomeScreen.this, "Attendance Button Clicked", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(0);
        } else if (id == R.id.nav_udhariyaan) {
            Toast.makeText(HomeScreen.this, "Udhariyaan Button Clicked", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(1);
        } else if (id == R.id.nav_notes) {
            Toast.makeText(HomeScreen.this, "Notes Button Clicked", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(2);
        } else if (id == R.id.nav_browser) {
            Toast.makeText(HomeScreen.this, "WebBrowser Button Clicked", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(3);
        }else if (id == R.id.nav_introduction) {
            Toast.makeText(this, "Introduction Clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_logout) {
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Log Out")
                    .setMessage("Do you want to log out?")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logOut();
                        }
                    }).setNegativeButton("no", null).show();

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logOut() {
        client.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> history) {
                startActivity(new Intent(HomeScreen.this, SplashScreen.class));
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.more_button:
                getDeveloperDetails();
                drawer.openDrawer(Gravity.LEFT);
                break;
        }
    }

    private void getDeveloperDetails() {
        String url = "https://us-central1-calnote-b55c7.cloudfunctions.net/getDeveloper";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(HomeScreen.this, response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeScreen.this, "That didn't work!", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);

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
