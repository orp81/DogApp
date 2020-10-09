package com.example.dogapp.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.dogapp.Adapters.AutoCompleteAdapter;
import com.example.dogapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

import de.hdodenhof.circleimageview.CircleImageView;

public class SecondRegisterFragment extends Fragment {

    // Activity requests
    final int CAMERA_REQUEST = 1;
    final int WRITE_PERMISSION_REQUEST = 2;
    final int SELECT_IMAGE = 3;
    final int LOCATION_PERMISSION_REQUEST = 4;
    boolean isFromCamera;
    boolean permission = true;

    Uri fileUri;
    AlertDialog alertDialog;
    Bitmap bitmap1, bitmap2;

    //views
    ImageButton locationBtn1;
    TextView pressTv, notReqTv;
    CircleImageView profileBtn;

    //Location
    FusedLocationProviderClient client;
    //TextView locationTv;
    Geocoder geocoder;
    Handler handler = new Handler();
    Timer timer;
    private int counter;

    // Firebase storage
    StorageReference myStorageRef;

    //private ProgressBar progressBar;

    private TextInputLayout dateEt;
    private TextInputLayout locationInputLayout;
    private RadioGroup genderGroup;
    private LottieAnimationView aboutAnim;
    private Button next2Btn, regBtn, locationBtn;
    private AutoCompleteTextView locationEt;

    private boolean isWalker;
    private boolean isValid;
    private boolean isLocation;

    //to create user
    private String fullName, email, password, location, dateOfBirth;
    private Boolean type;
    private Integer age, gender;

    public interface OnSecondRegisterFragmentListener {

        void startLoader();

        void stopLoader();

        void createConfirmDialog();

        void onRegister(Uri fileUri,String name, String email, String password, String date, Integer age, Integer gender, Boolean type, String location);

        void onBackSecond();

        void onNextSecond(String name, String email, String password, String date, Integer age, Integer gender, Boolean type, String location);
    }

    private OnSecondRegisterFragmentListener listener;

