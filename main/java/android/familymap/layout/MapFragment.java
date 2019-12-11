package android.familymap.layout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.familymap.data.DataCache;
import android.familymap.R;
import android.os.Bundle;
import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import model.EventModel;
import model.PersonModel;

//TODO: implement shared preferences update listener to redraw map when preferences change

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap map;
    private ArrayList<Polyline> lines = new ArrayList<>();

    private EventModel selectedEvent;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, container,  false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle bundle = getArguments();
        Boolean hasOptionsMenu = true;
        if (bundle != null) {
            hasOptionsMenu = bundle.getBoolean("hasOptionsMenu", true);
            String selectedEventID = bundle.getString("selectedEventID", null);
            setSelectedEvent(selectedEventID);
        };


        TextView markerInfo = view.findViewById(R.id.marker_info);
        markerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PersonActivity.class);
                intent.putExtra("personID", selectedEvent.getPersonID());
                startActivity(intent);
            }
        });


        setHasOptionsMenu(hasOptionsMenu);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (map != null) {
            map.clear();
            lines = new ArrayList<>();


            drawMapMarkers();
            DataCache cache = DataCache.getInstance();

            if (selectedEvent == null || cache.getPersonEvents(selectedEvent.getPersonID()) == null) {
                selectedEvent = null;
                TextView textView = getView().findViewById(R.id.marker_info);
                textView.setText(getContext().getResources().getString(R.string.marker_info_title));
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_grey_48dp, 0, 0, 0);
            }
            else {
                map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude())));
                drawMapLines(selectedEvent, cache.getPersonByID(selectedEvent.getPersonID()));
            }
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        drawMapMarkers();


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                EventModel event = (EventModel) marker.getTag();
                PersonModel person = DataCache.getInstance().getPersonByID(event.getPersonID());

                LatLng position = new LatLng(event.getLatitude(), event.getLongitude());
                MapFragment.this.map.animateCamera(CameraUpdateFactory.newLatLng(position));

                TextView infoWindow = getView().findViewById(R.id.marker_info);

                if (event == null || person == null) {
                    infoWindow.setText(R.string.marker_error_missing_info);
                    return true;
                }


                infoWindow.setText(String.format("%s %s\n%s: %s, %s (%d)",
                        person.getFirstName(), person.getLastName(), event.getEventType().toUpperCase(),
                        event.getCity(), event.getCountry(), event.getYear()));

                if (person.getGender().equals("f")) {
                    infoWindow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_pink_48dp, 0, 0, 0);
                }
                else if (person.getGender().equals("m")) {
                    infoWindow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_blue_48dp, 0, 0, 0);
                }
                else {
                    infoWindow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_grey_48dp, 0, 0, 0);
                }

                drawMapLines(event, person);

                selectedEvent = event;

                return true;
            }
        });


        DataCache cache = DataCache.getInstance();

        if (selectedEvent != null) {

            PersonModel person = cache.getPersonByID(selectedEvent.getPersonID());

            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude())));
            drawMapLines(selectedEvent, person);

            TextView infoWindow = getView().findViewById(R.id.marker_info);

            infoWindow.setText(String.format("%s %s\n%s: %s, %s (%d)",
                    person.getFirstName(), person.getLastName(), selectedEvent.getEventType().toUpperCase(),
                    selectedEvent.getCity(), selectedEvent.getCountry(), selectedEvent.getYear()));

            if (person.getGender().equals("f")) {
                infoWindow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_pink_48dp, 0, 0, 0);
            }
            else if (person.getGender().equals("m")) {
                infoWindow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_blue_48dp, 0, 0, 0);
            }
            else {
                infoWindow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_grey_48dp, 0, 0, 0);
            }
        }


    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_activity_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.search_icon:
                Toast.makeText(getContext(), "You clicked the search button", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings_icon:
                Intent openSettings = new Intent(getContext(), SettingsActivity.class);
                startActivity(openSettings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void drawMapMarkers() {
        DataCache cache = DataCache.getInstance();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());


        for (LinkedList<EventModel> personEvents : cache.getEvents(preferences)) {
            for (EventModel event : personEvents) {
                Marker marker = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(cache.getColor(event.getEventType())))
                        .position(new LatLng(event.getLatitude(), event.getLongitude())));

                marker.setTag(event);
            }
        }
    }

    private void drawMapLines(EventModel event, PersonModel person) {
//        clear the old lines from the map
        for (Polyline line : lines) {
            line.remove();
        }
        lines.clear();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (preferences.getBoolean("life_story_switch", true)) {
            drawLifeStoryLines(person, event);
        }
        if (preferences.getBoolean("family_tree_switch", true)) {
            drawFamilyTreeLines(person, event);
        }
        if (preferences.getBoolean("spouse_switch", true)) {
            drawSpouseLines(person, event);
        }

    }

    private void drawLifeStoryLines(PersonModel person, EventModel selectedEvent) {
        DataCache cache = DataCache.getInstance();

        ArrayList<PolylineOptions> lines = cache.getLifeEventLines(person, selectedEvent);

        Context context = getContext();

        for (PolylineOptions line : lines) {
            line.color(context.getResources().getColor(R.color.colroLifeStoryLines));
            this.lines.add(map.addPolyline(line));
        }


    }

    private void drawFamilyTreeLines(PersonModel personModel, EventModel selectedEvent) {
        DataCache cache = DataCache.getInstance();

        ArrayList<PolylineOptions> lines = cache.getFamilyTreeLines(personModel, selectedEvent);

        Context context = getContext();

        for (PolylineOptions line : lines) {
            line.color(context.getResources().getColor(R.color.colorFamilyTreeLines));
            this.lines.add(map.addPolyline(line));
        }

    }

    private void drawSpouseLines(PersonModel personModel, EventModel selectedEvent) {
        if (personModel.getSpouseID() == null) {
            return;
        }

        Context context = getContext();

        DataCache cache = DataCache.getInstance();

        PolylineOptions line = cache.getSpouseLine(personModel, selectedEvent);

        if (line != null) {
            line.color(context.getResources().getColor(R.color.colorSpouseLines));
            this.lines.add(map.addPolyline(line));
        }
    }

    private void setSelectedEvent(String eventID) {
        if (eventID == null) {
            return;
        }

        DataCache cache = DataCache.getInstance();

        selectedEvent = cache.getEventByID(eventID);
    }

}
