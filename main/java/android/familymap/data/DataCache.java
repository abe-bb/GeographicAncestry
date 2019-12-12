package android.familymap.data;

import android.content.SharedPreferences;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import model.AuthTokenModel;
import model.EventModel;
import model.PersonModel;

public class DataCache {
    private final float  BASE_LINE_WIDTH = 18;
    private static DataCache sDataCache;

    private HashMap<String, Float> colorMap = new HashMap<>();
    private float lastAddedColor = 0;

    AuthTokenModel mAuthToken;

    FamilyTree mFamilyTree;
    PersonModel[] people;

    HashMap<String, LinkedList<EventModel>> mEventsMap;
    HashMap<String, LinkedList<EventModel>> mFilteredEventsMap;

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
        people = persons;

        mFamilyTree = new FamilyTree(user);
        mFamilyTree.fillTree(persons);
        if (mEventsMap != null) {
            mEventsMap.clear();
        }
        buildEventsMap(events);
    }


    public Collection<LinkedList<EventModel>> getEvents(SharedPreferences preferences) {
        ArrayList<PersonModel> filteredPeople = mFamilyTree.buildFilteredPersonList(preferences);
        buildFilteredEventsMap(filteredPeople);

        return mFilteredEventsMap.values();
    }

    public PersonModel getPersonByID(String personID) {
        return mFamilyTree.getPersonByID(personID);
    }

    public EventModel getEventByID(String eventID) {
        for (LinkedList<EventModel> events : mFilteredEventsMap.values()) {
            for (EventModel event : events) {
                if (event.getEventID().equals(eventID)) {
                    return event;
                }
            }
        }
        return null;
    }

    private void buildEventsMap(EventModel[] events) {
        if (mEventsMap == null) {
            mEventsMap = new HashMap<>();
        }

        for (EventModel event : events) {
            String personID = event.getPersonID();

            LinkedList<EventModel> personEvents = mEventsMap.get(personID);
            if (personEvents == null) {
                personEvents = new LinkedList<>();
                mEventsMap.put(personID, personEvents);
            }

            addChronologically(event, personEvents);

        }

        for (LinkedList<EventModel> personEvents : mEventsMap.values()) {
            ensureBirthFirst(personEvents);
        }
    }

    private void addChronologically(EventModel event, LinkedList<EventModel> events) {
        boolean added = false;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getYear() > event.getYear()) {
                events.add(i, event);
                added = true;
                break;
            }
            else if (events.get(i).getYear().equals(event.getYear())) {
                if (events.get(i).getEventType().toLowerCase().compareTo(event.getEventType().toLowerCase()) > 0) {
                    events.add(i, event);
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            events.add(event);
        }
    }

    private void buildFilteredEventsMap(ArrayList<PersonModel> filteredPeople) {
        mFilteredEventsMap = new HashMap<>();

        for (PersonModel person : filteredPeople) {
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

        LinkedList<EventModel> events = mFilteredEventsMap.get(person.getSpouseID());
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

        LinkedList<EventModel> events = mFilteredEventsMap.get(person.getPersonID());
        if (events == null) {
            return lines;
        }

        EventModel prev = null;
        for (EventModel event : events) {
            if (prev != null) {
                lines.add(new PolylineOptions().add(new LatLng(prev.getLatitude(), prev.getLongitude()))
                        .add(new LatLng(event.getLatitude(), event.getLongitude()))
                        .width(BASE_LINE_WIDTH));
            }
            prev = event;
        }

        return lines;
    }

    public LinkedList<EventModel> getPersonEvents(String personID) {
        return mFilteredEventsMap.get(personID);
    }

    static public EventModel getBirthOrFirstEvent(String personID, LinkedList<EventModel> personEvents) {
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

    public LinkedList<LifeEvent> getLifeEvents(String personID) {
        LinkedList<LifeEvent> lifeEvents = new LinkedList<>();
        LinkedList<EventModel> events = mFilteredEventsMap.get(personID);

//        Return empty list if no Life events were found
        if (events == null) {
            return lifeEvents;
        }

        for (EventModel event : events) {
            LifeEvent lifeEvent = buildLifeEvent(event);
            lifeEvents.add(lifeEvent);

        }
        return lifeEvents;
    }


    public LinkedList<FamilyMember> getDirectFamilyMembers(String personID) {
        LinkedList<FamilyMember> familyMembers = mFamilyTree.buildDirectFamilyList(personID);
        if (familyMembers == null) {
            familyMembers = new LinkedList<>();
        }

        return familyMembers;
    }

    public Float getColor(String eventType) {
        if (colorMap.containsKey(eventType.toLowerCase())) {
            return colorMap.get(eventType.toLowerCase());
        }
        else {
            lastAddedColor = (lastAddedColor + 61) % 360;
            colorMap.put(eventType, lastAddedColor);
            return lastAddedColor;
        }
    }

    public ArrayList<LifeEvent> searchEvents(String searchString) {
//        TODO: implement this method
        ArrayList<LifeEvent> events = new ArrayList<>();
        String caseInsensitiveQuery = searchString.toLowerCase();

        for (LinkedList<EventModel> eventModels : mFilteredEventsMap.values()) {
            for (EventModel event : eventModels) {
                if (event.getCountry().toLowerCase().contains(caseInsensitiveQuery) ||
                        event.getCity().toLowerCase().contains(caseInsensitiveQuery) ||
                        event.getEventType().toLowerCase().contains(caseInsensitiveQuery) ||
                        event.getYear().toString().contains(caseInsensitiveQuery)) {
                    events.add(buildLifeEvent(event));
                }
            }
        }

        return events;
    }

    public ArrayList<PersonModel> searchPersons(String searchString) {
        ArrayList<PersonModel> persons = new ArrayList<>();
        String caseInsensitiveQuery = searchString.toLowerCase();

        for (PersonModel person : people) {
            if (person.getFirstName().toLowerCase().contains(searchString) ||
                    person.getLastName().toLowerCase().contains(caseInsensitiveQuery)) {
                persons.add(person);
            }
        }
        return persons;
    }

    public FamilyTree getFamilyTree() {
        return mFamilyTree;
    }

    public HashMap<String, LinkedList<EventModel>> getFilteredEventsMap() {
        return mFilteredEventsMap;
    }

    public HashMap<String, LinkedList<EventModel>> getEventsMap() {
        return mEventsMap;
    }

    private void ensureBirthFirst(List<EventModel> lifeEvents) {
        if (lifeEvents.isEmpty()) {
            return;
        }

        if (lifeEvents.get(0).getEventType().toLowerCase().equals("birth")) {
            return;
        }

        for (int i = 0; i < lifeEvents.size(); i++) {
            if (lifeEvents.get(i).getEventType().toLowerCase().equals("birth")) {
                EventModel birth = lifeEvents.remove(i);
                lifeEvents.add(0, birth);
                break;
            }

        }
    }

    private LifeEvent buildLifeEvent(EventModel event) {
        PersonModel person = getPersonByID(event.getPersonID());

        float[] hsvColor = new float[3];
        hsvColor[0] = getColor(event.getEventType());
        hsvColor[1] = 1;
        hsvColor[2] = 1;

        return new LifeEvent(event.getEventID(), event.getEventType(), event.getCountry(),
                event.getCity(), event.getYear(), person.getFirstName(), person.getLastName(),
                Color.HSVToColor(hsvColor));
    }

}
