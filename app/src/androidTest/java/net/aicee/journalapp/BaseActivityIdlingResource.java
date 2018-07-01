package net.aicee.journalapp;



import android.app.ProgressDialog;
import android.support.test.espresso.IdlingResource;

/**
 * Monitor Activity idle status by watching ProgressDialog.
 */
public class BaseActivityIdlingResource implements IdlingResource {

    private BaseActivity baseActivity;
    private ResourceCallback resourceCallback;

    public BaseActivityIdlingResource(BaseActivity activity) {
        baseActivity = activity;
    }

    @Override
    public String getName() {
        return "BaseActivityIdlingResource:" + baseActivity.getLocalClassName();
    }

    @Override
    public boolean isIdleNow() {
        ProgressDialog dialog = baseActivity.progressDialog;
        boolean idle = (dialog == null || !dialog.isShowing());

        if (resourceCallback != null && idle) {
            resourceCallback.onTransitionToIdle();
        }

        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        resourceCallback = callback;
    }
}
