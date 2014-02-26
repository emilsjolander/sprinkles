package se.emilsjolander.sprinkles.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthService extends Service {

    private AbstractAccountAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        authenticator = new SprinklesSampleAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
