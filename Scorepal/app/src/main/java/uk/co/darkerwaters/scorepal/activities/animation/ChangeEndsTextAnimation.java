package uk.co.darkerwaters.scorepal.activities.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;

public class ChangeEndsTextAnimation extends TextViewAnimation {

    private static final long K_FADE_IN_DURATION = 1500;
    private static final long K_SLIDE_OUT_DURATION = 1500;
    private static final int K_REPETITIONS = 3;

    private Animation activeInAnimation;
    private ObjectAnimator activeOutAnimation;

    public ChangeEndsTextAnimation(Activity context, TextView view) {
        // set the content of the text view
        super(context, view, K_REPETITIONS);
        // set our text here
        setAnimatedText(context.getString(R.string.change_ends));

        // no active animation yet
        this.activeInAnimation = null;
        this.activeOutAnimation = null;

        // animate this in
        animateTextIn();
    }

    @Override
    protected synchronized void animateTextIn() {
        if (!this.isCancel) {
            // scale this from nothing up to size
            this.activeInAnimation = new ScaleAnimation(
                    1f, 1f, // Start and end values for the X axis scaling
                    0f, 1f, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
            // Needed to keep the result of the animation
            this.activeInAnimation.setFillAfter(true);
            this.activeInAnimation.setDuration(K_FADE_IN_DURATION);
            this.activeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // fine, starting, show the view
                    view.setVisibility(View.VISIBLE);
                    view.setScaleY(1f);
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
            this.view.startAnimation(this.activeInAnimation);
        }
    }

    @Override
    protected synchronized void animateTextOut() {
        if (!this.isCancel) {
            // slide this view out to one side
            Display display = this.context.getWindowManager().getDefaultDisplay();
            Point windowSize = new Point();
            display.getSize(windowSize);
            // we want to translate this text out
            float movement = (0.5f * windowSize.x) + (view.getWidth() * 0.5f);
            // and go left / right
            movement *= repetitions % 2 == 0 ? -1f : 1f;
            // and create the animator here
            this.activeOutAnimation = ObjectAnimator.ofFloat(this.view, "translationX", movement);
            this.activeOutAnimation.setDuration(K_SLIDE_OUT_DURATION);
            this.activeOutAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    // fine, nothing really
                }
                @Override
                public void onAnimationEnd(Animator animator) {
                    // need to do it all again
                    if (--repetitions > 0) {
                        view.setTranslationX(0f);
                        animateTextIn();
                    } else {
                        // all done
                        activeOutAnimation = null;
                        cancel();
                    }
                }
                @Override
                public void onAnimationCancel(Animator animator) {
                }
                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            this.activeOutAnimation.start();
        }
    }

    @Override
    public synchronized void cancel() {
        super.cancel();
        // and clear / cancel the animations
        if (null != this.activeInAnimation) {
            this.activeInAnimation.cancel();
            this.activeInAnimation = null;
        }
        if (null != this.activeOutAnimation) {
            this.activeOutAnimation.cancel();
            this.activeOutAnimation = null;
        }
        // and reset the view attributes we fiddled with
        view.setScaleY(1f);
        view.setVisibility(View.INVISIBLE);
    }
}
