package uk.co.darkerwaters.noteinvaders;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the selection activity
                Intent myIntent = new Intent(StartActivity.this, InstrumentActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                StartActivity.this.startActivity(myIntent);
            }
        });
    }
}
