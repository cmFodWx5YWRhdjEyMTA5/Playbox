package uk.co.darkerwaters.smspeak;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    private static final int MY_PERMISSIONS_HEADSET_REQUEST_CODE = 133;

    private static final int ACT_CHECK_TTS_DATA = 1000;

    private boolean permissionContacts = false;
    private boolean permissionSms = false;
    private boolean permissionHeadphones = false;

    private Switch switchTime;
    private Switch switchIntro;
    private Switch switchContact;
    private Switch switchMessage;

    private TextView introText;
    private TextView exampleText;

    private FloatingActionButton powerFab;
    private TextView powerText;

    private Switch switchHeadphones;
    private Switch switchHeadset;

    private BroadcastReceiver headphoneStateReceiver;

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

        this.switchHeadphones = (Switch) findViewById(R.id.switch_headphones);
        this.switchHeadset = (Switch) findViewById(R.id.switch_headset);

        findViewById(R.id.imageExample).setOnClickListener(new View.OnClickListener() {
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

        // headphones
        this.switchHeadphones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // set this on the state
                State.GetInstance(MainActivity.this).setIsTalkHeadphones(MainActivity.this.switchHeadphones.isChecked());
                // reconstruct the example text
                constructExampleMessage();
                if (MainActivity.this.switchHeadphones.isChecked()) {
                    // and check permissions as they want to use this
                    checkHeadsetPermission();
                }
            }
        });
        this.switchHeadset.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // set this on the state
                State.GetInstance(MainActivity.this).setIsTalkHeadset(MainActivity.this.switchHeadset.isChecked());
                // reconstruct the example text
                constructExampleMessage();
                if (MainActivity.this.switchHeadset.isChecked()) {
                    // and check permissions as they want to use this
                    checkHeadsetPermission();
                }
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

        // when to talk too
        this.switchHeadphones.setChecked(state.isTalkHeadphones());
        this.switchHeadset.setChecked(state.isTalkHeadset());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // listen for headphone changes
        this.headphoneStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onHeadphoneStateChanged(intent);
            }
        };
        registerReceiver(this.headphoneStateReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    protected void onPause() {
        // stop listening for headphone changes
        unregisterReceiver(this.headphoneStateReceiver);
        this.headphoneStateReceiver = null;
        // and pause
        super.onPause();
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

        if (State.GetInstance(this).isTalkActive(this)) {
            // we are active
            this.powerText.setText(R.string.power_on);
        }
        else {
            // we are not active
            this.powerText.setText(R.string.power_off);
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
            // if they enabled this then chances are that they want to use it
            this.switchContact.setChecked(true);
        }
    }

    protected void checkHeadsetPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            // Do something, when permissions not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(this ,Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
                // If we should give explanation of requested permissions

                // Show an alert dialog here with request explanation
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission is required to detect headphones being plugged in.");
                builder.setTitle("Please grant this permission");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS},
                                MY_PERMISSIONS_HEADSET_REQUEST_CODE
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
                                Manifest.permission.MODIFY_AUDIO_SETTINGS
                        },
                        MY_PERMISSIONS_HEADSET_REQUEST_CODE
                );
            }
        }
        else {
            // permission is granted
            this.permissionHeadphones = true;
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
            case MY_PERMISSIONS_HEADSET_REQUEST_CODE:
                // When request is cancelled, the results array are empty
                if((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    // Permissions are granted
                    this.permissionHeadphones = true;
                } else {
                    // Permissions are denied
                    this.permissionHeadphones = false;
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



    private void onHeadphoneStateChanged(Intent intent) {
        // state of the headphone changed, update our status we are showing
        constructExampleMessage();/*
        String action = intent.getAction();
        Log.i("SMSpeak", action);
        if( (action.compareTo(Intent.ACTION_HEADSET_PLUG))  == 0)   //if the action match a headset one
        {
            int headSetState = intent.getIntExtra("state", 0);      //get the headset state property
            int hasMicrophone = intent.getIntExtra("microphone", 0);//get the headset microphone property
            if( (headSetState == 0) && (hasMicrophone == 0))        //headset was unplugged & has no microphone
            {
                //do whatever
            }
        }*/
    }

    protected void onPermissionsChanged() {
        // called when the permissions change for something on this activity (SMS or Contacts allowed)
        if (this.permissionContacts == false) {
            // disable the reading of the contact, not allowed
            this.switchContact.setChecked(false);
        }
        if (this.permissionHeadphones == false) {
            this.switchHeadphones.setChecked(false);
        }
        // and reconstruct the example
        constructExampleMessage();
    }

}
