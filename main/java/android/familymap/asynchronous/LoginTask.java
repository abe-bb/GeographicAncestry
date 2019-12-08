package android.familymap.asynchronous;

import android.familymap.data.DataCache;
import android.familymap.data.ServerProxy;
import android.familymap.data.ServerAccessError;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import netabs.AuthResult;
import netabs.UserLoginRequest;

public class LoginTask extends AsyncTask<UserLoginRequest, Void, ServerAccessError> {

    private final List<AuthTaskListener> listeners = new ArrayList<>();

    @Override
    protected ServerAccessError doInBackground(UserLoginRequest... userLoginRequests) {
        ServerProxy server = ServerProxy.getInstance();

        try {
            AuthResult result = server.loginUser(userLoginRequests[0]);
            DataCache.getInstance().setAuthToken(result.getAuthToken());
            server.fillDataCache();
            return null;
        }
        catch(ServerAccessError e) {
            return e;
        }
    }


    @Override
    protected void onPostExecute(ServerAccessError serverAccessError) {
        for (AuthTaskListener listener : listeners) {
            listener.taskComplete(serverAccessError);
        }
    }

    public void registerListener(AuthTaskListener listner) {
        listeners.add(listner);
    }
}
