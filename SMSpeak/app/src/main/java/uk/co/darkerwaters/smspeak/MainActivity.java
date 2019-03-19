package uk.co.darkerwaters.smspeak;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.time.LocalDateTime;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_CONTACT_REQUEST_CODE = 131;
    private static final int MY_PERMISSIONS_SMS_REQUEST_CODE = 132;

    private static final int ACT_CHECK_TTS_DATA = 1000;

    private boolean permissionContacts = false;
    private boolean permissionSms = false;

    private Switch switchTime;
    private Switch switchIntro;
    private Switch switchContact;
    private Switch switchMessage;

    private TextView introText;
    private TextView exampleText;

    private FloatingActionButton powerFab;
    private TextView powerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.switchTime = (Switch) findViewById(R.id.tts_talkTimeSwitch);
        this.switchIntro = (Switch) findViewById(R.id.tts_talkIntroSwitch);
        this.switchContact = (Switch) findViewById(R.id.tts_talkContactSwitch);
        this.switchMessage = (Switch) findViewById(R.id.tts_talkMessageSwitch);

        this.exampleText = (TextView) findViewById(R.id.exampleText);
        this.introText = (TextView) findViewById(R.id.editText_IntroText);

        this.powerFab = (FloatingActionButton) findViewById(R.id.powerActionButton);
        this.powerText = (TextView) findViewById(R.id.powerText);

        findViewById(R.id.button_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpeakService.SpeakMessage(MainActivity.this,
                        "012345",
                        getResources().getString(R.string.example_contact),
                        getResources().getString(R.string.example_message));
            }
        });

        // setup listeners
        this.switchTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // set this on the state
                State.GetInstance(MainActivity.this).setIsTalkTime(MainActivity.this.switchTime.isChecked());
                // reconstruct the example text
                constructExampleMessage();
            }
        });
        this.switchIntro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // set this on the state
                State.GetInstance(MainActivity.this).setIsTalkIntro(MainActivity.this.switchIntro.isChecked());
                // reconstruct the example text
                constructExampleMessage();
            }
        });
        this.switchContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // set this on the state
                State.GetInstance(MainActivity.this).setIsTalkContact(MainActivity.this.switchContact.isChecked());
                // reconstruct the example text
                constructExampleMessage();
                if (MainActivity.this.switchContact.isChecked()) {
                    // and check permissions as they want to use this
                    checkContactsPermission();
                }
            }
        });
        this.switchMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // set this on the state
                State.GetInstance(MainActivity.this).setIsTalkMessage(MainActivity.this.switchMessage.isChecked());
                // reconstruct the example text
                constructExampleMessage();
            }
        });
        // and the intro text
        this.introText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // fine, whatever...
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // set this on the state
                State.GetInstance(MainActivity.this).setIntro(charSequence == null ? "" : charSequence.toString());
                // and reconstruct the example
                constructExampleMessage();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // set all the defaults correctly
        setControlsFromData();

        // check permission for the SMS reading action
        checkSmsPermission();

        // show the default message
        constructExampleMessage();

        //check that TTS is installed, we need that
        Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA);
    }

    private void setControlsFromData() {
        State state = State.GetInstance(this);
        // set the switches
        this.switchTime.setChecked(state.isTalkTime());
        this.switchIntro.setChecked(state.isTalkIntro());
        this.switchContact.setChecked(state.isTalkContact());
        this.switchMessage.setChecked(state.isTalkMessage());
        // and the intro
        this.introText.setText(state.getIntro());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void constructExampleMessage() {
        String exampleMessage = SpeakService.constructMessage(this,
                getResources().getString(R.string.example_contact),
                getResources().getString(R.string.example_message));
        // show the failure of this application to construct a message
        if (exampleMessage == null || exampleMessage.isEmpty() || this.permissionSms == false) {
            this.exampleText.setText(R.string.example_failure);
        }
        else {
            this.exampleText.setText(exampleMessage);
        }
    }

    protected void checkSmsPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS) +
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Do something, when permissions not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECEIVE_SMS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this ,Manifest.permission.READ_SMS)){
                // If we should give explanation of requested permissions

                // Show an alert dialog here with request explanation
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permissions are required to read you your SMS messages.");
                builder.setTitle("Please grant those permissions");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{
                                        Manifest.permission.RECEIVE_SMS,
                                        Manifest.permission.READ_SMS,
                                },
                                MY_PERMISSIONS_SMS_REQUEST_CODE
                        );
                    }
                });
                builder.setNeutralButton("Cancel",null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS
                        },
                        MY_PERMISSIONS_SMS_REQUEST_CODE
                );
            }
        }
        else {
            // permission is granted
            this.permissionSms = true;
        }
    }

    protected void checkContactsPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Do something, when permissions not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(this ,Manifest.permission.READ_CONTACTS)) {
                // If we should give explanation of requested permissions

                // Show an alert dialog here with request explanation
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission is required to read you your contact name.");
                builder.setTitle("Please grant this permission");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                MY_PERMISSIONS_CONTACT_REQUEST_CODE
                        );
                    }
                });
                builder.setNeutralButton("Cancel",null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Directly request for required permission, without explanation
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.READ_CONTACTS
                        },
                        MY_PERMISSIONS_CONTACT_REQUEST_CODE
                );
            }
        }
        else {
            // permission is granted
            this.permissionContacts = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_SMS_REQUEST_CODE:
                // When request is cancelled, the results array are empty
                if((grantResults.length >= 2) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                    // Permissions are granted
                    this.permissionSms = true;
                } else {
                    // Permissions are denied
                    this.permissionSms = false;
                }
                onPermissionsChanged();
                break;
            case MY_PERMISSIONS_CONTACT_REQUEST_CODE:
                // When request is cancelled, the results array are empty
                if((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    // Permissions are granted
                    this.permissionContacts = true;
                } else {
                    // Permissions are denied
                    this.permissionContacts = false;
                }
                onPermissionsChanged();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACT_CHECK_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Data exists, so we can instantiate the TTS engine
            } else {
                // Data is missing, so we start the TTS installation process
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    protected void onPermissionsChanged() {
        // called when the permissions change for something on this activity (SMS or Contacts allowed)
        if (this.permissionContacts == false) {
            // disable the reading of the contact, not allowed
            this.switchContact.setChecked(false);
        }
        // and reconstruct the example
        constructExampleMessage();
    }

}
