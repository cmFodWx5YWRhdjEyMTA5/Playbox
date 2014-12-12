package uk.co.darkerwaters.tennisbuddy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale; 

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

public class ScoreboardActivity extends Activity implements OnInitListener {
	//ListView for displaying suggested words
	private ListView wordList;
	     
	//Log tag for output information
	private final String TAG = "ScoreboardActivity";//***enter your own tag here***
	 
	//TTS variables
	 
	//variable for checking TTS engine data on user device
	private int MY_DATA_CHECK_CODE = 0;
	
	private ListView mList;
    private TextView mInputStatus;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mRecognizerIntent;
	     
	//Text To Speech instance
	private TextToSpeech repeatTTS;

    Hashtable<String, String> voiceMatches = new Hashtable<String, String>();
    
    private void addCommandMatches(String cmd, ArrayList<String> matches) {
        if (!voiceMatches.containsKey(cmd)) {
                voiceMatches.put(cmd, cmd);
        }
        for (String s : matches) {
                voiceMatches.put(s, cmd);
        }
	}
	
	private String getCommand(ArrayList<String> matches) {
	        for (String s : matches) {
	                String cmd = voiceMatches.get(s);
	                if (cmd != null)
	                        return cmd;
	        }
	        return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//call superclass
		super.onCreate(savedInstanceState);
		//set content view
		setContentView(R.layout.activity_scoreboard);
		
		Log.d(TAG, "speech recognition available: " + SpeechRecognizer.isRecognitionAvailable(getBaseContext()));
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
        mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mRecognizerIntent.putExtra("calling_package", "com.jmoyer.adk_moto");
		
		//gain reference to speak button
		Button speechBtn = (Button) findViewById(R.id.speech_btn);
		//gain reference to word list
		wordList = (ListView) findViewById(R.id.word_list);
		
		mInputStatus = (TextView) findViewById(R.id.input_status);
		
		//find out whether speech recognition is supported
		PackageManager packManager = getPackageManager();
		List<ResolveInfo> intActivities = packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (intActivities.size() != 0) {
		    //speech recognition is supported - detect user button clicks
		    //speechBtn.setOnClickListener(this);
		    
		  //prepare the TTS to repeat chosen words
		    Intent checkTTSIntent = new Intent();  
		    //check TTS data  
		    checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);  
		    //start the checking Intent - will retrieve result in onActivityResult
		    startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
		}
		else
		{
		    //speech recognition not supported, disable button and output message
		    speechBtn.setEnabled(false);
		    Toast.makeText(this, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
		}
		
		//detect user clicks of suggested words
		wordList.setOnItemClickListener(new OnItemClickListener() {
		             
		    //click listener for items within list
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
		    {
		        //cast the view
		        TextView wordView = (TextView)view;
		        //retrieve the chosen word
		        String wordChosen = (String) wordView.getText();
		        //output for debugging
		        Log.v(TAG, "chosen: "+wordChosen);
		        //output Toast message
		        Toast.makeText(ScoreboardActivity.this, "You said: "+wordChosen, Toast.LENGTH_SHORT).show();//**alter for your Activity name***
		        
		      //speak the word using the TTS
		        repeatTTS.speak("You said: "+wordChosen, TextToSpeech.QUEUE_FLUSH, null);
		    }
		});
		repeatTTS = new TextToSpeech(this, this);  
        mSpeechRecognizer.startListening(mRecognizerIntent);
	}
	
	/**
	 * onInit fires when TTS initializes
	 */
	public void onInit(int initStatus) { 
	    //if successful, set locale
	    if (initStatus == TextToSpeech.SUCCESS)   
	        repeatTTS.setLanguage(Locale.UK);//***choose your own locale here***
	}
	
    private void cancelSpeechRecognition() {
        mSpeechRecognizer.stopListening();
        mSpeechRecognizer.cancel();
        mInputStatus.setText(R.string.not_listening);
        
        
    }
    
