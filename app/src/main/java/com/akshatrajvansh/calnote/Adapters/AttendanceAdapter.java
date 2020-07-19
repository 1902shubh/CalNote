package com.akshatrajvansh.calnote.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.akshatrajvansh.calnote.Fragments.FragmentAttendance;
import com.akshatrajvansh.calnote.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private static final String TAG = "AttendanceAdapter";
    private GoogleSignInAccount googleSignIn;
    private FirebaseFirestore firebaseFirestore;
    private Context mContext;

    public AttendanceAdapter(Context context) {
        mContext = context;
        googleSignIn = GoogleSignIn.getLastSignedInAccount(context);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_attendance_single, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        try {
            holder.subjectCode.setText(FragmentAttendance.SubjectCode.get(position));
            holder.subjectName.setText(FragmentAttendance.SubjectName.get(position));
            holder.attendedClasses.setText("Attended: " + FragmentAttendance.AttendedClasses.get(position));
            holder.bunkedClasses.setText("Bunked: " + FragmentAttendance.BunkedClasses.get(position));
            String percent = getPercent(FragmentAttendance.AttendedClasses.get(position), FragmentAttendance.BunkedClasses.get(position)) + "%";
            holder.totalClasses.setText(getTotal(FragmentAttendance.AttendedClasses.get(position), FragmentAttendance.BunkedClasses.get(position)));
            holder.percentAttendance.setText(percent);
            holder.progressBar.setProgress(Integer.parseInt(getPercent(FragmentAttendance.AttendedClasses.get(position), FragmentAttendance.BunkedClasses.get(position))));


            holder.cardViewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: clicked on: " + FragmentAttendance.SubjectCode.get(position));
                    if (holder.moreDetails.getVisibility() == View.VISIBLE)
                        holder.moreDetails.setVisibility(View.GONE);
                    else
                        holder.moreDetails.setVisibility(View.VISIBLE);
                }
            });
            holder.AttendedClasses.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    FragmentAttendance.AttendedClasses.set(position, String.valueOf(Integer.parseInt(FragmentAttendance.AttendedClasses.get(position)) + 1));
                    UpdateCloud();
                }
            });

            holder.BunkedClasses.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentAttendance.BunkedClasses.set(position, String.valueOf(Integer.parseInt(FragmentAttendance.BunkedClasses.get(position)) + 1));
                    UpdateCloud();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPercent(String AttendedClasses, String BunkedClasses) {
        int att = Integer.parseInt(AttendedClasses);
        int bun = Integer.parseInt(BunkedClasses);
        int total = att + bun;
        int percent = (att * 100) / total;
        return String.valueOf(percent);
    }

    public String getTotal(String AttendedClasses, String BunkedClasses) {
        int att = Integer.parseInt(AttendedClasses);
        int bun = Integer.parseInt(BunkedClasses);
        int total = att + bun;
        return "Total: "+ total;
    }


    @Override
    public int getItemCount() {
        return FragmentAttendance.SubjectCode.size();
    }

    void deleteItem(int position) {
        Toast.makeText(mContext, "Deleting " + FragmentAttendance.SubjectCode.get(position), Toast.LENGTH_SHORT).show();
        FragmentAttendance.SubjectCode.remove(position);
        FragmentAttendance.AttendedClasses.remove(position);
        FragmentAttendance.BunkedClasses.remove(position);
        FragmentAttendance.SubjectName.remove(position);
        FragmentAttendance.attendanceAdapter.notifyDataSetChanged();
        UpdateCloud();
    }

    private void UpdateCloud() {
        FragmentAttendance.attendanceAdapter.notifyDataSetChanged();
        Map<String, Object> user = new HashMap<>();
        user.put("Subject Name", FragmentAttendance.SubjectName);
        user.put("Subject Code", FragmentAttendance.SubjectCode);
        user.put("Attended Classes", FragmentAttendance.AttendedClasses);
        user.put("Bunked Classes", FragmentAttendance.BunkedClasses);
        // Add a new document with a generated ID
        Log.d("DEBX", googleSignIn.getId());
        firebaseFirestore.collection("Users").document(googleSignIn.getId()).collection("Attendance").document(googleSignIn.getId())
                .set(user, SetOptions.merge())
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

        TextView subjectCode, subjectName, attendedClasses, bunkedClasses, totalClasses, percentAttendance;
        Button AttendedClasses, BunkedClasses;
        CardView cardViewLayout;
        ConstraintLayout moreDetails;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            cardViewLayout = itemView.findViewById(R.id.subject_cardview);
            subjectCode = itemView.findViewById(R.id.subcode);
            subjectName = itemView.findViewById(R.id.subname);
            AttendedClasses = itemView.findViewById(R.id.attended);
            BunkedClasses = itemView.findViewById(R.id.bunked);
            totalClasses = itemView.findViewById(R.id.totalClasses);
            percentAttendance = itemView.findViewById(R.id.att_percent);
            attendedClasses = itemView.findViewById(R.id.attendedClasses);
            bunkedClasses = itemView.findViewById(R.id.bunkedClasses);
            moreDetails = itemView.findViewById(R.id.att_more_layout);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
