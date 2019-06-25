package uk.co.darkerwaters.scorepal.activities;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.Application;

public class BaseActivity extends AppCompatActivity {

    protected Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup our pointers to each other so we an short-cut about the app
        this.application = (Application)getApplication();
        // set this on the application
        this.application.setActiveActivity(this);

    }

    @Override
    protected void onDestroy() {
        // tell the application this
        this.application.activityDestroyed(this);
        // and destroy us
        super.onDestroy();
    }

    public static void SetIconTint(ImageButton button, int tint) {
        DrawableCompat.setTint(button.getDrawable(), tint);
    }

    public static void SetIconTint(View button, int tint) {
        if (button instanceof Button) {
            SetIconTint((Button) button, tint);
        }
        else if (button instanceof ImageButton) {
            SetIconTint((ImageButton) button, tint);
        }
    }

    public static void SetIconTint(Button button, int tint) {
        for (Drawable icon : button.getCompoundDrawables()) {
            if (null != icon) {
                DrawableCompat.setTint(icon, tint);
            }
        }
    }

    protected void setIconTint(ImageButton button, int tint) {
        BaseActivity.SetIconTint(button, tint);
    }

    protected void setIconTint(Button button, int tint) {
        BaseActivity.SetIconTint(button, tint);
    }

    public static void SetTextViewBold(TextView textView) {
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
    }

    public static void SetTextViewNoBold(TextView textView) {
        textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
    }
}
