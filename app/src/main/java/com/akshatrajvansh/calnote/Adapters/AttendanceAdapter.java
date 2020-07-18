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
import com.akshatrajvansh.calnote.Fragments.FragmentAttendance;
import com.akshatrajvansh.calnote.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.ThrowOnExtraProperties;

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

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        //if (SubjectCode.get(position) != null && !SubjectCode.get(position).contains("CODE")) {
        try {
            holder.subjectCode.setText(FragmentAttendance.SubjectCode.get(position));
            holder.attendance.setText(getAttendance(FragmentAttendance.AttendedClasses.get(position), FragmentAttendance.BunkedClasses.get(position)));
            holder.percent.setText(getPercent(FragmentAttendance.AttendedClasses.get(position), FragmentAttendance.BunkedClasses.get(position)));

            holder.cardViewLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, "Coming Soon", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            holder.cardViewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: clicked on: " + FragmentAttendance.SubjectCode.get(position));
                    Toast.makeText(mContext, FragmentAttendance.SubjectCode.get(position), Toast.LENGTH_SHORT).show();
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

            holder.subjectCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, FragmentAttendance.SubjectName.get(position), Toast.LENGTH_SHORT).show();
                }
            });

            holder.attendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, getPercent(FragmentAttendance.AttendedClasses.get(position), FragmentAttendance.BunkedClasses.get(position)), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAttendance(String AttendedClasses, String BunkedClasses) {
        return Integer.valueOf(AttendedClasses) + "/" + (Integer.parseInt(AttendedClasses) + Integer.parseInt(BunkedClasses));
    }

    public String getWarning(String AttendedClasses, String BunkedClasses) {
        int att = Integer.parseInt(AttendedClasses);
        int bun = Integer.parseInt(BunkedClasses);
        int total = att + bun;
        int percent = (att * 100) / total;
        if (percent >= 80)
            return "Good Relief";
        else if (percent >= 75)
            return "Watch Out!";
        else return "Good Grief";
    }

    public String getPercent(String AttendedClasses, String BunkedClasses) {
        int att = Integer.parseInt(AttendedClasses);
        int bun = Integer.parseInt(BunkedClasses);
        int total = att + bun;
        int percent = (att * 100) / total;
        return percent + "%";
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

        TextView subjectCode;
        TextView percent;
        TextView attendance;
        Button AttendedClasses, BunkedClasses;
        CardView cardViewLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            cardViewLayout = itemView.findViewById(R.id.subject_cardview);
            subjectCode = itemView.findViewById(R.id.subcode);
            AttendedClasses = itemView.findViewById(R.id.attended);
            BunkedClasses = itemView.findViewById(R.id.bunked);
            percent = itemView.findViewById(R.id.percentage);
            attendance = itemView.findViewById(R.id.class_attended);
        }
    }
}
