package com.example.dogapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dogapp.Fragments.RegisterFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity implements RegisterFragment.OnRegisterFragmentListener {

    RelativeLayout loginContainer;

    final String REGISTER_FRAGMENT_TAG = "register_fragment";

    Button loginBtn, regBtn;
    TextInputLayout emailEt, passwordEt;

    String fullName;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); //sign in/up auth instance
    FirebaseAuth.AuthStateListener authStateListener; //listens to login/out changes

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page_layout);

        loginBtn = findViewById(R.id.login_btn);
        regBtn = findViewById(R.id.register_btn);
        emailEt = findViewById(R.id.email_login_input);
        passwordEt = findViewById(R.id.password_login_input);

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
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Hi " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                    else { //only sign in
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.login_container, new RegisterFragment(), REGISTER_FRAGMENT_TAG).addToBackStack(null).commit();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateEmail() | !validatePassword()) {
                    return;
                } else {
                    //login account with firebase
                    firebaseAuth.signInWithEmailAndPassword(emailEt.getEditText().getText().toString(), passwordEt.getEditText().getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    private boolean validateEmail() {

        if (emailEt.getEditText().getText().toString().isEmpty()) {
            emailEt.setError("Field cannot be empty");
            return false;

        } else {
            emailEt.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {

        if (passwordEt.getEditText().getText().toString().isEmpty()) {
            passwordEt.setError("Field cannot be empty");
            return false;

        } else {
            passwordEt.setError(null);
            return true;
        }
    }

    @Override
    public void onRegister(final String fullName, String email, final String username, String password) {

        this.fullName = fullName; //for the auth listener

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("full_name", fullName);
                    intent.putExtra("username", username);
                    startActivity(intent);

                    //close fragment
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(REGISTER_FRAGMENT_TAG);
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        getSupportFragmentManager().popBackStack(); //remove from back stack
                    }

                    //close activity
                    finish();
                } else {
                    //close fragment
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(REGISTER_FRAGMENT_TAG);
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        getSupportFragmentManager().popBackStack(); //remove from back stack
                    }
                    Toast.makeText(LoginActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBack() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(REGISTER_FRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack(); //remove from back stack
        }
    }
}
