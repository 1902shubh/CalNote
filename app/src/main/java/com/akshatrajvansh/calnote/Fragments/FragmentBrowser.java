package com.akshatrajvansh.calnote.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.akshatrajvansh.calnote.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FragmentBrowser extends Fragment {

    WebView webView;
    FloatingActionButton back;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);
        webView = view.findViewById(R.id.webView);
        back = view.findViewById(R.id.backPressed);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.google.com");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPressed();
            }
        });
        return view;
    }

    public void backPressed(){
        Toast.makeText(getContext(), "Back button pressed", Toast.LENGTH_SHORT).show();
    }
}