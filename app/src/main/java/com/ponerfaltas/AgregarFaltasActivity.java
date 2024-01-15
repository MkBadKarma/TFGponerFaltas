package com.ponerfaltas;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
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
    private String selectedClass;  // Variable para almacenar la clase seleccionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_faltas);

        selectedClass = getIntent().getStringExtra("selectedClass");  // Obtener la clase seleccionada

        Spinner spinnerSubject = findViewById(R.id.spinnerSubject);
        loadSubjectOptions(selectedClass, spinnerSubject);

        DatePicker datePicker = findViewById(R.id.datePicker);
        Button btnGuardarFaltas = findViewById(R.id.btnGuardarFaltas);

        btnGuardarFaltas.setOnClickListener(view -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1; // Month is 0-based
            int year = datePicker.getYear();

            String formattedDate = String.format("%04d-%02d-%02d", year, month, day);

            int selectedAbsences = getSelectedAbsences();

            if (selectedClass != null) {
                saveAbsences(selectedClass, formattedDate, selectedAbsences);
            } else {
                Toast.makeText(this, "Selected class is null", Toast.LENGTH_SHORT).show();
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
                        List<String> asignaturas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String clasesString = document.getString("clase");
                            if (clasesString != null) {
                                clasesString = clasesString.trim();
                                asignaturas.addAll(Arrays.asList(clasesString.split(", ")));
                            } else {
                                Log.d(TAG, "Clases list is null or empty");
                            }
                        }

                        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, asignaturas);
                        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerSubject.setAdapter(subjectAdapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private int getSelectedAbsences() {
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.radioButton1) {
            return 1;
        } else if (selectedId == R.id.radioButton2) {
            return 2;
        } else {
            return 0;  // Valor predeterminado o manejo de errores según tu lógica
        }
    }

    private void saveAbsences(String selectedClass, String date, int absences) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference absencesRef = db.collection("Faltas").document();

        Map<String, Object> absenceData = new HashMap<>();
        absenceData.put("student_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        absenceData.put("fecha", date);
        absenceData.put("numero_faltas", absences);
        absenceData.put("asignaturas", getSelectedSubjects());

        absencesRef.set(absenceData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Faltas guardadas exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar las faltas: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private List<String> getSelectedSubjects() {
        List<String> selectedSubjects = new ArrayList<>();
        return selectedSubjects;
    }
}