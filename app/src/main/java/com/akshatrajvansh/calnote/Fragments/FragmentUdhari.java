package com.akshatrajvansh.calnote.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akshatrajvansh.calnote.Adapters.LoanRecAdapter;
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

public class FragmentUdhari extends Fragment {
    private FirebaseFirestore firebaseFirestore;
    private GoogleSignInAccount googleSignIn;
    private Context context;
    private ArrayList<String> DebtNames = new ArrayList<String>();
    private ArrayList<String> DebtAmounts = new ArrayList<String>();
    private ArrayList<String> DebtAction = new ArrayList<String>();
    private EditText loanName, loanAmount;
    private RadioButton pay, get;
    private FloatingActionButton addDebt;
    private String debtName, debtAmount, debtAction;
    RecyclerView loanRecView;
    private RecyclerView.LayoutManager layoutMan;
    private RecyclerView.Adapter loanAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_udhari, container, false);
        context = getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        googleSignIn = GoogleSignIn.getLastSignedInAccount(context);
        addDebt = view.findViewById(R.id.add_new_debt);
        loanRecView = view.findViewById(R.id.recycler_view_udhari);
        loanRecView.setHasFixedSize(true);
        layoutMan = new LinearLayoutManager(context);
        loanRecView.setLayoutManager(layoutMan);
        addDebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDebtPrompt();
            }
        });
        debtKeeper();
        return view;
    }
    public void addDebtPrompt() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptsView = layoutInflater.inflate(R.layout.debt_adding_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);
        loanName = (EditText) promptsView.findViewById(R.id.person_name);
        loanAmount = (EditText) promptsView.findViewById(R.id.amount_of_money);
        pay = (RadioButton) promptsView.findViewById(R.id.radio_to_pay);
        get = (RadioButton) promptsView.findViewById(R.id.radio_to_get);
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                debtName = loanName.getText().toString();
                debtAmount = loanAmount.getText().toString();
                if (pay.isChecked()) {
                    get.setChecked(false);
                    debtAction = "pay";
                } else if (get.isChecked()) {
                    pay.setChecked(false);
                    debtAction = "get";
                }
                addNewLoan(debtName, debtAmount, debtAction);
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

    private void addNewLoan(String debtName, String debtAmount, String debtAction) {
        Map<String, Object> debtDetails = new HashMap<>();
        debtDetails.put("Person Name", debtName);
        debtDetails.put("Amount", "Rs. " + debtAmount);
        debtDetails.put("Action", debtAction);
        // Add a new document with a generated ID
        firebaseFirestore.collection("Users").document(googleSignIn.getId()).collection("Udhariyaan").document(debtName)
                .set(debtDetails)
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
        debtKeeper();
    }
    private void clearDebt() {
        DebtAction.clear();
        DebtAmounts.clear();
        DebtNames.clear();
    }
    private void debtKeeper() {
        CollectionReference collectionReference = firebaseFirestore.collection("Users")
                .document(googleSignIn.getId()).collection("Udhariyaan");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    clearDebt();
                    Log.d("Firestore", "Current data: " + queryDocumentSnapshots.getDocuments());
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d("TAG", "onSuccess: LIST EMPTY");

                        } else {
                            Log.d("Debt", documentSnapshot.toString());
                            DebtNames.add(documentSnapshot.getString("Person Name"));
                            DebtAmounts.add(documentSnapshot.getString("Amount"));
                            DebtAction.add(documentSnapshot.getString("Action"));
                            loanAdapter = new LoanRecAdapter(context, DebtNames, DebtAmounts, DebtAction);
                            loanRecView.setAdapter(loanAdapter);
                        }
                    }
                } else {
                    Log.d("Firestore", "Current data: null");
                }
            }
        });
    }
}
