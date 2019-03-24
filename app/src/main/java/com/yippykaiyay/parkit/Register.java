package com.yippykaiyay.parkit;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


// sanpled from tonikami

public class Register extends ActionBarActivity implements View.OnClickListener{
    EditText nameText, ageText, usernameText, passwordText, password2Text;
    Button registerButton;
    TextView cancelLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameText = (EditText) findViewById(R.id.nameText);
        ageText = (EditText) findViewById(R.id.ageText);
        usernameText = (EditText) findViewById(R.id.usernameText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        password2Text = (EditText) findViewById(R.id.password2Text);
        registerButton = (Button) findViewById(R.id.registerButton);

        cancelLink = (TextView) findViewById(R.id.tvCancelLink);
        registerButton.setOnClickListener(this);
        cancelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(Register.this, Login.class);
                startActivity(registerIntent);
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (nameText.getText().toString().equals("") || ageText.getText().toString().equals("") || usernameText.getText().toString().equals("") || passwordText.getText().toString().equals("") || password2Text.getText().toString().equals("")){

            showErrorMessage2();
        }
        else{

            if (usernameText.getText().toString().contains("@")) {
                if (passwordText.getText().toString().equals(password2Text.getText().toString())){
                    switch (v.getId()) {
                        case R.id.registerButton:
                            String name = nameText.getText().toString();
                            String username = usernameText.getText().toString();
                            String password = passwordText.getText().toString();
                            int age = Integer.parseInt(ageText.getText().toString());

                            User user = new User(name, age, username, password);
                            registerUser(user);
                            break;

                    }
                }
                else{
                    showErrorMessage();
                }
            }
            else {
                showErrorMessage3();
            }
        }
    }



    private void registerUser(User user) {
        ServerRequests serverRequest = new ServerRequests(this);
        serverRequest.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                Intent loginIntent = new Intent(Register.this, Login.class);
                startActivity(loginIntent);
            }
        });
    }



    private void showErrorMessage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Register.this);
        dialogBuilder.setMessage("Passwords Do Not Match");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    private void showErrorMessage2() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Register.this);
        dialogBuilder.setMessage("Fields Cannot Be Blank");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    private void showErrorMessage3() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Register.this);
        dialogBuilder.setMessage("Please Enter A Valid Email Address");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }





/*    private void checkUsername(String username){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.checkUsernameInBackground(username, new GetUsernameCallback()){
            @Override
                    public void done(username returnedUsername)
        }

    }*/



}
