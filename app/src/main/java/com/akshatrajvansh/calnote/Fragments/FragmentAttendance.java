package com.akshatrajvansh.calnote.Fragments;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akshatrajvansh.calnote.Adapters.AttendanceAdapter;
import com.akshatrajvansh.calnote.Adapters.DeleteCallback;
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

public class FragmentAttendance extends Fragment {
    RecyclerView recyclerView;
    FloatingActionButton addNew;
    public static RecyclerView.Adapter attendanceAdapter;
    private FirebaseFirestore firebaseFirestore;
    private GoogleSignInAccount googleSignIn;
    private RecyclerView.LayoutManager layoutManager;
    EditText subjectName, subjectCode, classesAttended, classesBunked;
    public static ArrayList<String> SubjectName = new ArrayList<>();
    public static ArrayList<String> SubjectCode = new ArrayList<>();
    public static ArrayList<String> AttendedClasses = new ArrayList<>();
    public static ArrayList<String> BunkedClasses = new ArrayList<>();
    SQLiteDatabase localStorage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        googleSignIn = GoogleSignIn.getLastSignedInAccount(getContext());
        localStorage = getContext().openOrCreateDatabase("attendance", MODE_PRIVATE, null);
        localStorage.execSQL("CREATE TABLE IF NOT EXISTS attendance (data VARCHAR)");
        recyclerView = view.findViewById(R.id.recycler_view);
        addNew = view.findViewById(R.id.add_subjects);
        attendanceAdapter = new AttendanceAdapter(getContext());
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(attendanceAdapter);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new DeleteCallback((AttendanceAdapter) attendanceAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        syncNotes();
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewSubject();
            }
        });
        return view;
    }

    private void storeDataOffline() {
        try {
            JSONObject json = new JSONObject();
            json.put("subjectName", new JSONArray(SubjectName));
            json.put("subjectCode", new JSONArray(SubjectCode));
            json.put("classesAttended", new JSONArray(AttendedClasses));
            json.put("classesBunked", new JSONArray(BunkedClasses));
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
            Cursor c = localStorage.rawQuery("SELECT * FROM attendance", null);
            c.moveToFirst();
            int dataIndex = c.getColumnIndex("data");
            String result = c.getString(dataIndex);
            c.close();
            Log.d("JSONFormat", "Result: " + result);
            Gson gson = new Gson();
            String str = gson.toJson(result);
            JSONObject jsonObject = new JSONObject(result);
            Log.d("JSONFormat", "jsonObject: " + jsonObject);
            JSONArray jsonSubjectName = jsonObject.getJSONArray("subjectName");
            JSONArray jsonSubjectCode = jsonObject.getJSONArray("subjectCode");
            JSONArray jsonAttendedClasses = jsonObject.getJSONArray("classesAttended");
            JSONArray jsonBunkedClasses = jsonObject.getJSONArray("classesBunked");
            SubjectName.clear();
            SubjectCode.clear();
            AttendedClasses.clear();
            BunkedClasses.clear();
            for (int i = 0; i < jsonSubjectName.length(); i++) {
                SubjectName.add(jsonSubjectName.getString(i));
                SubjectCode.add(jsonSubjectCode.getString(i));
                AttendedClasses.add(jsonAttendedClasses.getString(i));
                BunkedClasses.add(jsonBunkedClasses.getString(i));
            }
            Log.d("JSONFormat", "str: " + str);
            attendanceAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void syncNotes() {
        retrieveDataOffline();
        Log.i("Data Coming", "inside the function");
        DocumentReference documentReference = firebaseFirestore.collection("Users")
                .document(googleSignIn.getId()).collection("Attendance").document(googleSignIn.getId());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult() != null) {
                    DocumentSnapshot snapshot = task.getResult();
                    try {
                        SubjectName = (ArrayList<String>) snapshot.getData().get("Subject Name");
                        SubjectCode = (ArrayList<String>) snapshot.getData().get("Subject Code");
                        AttendedClasses = (ArrayList<String>) snapshot.getData().get("Attended Classes");
                        BunkedClasses = (ArrayList<String>) snapshot.getData().get("Bunked Classes");
                        storeDataOffline();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    attendanceAdapter = new AttendanceAdapter(getContext());
                    layoutManager = new LinearLayoutManager(getContext());
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(attendanceAdapter);
                    ItemTouchHelper itemTouchHelper = new
                            ItemTouchHelper(new DeleteCallback((AttendanceAdapter) attendanceAdapter));
                    itemTouchHelper.attachToRecyclerView(recyclerView);
                }
            }
        });
    }

    public void addNewSubject() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptsView = layoutInflater.inflate(R.layout.subject_adding_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(promptsView);
        subjectName = (EditText) promptsView.findViewById(R.id.subject_name);
        subjectCode = (EditText) promptsView.findViewById(R.id.subject_code);
        classesAttended = (EditText) promptsView.findViewById(R.id.attendedClasses);
        classesBunked = (EditText) promptsView.findViewById(R.id.bunkedClasses);

        alertDialogBuilder.setCancelable(false).setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SubjectName.add(subjectName.getText().toString());
                SubjectCode.add(subjectCode.getText().toString());
                AttendedClasses.add(classesAttended.getText().toString());
                BunkedClasses.add(classesBunked.getText().toString());
                saveNotes();
                attendanceAdapter.notifyDataSetChanged();
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
        attendanceAdapter.notifyDataSetChanged();
    }

    public void saveNotes() {
        storeDataOffline();
        Map<String, Object> Notes = new HashMap<>();
        Notes.put("Subject Name", SubjectName);
        Notes.put("Subject Code", SubjectCode);
        Notes.put("Attended Classes", AttendedClasses);
        Notes.put("Bunked Classes", BunkedClasses);
        // Add a new document with a generated ID
        try {
            firebaseFirestore.collection("Users").document(googleSignIn.getId()).collection("Attendance").document(googleSignIn.getId())
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        //storeDataOffline();
    }

}