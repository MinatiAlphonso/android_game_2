package edu.sdsmt.group3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private final Authenticator _authenticator = Authenticator.INSTANCE;
    private static final String PLAYER_EMAIL = "edu.sdsmt.group3.EMAIL";
    private static final String PLAYER_PASSWORD = "edu.sdsmt.group3.PASSWORD";
    private static final String PLAYER_USERNAME = "edu.sdsmt.group3.USERNAME";
    private static final String PLAYER_PASSWORD_VERIFICATION = "edu.sdsmt.group3.PASSWORD_VERIFICATION";

    private EditText _playerEmailInput;
    private EditText _passwordInput;
    private EditText _retypePasswordInput;
    private CheckBox _rememberMeInput;
    private EditText _usernameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        _playerEmailInput = findViewById(R.id.playerEmailInput);
        _passwordInput = findViewById(R.id.passwordInput);
        _retypePasswordInput = findViewById(R.id.retypePasswordInput);
        _rememberMeInput = findViewById(R.id.rememberMe);

        SharedPreferences preferences = getSharedPreferences("Login", 0);
        String email = preferences.getString("email", "");
        if (email.isEmpty()){
            //.. data not saved
        } else {
            String password = preferences.getString("password", "");
            _playerEmailInput.setText(email);
            _passwordInput.setText(password);
            _retypePasswordInput.setText(password);
            _rememberMeInput.setChecked(true);
        }


        if(savedInstanceState != null){
            setValues(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putString(PLAYER_EMAIL, _playerEmailInput.getText().toString());
        bundle.putString(PLAYER_PASSWORD, _passwordInput.getText().toString());
        bundle.putString(PLAYER_PASSWORD_VERIFICATION, _retypePasswordInput.getText().toString());
    }

    public void onLogin(View view) {
        // validate login/signup
        String playerEmail = _playerEmailInput.getText().toString();
        String password = _passwordInput.getText().toString();
        String passwordVerification = _retypePasswordInput.getText().toString();

        if (password.isEmpty() || passwordVerification.isEmpty() || playerEmail.isEmpty()) {
            Toast.makeText(view.getContext(), "Please enter an email/password", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(passwordVerification)) {
            Toast.makeText(view.getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else {

            if (_rememberMeInput.isChecked()) {
                storePreferences(playerEmail, password);
            }
            // password match, check if in system
            _authenticator.setCurrentIntent(this);
            _authenticator.setEmail(playerEmail);
            _authenticator.setPassword(password);
            _authenticator.signIn();
        }
    }

    private void storePreferences(String email, String password) {
        SharedPreferences preferences = getSharedPreferences("Login", 0);
        preferences.edit().putString("email", email).apply();
        preferences.edit().putString("password", password).apply();
    }

    public void onSignup(View view) {
        // show dialog for user to sign up
        SignUpDlg dlg = new SignUpDlg();
        dlg.show(getSupportFragmentManager(), "signup");
    }

    private void setValues(Bundle bundle) {
        String playerEmail = bundle.getString(PLAYER_EMAIL);
        String password = bundle.getString(PLAYER_PASSWORD);
        String passwordVerification = bundle.getString(PLAYER_PASSWORD_VERIFICATION);
        _playerEmailInput.setText(playerEmail);
        _passwordInput.setText(password);
        _retypePasswordInput.setText(passwordVerification);
    }

    public void resetDatabase(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = database.getReference("Games");
        gameRef.removeValue();
    }
}