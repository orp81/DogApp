package com.example.dogapp.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dogapp.R;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;

public class SecondRegisterFragment extends Fragment {

    private TextInputLayout dateEt, locationEt;
    private RadioGroup genderGroup, typeGroup;

    private boolean isValid;

    //to create user
    private String fullName, email, password, gender = "", location, type = "", dateOfBirth;

    public interface OnSecondRegisterFragmentListener {
        void onRegister(String name, String email, String password, String date, String gender, String title, String location);
        void onBackSecond();
    }

    private OnSecondRegisterFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //in case the Activity doesn't implements the interface
        try {
            listener = (OnSecondRegisterFragmentListener) context; //the activity is the callback
        } catch (ClassCastException ex) {
            throw new ClassCastException("The Activity must implement OnSecondRegisterFragmentListener interface");
        }
    }

    public static SecondRegisterFragment newInstance(String fullName, String email, String password) {
        SecondRegisterFragment fragment = new SecondRegisterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("fullName", fullName);
        bundle.putString("email", email);
        bundle.putString("password", password);
        fragment.setArguments(bundle);
        return fragment; //holds the bundle
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.register_fragment_layout_2, container, false);

        //get user data
        fullName = getArguments().getString("fullName");
        email = getArguments().getString("email");
        password = getArguments().getString("password");

        ImageButton profileBtn = rootView.findViewById(R.id.profile_btn);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View dialogView;
                final AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                dialogView = getLayoutInflater().inflate(R.layout.picture_dialog_layout, null);
                builder1.setTitle("Choose where").setView(dialogView).show();
            }
        });

        dateEt = rootView.findViewById(R.id.date_input);
        dateEt.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                final int globalYear = calendar.get(Calendar.YEAR);
                final int globalMonth = calendar.get(Calendar.MONTH);
                final int globalDay = calendar.get(Calendar.DAY_OF_MONTH);

                if (Build.VERSION.SDK_INT >= 26) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            //LocalDate myDate = LocalDate.of(year,month,dayOfMonth);
                            //age = calculateAge(myDate,LocalDate.of(globalYear,globalMonth,globalDay));
                            dateOfBirth = dayOfMonth+"/"+(month+1)+"/"+year;
                            dateEt.getEditText().setText(dateOfBirth);
                        }
                    }, globalYear, globalMonth, globalDay);
                    datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    datePickerDialog.show();
                }
            }
        });

        locationEt = rootView.findViewById(R.id.location_input);
        locationEt.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //calculate location
                location = "Holon, Israel";
                locationEt.getEditText().setText(location);
            }
        });

        genderGroup = rootView.findViewById(R.id.gender_radio_group);
        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.male:
                        gender = "Male";
                        break;
                    case R.id.female:
                        gender = "Female";
                        break;
                    case R.id.other:
                        gender = "Other";
                        break;
                }
            }
        });

        typeGroup = rootView.findViewById(R.id.type_radio_group);
        typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.dog_owner:
                        type = getString(R.string.dog_owner);
                        break;
                    case R.id.dog_walker:
                        type = getString(R.string.dog_walker);
                        break;
                }
            }
        });

        Button regBtn = rootView.findViewById(R.id.reg_2_btn);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isValid = validateFields();
                if(isValid) {
                    listener.onRegister(fullName, email, password, dateOfBirth, gender, type, location);
                }
                else {
                    return;
                }
            }
        });

        ImageButton backBtn = rootView.findViewById(R.id.back_frag_btn_2);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBackSecond();
            }
        });

        return rootView;
    }

    private boolean validateFields() {
        if(!validLocation() | !validDate() | !validGroups()) {
            return false;
        }
        return true;
    }

    private boolean validGroups() {
        if(type.isEmpty() || gender.isEmpty()) {
            Toast.makeText(getActivity(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validLocation() {
        if(locationEt.getEditText().getText().toString().isEmpty()) {
            locationEt.setError(getString(R.string.field_empty_error));
            return false;
        }
        else {
            locationEt.setError(null);
            return true;
        }
    }

    private boolean validDate() {
        if(dateEt.getEditText().getText().toString().isEmpty()) {
            dateEt.setError(getString(R.string.field_empty_error));
            return false;
        }
        else {
            dateEt.setError(null);
            return true;
        }
    }

    public static int calculateAge(LocalDate birthDate, LocalDate currentDate) {
        if (Build.VERSION.SDK_INT >= 26) {
            if ((birthDate != null) && (currentDate != null)) {
                return Period.between(birthDate, currentDate).getYears();
            } else {
                return 0;
            }
        }
        return 0;
    }

}