    //location or images taken permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), R.string.no_permissions, Toast.LENGTH_SHORT).show();
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                permission = false;
            } else {
                Toast.makeText(getActivity(), R.string.permissions_granted, Toast.LENGTH_SHORT).show();
                permission = true;
                openGallery();
            }
        }

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), R.string.no_permissions, Toast.LENGTH_SHORT).show();
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            } else {
                if (isLocation) {
                    startLocation();
                }
                Toast.makeText(getActivity(), R.string.permissions_granted, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                bitmap2 = null;
                try {
                    bitmap1 = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), fileUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profileBtn.setImageBitmap(bitmap1);
                pressTv.setVisibility(View.GONE);
                isFromCamera = true;
                //alertDialog.dismiss();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                fileUri = null;
            }

        }

        if (requestCode == SELECT_IMAGE && permission == true) {
            if (resultCode == Activity.RESULT_OK) {
                fileUri = data.getData();
                bitmap1 = null;
                try {
                    bitmap2 = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), fileUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profileBtn.setImageBitmap(bitmap2);
                pressTv.setVisibility(View.GONE);
                isFromCamera = false;
                if (isFromCamera)
                    getActivity().getContentResolver().delete(fileUri, null, null);
                //alertDialog.dismiss();
            }
        }


    }

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

    public static SecondRegisterFragment newInstance(String fullName, String email, String password, boolean isWalker) {
        SecondRegisterFragment fragment = new SecondRegisterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("fullName", fullName);
        bundle.putString("email", email);
        bundle.putString("password", password);
        bundle.putBoolean("isWalker", isWalker);
        fragment.setArguments(bundle);
        return fragment; //holds the bundle
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.register_fragment_layout_2, container, false);

        //normal user
        regBtn = rootView.findViewById(R.id.reg_2_btn);
        pressTv = rootView.findViewById(R.id.press_tv);
        notReqTv = rootView.findViewById(R.id.not_req_tv);
        profileBtn = rootView.findViewById(R.id.profile_btn);
        locationBtn = rootView.findViewById(R.id.location_btn);
        locationInputLayout = rootView.findViewById(R.id.location_input_layout);

        //walker user
        next2Btn = rootView.findViewById(R.id.next_2_btn);
        aboutAnim = rootView.findViewById(R.id.reg_normal_anim);

        //user type
        isWalker = getArguments().getBoolean("isWalker");
        type = isWalker; //true for walker, false for user

        if (isWalker) { //walker registration options

            regBtn.setVisibility(View.GONE);
            pressTv.setVisibility(View.GONE);
            notReqTv.setVisibility(View.GONE);
            profileBtn.setVisibility(View.GONE);
            aboutAnim.setVisibility(View.VISIBLE);
            next2Btn.setVisibility(View.VISIBLE);

            setWalkerEventsListeners();

        } else { //normal user registration options

            next2Btn.setVisibility(View.GONE);
            aboutAnim.setVisibility(View.GONE);
            regBtn.setVisibility(View.VISIBLE);
            pressTv.setVisibility(View.VISIBLE);
            notReqTv.setVisibility(View.VISIBLE);
            profileBtn.setVisibility(View.VISIBLE);
            profileBtn.animate().scaleX(1.3f).scaleY(1.3f).setDuration(500).withEndAction(new Runnable() {
                @Override
                public void run() {
                    profileBtn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(500).start();
                }
            }).start();

            setNormalEventsListeners();
        }

        //**********COMMON EVENTS**********************//

        // storage instance
        myStorageRef = FirebaseStorage.getInstance().getReference("Images");

        //get user data
        fullName = getArguments().getString("fullName");
        email = getArguments().getString("email");
        password = getArguments().getString("password");

        dateEt = rootView.findViewById(R.id.date_input);
        dateEt.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                final int globalYear = calendar.get(Calendar.YEAR);
                final int globalMonth = calendar.get(Calendar.MONTH);
                final int globalDay = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (Build.VERSION.SDK_INT >= 26) {
                            if (year > 2007) {
                                Toast.makeText(getActivity(), R.string.must_be_13, Toast.LENGTH_LONG).show();
                                return;
                            }
                            LocalDate myDate = LocalDate.of(year, month, dayOfMonth);
                            age = calculateAge(myDate, LocalDate.of(globalYear, globalMonth, globalDay)); //saving user's age
                        } else {
                            if (year > 2007) {
                                Toast.makeText(getActivity(), R.string.must_be_13, Toast.LENGTH_LONG).show();
                                return;
                            }
                            age = getAge(year, month, dayOfMonth);
                        }
                        dateOfBirth = dayOfMonth + "/" + (month + 1) + "/" + year;
                        dateEt.getEditText().setText(dateOfBirth);
                    }
                }, globalYear, globalMonth, globalDay);
                dpd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dpd.show(); //shows dialog
            }
        });

        locationEt = rootView.findViewById(R.id.location_auto_complete);

        //auto complete google api
        AutoCompleteAdapter autoCompleteAdapter = new AutoCompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
        locationEt.setAdapter(autoCompleteAdapter);

        geocoder = new Geocoder(getActivity());
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isLocation = isLocationEnabled(getActivity());

                if (Build.VERSION.SDK_INT >= 23) {
                    int hasLocationPermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED)
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    else {
                        if (isLocation) {
                            locationEt.clearFocus();
                            startLocation();
                        } else {
                            Toast.makeText(getActivity(), R.string.turn_on_location_first, Toast.LENGTH_SHORT).show();
                            locationEt.setHint(getString(R.string.enter_loc_manual));
                        }

                    }
                } else {
                    if (isLocation) {
                        locationEt.clearFocus();
                        startLocation();
                    } else {
                        locationEt.setHint(getString(R.string.enter_loc_manual));
                        Toast.makeText(getActivity(), R.string.turn_on_location_first, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        genderGroup = rootView.findViewById(R.id.gender_radio_group);
        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int[] arr = getResources().getIntArray(R.array.gender_values);
                switch (checkedId) {
                    case R.id.male:
                        gender = arr[0];
                        break;
                    case R.id.female:
                        gender = arr[1];
                        break;
                    case R.id.other:
                        gender = arr[2];
                        break;
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

    private void setWalkerEventsListeners() {
        next2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isValid = validateFields();
                if (isValid) {

                    listener.onNextSecond(fullName, email, password, dateOfBirth, age, gender, type, location);

                } else {
                    return;
                }
            }
        });
    }

    //events for normal user registration
    private void setNormalEventsListeners() {
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildSliderBottomDialog();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isValid = validateFields();
                if (isValid) {

                    listener.startLoader();

                    /*if (bitmap1 != null) {
                        handleUpload(bitmap1);
                    } else if (bitmap2 != null) {
                        handleUpload(bitmap2);
                    } else {
                        //TODO fix register without photo
                        Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.user_drawer_icon_256);
                        handleUpload(bitmap);
                    }*/

                    if (isFromCamera) {
                        getActivity().getContentResolver().delete(fileUri, null, null);
                    }

                    listener.onRegister(fileUri,fullName, email, password, dateOfBirth, age, gender, type, location);

                } else {
                    return;
                }
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE);
    }

    private void buildSliderBottomDialog() {

        final BottomSheetDialog bottomDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_profile_slider, null);

        LinearLayout takePic, selectPic;
        takePic = bottomSheetView.findViewById(R.id.slider_take_pic);
        selectPic = bottomSheetView.findViewById(R.id.slider_select_pic);

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "from");
                fileUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAMERA_REQUEST);

                bottomDialog.dismiss();
            }
        });

        selectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check permissions
                if (Build.VERSION.SDK_INT >= 23) {
                    int hasWritePermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    int hasReadPermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (hasWritePermission != PackageManager.PERMISSION_GRANTED && hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
                    } else {
                        openGallery();
                    }
                }

                bottomDialog.dismiss();
            }
        });

        bottomDialog.setContentView(bottomSheetView);
        bottomDialog.show();
    }

    private void startLocation() {

        final View dialogView;
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        dialogView = getLayoutInflater().inflate(R.layout.loader_dialog, null);
        TextView body = dialogView.findViewById(R.id.loader_tv);
        body.setText(R.string.fetch_location);
        alertDialog = builder1.setView(dialogView).setCancelable(false).show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        client = LocationServices.getFusedLocationProviderClient(getActivity());
        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location lastLocation = locationResult.getLastLocation();
                final double lat = lastLocation.getLatitude();
                final double lng = lastLocation.getLongitude();

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                            final Address bestAddress = addresses.get(0);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(getActivity()!=null) {
                                        locationEt.setText(bestAddress.getLocality() + ",  " + bestAddress.getCountryName());
                                        location = locationEt.getText().toString().trim();
                                        locationEt.setHint(getString(R.string.your_location));
                                        alertDialog.dismiss();
                                    }
                                }
                            });
                        } catch (IOException e) {
                            alertDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, callback, null);
        } else if (Build.VERSION.SDK_INT <= 22)
            client.requestLocationUpdates(request, callback, null);
    }

    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    //clean
    /*private String getExtension(Uri uri) {
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private void FileUploader() {
        //StorageReference Ref = myStorageRef.child(System.currentTimeMillis() + '.' + getExtension(fileUri));
        StorageReference storageRef = myStorageRef.child(email);
        //upload file to firebase storage, if succeed or fail
        if (fileUri != null) {
            storageRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            //Toast.makeText(getActivity(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });
        }

    }*/

