package android.familymap.layout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.familymap.asynchronous.AuthTaskListener;
import android.familymap.R;
import android.familymap.data.ServerAccessError;
import android.os.Bundle;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements AuthTaskListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.main_fragment_container);

        if (fragment == null) {
            fragment = new LoginFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void taskComplete(ServerAccessError possibleError) {
        if (possibleError == null) {
            swapInMapFragment();
        }
        else {
            Toast.makeText(getApplicationContext(), possibleError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void swapInMapFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        MapFragment mapFragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("hasOptionMenu", true);
        mapFragment.setArguments(bundle);
        transaction.replace(R.id.main_fragment_container, mapFragment);
        transaction.commit();
    }
}
