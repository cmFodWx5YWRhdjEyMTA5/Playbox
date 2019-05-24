package uk.co.darkerwaters.staveinvaders.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.actvities.GamePlayActivity;

public class SlideInOutAnimator {

    private final static long K_PAUSE_DELAY = 500;

    private final View view;

    public SlideInOutAnimator(View view) {
        this.view = view;
        view.setVisibility(View.GONE);
    }

    public void slideIn() {
        slideIn(null);
    }

    public void slideIn(final Runnable onCompleted) {
        // perform the animation in
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);

        if (view.getHeight() > 0) {
            slideInNow(onCompleted);
        } else {
            // wait till height is measured
            view.post(new Runnable() {
                @Override
                public void run() {
                    slideInNow(onCompleted);
                }
            });
        }
    }

    private void slideInNow(final Runnable onCompleted) {
        view.setTranslationX(-view.getWidth());
        view.animate()
                .translationX(0)
                .alpha(1f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                slideOut(onCompleted);
                            }
                        }, K_PAUSE_DELAY);
                    }
                });
    }

    private void slideOut(final Runnable onCompleted) {
        // perform the animation out
        this.view.animate()
                .translationX(view.getWidth())
                .alpha(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // superfluous restoration
                        view.setVisibility(View.GONE);
                        view.setAlpha(1f);
                        view.setTranslationY(0f);
                        if (null != onCompleted) {
                            onCompleted.run();
                        }
                    }
                });
    }
}
