package uk.co.darkerwaters.scorepal.activities.animation;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;

public class GameOverTextAnimation extends TextViewAnimation {

    private static final long K_FADE_IN_DURATION = 500;
    private static final long K_FADE_OUT_DURATION = 1000;
    private static final float K_ENLARGED_SIZE = 1.2f;

    private Animation activeAnimation;

    public GameOverTextAnimation(Activity context, TextView view) {
        super(context, view, -1);
        // set the text content on the view
        setAnimatedText(context.getString(R.string.match_over));

        // no active animation yet
        this.activeAnimation = null;

        // animate this in
        animateTextIn();
    }

    @Override
    protected synchronized void animateTextIn() {
        if (false == this.isCancel) {
            // scale this from size to a little larger...
            this.activeAnimation = new ScaleAnimation(
                    1f, K_ENLARGED_SIZE, // Start and end values for the X axis scaling
                    1f, K_ENLARGED_SIZE, // Start and end values for the Y axis scaling
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
                    K_ENLARGED_SIZE, 1f, // Start and end values for the X axis scaling
                    K_ENLARGED_SIZE, 1f, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling

            this.activeAnimation.setDuration(K_FADE_OUT_DURATION);
            this.activeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // fine, starting
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    // need to do it all again
                    animateTextIn();
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
