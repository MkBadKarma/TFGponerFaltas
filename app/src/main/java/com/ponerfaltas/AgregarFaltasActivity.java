package com.ponerfaltas;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgregarFaltasActivity extends AppCompatActivity {
    private static final String TAG = "AgregarFaltasActivity";
    private String selectedClass;
    private String studentName;
    private String teacherName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_faltas);

        selectedClass = getIntent().getStringExtra("selectedClass");

        Spinner spinnerSubject = findViewById(R.id.spinnerSubject);
        loadSubjectOptions(selectedClass, spinnerSubject);

        DatePicker datePicker = findViewById(R.id.datePicker);
        Button btnGuardarFaltas = findViewById(R.id.btnGuardarFaltas);

        btnGuardarFaltas.setOnClickListener(view -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();

            String formattedDate = String.format("%04d-%02d-%02d", year, month, day);

            if (selectedClass != null) {
                saveAbsences(selectedClass, formattedDate);
            } else {
                showToast("Selected class is null");
            }
        });
    }

    private void loadSubjectOptions(String selectedClass, Spinner spinnerSubject) {
        FirebaseFirestore.getInstance()
                .collection("student")
                .whereEqualTo("curso", selectedClass)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> subjects = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            studentName = document.getString("nombre");
                            String classesString = document.getString("clase");
                            if (classesString != null) {
                                classesString = classesString.trim();
                                subjects.addAll(Arrays.asList(classesString.split(", ")));
                            } else {
                                Log.d(TAG, "Classes list is null or empty");
                            }
                        }

                        setupSubjectSpinner(spinnerSubject, subjects);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void setupSubjectSpinner(Spinner spinnerSubject, List<String> subjects) {
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);
    }

    private void saveAbsences(String selectedClass, String formattedDate) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            if (userEmail != null) {
                FirebaseFirestore.getInstance()
                        .collection("teacher")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    teacherName = document.getString("nombre");
                                }
                                saveAbsencesToFirestore(formattedDate);
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        });
            }
        }
    }

    private void saveAbsencesToFirestore(String formattedDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference absencesRef = db.collection("Faltas").document();

        Map<String, Object> absenceData = new HashMap<>();
        absenceData.put("student_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        absenceData.put("nombre_alumno", studentName);
        absenceData.put("nombre_maestro", teacherName);
        absenceData.put("fecha", formattedDate);
        absenceData.put("numero_faltas", getSelectedAbsences());
        absenceData.put("asignatura", getSelectedSubjects());

        absencesRef.set(absenceData)
                .addOnSuccessListener(aVoid -> {
                    showToast("Faltas guardadas exitosamente");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error al guardar las faltas: " + e.getMessage()));
    }

    private String getSelectedSubjects() {
        Spinner spinnerSubject = findViewById(R.id.spinnerSubject);
        return (spinnerSubject != null && spinnerSubject.getSelectedItem() != null) ?
                spinnerSubject.getSelectedItem().toString() : "";
    }

    private String getSelectedAbsences() {
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.radioButton1) {
            return "1";
        } else if (selectedId == R.id.radioButton2) {
            return "2";
        } else {
            return "0";
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
