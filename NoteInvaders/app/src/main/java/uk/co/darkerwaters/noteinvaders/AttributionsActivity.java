package uk.co.darkerwaters.noteinvaders;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.state.State;

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
        // find all the attribution files
        try {
            for (String file : getAssets().list("attributions")) {
                addView(mListViews, readFile("attributions/" + file));
            }
        }
        catch (IOException e) {
            Log.e(State.K_APPTAG, "failed to find attribution files: " + e.getMessage());
        }

        vpArticle.setAdapter(myAdapter);
    }

    private String readFile(String attributeFile) {
        StringBuilder fileContent = new StringBuilder();
        try
        {
            InputStream instream = getAssets().open(attributeFile);
            if (instream != null)
            {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                while ((line = buffreader.readLine()) != null) {
                    fileContent.append(line);
                }
                buffreader.close();
                inputreader.close();
                instream.close();
            }
        }
        catch (Exception e) {
            Log.e(State.K_APPTAG, "error reading file: " + e.getMessage());
        }
        return fileContent.toString();
    }

    private void addView(List<View> viewList, String content)
    {
        WebView webView=new WebView(this);
        webView.loadData(content, "text/html", "UTF-8");
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
