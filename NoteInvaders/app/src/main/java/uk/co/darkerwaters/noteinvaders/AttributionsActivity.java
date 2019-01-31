package uk.co.darkerwaters.noteinvaders;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

public class AttributionsActivity extends AppCompatActivity {

    private ViewPager vpArticle;
    private MyPagerAdapter myAdapter;
    private List<View> mListViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_attributions);

        myAdapter = new MyPagerAdapter();
        vpArticle = (ViewPager) findViewById(R.id.web_view_pager);

        mListViews = new ArrayList<View>();
        addView(mListViews, "attributions/bass_clef.html");
        addView(mListViews, "http://www.google.com");

        vpArticle.setAdapter(myAdapter);
    }

    private void addView(List<View> viewList, String url)
    {
        WebView webView=new WebView(this);
        webView.loadUrl(url);
        viewList.add(webView);
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }
    }
}
