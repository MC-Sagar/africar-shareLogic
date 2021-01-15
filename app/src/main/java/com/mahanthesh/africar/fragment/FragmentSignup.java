package com.mahanthesh.africar.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mahanthesh.africar.activity.HomepageActivity;
import com.mahanthesh.africar.R;
import com.mahanthesh.africar.utils.Utils;

public class FragmentSignup extends Fragment {

    Button btnSignup;
    EditText et_email, et_newPass, et_confirmPass;
    private CallbackFragment mCallbackFragment;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_fragment, container, false);

        btnSignup = view.findViewById(R.id.signup_btn);
        et_email = view.findViewById(R.id.et_email);
        et_newPass = view.findViewById(R.id.et_newPassword);
        et_confirmPass = view.findViewById(R.id.et_confirmPass);
        progressBar = view.findViewById(R.id.spinner);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                createAccount();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbackFragment = (CallbackFragment) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbackFragment = null;
    }

    private void createAccount() {
        et_email.setError(null);
        et_newPass.setError(null);
        et_confirmPass.setError(null);

        final String email = et_email.getText().toString();
        String password = et_newPass.getText().toString();
        String confirmPass = et_confirmPass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            progressBar.setVisibility(View.INVISIBLE);
            et_email.setError("Required!");
        } else if (TextUtils.isEmpty(password)) {
            progressBar.setVisibility(View.INVISIBLE);
            et_newPass.setError("Required!");
        } else if (!password.equals(confirmPass)) {
            progressBar.setVisibility(View.INVISIBLE);
            et_confirmPass.setError("Required!");
        } else {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getContext(), "Error creating account", Toast.LENGTH_SHORT).show();
                    } else {
                        Utils.saveLocalUser(getContext(),
                                et_email.getText().toString(),
                                et_email.getText().toString(),
                                task.getResult().getUser().getUid()
                        );
                        progressBar.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(getActivity(), HomepageActivity.class);
                        startActivity(intent);
                        Toast.makeText(getContext(), "Successful", Toast.LENGTH_SHORT).show();

                    }
                    Utils.closeKeyboard(getContext(), et_email);
                }
            });

        }


    }
}
