package com.akshatrajvansh.calnote.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import java.util.HashMap;
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
    int matchParent;
    private EditText titleET, contentET;
    private Button saveBT, deleteBT, cancelBT;
    private LinearLayout linearLayoutET;
    ConstraintLayout constraintLayout;

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
        constraintLayout = view.findViewById(R.id.constraintLayoutNotes);
        listView = view.findViewById(R.id.listView);
        matchParent = constraintLayout.getLayoutParams().height;
        addNewNote = view.findViewById(R.id.addNewNote);
        linkEditLayout(view);
        notesAdapter = new NotesAdapter(getContext(), titles, contents);
        listView.setAdapter(notesAdapter);
        syncNotes();
        defaultView();
        addNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewNote();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                openNote(view, position);
            }
        });
        saveBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titles.set(Integer.parseInt(titleET.getTag().toString()), titleET.getText().toString());
                contents.set(Integer.parseInt(titleET.getTag().toString()), contentET.getText().toString());
                notesAdapter.notifyDataSetChanged();
                saveNotes();
                hideKeyboard();
                defaultView();
            }
        });
        deleteBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote(Integer.parseInt(titleET.getTag().toString()));
                notesAdapter.notifyDataSetChanged();
                saveNotes();
                hideKeyboard();
                defaultView();
            }
        });
        cancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                defaultView();
            }
        });
        return view;
    }

    public void hideKeyboard() {
        Activity activity = getActivity();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showKeyboard() {
        Activity activity = getActivity();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @SuppressLint("RestrictedApi")
    private void defaultView() {
        linearLayoutET.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        addNewNote.setVisibility(View.VISIBLE);
    }

    private void linkEditLayout(View view) {
        linearLayoutET = view.findViewById(R.id.editLayout);
        titleET = view.findViewById(R.id.topicEdit);
        contentET = view.findViewById(R.id.contentEdit);
        saveBT = view.findViewById(R.id.saveBtn);
        deleteBT = view.findViewById(R.id.deleteBtn);
        cancelBT = view.findViewById(R.id.cancelBtn);
    }

    @SuppressLint("RestrictedApi")
    private void openNote(View view, int position) {
        linearLayoutET.setVisibility(View.VISIBLE);
        addNewNote.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        titleET.setText(titles.get(position));
        contentET.setText(contents.get(position));
        titleET.setTag(position);
    }

    private void storeDataOffline() {
        try {
            Log.d("JSONFormat", "Titles: " + titles);
            Log.d("JSONFormat", "Contents: " + contents);
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
            for (int i = 0; i < jsonTitle.length(); i++) {
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