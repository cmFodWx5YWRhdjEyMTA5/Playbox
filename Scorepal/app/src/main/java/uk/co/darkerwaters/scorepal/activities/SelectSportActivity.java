package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.GameRecyclerAdapter;
import uk.co.darkerwaters.scorepal.activities.handlers.SportRecyclerAdapter;

public class SelectSportActivity extends ListedActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sport);

        setupRecyclerView(R.id.recyclerView, new SportRecyclerAdapter(application, this));
    }
}