    private RecognitionListener mRecognitionListener = new RecognitionListener() {
                @Override
                public void onBufferReceived(byte[] buffer) {
                        // TODO Auto-generated method stub
                        //Log.d(TAG, "onBufferReceived");
                }

                @Override
                public void onError(int error) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "onError: " + error);
                        mInputStatus.setText(R.string.error);
                        mSpeechRecognizer.startListening(mRecognizerIntent);
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                        // TODO Auto-generated method stub
                        //Log.d(TAG, "onEvent");
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                        // TODO Auto-generated method stub
                        //Log.d(TAG, "onPartialResults");
                }

                @Override
                public void onReadyForSpeech(Bundle params) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "onReadyForSpeech");
                        mInputStatus.setText(R.string.speak);
                }

                @Override
                public void onResults(Bundle results) {
                        String cmd;

                        Log.d(TAG, "onResults");
                        Toast.makeText(getBaseContext(), "got voice results!", Toast.LENGTH_SHORT);

                        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        
                        cmd = getCommand(matches);
                        if (cmd == null) {
                                for (String s : matches) {
                                        Log.d(TAG, s);
                                        if (s.startsWith("serv")) {
                                                Log.d(TAG, "matched server");
                                                cmd = "server";
                                                addCommandMatches(cmd, matches);
                                                break;
                                        } else if (s.startsWith("receiv")) {
                                                Log.d(TAG, "matched receiver");
                                                cmd = "receiver";
                                                addCommandMatches(cmd, matches);
                                                break;
                                        } else if (s.startsWith("back")) {
                                                Log.d(TAG, "matched back");
                                                cmd = "back";
                                                addCommandMatches(cmd, matches);
                                                break;
                                        }
                                }
                        }
                        
                        if (null == cmd && false == matches.isEmpty()) {
                            repeatTTS.speak("Sorry I don't understand: "+ matches.get(0), TextToSpeech.QUEUE_FLUSH, null);
                        }
                        else {
                        	repeatTTS.speak("Processing: " + cmd, TextToSpeech.QUEUE_FLUSH, null);
                        }
                         
                        mInputStatus.setText(R.string.initializing);
                        mSpeechRecognizer.startListening(mRecognizerIntent);
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                        // TODO Auto-generated method stub
                        //Log.d(TAG, "onRmsChanged");
                }

                @Override
                public void onBeginningOfSpeech() {
                        // TODO Auto-generated method stub
                        //Log.d(TAG, "onBeginningOfSpeech");
                }

                @Override
                public void onEndOfSpeech() {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "onEndOfSpeech");
                        mInputStatus.setText(R.string.processing);
                }
        
    };

    @Override
    public void onDestroy() {
            if (mSpeechRecognizer != null) {
                    Log.d(TAG, "onDestroy: stopping listening");
                    cancelSpeechRecognition();
                    mSpeechRecognizer.destroy();
            }
            super.onDestroy();
    }
	
	/**
	 * onActivityResults handles:
	 *  - retrieving results of speech recognition listening
	 *  - retrieving result of TTS data check
	 */
/*	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    //check speech recognition result 
	    if (requestCode == VR_REQUEST && resultCode == RESULT_OK) 
	    {
	        //store the returned word list as an ArrayList
	        ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	        //set the retrieved list to display in the ListView using an ArrayAdapter
	        wordList.setAdapter(new ArrayAdapter<String> (this, R.layout.word, suggestedWords));
	    }
	         
	    //tss code here
	  //returned from TTS data check
	    if (requestCode == MY_DATA_CHECK_CODE) 
	    {  
	        //we have the data - create a TTS instance
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)  
	            repeatTTS = new TextToSpeech(this, this);  
	        //data not installed, prompt the user to install it  
	        else
	        {  
	            //intent will take user to TTS download page in Google Play
	            Intent installTTSIntent = new Intent();  
	            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);  
	            startActivity(installTTSIntent);  
	        }  
	    }
	 
	    //call superclass method
	    super.onActivityResult(requestCode, resultCode, data);
	}*/
}
