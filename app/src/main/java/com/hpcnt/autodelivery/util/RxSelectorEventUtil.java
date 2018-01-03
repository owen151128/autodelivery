package com.hpcnt.autodelivery.util;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class RxSelectorEventUtil {
    private static RxSelectorEventUtil instance;
    private BehaviorSubject<Integer> selectorSubject;

    private RxSelectorEventUtil() {
        selectorSubject = BehaviorSubject.create();
    }

    public static synchronized RxSelectorEventUtil getInstance() {
        if (instance == null)
            instance = new RxSelectorEventUtil();
        return instance;
    }

    public void sendSelectorEvent(int mode) {
        selectorSubject.onNext(mode);
    }

    public void close() {
        selectorSubject.onComplete();
    }

    public Observable<Integer> receiveSelectorEvent() {
        return selectorSubject;
    }
}
