package android.familymap.asynchronous;

import android.familymap.data.DataCache;
import android.familymap.data.ServerProxy;
import android.familymap.data.ServerAccessError;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import netabs.AuthResult;
import netabs.UserRegisterRequest;

public class RegisterTask extends AsyncTask<UserRegisterRequest, Void, ServerAccessError> {
    private final List<AuthTaskListener> listeners = new ArrayList<>();




    @Override
    protected ServerAccessError doInBackground(UserRegisterRequest... userRegisterRequests) {
        ServerProxy server = ServerProxy.getInstance();

        try {
            AuthResult result = server.registerUser(userRegisterRequests[0]);
            DataCache.getInstance().setAuthToken(result.getAuthToken());
            server.fillDataCache();
            return null;
        }
        catch (ServerAccessError e) {
            return e;
        }
    }

    @Override
    protected void onPostExecute(ServerAccessError serverAccessError) {
        for (AuthTaskListener listener : listeners) {
            listener.taskComplete(serverAccessError);
        }
    }

    public void registerListener(AuthTaskListener listener) {
        listeners.add(listener);
    }
}
