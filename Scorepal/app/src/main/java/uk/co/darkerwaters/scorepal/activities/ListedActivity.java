package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class ListedActivity extends BaseActivity {

    protected GridLayoutManager layoutManager;
    protected RecyclerView recyclerView;
    protected RecyclerView.Adapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupRecyclerView(int viewId, RecyclerView.Adapter adapter) {
        this.recyclerView = findViewById(viewId);
        this.recyclerViewAdapter = adapter;
        int span = 1;
        if (application.getDisplaySize(this).getWidth() >= 550f) {
            span = 2;
        }
        layoutManager = new GridLayoutManager(this, span);
        if (null != this.recyclerView) {
            this.recyclerView.setLayoutManager(layoutManager);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != this.recyclerView) {
            // update the cards from the possible settings change
            this.recyclerView.setAdapter(this.recyclerViewAdapter);
        }
    }
}
