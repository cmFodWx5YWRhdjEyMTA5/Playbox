package uk.co.darkerwaters.noteinvaders;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;

import java.util.List;

import uk.co.darkerwaters.noteinvaders.drawing.PaletteBitmap;
import uk.co.darkerwaters.noteinvaders.drawing.PaletteBitmapTranscoder;

public abstract class SelectableItemActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView title;
    private TextView subtitle;
    private SelectableItemAdapter adapter;

    protected abstract List<? extends SelectableItem> getItemList();
    protected abstract int getContentViewRes();
    protected abstract int getTitleImageRes();
    protected abstract int getSpan();
    public abstract void onSelectableItemClicked(SelectableItem item);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(this.getContentViewRes());

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCollapsingToolbar();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        title = (TextView) findViewById(R.id.instrument_title);
        subtitle = (TextView) findViewById(R.id.instrument_subtitle);

        // create the list of things to select
        adapter = new SelectableItemAdapter(this, this.getItemList());

        int span = getSpan();

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, span);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(span, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // load in the image to the view and set the text to show up over this
        final int titleImageId = getTitleImageRes();
        PaletteBitmap.loadImageIntoView(this,
                (ImageView) findViewById(R.id.backdrop),
                titleImageId,
                new PaletteBitmap.PaletteBitmapImageLoader() {
                    @Override
                    public void onImageLoaded(PaletteBitmap resource) {
                        // Load default colors
                        int backgroundColor = ContextCompat.getColor(SelectableItemActivity.this,
                                android.R.color.background_dark);
                        int titleColor = title.getCurrentTextColor();
                        int subtitleColor = title.getCurrentTextColor();

                        Palette.Swatch swatch = PaletteBitmap.getBestSwatch(resource.palette);
                        if (null != swatch) {
                            backgroundColor = ColorUtils.setAlphaComponent(swatch.getRgb(), 180);
                            titleColor = swatch.getTitleTextColor();
                            subtitleColor = swatch.getBodyTextColor();
                        }

                        // Set the title background and text colors
                        if (null != title) {
                            title.setBackgroundColor(backgroundColor);
                            title.setTextColor(titleColor);
                        }
                        if (null != subtitle) {
                            subtitle.setBackgroundColor(backgroundColor);
                            subtitle.setTextColor(subtitleColor);
                        }
                    }
                });

        // update all our cards, the data may have changed
        this.adapter.notifyDataSetChanged();
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
