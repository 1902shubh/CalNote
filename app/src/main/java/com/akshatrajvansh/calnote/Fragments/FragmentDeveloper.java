package com.akshatrajvansh.calnote.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akshatrajvansh.calnote.Adapters.NotesAdapter;
import com.akshatrajvansh.calnote.HomeScreen;
import com.akshatrajvansh.calnote.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class FragmentDeveloper extends Fragment {
    private FirebaseFirestore firebaseFirestore;
    private GoogleSignInAccount googleSignIn;
    private String devName, devInfo, devTitle;
    private ImageView devImageBlurIV;
    private CircularImageView devImageIV;
    private TextView devNameTV, devInfoTV, devTitleTV;
    private FloatingActionButton share;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_developer, container, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        googleSignIn = GoogleSignIn.getLastSignedInAccount(getContext());
        devNameTV = view.findViewById(R.id.dev_name);
        devInfoTV = view.findViewById(R.id.dev_info);
        devTitleTV = view.findViewById(R.id.dev_title);
        devImageBlurIV = view.findViewById(R.id.dev_image_blur);
        devImageIV = view.findViewById(R.id.dev_image);
        share = view.findViewById(R.id.shareFab);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });
        //getDetails();
        return view;
    }

    private void getDetails() {
        DocumentReference documentReference = firebaseFirestore.collection("Developer")
                .document(googleSignIn.getId());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult() != null) {
                    DocumentSnapshot snapshot = task.getResult();
                    try {
                        devName = snapshot.getString("Name");
                        devTitle = snapshot.getString("Title");
                        devInfo = snapshot.getString("Info");
                        devNameTV.setText(devName);
                        devTitleTV.setText(devTitle);
                        devInfoTV.setText(devInfo);
                       /* Uri photo_url = Uri.parse(snapshot.getString("Image"));
                        if (photo_url != null) {
                            Glide.with(getActivity()).load(photo_url)
                                    .into(devImageBlurIV);
                            Glide.with(getActivity()).load(photo_url)
                                    .into(devImageIV);
                        }*/
                        URL url = new URL(snapshot.getString("Image"));
                        Toast.makeText(getContext(), snapshot.getString("Image"), Toast.LENGTH_SHORT).show();
                        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                        httpURLConnection.connect();
                        InputStream inputStream = httpURLConnection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        devImageBlurIV.setImageBitmap(bitmap);
                        devImageIV.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}