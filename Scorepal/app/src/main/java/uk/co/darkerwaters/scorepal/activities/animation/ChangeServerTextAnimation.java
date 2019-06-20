package uk.co.darkerwaters.scorepal.activities.animation;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;

public class ChangeServerTextAnimation extends TextViewAnimation {

    private static final long K_FADE_IN_DURATION = 1500;
    private static final int K_REPETITIONS = 2;

    private Animation activeAnimation;

    public ChangeServerTextAnimation(Activity context, TextView view) {
        super(context, view, K_REPETITIONS);
        // set the text content on the view
        setAnimatedText(context.getString(R.string.change_server));

        // no active animation yet
        this.activeAnimation = null;

        // animate this in
        animateTextIn();
    }

    @Override
    protected synchronized void animateTextIn() {
        if (false == this.isCancel) {
            // scale this from nothing up to size
            this.activeAnimation = new ScaleAnimation(
                    1f, 1f, // Start and end values for the X axis scaling
                    0f, 1f, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling

            this.activeAnimation.setDuration(K_FADE_IN_DURATION);
            this.activeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // fine, starting, show the view
                    view.setVisibility(View.VISIBLE);
                    view.setTranslationX(0f);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // we animated in, now we need to slide out
                    animateTextOut();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            this.view.startAnimation(this.activeAnimation);
        }
    }

    @Override
    protected synchronized void animateTextOut() {
        if (false == isCancel) {
            // scale this back down to nothing
            this.activeAnimation = new ScaleAnimation(
                    1f, 1f, // Start and end values for the X axis scaling
                    1f, 0f, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling

            this.activeAnimation.setDuration(K_FADE_IN_DURATION);
            this.activeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // fine, starting, show the view
                    view.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    // need to do it all again
                    if (--repetitions > 0) {
                        animateTextIn();
                    } else {
                        // all done
                        activeAnimation = null;
                        cancel();
                    }
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            // and start the animation
            this.view.startAnimation(this.activeAnimation);
        }
    }

    @Override
    public synchronized void cancel() {
        super.cancel();
        // and clear / cancel the animation
        if (null != this.activeAnimation) {
            this.activeAnimation.cancel();
            this.activeAnimation = null;
        }
        // and reset the view attributes we fiddled with
        view.setScaleY(1f);
        view.setVisibility(View.INVISIBLE);
    }
}