/*    private void handleUpload(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final StorageReference storage = FirebaseStorage.getInstance().getReference().child("Profiles").child(email + ".jpeg");

        storage.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(storage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void getDownloadUrl(StorageReference storage) {
        storage.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        setUserProfileUrl(uri);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void setUserProfileUrl(Uri uri) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        if (user != null) {
            user.updateProfile(request)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.stopLoader();
                            listener.createConfirmDialog();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            listener.stopLoader();
                        }
                    });
        }
    }*/

    private boolean validateFields() {
        if (!validLocation() | !validDate() | !validGroups()) {
            return false;
        }
        return true;
    }

    private boolean validGroups() {
        if (gender == null) {
            Toast.makeText(getActivity(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validLocation() {
        if (locationEt.getText().toString().isEmpty()) {
            locationInputLayout.setError(getString(R.string.field_empty_error));
            return false;
        } else {
            locationInputLayout.setError(null);
            location = locationEt.getText().toString().trim();
            return true;
        }
    }

    private boolean validDate() {
        if (dateEt.getEditText().getText().toString().isEmpty()) {
            dateEt.setError(getString(R.string.field_empty_error));
            return false;
        } else {
            dateEt.setError(null);
            return true;
        }
    }

    //API 26 and above
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

    //Below API 26
    private Integer getAge(int year, int month, int day) {

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        Integer ageInt = Integer.valueOf(age);

        return ageInt;
    }
}
