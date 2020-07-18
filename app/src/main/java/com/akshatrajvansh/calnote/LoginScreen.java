package com.akshatrajvansh.calnote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.akshatrajvansh.calnote.Adapters.LoginSlidesAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginScreen extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    GoogleSignInClient mGoogleSignInClient;
    SignInButton googleButton;
    private ViewPager viewPager;
    private int[] layouts = {R.layout.login_intro_slide1, R.layout.login_intro_slide2, R.layout.login_intro_slide3};
    private LoginSlidesAdapter loginSlidesAdapter;

    private LinearLayout Dots_Layout;
    private ImageView[] dots;

    private int RC_SIGN_IN = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_intro_screen);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleButton = findViewById(R.id.google_button);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        viewPager = findViewById(R.id.viewPager);
        loginSlidesAdapter = new LoginSlidesAdapter(layouts, this);
        viewPager.setAdapter(loginSlidesAdapter);

        Dots_Layout = findViewById(R.id.dotsLayout);
        googleButton.setVisibility(View.INVISIBLE);
        createDots(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                createDots(position);
                if (position == layouts.length - 1)
                    googleButton.setVisibility(View.VISIBLE);
                else
                    googleButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void createDots(int current_position) {
        if (Dots_Layout != null)
            Dots_Layout.removeAllViews();

        dots = new ImageView[layouts.length];

        for (int i = 0; i < layouts.length; i++) {
            dots[i] = new ImageView(this);
            if (i == current_position) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dots));
            } else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.default_dots));
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(15, 0, 15, 0);

            Dots_Layout.addView(dots[i], params);
        }
    }

    private void updateUI(GoogleSignInAccount acct) {
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();

            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("Name", personName);
            userDetails.put("Email", personEmail);
            userDetails.put("Account Name", personGivenName);
            //  userDetails.put("Photo", personPhoto);

            // Add a new document with a generated ID
            db.collection("Users").document(personId)
                    .set(userDetails)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Firestore-Login", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firestore-Login", "Error writing document", e);
                        }
                    });
            Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
            Intent intent = new Intent(LoginScreen.this, HomeScreen.class);
            intent.putExtra("UID", account.getId());
            startActivity(intent);
            finish();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GOOGLE", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }


}
