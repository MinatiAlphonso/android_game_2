package edu.sdsmt.group3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpDlg extends DialogFragment {
    private AlertDialog dlg;

    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
    private FirebaseUser firebaseUser;
    private View dlgView;
    private String email;
    private String username;
    private String password;
    private String verificationPassword;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());// Set the title
        builder.setTitle(R.string.sign_up);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.signup_dlg, null);
        builder.setView(view);

        // Add a cancel button
        builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
            // Cancel just closes the dialog box
        });

        builder.setPositiveButton(android.R.string.ok, (dialog, id) -> {
            // create account
            EditText emailInput = (EditText) dlg.findViewById(R.id.SU_Email);
            EditText passwordInput = (EditText) dlg.findViewById(R.id.SU_Password);
            EditText verificationPasswordInput = dlg.findViewById(R.id.SU_Password_Verification);
            EditText usernameInput = (EditText) dlg.findViewById(R.id.SU_Username);

            email = emailInput.getText().toString();
            password = passwordInput.getText().toString();
            verificationPassword = verificationPasswordInput.getText().toString();
            username = usernameInput.getText().toString();


            if (!password.equals(verificationPassword)) {
                // passwords dont match
            } else {
                createUser();
            }
        });

        dlg = builder.create();

        return dlg;
    }

    public void createUser() {
        Task<AuthResult> result = userAuth.createUserWithEmailAndPassword(email, password);
        result.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Log.d("AUTH", "createUserWithEmail:onComplete:" + task.isSuccessful());
                    setDisplayName();
                    firebaseUser = userAuth.getCurrentUser();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("/"+firebaseUser.getUid()+"/password", password);
                    result.put("/"+firebaseUser.getUid()+"/email", email);
                    userRef.updateChildren(result);
                } else if(task.getException().getMessage().equals("The email address is already in use by another account.")){
                    //TODO handle user already registered case
                }else {
                    Log.d("AUTH", "Problem: " + task.getException().getMessage());
                }
            }
        });
    }

    private void setDisplayName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("AUTH", "User profile updated.");
                        }
                    }
                });

    }
}
