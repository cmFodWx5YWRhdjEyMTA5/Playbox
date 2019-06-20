package uk.co.darkerwaters.scorepal.activities.animation;

import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;

import java.util.logging.Handler;

public class EndSwappingAnimator {

    private static final long K_TRANSITION_TIME = 3000;
    private static final long K_RESET_TIME = 500;

    private final Scene startScene;
    private final Scene endScene;
    private final Transition animatorTarget;
    private final Transition animatorReset;

    private int transitionCount;

    public EndSwappingAnimator(Scene start, Scene end, int count) {
        this.startScene = start;
        this.endScene = end;
        this.transitionCount = count;

        // create the animator
        this.animatorTarget = new ChangeBounds();
        this.animatorTarget.setDuration(K_TRANSITION_TIME);
        this.animatorReset = new ChangeBounds();
        this.animatorReset.setDuration(K_RESET_TIME);

        this.animatorTarget.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                // put it back
                resetTransition();
            }
            @Override
            public void onTransitionCancel(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionPause(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionResume(@NonNull Transition transition) {
            }
        });
        this.animatorReset.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                // put it back
                performTransition();
            }
            @Override
            public void onTransitionCancel(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionPause(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionResume(@NonNull Transition transition) {
            }
        });

        performTransition();
    }

    private void performTransition() {
        if (this.transitionCount-- > 0) {
            // start the transition
            TransitionManager.go(this.endScene, this.animatorTarget);
        }
    }

    private void resetTransition() {
        TransitionManager.go(this.startScene, this.animatorReset);
    }
}
