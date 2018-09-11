package uk.co.darkerwaters.noteinvaders;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.instruments.Instrument;
import uk.co.darkerwaters.noteinvaders.instruments.InstrumentList;

public class InstrumentActivity extends SelectableItemActivity {

    @Override
    protected int getContentViewRes() {
        return R.layout.activity_instrument;
    }

    @Override
    protected List<Instrument> getItemList() {
        return InstrumentList.GET().toList();
    }

    @Override
    protected int getTitleImageRes() {
        return R.drawable.instruments;
    }

    @Override
    public void onSelectableItemClicked(SelectableItem item) {
        // start the selection activity
        Intent myIntent = new Intent(this, InputActivity.class);
        myIntent.putExtra("instrument", item.getName()); //Optional parameters
        this.startActivity(myIntent);
    }

    @Override
    protected int getSpan() {
        return 2;
    }
}