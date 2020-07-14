package com.akshatrajvansh.calnote.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.akshatrajvansh.calnote.Fragments.FragmentAttendance;
import com.akshatrajvansh.calnote.Fragments.FragmentFriends;
import com.akshatrajvansh.calnote.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private static final String TAG = "FriendsAdapter";
    private GoogleSignInAccount googleSignIn;
    private FirebaseFirestore firebaseFirestore;
    private Context mContext;

    public FriendsAdapter(Context context) {
        mContext = context;
        googleSignIn = GoogleSignIn.getLastSignedInAccount(context);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_cardview, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        holder.friendsDP.setImageResource(FragmentFriends.FriendsDP.get(position));
        holder.friendsName.setText(FragmentFriends.FriendsName.get(position));
    }



    @Override
    public int getItemCount() {
        return FragmentFriends.FriendsName.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView friendsName;
        ImageView friendsDP;

        public ViewHolder(View itemView) {
            super(itemView);
           friendsName = itemView.findViewById(R.id.friends_name);
           friendsDP = itemView.findViewById(R.id.friends_dp);
        }
    }
}
