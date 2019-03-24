package com.yippykaiyay.parkit;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


//

public class Login extends ActionBarActivity implements View.OnClickListener {
    Button loginButton;
    TextView registerLink, termsLink;
    EditText usernameText, passwordText;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        usernameText = (EditText) findViewById(R.id.usernameText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        registerLink = (TextView) findViewById(R.id.tvRegisterLink);
        termsLink = (TextView) findViewById(R.id.tvTermsConditions);

        loginButton.setOnClickListener(this);
        registerLink.setOnClickListener(this);
        termsLink.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginButton:
                String username = usernameText.getText().toString();
                String password = passwordText.getText().toString();

                User user = new User(username, password);

                authenticate(user);
                break;
            case R.id.tvRegisterLink:
                Intent registerIntent = new Intent(Login.this, Register.class);
                startActivity(registerIntent);
                break;
            case R.id.tvTermsConditions:
                AlertDialog.Builder termsDialog = new AlertDialog.Builder(Login.this);
                termsDialog.setIcon(R.mipmap.ic_menu_about);
                termsDialog.setTitle("Disclaimer");
                termsDialog.setMessage("It is illegal to operate a mobile while driving. Please use Park-It responsibly and obey the laws in your area.");
                termsDialog.setPositiveButton("Ok", null);
                termsDialog.show();
                break;
        }
    }

    private void authenticate(User user) {
        ServerRequests serverRequest = new ServerRequests(this);
        serverRequest.fetchUserDataAsyncTask(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                if (returnedUser == null) {
                    showErrorMessageIncorrect();
                } else {
                    logUserIn(returnedUser);
                }
            }
        });
    }

    private void showErrorMessageIncorrect() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Login.this);
        dialogBuilder.setMessage("Login details are incorrect");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    private void logUserIn(User returnedUser) {
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);
        startActivity(new Intent(this, MapMainActivity.class));
    }




    //allows exit of app when back button pressed on log in screen
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
