package android.familymap.data;

import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import model.AuthTokenModel;
import model.EventModel;
import model.PersonModel;

public class DataCache {
    private final float  BASE_LINE_WIDTH = 15;
    private static DataCache sDataCache;

    AuthTokenModel mAuthToken;

    FamilyTree mFamilyTree;

    HashMap<String, ArrayList<EventModel>> mEventsMap;
    HashMap<String, ArrayList<EventModel>> mFilteredEventsMap;

    public static DataCache getInstance() {
        if (sDataCache == null) {
            sDataCache = new DataCache();
        }

        return sDataCache;
    }

    private DataCache() {}

    public AuthTokenModel getAuthToken() {
        return mAuthToken;
    }

    public void setAuthToken(AuthTokenModel authToken) {
        mAuthToken = authToken;
    }


    public void buildCache(PersonModel user, PersonModel[] persons, EventModel[] events) {
        mFamilyTree = new FamilyTree(user);
        mFamilyTree.fillTree(persons);
        buildEventsMap(events);
    }


    public Collection<ArrayList<EventModel>> getEvents(SharedPreferences preferences) {
        ArrayList<PersonModel> persons = mFamilyTree.buildFilteredPersonList(preferences);
        buildFilteredEventsMap(persons);

        return mFilteredEventsMap.values();
    }

    public PersonModel getPersonByID(String personID) {
        return mFamilyTree.getPersonByID(personID);
    }

    private void buildEventsMap(EventModel[] events) {
        if (mEventsMap == null) {
            mEventsMap = new HashMap<>();
        }

        for (EventModel event : events) {
            String personID = event.getPersonID();

            ArrayList<EventModel> personEvents = mEventsMap.get(personID);
            if (personEvents == null) {
                personEvents = new ArrayList<>();
                mEventsMap.put(personID, personEvents);
            }

            personEvents.add(event);

        }
    }

    private void buildFilteredEventsMap(ArrayList<PersonModel> persons) {
        mFilteredEventsMap = new HashMap<>();

        for (PersonModel person : persons) {
            String personID = person.getPersonID();

            mFilteredEventsMap.put(personID, mEventsMap.get(personID));
        }
    }

    public ArrayList<PolylineOptions> getFamilyTreeLines(PersonModel person, EventModel selectedEvent) {
        return mFamilyTree.buildFamilyTreeLines(person.getPersonID(), selectedEvent, mFilteredEventsMap, BASE_LINE_WIDTH);
    }

    public PolylineOptions getSpouseLine(PersonModel person, EventModel selectedEvent) {
        if (person == null || person.getSpouseID() == null) {
            return null;
        }

        ArrayList<EventModel> events = mFilteredEventsMap.get(person.getSpouseID());
        EventModel personEvent = getBirthOrFirstEvent(person.getPersonID(), events);

        if (selectedEvent == null || personEvent == null) {
            return null;
        }

        return new PolylineOptions().add(new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude()))
                .add(new LatLng(personEvent.getLatitude(), personEvent.getLongitude()))
                .width(BASE_LINE_WIDTH);


    }

    public ArrayList<PolylineOptions> getLifeEventLines(PersonModel person, EventModel selectedEvent) {
        ArrayList<PolylineOptions> lines = new ArrayList<>();
        if (person == null) {
            return lines;
        }

        ArrayList<EventModel> events = mFilteredEventsMap.get(person.getPersonID());
        if (events == null) {
            return lines;
        }

        for (EventModel event : events) {
            lines.add(new PolylineOptions().add(new LatLng(event.getLatitude(), event.getLongitude()))
                    .add(new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude()))
                    .width(BASE_LINE_WIDTH));
        }

        return lines;
    }

    public ArrayList<EventModel> getPersonEvents(String personID) {
        return mFilteredEventsMap.get(personID);
    }

    static public EventModel getBirthOrFirstEvent(String personID, ArrayList<EventModel> personEvents) {
        if (personEvents == null || personEvents.isEmpty()) {
            return null;
        }

        EventModel eventToReturn = personEvents.get(0);

        for (EventModel event : personEvents) {
            if (event.getEventType().toLowerCase().equals("birth")) {
                return event;
            }
            else if (event.getYear() < eventToReturn.getYear()) {
                eventToReturn = event;
            }
        }
        return eventToReturn;
    }

}
