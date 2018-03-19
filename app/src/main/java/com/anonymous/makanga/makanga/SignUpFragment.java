package com.anonymous.makanga.makanga;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import java.util.ArrayList;
import java.util.List;


public class SignUpFragment extends Fragment {
    private EditText email,password;
    private Button registration;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener firebaseListener;


    private AutoCompleteTextView country_codeText;
    private List<String> country_codeArrayList;
    private View view;
    String userType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            userType = getArguments().getString("userType");
        }
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sign_up, container, false);


        country_codeText = view.findViewById(R.id.country_codeText);

        country_codeArrayList = new ArrayList<>();
        country_codeArrayList.add("+91");
        country_codeArrayList.add("+92");
        country_codeArrayList.add("+93");
        country_codeArrayList.add("+94");
        country_codeArrayList.add("+95");


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.select_dialog_item_for_auto,R.id.text, country_codeArrayList);

        country_codeText.setThreshold(1);
        country_codeText.setAdapter(arrayAdapter);
        country_codeText.setText(country_codeArrayList.get(0));

        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        registration = view.findViewById(R.id.registration);

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

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailText = email.getText().toString();
                final String passwordText = password.getText().toString();
                auth.createUserWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(getActivity(),"Sign up error",Toast.LENGTH_SHORT).show();
                        }else{
                            String user_id = auth.getCurrentUser().getUid();
                            if (userType.equals("Drivers")){
                                DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userType).child(user_id).child("name");
                                currentUserDb.setValue(email);
                            }else{
                                DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userType).child(user_id);
                                currentUserDb.setValue(true);
                            }

                        }
                    }
                });

            }
        });
        return view;
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
