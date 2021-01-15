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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mahanthesh.africar.activity.HomepageActivity;
import com.mahanthesh.africar.R;
import com.mahanthesh.africar.utils.Constants;
import com.mahanthesh.africar.utils.Utils;

public class FragmentLogin extends Fragment {

    Button btnLogin;
    EditText et_email, et_password;
    ProgressBar spinner;

    private CallbackFragment mCallback;
    private FirebaseAuth mAuth;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment,container,false);

        btnLogin = view.findViewById(R.id.login_btn);
        et_email = view.findViewById(R.id.et_login);
        et_password = view.findViewById(R.id.et_password);
        spinner = view.findViewById(R.id.login_spinner);

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ValidateLogin();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (CallbackFragment) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    private void ValidateLogin() {
        et_email.setError(null);
        et_password.setError(null);

        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

        if(TextUtils.isEmpty(email)){
            et_email.setError("Required!");
            et_email.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(password)){
            et_password.setError("Required!");
            et_password.requestFocus();
            return;
        }

        login();
    }

    private void login(){
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        spinner.setVisibility(View.VISIBLE);

        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                if(mCallback != null) {
                    Utils.saveLocalUser(requireContext(), Constants.DEFAULT_USER, et_email.getText().toString(), authResult.getUser().getUid());
                    spinner.setVisibility(View.INVISIBLE);
                    //Open Home
                    Intent intent = new Intent(getActivity(), HomepageActivity.class);
                    startActivity(intent);
                    Toast.makeText(getContext(), "Successful",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                spinner.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
