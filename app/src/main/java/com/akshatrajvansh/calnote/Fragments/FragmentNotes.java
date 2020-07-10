package com.akshatrajvansh.calnote.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.akshatrajvansh.calnote.Adapters.NotesAdapter;
import com.akshatrajvansh.calnote.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class FragmentNotes extends Fragment {
    ListView listView;
    static ArrayList<String> titles = new ArrayList<>();
    static ArrayList<String> contents = new ArrayList<>();
    NotesAdapter notesAdapter;
    FloatingActionButton addNewNote;
    EditText newNoteTitle, newNoteContent;
    private FirebaseFirestore firebaseFirestore;
    private GoogleSignInAccount googleSignIn;
    SQLiteDatabase localStorage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        googleSignIn = GoogleSignIn.getLastSignedInAccount(getContext());

        localStorage = getContext().openOrCreateDatabase("notes", MODE_PRIVATE, null);
        localStorage.execSQL("CREATE TABLE IF NOT EXISTS notes (data VARCHAR)");

        listView = view.findViewById(R.id.listView);
        addNewNote = view.findViewById(R.id.addNewNote);
        notesAdapter = new NotesAdapter(getContext(), titles, contents);
        listView.setAdapter(notesAdapter);
        syncNotes();
        addNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewNote();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                editNote(position);
            }
        });
        return view;
    }

    private void storeDataOffline() {
        try {
            Log.d("JSONFormat", "Titles: "+titles);
            Log.d("JSONFormat", "Contents: "+contents);
            JSONObject json = new JSONObject();
            json.put("jsonTitles", new JSONArray(titles));
            json.put("jsonContents", new JSONArray(contents));
            String stringJSON = json.toString();
            Log.d("JSONFormat", stringJSON);
            localStorage.execSQL("DELETE from notes");
            String sql = "INSERT INTO notes (data) VALUES (?)";
            localStorage.execSQL("CREATE TABLE IF NOT EXISTS notes (data VARCHAR)");
            SQLiteStatement statement = localStorage.compileStatement(sql);
            statement.bindString(1, stringJSON);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void retrieveDataOffline() {
        try {
            Cursor c = localStorage.rawQuery("SELECT * FROM notes", null);
            c.moveToFirst();
            String result = c.getString(0);
            c.close();
            Log.d("JSONFormat", "Result: " + result);
            Gson gson = new Gson();
            String str = gson.toJson(result);
            JSONObject jsonObject = new JSONObject(result);
            Log.d("JSONFormat", "jsonObject: " + jsonObject);
            JSONArray jsonTitle = jsonObject.getJSONArray("jsonTitles");
            JSONArray jsonContents = jsonObject.getJSONArray("jsonContents");
            titles.clear();
            contents.clear();
            for(int i=0; i<jsonTitle.length(); i++){
                titles.add(jsonTitle.getString(i));
                contents.add(jsonContents.getString(i));
            }
            Log.d("JSONFormat", "str: " + str);
            Log.d("JSONFormat", "titles: " + titles);

            notesAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteNote(int position) {
        titles.remove(position);
        contents.remove(position);
        notesAdapter.notifyDataSetChanged();
        saveNotes();
    }

    public void editNote(final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptsView = layoutInflater.inflate(R.layout.fragment_notes_addnote, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(promptsView);
        newNoteTitle = (EditText) promptsView.findViewById(R.id.title_note);
        newNoteTitle.setText(titles.get(position));
        newNoteContent = (EditText) promptsView.findViewById(R.id.content_note);
        newNoteContent.setText(contents.get(position));
        alertDialogBuilder.setCancelable(false).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                titles.set(position, newNoteTitle.getText().toString());
                contents.set(position, newNoteContent.getText().toString());
                notesAdapter.notifyDataSetChanged();
            }
        })
                .setNegativeButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                deleteNote(position);
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        saveNotes();
    }

    public void addNewNote() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptsView = layoutInflater.inflate(R.layout.fragment_notes_addnote, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(promptsView);
        newNoteTitle = (EditText) promptsView.findViewById(R.id.title_note);
        newNoteContent = (EditText) promptsView.findViewById(R.id.content_note);
        alertDialogBuilder.setCancelable(false).setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                titles.add(newNoteTitle.getText().toString());
                contents.add(newNoteContent.getText().toString());
                saveNotes();
                notesAdapter.notifyDataSetChanged();
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
        notesAdapter.notifyDataSetChanged();
    }

    public void saveNotes() {
        storeDataOffline();
        Map<String, Object> Notes = new HashMap<>();
        Notes.put("Titles", titles);
        Notes.put("Content", contents);
        firebaseFirestore.collection("Users").document(googleSignIn.getId()).collection("Notes").document(googleSignIn.getId())
                .set(Notes, SetOptions.merge())
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

    private void syncNotes() {
        retrieveDataOffline();
        Log.i("Data Coming", "inside the function");
        DocumentReference documentReference = firebaseFirestore.collection("Users")
                .document(googleSignIn.getId()).collection("Notes").document(googleSignIn.getId());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult() != null) {
                    DocumentSnapshot snapshot = task.getResult();
                    try {
                        titles = (ArrayList<String>) snapshot.getData().get("Titles");
                        contents = (ArrayList<String>) snapshot.getData().get("Content");
                        notesAdapter = new NotesAdapter(getContext(), titles, contents);
                        listView.setAdapter(notesAdapter);
                        storeDataOffline();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}