package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private static String TAG="PhoneLoginActivity";

    private Button SendVerificationCodeButton,VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth=FirebaseAuth.getInstance();

        SendVerificationCodeButton=findViewById(R.id.send_var_code_button);
        VerifyButton=findViewById(R.id.verify_button);
        InputPhoneNumber=findViewById(R.id.phone_number_input);
        InputVerificationCode=findViewById(R.id.verification_code_input);
        Context context=PhoneLoginActivity.this;
        loadingBar=new ProgressDialog(this);


        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {



                String phoneNumber="+91"+InputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this,"Phone number required",Toast.LENGTH_LONG).show();
                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, while we are authenticating your phone....");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this,"Please write verification code first...",Toast.LENGTH_LONG).show();

                }
                else
                {
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait, while we are verifying your code....");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"Invalid phone number, please enter phone number with your country code...",Toast.LENGTH_LONG).show();
                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);
                loadingBar.dismiss();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this,"Code has been sent, please check and verify...",Toast.LENGTH_LONG).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
                }
        };
        }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {

                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,"Logged in successfully",Toast.LENGTH_LONG).show();

                            SendUserToMainActivity();
                        } else {
                            String message= Objects.requireNonNull(task.getException()).toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error :"+message,Toast.LENGTH_LONG).show();


                            // Sign zin failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}


