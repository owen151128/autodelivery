package com.hpcnt.autodelivery;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.trello.rxlifecycle2.LifecycleTransformer;

public interface LifeCycleProvider {

    @NonNull
    @CheckResult
    <T> LifecycleTransformer<T> bindToLifecycle();
}
