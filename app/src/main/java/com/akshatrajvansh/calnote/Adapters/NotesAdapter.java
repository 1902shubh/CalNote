package com.akshatrajvansh.calnote.Adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.akshatrajvansh.calnote.R;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class NotesAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> title;
    ArrayList<String> content;
    LayoutInflater inflter;

    public NotesAdapter(Context applicationContext, ArrayList<String> title, ArrayList<String> content) {
        this.context = context;
        this.title = title;
        this.content = content;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return title.size();
    }

    @Override
    public Object getItem(int i) {
        return "NOTE";
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.fragment_notes_single, null);
        TextView Title = (TextView) view.findViewById(R.id.topic);
        TextView Content = (TextView) view.findViewById(R.id.content);
        if (title.size() > 0 && content.size() > 0) {
            Title.setText(title.get(i));
            Content.setText(content.get(i));
        }
        return view;
    }
}
