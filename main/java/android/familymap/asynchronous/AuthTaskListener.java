package android.familymap.asynchronous;

import android.familymap.data.ServerAccessError;

public interface AuthTaskListener {
    void taskComplete(ServerAccessError possibleError);
}

