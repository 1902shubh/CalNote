package com.akshatrajvansh.calnote.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akshatrajvansh.calnote.Adapters.FriendsAdapter;
import com.akshatrajvansh.calnote.R;

import java.util.ArrayList;

public class FragmentFriends extends Fragment {
    public static ArrayList<String> FriendsName = new ArrayList<>();
    public static ArrayList<Integer> FriendsDP = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    public static RecyclerView.Adapter friendsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        FriendsName.clear();
        FriendsDP.clear();
        FriendsName.add("SAM SMITH");
        FriendsDP.add(R.drawable.login);
        FriendsName.add("JOHN LEGEND");
        FriendsDP.add(R.drawable.login);
        FriendsName.add("JOE JONAS");
        FriendsDP.add(R.drawable.login);
        recyclerView = view.findViewById(R.id.friends_rec);
        friendsAdapter = new FriendsAdapter(getContext());
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(friendsAdapter);
        return view;
    }
}