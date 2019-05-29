package uk.co.darkerwaters.staveinvaders.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.activities.handlers.AttributionRecyclerAdapter;

public class AttributionsActivity extends AppCompatActivity {

    private RecyclerView attributionsView;
    private AttributionRecyclerAdapter adapter;
    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attributions);

        this.application = (Application) this.getApplication();

        this.attributionsView = findViewById(R.id.attributionsView);

        this.adapter = new AttributionRecyclerAdapter(this.application, this);
        // setup the list
        this.attributionsView.setLayoutManager(new GridLayoutManager(this, 1));
        this.attributionsView.setAdapter(this.adapter);
    }
}
