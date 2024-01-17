package com.ponerfaltas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListadoAlumnos extends AppCompatActivity {

    private static final String TAG = "ListadoAlumnos";
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_alumnos);

        listView = findViewById(R.id.listview_students);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedStudentId = (String) parent.getItemAtPosition(position);
            String selectedTeacherId = getIntent().getStringExtra("selectedTeacherId");
            String selectedClass = getIntent().getStringExtra("selectedClass");

            Log.d(TAG, "selectedTeacherId: " + selectedTeacherId);
            Log.d(TAG, "selectedClass: " + selectedClass);
            Log.d(TAG, "selectedStudentId: " + selectedStudentId);

            if (selectedStudentId != null && selectedTeacherId != null && selectedClass != null) {
                goToAgregarFaltasActivity(selectedTeacherId, selectedClass, selectedStudentId);
            } else {
                Log.d(TAG, "Selected Student ID, Teacher ID, or Class is null");
            }
        });

        String selectedClass = getIntent().getStringExtra("selectedClass");
        if (selectedClass != null) {
            loadStudents(selectedClass);
        } else {
            Log.d(TAG, "Selected Class is null");
        }
    }

    private void loadStudents(String selectedClass) {
        FirebaseFirestore.getInstance()
                .collection("student")
                .whereEqualTo("curso", selectedClass)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> studentNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String studentName = document.getString("nombre");
                            if (studentName != null) {
                                studentNames.add(studentName);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ListadoAlumnos.this, android.R.layout.simple_list_item_1, studentNames);
                        listView.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void goToAgregarFaltasActivity(String selectedTeacherId, String selectedClass, String selectedStudentId) {
        Intent intent = new Intent(ListadoAlumnos.this, AgregarFaltasActivity.class);
        intent.putExtra("selectedTeacherId", selectedTeacherId);
        intent.putExtra("selectedClass", selectedClass);
        intent.putExtra("selectedStudentId", selectedStudentId);
        intent.putExtra("studentName", selectedStudentId);
        startActivity(intent);
    }

}
