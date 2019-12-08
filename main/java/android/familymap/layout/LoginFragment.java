package android.familymap.layout;

import android.familymap.asynchronous.LoginTask;
import android.familymap.R;
import android.familymap.asynchronous.RegisterTask;
import android.familymap.data.ServerProxy;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import netabs.UserLoginRequest;
import netabs.UserRegisterRequest;


public class LoginFragment extends Fragment {
    private EditText mServerHost;
    private EditText mServerPort;
    private EditText mUserName;
    private EditText mPassword;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mEmail;
    private RadioButton mMale;
    private RadioButton mFemale;
    private Button mRegisterButton;
    private Button mLoginButton;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

//        Get all the UI elements from the view
        mServerHost = v.findViewById(R.id.server_host_field);
        mServerPort = v.findViewById(R.id.server_port_field);
        mUserName = v.findViewById(R.id.username_field);
        mPassword = v.findViewById(R.id.password_field);
        mFirstName = v.findViewById(R.id.first_name_field);
        mLastName = v.findViewById(R.id.last_name_field);
        mEmail = v.findViewById(R.id.email_field);
        mMale = v.findViewById(R.id.button_male);
        mFemale = v.findViewById(R.id.button_female);

        mRegisterButton = v.findViewById(R.id.button_register);
        mLoginButton = v.findViewById(R.id.button_login);

//        Create and add text listeners to enable and disable the register/login buttons as needed
        TextWatcher loginWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                intentionally blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mServerHost.getText().length() > 0 && mServerPort.getText().length() > 0
                        && mUserName.getText().length() > 0 && mPassword.getText().length() > 0) {
                    mLoginButton.setEnabled(true);
                }
                else {
                    mLoginButton.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
//                intentionally blank
            }
        };

        TextWatcher registerWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                intentionally blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mServerHost.getText().length() > 0 && mServerPort.getText().length() > 0
                        && mUserName.getText().length() > 0 && mPassword.getText().length() > 0
                        && mFirstName.getText().length() > 0 && mLastName.getText().length() > 0
                        && mEmail.getText().length() > 0) {
                    mRegisterButton.setEnabled(true);
                }
                else {
                    mRegisterButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                intentionally blank
            }
        };

        mServerHost.addTextChangedListener(loginWatcher);
        mServerHost.addTextChangedListener(registerWatcher);
        mServerPort.addTextChangedListener(loginWatcher);
        mServerPort.addTextChangedListener(registerWatcher);
        mUserName.addTextChangedListener(loginWatcher);
        mUserName.addTextChangedListener(registerWatcher);
        mPassword.addTextChangedListener(loginWatcher);
        mPassword.addTextChangedListener(registerWatcher);
        mFirstName.addTextChangedListener(registerWatcher);
        mLastName.addTextChangedListener(registerWatcher);
        mEmail.addTextChangedListener(registerWatcher);

//        Create the register and login button listeners
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerProxy.initializeServer(mServerHost.getText().toString(), mServerPort.getText().toString());

                String gender = mFemale.isChecked() ? "f" : "m";

                UserRegisterRequest request = new UserRegisterRequest(mUserName.getText().toString(),
                        mPassword.getText().toString(), mUserName.getText().toString(),
                        mFirstName.getText().toString(), mLastName.getText().toString(), gender);

                RegisterTask registerTask = new RegisterTask();
                registerTask.registerListener((MainActivity) getActivity());
                registerTask.execute(request);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerProxy.initializeServer(mServerHost.getText().toString(), mServerPort.getText().toString());

                UserLoginRequest request = new UserLoginRequest(mUserName.getText().toString(), mPassword.getText().toString());
                LoginTask loginTask = new LoginTask();
                loginTask.registerListener((MainActivity) getActivity());
                loginTask.execute(request);

            }
        });


        return v;
    }

    // TODO: Remove this, should be unneeded once MainActivity has the callback
//    @Override
//    public void taskComplete(ServerAccessError possibleError) {
//        Toast userMessage;
//
//        if (possibleError != null) {
//            userMessage = Toast.makeText(getContext(), possibleError.getMessage(), Toast.LENGTH_SHORT);
//        }
//        else {
//            DataCache cache = DataCache.getInstance();
//            userMessage = Toast.makeText(getContext(), cache.getUser().getFirstName() + " " + cache.getUser().getLastName(), Toast.LENGTH_SHORT);
//        }
//        userMessage.show();
//    }
}
