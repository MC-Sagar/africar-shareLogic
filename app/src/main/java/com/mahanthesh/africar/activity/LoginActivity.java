package com.mahanthesh.africar.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.mahanthesh.africar.R;
import com.mahanthesh.africar.fragment.CallbackFragment;
import com.mahanthesh.africar.fragment.FragmentLogin;
import com.mahanthesh.africar.fragment.FragmentSignup;


public class LoginActivity extends AppCompatActivity implements CallbackFragment {

    Fragment fragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    CallbackFragment callbackFragment, callbackFragment2;
    TextView signupPointer, loginPointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Fullscreen);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
           // getWindow().insetsController.hide(WindowInsets.Type.statusBars());
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_login);

        addFragment();

        signupPointer = (TextView) findViewById(R.id.signup_pointer);
        loginPointer = (TextView) findViewById(R.id.login_pointer);

    }

    public void addFragment() {
        fragment = new FragmentLogin();
        setCallbackFragment(this);
        setCallbackFragment2(this);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_container, fragment);
        fragmentTransaction.commit();
    }

    public void replaceFragment() {
        fragment = new FragmentSignup();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();

    }

    public void replaceFragment2() {
        fragment = new FragmentLogin();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();
    }

    public void showLogin(View view) {
        if (callbackFragment2 != null) {
            callbackFragment2.changeFragment2();
            loginPointer.setVisibility(View.VISIBLE);
            signupPointer.setVisibility(View.INVISIBLE);
        }

    }

    public void showSignup(View v) {
        if (callbackFragment2 != null) {
            callbackFragment2.changeFragment2();
            loginPointer.setVisibility(View.INVISIBLE);
            signupPointer.setVisibility(View.VISIBLE);
        }

    }


    public void setCallbackFragment(CallbackFragment callbackFragment) {
        this.callbackFragment = callbackFragment;
    }

    public void setCallbackFragment2(CallbackFragment callbackFragment2) {
        this.callbackFragment2 = callbackFragment2;
    }

    @Override
    public void changeFragment() {
        replaceFragment();
    }

    @Override
    public void changeFragment2() {
        replaceFragment2();
    }
}