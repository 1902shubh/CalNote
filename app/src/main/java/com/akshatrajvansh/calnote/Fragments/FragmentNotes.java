package com.akshatrajvansh.calnote.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import com.akshatrajvansh.calnote.Adapters.LoanRecAdapter;
import com.akshatrajvansh.calnote.Adapters.NotesAdapter;
import com.akshatrajvansh.calnote.R;
import com.facebook.share.Share;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.CheckedOutputStream;

import javax.annotation.Nullable;

public class FragmentNotes extends Fragment {
    ListView listView;
    static ArrayList<String> titles = new ArrayList<>();
    static ArrayList<String> contents = new ArrayList<>();
    NotesAdapter notesAdapter;
    FloatingActionButton addNewNote;
    EditText newNoteTitle, newNoteContent;
    private FirebaseFirestore firebaseFirestore;
    private GoogleSignInAccount googleSignIn;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sharedPreferences = context.getSharedPreferences("com.akshatrajvansh.calnote.Notes", Context.MODE_PRIVATE);
        firebaseFirestore = FirebaseFirestore.getInstance();
        googleSignIn = GoogleSignIn.getLastSignedInAccount(context);
        storeDataOffline();
        getPreviousNotes();
    }

    private void storeDataOffline() {
        HashSet<String> TitleSet = new HashSet<>(FragmentNotes.titles);
        HashSet<String> ContentSet = new HashSet<>(FragmentNotes.contents);
        sharedPreferences.edit().putStringSet("Title", TitleSet).apply();
        sharedPreferences.edit().putStringSet("Content", ContentSet).apply();
        Log.d("sharedNotes", TitleSet.toString());
    }

    private void getPreviousNotes() {
        HashSet<String> TitleSet = (HashSet<String>) sharedPreferences.getStringSet("Title", null);
        HashSet<String> ContentSet = (HashSet<String>) sharedPreferences.getStringSet("Content", null);
        if (TitleSet == null || ContentSet == null) {
            titles.add("Example Title");
            contents.add("Example Context");
        } else {
            titles = new ArrayList(TitleSet);
            contents = new ArrayList(ContentSet);
        }
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
        storeDataOffline();
        notesAdapter.notifyDataSetChanged();
    }

    public void saveNotes() {
        Map<String, Object> Notes = new HashMap<>();
        Notes.put("Titles", titles);
        Notes.put("Content", contents);
        // Add a new document with a generated ID
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
        Log.i("Data Coming", "inside the function");
        DocumentReference documentReference = firebaseFirestore.collection("Users")
                .document(googleSignIn.getId()).collection("Notes").document(googleSignIn.getId());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult() != null) {
                    DocumentSnapshot snapshot = task.getResult();
                    Log.d("FireStore", snapshot.getData().get("Titles").toString());
                    titles = (ArrayList<String>) snapshot.getData().get("Titles");
                    contents = (ArrayList<String>) snapshot.getData().get("Content");
                    notesAdapter = new NotesAdapter(getContext(), titles, contents);
                    listView.setAdapter(notesAdapter);
                }
            }
        });
    }

}