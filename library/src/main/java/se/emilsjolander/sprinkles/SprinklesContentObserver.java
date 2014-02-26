package se.emilsjolander.sprinkles;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by zsiegel on 2/25/14.
 */
public class SprinklesContentObserver extends ContentObserver {

    private Account mAccount;
    private String mAuthority;

    public SprinklesContentObserver(Account account, String authority) {
        super(null);
        this.mAccount = account;
        this.mAuthority = authority;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        sync();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
    }

    private void sync() {
        if (mAccount != null) {
            Bundle extras = new Bundle();
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
            ContentResolver.requestSync(mAccount, mAuthority, extras);
        } else {
            Log.e("Sprinkles", "ContentObserver has a null account");
        }
    }
}
