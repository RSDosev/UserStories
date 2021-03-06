package com.radoslavdosev.userstories.base.mvp;

/**
 * Every presenter in the app must either implement this interface or extend BasePresenter
 * indicating the MvpView type that wants to be attached with.
 */
public interface Presenter<V extends MvpView> {

    void attachView(final V mvpView);

    void detachView();
}
