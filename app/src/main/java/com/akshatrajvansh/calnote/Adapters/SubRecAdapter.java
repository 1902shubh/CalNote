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

public class SubRecAdapter extends RecyclerView.Adapter<SubRecAdapter.ViewHolder> {

    private static final String TAG = "SubRecAdapter";
    private GoogleSignInAccount googleSignIn;
    private ArrayList<String> SubjectCode;
    private ArrayList<String> SubjectName;
    private ArrayList<String> AttendedClasses;
    private ArrayList<String> BunkedClasses;
    FirebaseFirestore firebaseFirestore;
    private Context mContext;

    public SubRecAdapter(Context context, ArrayList<String> subjectCode, ArrayList<String> subjectName, ArrayList<String> attendedClasses, ArrayList<String> bunkedClasses) {
        SubjectCode = subjectCode;
        SubjectName = subjectName;
        AttendedClasses = attendedClasses;
        BunkedClasses = bunkedClasses;
        mContext = context;
        googleSignIn = GoogleSignIn.getLastSignedInAccount(context);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subjects_cardview, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        //if (SubjectCode.get(position) != null && !SubjectCode.get(position).contains("CODE")) {
            holder.subjectCode.setText(SubjectCode.get(position));
            holder.attendance.setText(getAttendance(AttendedClasses.get(position), BunkedClasses.get(position)));
            holder.warning.setText(getWarning(AttendedClasses.get(position), BunkedClasses.get(position)));

            holder.cardViewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: clicked on: " + SubjectCode.get(position));
                    Toast.makeText(mContext, SubjectCode.get(position), Toast.LENGTH_SHORT).show();
                }
            });

            holder.attended.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UpdateFirestore(SubjectCode.get(position), SubjectName.get(position), String.valueOf(Integer.valueOf(AttendedClasses.get(position)) + 1), BunkedClasses.get(position));
                }
            });

            holder.bunked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UpdateFirestore(SubjectCode.get(position), SubjectName.get(position), AttendedClasses.get(position), String.valueOf(Integer.valueOf(BunkedClasses.get(position)) + 1));
                }
            });

            holder.subjectCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, SubjectName.get(position), Toast.LENGTH_SHORT).show();
                }
            });

            holder.attendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, getPercent(AttendedClasses.get(position), BunkedClasses.get(position)), Toast.LENGTH_SHORT).show();
                }
            });
       // }
    }

    public String getAttendance(String attended, String bunked) {
        return Integer.valueOf(attended) + "/" + (Integer.valueOf(attended) + Integer.valueOf(bunked));
    }

    public String getWarning(String attended, String bunked) {
        int att = Integer.valueOf(attended);
        int bun = Integer.valueOf(bunked);
        int total = att + bun;
        int percent = (att * 100) / total;
        if (percent >= 80)
            return "Good Relief";
        else if (percent < 80 && percent >= 75)
            return "Watch Out!";
        else if (percent < 75)
            return "Good Grief";
        return "Yet to attend?!";
    }

    public String getPercent(String attended, String bunked) {
        int att = Integer.valueOf(attended);
        int bun = Integer.valueOf(bunked);
        int total = att + bun;
        int percent = (att * 100) / total;
        return percent + "%";
    }

    @Override
    public int getItemCount() {
        return SubjectCode.size();
    }

    public void deleteItem(int position) {
        Toast.makeText(mContext, "Deleted "+ SubjectCode.get(position), Toast.LENGTH_SHORT).show();
    }
    public void UpdateFirestore(String subjectCode, String subjectName, String attended, String bunked) {
        Map<String, Object> user = new HashMap<>();
        user.put("Subject Name", subjectName);
        user.put("Subject Code", subjectCode);
        user.put("Attended", attended);
        user.put("Bunked", bunked);
        // Add a new document with a generated ID
        Log.d("DEBX", googleSignIn.getId());
        firebaseFirestore.collection("Users").document(googleSignIn.getId()).collection("Attendance").document(subjectCode)
                .set(user)
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

        TextView subjectCode;
        TextView warning;
        TextView attendance;
        Button attended, bunked;
        CardView cardViewLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            cardViewLayout = itemView.findViewById(R.id.subject_cardview);
            subjectCode = itemView.findViewById(R.id.subcode);
            attended = itemView.findViewById(R.id.attended);
            bunked = itemView.findViewById(R.id.bunked);
            warning = itemView.findViewById(R.id.warnings);
            attendance = itemView.findViewById(R.id.class_attended);
        }
    }
}
