package com.yoshione.fingen.utils;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FabMenuController {
    private static final int ANIM_DURATION = 100;

    private FloatingActionButton mRoot;
    private List<View> mChildren;
    private List<Integer> mVisibility;
    private ValueAnimator mAnimator;
    private View mFabBGLayout;
    private Activity mActivity;
    private boolean isFABOpen = false;

    public FabMenuController(FloatingActionButton root, View fabBGLayout, Activity activity, View... children) {
        mRoot = root;
        mFabBGLayout = fabBGLayout;
        mActivity = activity;

        mChildren = new ArrayList<>(Arrays.asList(children));
        mVisibility = new ArrayList<>(mChildren.size());
        for (int i = 0; i < mChildren.size(); i++) {
            mVisibility.add(i, View.VISIBLE);
        }

        mAnimator = ValueAnimator.ofFloat(0f, 0.90f);
        mAnimator.addUpdateListener(valueAnimator -> mFabBGLayout.setAlpha((Float) valueAnimator.getAnimatedValue()));
        mAnimator.setDuration(ANIM_DURATION);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setRepeatCount(0);

        fabBGLayout.setOnClickListener(view -> closeFABMenu());
        mRoot.setOnClickListener(view -> {
            if(!isFABOpen){
                showFABMenu();
            }else{
                closeFABMenu();
            }
        });
    }

    public void showFABMenu() {
        isFABOpen = true;
        for (int i = 0; i < mChildren.size(); i++) {
            mChildren.get(i).setVisibility(mVisibility.get(i));
        }

        mRoot.animate().setDuration(ANIM_DURATION).rotationBy(-180);
        int offset = 75;
        int step = 55;
        for (int i = 0, k = 0; i < mChildren.size(); i++) {
            View v = mChildren.get(i);
            if (v.getVisibility() == View.VISIBLE)
                mChildren.get(i).animate().setDuration(ANIM_DURATION).translationY(-ScreenUtils.dpToPx(offset + step * k++, mActivity));
        }

        mFabBGLayout.setAlpha(0);
        mFabBGLayout.setVisibility(View.VISIBLE);
        mAnimator.start();
    }

    public void closeFABMenu() {
        isFABOpen = false;
        mFabBGLayout.setVisibility(View.GONE);
        mRoot.animate().setDuration(ANIM_DURATION).rotationBy(180);

        for (View v : mChildren) {
            if (v.equals(mChildren.get(mChildren.size() - 1))) {
                v.animate().setDuration(ANIM_DURATION).translationY(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {}

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (!isFABOpen) {
                            for (View v : mChildren) {
                                v.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {}

                    @Override
                    public void onAnimationRepeat(Animator animator) {}
                });
            } else {
                v.animate().setDuration(ANIM_DURATION).translationY(0);
            }
        }
    }

    public void forceCloseFABMenu() {
        isFABOpen = false;
        mFabBGLayout.setVisibility(View.GONE);
        mRoot.setRotation(0);
        for (View v : mChildren) {
            v.setVisibility(View.GONE);
        }
    }

    public boolean isFABOpen() {
        return isFABOpen;
    }

    public void setViewVisibility(View view, int visibility) {
        if (mChildren.contains(view))
            mVisibility.set(mChildren.indexOf(view), visibility);
    }

}
