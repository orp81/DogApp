package com.example.dogapp.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dogapp.Enteties.User;
import com.example.dogapp.Fragments.ForgotPasswordFragment;
import com.example.dogapp.Fragments.RegisterFragment;
import com.example.dogapp.Fragments.SecondRegisterFragment;
import com.example.dogapp.Fragments.WalkerFinalRegisterFragment;
import com.example.dogapp.Models.SliderItem;
import com.example.dogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements RegisterFragment.OnRegisterFragmentListener, SecondRegisterFragment.OnSecondRegisterFragmentListener, ForgotPasswordFragment.OnForgotPasswordListener, WalkerFinalRegisterFragment.MyFinalWalkerFragmentListener {

    private RelativeLayout loginContainer;

    private final String REGISTER_FRAGMENT_TAG = "register_fragment";
    private final String REGISTER_FRAGMENT_2_TAG = "reg_2_frag";
    private final String REGISTER_FRAGMENT_3_TAG = "reg_3_frag";
    private final String FORGOT_PASS_TAG = "forgot_pass_frag";

    private Button loginBtn, registerBtn;
    private TextInputLayout emailEt, passwordEt;
    private TextView forgotPassTv;
    private TextView guestTv;
    private ProgressBar progressBar;
    private RelativeLayout hideLayout;

    private AlertDialog progressDialog;

    private String fullName;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    private FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes
    private FirebaseDatabase database = FirebaseDatabase.getInstance(); //actual database
    private DatabaseReference users = database.getReference("users"); //create new table named "users" and we get a reference to it

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page_layout);

        //fixed portrait mode
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        loginBtn = findViewById(R.id.login_btn);
        registerBtn = findViewById(R.id.register_btn);
        emailEt = findViewById(R.id.email_login_input);
        passwordEt = findViewById(R.id.password_login_input);
        forgotPassTv = findViewById(R.id.forgot_pass_tv);
        progressBar = findViewById(R.id.reg_2_progress_bar);
        hideLayout = findViewById(R.id.reg_2_hide_layout);
        guestTv = findViewById(R.id.continue_guest_tv);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                final FirebaseUser user = firebaseAuth.getCurrentUser(); //get current user

                if (user != null) {

                    if (fullName != null) { //sign up - update profile with full name

                        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fullName).build())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                        fullName = null;
                                        /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();*/
                                    }
                                });
                    } else { //only sign in
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };

        guestTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildLoaderDialog(getString(R.string.guest_login_in));
                firebaseAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            buildFailDialog(getString(R.string.went_wrong),getString(R.string.try_again));
                        }
                    }
                });
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildRegisterBottomSheetDialog();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateEmail() | !validatePassword()) {
                    return;
                } else {

                    buildLoaderDialog(getString(R.string.loggin_in));
                    //login account with firebase
                    firebaseAuth.signInWithEmailAndPassword(emailEt.getEditText().getText().toString(), passwordEt.getEditText().getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    if (!task.isSuccessful()) {
                                        buildFailDialog(getString(R.string.login_failed), getString(R.string.check_correct_details));
                                    }
                                }
                            });
                }
            }
        });

        forgotPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.login_container, new ForgotPasswordFragment(), FORGOT_PASS_TAG).addToBackStack(null).commit();
            }
        });

    }

    private boolean validateEmail() {

        if (emailEt.getEditText().getText().toString().isEmpty()) {
            emailEt.setError(getString(R.string.field_empty_error));
            return false;

        } else {
            emailEt.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {

        if (passwordEt.getEditText().getText().toString().isEmpty()) {
            passwordEt.setError(getString(R.string.field_empty_error));
            return false;

        } else {
            passwordEt.setError(null);
            return true;
        }
    }

    @Override
    public void onNext(String name, String email, String password, boolean isWalker) {
        SecondRegisterFragment fragment = SecondRegisterFragment.newInstance(name, email, password, isWalker);
        getSupportFragmentManager().beginTransaction().add(R.id.login_container, fragment, REGISTER_FRAGMENT_2_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onBack() {
        closeFragment(REGISTER_FRAGMENT_TAG);
    }

    @Override
    public void onBackSecond() {
        closeFragment(REGISTER_FRAGMENT_2_TAG);
    }

    @Override
    public void onNextSecond(String name, String email, String password, String date, Integer age, Integer gender, Boolean title, String location) {
        WalkerFinalRegisterFragment fragment = WalkerFinalRegisterFragment.newInstance(name, email, password, date, age, gender, title, location);
        getSupportFragmentManager().beginTransaction().add(R.id.login_container, fragment, REGISTER_FRAGMENT_3_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onRegister(final String name, final String email, String password, final String date, final Integer age, final Integer gender, final Boolean title, final String location) {
        this.fullName = name; //for the auth listener


        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    //push new User to database
                    User user = new User(name, date, age, email, gender, title, location, "profileUrl", "coverUrl", "id", true, "0", "",null, 0, null, false, 0,0,new ArrayList<SliderItem>());
                    users.child(firebaseAuth.getCurrentUser().getUid()).setValue(user);

                } else {
                    progressDialog.dismiss();
                    closeFragment(REGISTER_FRAGMENT_2_TAG);
                    closeFragment(REGISTER_FRAGMENT_TAG);
                    buildFailDialog(getString(R.string.reg_failed), getString(R.string.try_again));
                    fullName = null;
                }
            }
        });
    }

    private void buildRegisterBottomSheetDialog() {

        final BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_registration, null);
        Button regularRegBtn = bottomSheetView.findViewById(R.id.bottom_sheet_register_regular_btn);
        Button walkerRegBtn = bottomSheetView.findViewById(R.id.bottom_sheet_register_walker_btn);

        regularRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterFragment registerFragment = RegisterFragment.newInstance(false);
                getSupportFragmentManager().beginTransaction().replace(R.id.login_container, registerFragment, REGISTER_FRAGMENT_TAG).addToBackStack(null).commit();
                dialog.dismiss();
            }
        });

        walkerRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterFragment registerFragment = RegisterFragment.newInstance(true);
                getSupportFragmentManager().beginTransaction().replace(R.id.login_container, registerFragment, REGISTER_FRAGMENT_TAG).addToBackStack(null).commit();
                dialog.dismiss();
            }
        });


        dialog.setContentView(bottomSheetView);
        dialog.show();
    }

    private void buildLoaderDialog(String body) {
        final View dialogView;
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
        dialogView = getLayoutInflater().inflate(R.layout.loader_dialog, null);
        TextView bodyTv = dialogView.findViewById(R.id.loader_tv);
        bodyTv.setText(body);
        progressDialog = builder1.setView(dialogView).setCancelable(false).show();
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void buildConfirmDialog(String title, String body) {

        final AlertDialog confirmDialog;
        View dialogView = getLayoutInflater().inflate(R.layout.success_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        confirmDialog = builder.setView(dialogView).setCancelable(false).show();
        confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //dialog views
        TextView titleTv = dialogView.findViewById(R.id.success_title_tv);
        titleTv.setText(title);
        TextView bodyTv = dialogView.findViewById(R.id.success_body_tv);
        bodyTv.setText(body);
        Button closeBtn = dialogView.findViewById(R.id.success_dialog_close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void createConfirmDialog() {
        String welcome = getString(R.string.welcome) + " " + firebaseAuth.getCurrentUser().getDisplayName();
        buildConfirmDialog(getString(R.string.reg_complete), welcome);
    }

    private void buildFailDialog(String title, String body) {
        final AlertDialog failDialog;
        View dialogView = getLayoutInflater().inflate(R.layout.failed_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        failDialog = builder.setView(dialogView).show();
        failDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //dialog views
        TextView titleTv = dialogView.findViewById(R.id.failed_title_tv);
        titleTv.setText(title);
        TextView bodyTv = dialogView.findViewById(R.id.fail_body_tv);
        bodyTv.setText(body);
        Button closeBtn = dialogView.findViewById(R.id.fail_dialog_close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                failDialog.dismiss();
            }
        });
    }

    @Override
    public void sendEmail(final String emailToSend) {
        buildLoaderDialog(getString(R.string.sending_email));
        firebaseAuth.sendPasswordResetEmail(emailToSend).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    final AlertDialog confirmDialog;
                    View dialogView = getLayoutInflater().inflate(R.layout.success_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    confirmDialog = builder.setView(dialogView).show();
                    confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    TextView titleTv = dialogView.findViewById(R.id.success_title_tv);
                    titleTv.setText(R.string.email_sent);
                    TextView bodyTv = dialogView.findViewById(R.id.success_body_tv);
                    bodyTv.setText(getString(R.string.password_reset_sent_to) + " " + emailToSend);
                    Button closeBtn = dialogView.findViewById(R.id.success_dialog_close_btn);
                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmDialog.dismiss();
                        }
                    });
                } else {
                    final AlertDialog failDialog;
                    View dialogView = getLayoutInflater().inflate(R.layout.failed_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    failDialog = builder.setView(dialogView).show();
                    failDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    Button closeBtn = dialogView.findViewById(R.id.fail_dialog_close_btn);
                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            failDialog.dismiss();
                        }
                    });
                }
                progressDialog.dismiss();
                closeFragment(FORGOT_PASS_TAG);
            }
        });
    }

    @Override
    public void onForgotBack() {
        closeFragment(FORGOT_PASS_TAG);
    }

    private void closeFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack(); //remove from back stack
        }
    }

    //***************Walker 3rd page fragment events****************//
    @Override
    public void onWalkerRegisterClick(final String name, final String email, String password, final String date, final Integer age, final Integer gender, final Boolean title, final String location,
                                      final String aboutMe, final Integer exp, final Integer kmRange, final List<Integer> dogSizeList, final Boolean lastCall, final Integer payPerWalk) {

        this.fullName = name; //for the auth listener

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    //push new User to database
                    User user = new User(name, date, age, email, gender, title, location, "profileUrl", "coverUrl", "id", true, "0", aboutMe,exp, kmRange, dogSizeList, lastCall, payPerWalk,0,new ArrayList<SliderItem>());
                    users.child(firebaseAuth.getCurrentUser().getUid()).setValue(user);

                } else {
                    progressDialog.dismiss();
                    closeFragment(REGISTER_FRAGMENT_3_TAG);
                    closeFragment(REGISTER_FRAGMENT_2_TAG);
                    closeFragment(REGISTER_FRAGMENT_TAG);
                    buildFailDialog(getString(R.string.reg_failed), getString(R.string.try_again));
                    fullName = null;
                }
            }
        });
    }

    @Override
    public void onBackThird() {
        closeFragment(REGISTER_FRAGMENT_3_TAG);
    }

    @Override
    public void startWalkerRegisterLoader() {
        buildLoaderDialog(getString(R.string.create_new_acc));
    }

    @Override
    public void stopWalkerRegisterLoader() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void createWalkerConfirmDialog() {
        String welcome = getString(R.string.welcome) + " " + firebaseAuth.getCurrentUser().getDisplayName();
        buildConfirmDialog(getString(R.string.reg_complete), welcome);
    }


    //*******Client register fragment events********//
    @Override
    public void startLoader() {
        buildLoaderDialog(getString(R.string.create_new_acc));
    }

    @Override
    public void stopLoader() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
