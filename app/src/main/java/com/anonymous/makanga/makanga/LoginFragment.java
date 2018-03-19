package com.anonymous.makanga.makanga;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginFragment extends Fragment {
    private EditText email,password;
    private Button login;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener firebaseListener;
    String userType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            userType = getArguments().getString("userType");
        }
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_login, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null){
                    Intent intent;
                    if (userType.equals("Drivers")){
                        intent = new Intent(getActivity(),DriverMapActivity.class);
                    }else{
                        intent = new Intent(getActivity(),CustomerMapActivity.class);
                    }
                    startActivity(intent);
                    return;
                }
            }
        };

        email = v.findViewById(R.id.email);
        password = v.findViewById(R.id.password);

        login = v.findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailText = email.getText().toString();
                final String passwordText = password.getText().toString();
                auth.signInWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(getActivity(),"Sign In Error",Toast.LENGTH_SHORT).show();
                        }else{
                            String user_id = auth.getCurrentUser().getUid();
                            DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userType).child(user_id);
                            currentUserDb.setValue(true);
                        }
                    }
                });
            }
        });
        return v;
    }

    @Override
    public void onStart(){
        super.onStart();
        auth.addAuthStateListener(firebaseListener);
    }
    @Override
    public void onStop(){
        super.onStop();
        auth.removeAuthStateListener(firebaseListener);
    }

}
