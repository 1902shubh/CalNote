package com.akshatrajvansh.calnote.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akshatrajvansh.calnote.Adapters.SubRecAdapter;
import com.akshatrajvansh.calnote.Adapters.SwipeToDeleteCallback;
import com.akshatrajvansh.calnote.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class FragmentAttendance extends Fragment {
    private FirebaseFirestore firebaseFirestore;
    private GoogleSignInAccount googleSignIn;
    private ArrayList<String> SubjectCode = new ArrayList<String>();
    private ArrayList<String> AttendedClasses = new ArrayList<String>();
    private ArrayList<String> BunkedClasses = new ArrayList<String>();
    private ArrayList<String> SubjectName = new ArrayList<String>();
    private Context context;
    private EditText subjectName, subjectCode, subjectAtt, subjectBun;
    private String subName, subCode, subAtt, subBun;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton addSubjects;
    private RecyclerView.Adapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);
        context = getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        googleSignIn = GoogleSignIn.getLastSignedInAccount(context);
        recyclerView = view.findViewById(R.id.recycler_view);
        addSubjects = view.findViewById(R.id.add_subjects);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        addSubjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSubjectsPrompt();
            }
        });
        DataItems();
        return view;
    }
    public void addSubjectsPrompt() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptsView = layoutInflater.inflate(R.layout.subject_adding_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
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
                    clearAttendance();
                    Log.d("Firestore", "Current data: " + queryDocumentSnapshots.getDocuments());
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d("TAG", "onSuccess: LIST EMPTY");

                        } else {

                            SubjectName.add(documentSnapshot.getString("Subject Name"));
                            SubjectCode.add(documentSnapshot.getString("Subject Code"));
                            AttendedClasses.add(documentSnapshot.getString("Attended"));
                            BunkedClasses.add(documentSnapshot.getString("Bunked"));
                            mAdapter = new SubRecAdapter(context, SubjectCode, SubjectName, AttendedClasses, BunkedClasses);
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

    public void clearAttendance() {
        SubjectCode.clear();
        SubjectName.clear();
        AttendedClasses.clear();
        BunkedClasses.clear();
    }

}
