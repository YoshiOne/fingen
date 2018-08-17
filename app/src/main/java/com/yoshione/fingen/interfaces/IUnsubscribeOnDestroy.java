package com.yoshione.fingen.interfaces;

import io.reactivex.disposables.Disposable;

public interface IUnsubscribeOnDestroy {
    void unsubscribeOnDestroy(Disposable disposable);
}
