package com.akshatrajvansh.calnote.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.akshatrajvansh.calnote.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoanRecAdapter extends RecyclerView.Adapter<LoanRecAdapter.ViewHolder> {

    private static final String TAG = "LoanRecAdapter";
    private GoogleSignInAccount googleSignIn;
    private ArrayList<String> DebtName;
    private ArrayList<String> DebtAmount;
    private ArrayList<String> DebtAction;
    private FirebaseFirestore firebaseFirestore;
    private Context mContext;

    public LoanRecAdapter(Context context, ArrayList<String> debtName, ArrayList<String> debtAmount, ArrayList<String> debtAction) {

        DebtName = debtName;
        DebtAmount = debtAmount;
        DebtAction = debtAction;
        mContext = context;
        googleSignIn = GoogleSignIn.getLastSignedInAccount(context);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.debt_cardview, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        holder.debtName.setText(DebtName.get(position));
        holder.debtAmount.setText(DebtAmount.get(position));
        holder.debtAction.setText(DebtAction.get(position));
    }


    @Override
    public int getItemCount() {
        return DebtName.size();
    }


    private void UpdateFirestore(String debtName, String debtAmount, String debtAction) {
        Map<String, Object> debtDetails = new HashMap<>();
        debtDetails.put("Person Name", debtName);
        debtDetails.put("Amount", debtAmount);
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
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView debtName;
        TextView debtAmount;
        Button debtAction;

        public ViewHolder(View itemView) {
            super(itemView);
            debtName = itemView.findViewById(R.id.loan_name);
            debtAction = itemView.findViewById(R.id.loan_button);
            debtAmount = itemView.findViewById(R.id.loan_amount);
        }
    }
}
