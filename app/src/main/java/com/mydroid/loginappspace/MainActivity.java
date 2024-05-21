package com.mydroid.loginappspace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mydroid.loginappspace.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private static final int REQ_ONE_TAP = 2;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    String email = "";
    String pass = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                credentialsSetter();
                registerUser(email, pass);
            }
        });

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                credentialsSetter();
                logInUser(email, pass);
            }
        });

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        binding.Google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                binding.linearprogress.setVisibility(View.VISIBLE);
//                binding.linearprogress.show();
                signInWithGoogle();
            }
        });

        if (mAuth.getCurrentUser() != null)
        {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }


    }

    private void signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        try {
                            startIntentSenderForResult(beginSignInResult.getPendingIntent().getIntentSender(), REQ_ONE_TAP, null, 0, 0, 0);
                        } catch (Exception e) {
                            Log.e("MainActivity", "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MainActivity", e.getLocalizedMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Authentication Successful.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            } catch (ApiException e) {
                Log.e("MainActivity", "Sign-in failed: " + e.getLocalizedMessage());
            }
        }
    }
    private void credentialsSetter() {
        email = binding.emailEditText.getText().toString().trim();
        pass = binding.passwordEditText.getText().toString().trim();
    }

    private void registerUser(String email, String pass) {
        if (!email.isEmpty() && !pass.isEmpty())
        {
            setInProgress(true, "register");

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            setInProgress(false, "register");
                            if (task.isSuccessful())
                            {
                                Toast.makeText(MainActivity.this, "Registration is successful", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                            else {
                                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed\nPlease try again... ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(MainActivity.this, "Please Enter Credentials", Toast.LENGTH_SHORT).show();
        }

    }

    private void logInUser(String email, String pass) {
        if (!email.isEmpty() && !pass.isEmpty())
        {
            setInProgress(true, "login");

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            setInProgress(false, "login");
                            if (task.isSuccessful())
                            {
                                Toast.makeText(MainActivity.this, "Welcome Back ! ", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                            else {
                                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(MainActivity.this, "You are not registerd user\nPlease register ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else
        {
            Toast.makeText(MainActivity.this, "Please Enter Credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void setInProgress(boolean inProgress, String which) {
        if (which.equals("register"))
        {
            if (inProgress) {
                binding.registerProgressBar.setVisibility(View.VISIBLE);
                binding.registerButton.setVisibility(View.GONE);
            } else {
                binding.registerProgressBar.setVisibility(View.GONE);
                binding.registerButton.setVisibility(View.VISIBLE);
            }
        }

        if (which.equals("login"))
        {
            if (inProgress) {
                binding.loginProgressBar.setVisibility(View.VISIBLE);
                binding.loginButton.setVisibility(View.GONE);
            } else {
                binding.loginProgressBar.setVisibility(View.GONE);
                binding.loginButton.setVisibility(View.VISIBLE);
            }
        }
    }
}