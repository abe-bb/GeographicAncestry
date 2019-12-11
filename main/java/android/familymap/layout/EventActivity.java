package android.familymap.layout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.familymap.R;
import android.os.Bundle;
import android.view.MenuItem;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.event_fragment_container);

        if (fragment == null) {
            Intent intent = getIntent();
            String eventID = intent.getStringExtra("eventID");
            fragment = new MapFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("hasOptionsMenu", false);
            bundle.putString("selectedEventID", eventID);
            fragment.setArguments(bundle);

            fragmentManager.beginTransaction()
                    .add(R.id.event_fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent returnToMain = new Intent(this, MainActivity.class);
            returnToMain.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(returnToMain);
            return true;
        }

        return false;
    }
}
