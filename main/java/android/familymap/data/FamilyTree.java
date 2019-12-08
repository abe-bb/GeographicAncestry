package android.familymap.data;

import android.app.Person;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

import model.EventModel;
import model.PersonModel;

public class FamilyTree {
    private FamilyTreeNode root;

    public FamilyTree(PersonModel basePerson) {
        root = new FamilyTreeNode(basePerson);
    }

    public void fillTree(PersonModel[] persons) {
        fillTreeRecursive(persons, root);

    }

    private void fillTreeRecursive(PersonModel[] persons, FamilyTreeNode node) {
        PersonModel person = node.getPerson();

        if (person.getFatherID() != null) {
            PersonModel father = findPersonInArray(person.getFatherID(), persons);
            if (father != null) {
                node.setLeftNode(new FamilyTreeNode(father));
                fillTreeRecursive(persons, node.getLeftNode());
            }
        }

        if (person.getMotherID() != null) {
            PersonModel mother = findPersonInArray(person.getMotherID(), persons);
            if (mother != null) {
                node.setRightNode(new FamilyTreeNode(mother));
                fillTreeRecursive(persons, node.getRightNode());
            }
        }
    }

    private PersonModel findPersonInArray(String personID, PersonModel[] persons) {
        for (PersonModel person : persons) {
            if (personID.equals(person.getPersonID())) {
                return person;
            }
        }
        return null;
    }

    public PersonModel getPersonByID(String personID) {
        return getPersonByIDRecursive(root, personID);

    }

    private PersonModel getPersonByIDRecursive(FamilyTreeNode node, String personID) {
        if (node == null) {
            return null;
        }

        if (node.getPerson().getPersonID().equals(personID)) {
            return node.getPerson();
        }

        PersonModel person = getPersonByIDRecursive(node.getLeftNode(), personID);

        if (person != null) {
            return person;
        }

        person = getPersonByIDRecursive(node.getRightNode(), personID);
        return person;
    }

    public ArrayList<PersonModel> buildFilteredPersonList(SharedPreferences preferences) {

        boolean males = preferences.getBoolean("male_switch", true);
        boolean females = preferences.getBoolean("female_switch", true);
        boolean fatherSide = preferences.getBoolean("father_side_switch", true);
        boolean motherSide = preferences.getBoolean("mother_side_switch", true);

        ArrayList<PersonModel> people = new ArrayList<>();

        if (fatherSide) {
            buildFilteredPersonListRecursive(root.getLeftNode(), males, females, people);
        }
        if (motherSide) {
            buildFilteredPersonListRecursive(root.getRightNode(), males, females, people);
        }

        return people;
    }

    private void buildFilteredPersonListRecursive(FamilyTreeNode node, boolean males, boolean females, ArrayList<PersonModel> people) {
        if (node == null) {
            return;
        }

        if (males && node.getPerson().getGender().equals("m")) {
            people.add(node.getPerson());
        }
        else if (females && node.getPerson().getGender().equals("f")) {
            people.add(node.getPerson());
        }

        buildFilteredPersonListRecursive(node.getLeftNode(), males, females, people);
        buildFilteredPersonListRecursive(node.getRightNode(), males, females, people);
    }

    private FamilyTreeNode getSubtreeRecursive(FamilyTreeNode node, String personID) {
        if (node == null) {
            return null;
        }

        if (node.getPerson().getPersonID().equals(personID)) {
            return node;
        }

        FamilyTreeNode subtree = getSubtreeRecursive(node.getLeftNode(), personID);

        if (subtree != null) {
            return subtree;
        }

        subtree = getSubtreeRecursive(node.getRightNode(), personID);

        return subtree;
    }

    private void buildFamilyTreeLinesRecursive(FamilyTreeNode node, EventModel prevEvent, HashMap<String, ArrayList<EventModel>> events, float baseLineWidth, int depth, ArrayList<PolylineOptions> lines) {
        if (node == null) {
            return;
        }
        ArrayList<EventModel> personEvents = events.get(node.getPerson().getPersonID());

        EventModel event;
        if (personEvents != null) {
            event = DataCache.getBirthOrFirstEvent(node.getPerson().getPersonID(), personEvents);

            float lineWidth = baseLineWidth - (3 * depth);

            lineWidth =  (lineWidth < 3) ? 3 : lineWidth;

            lines.add(new PolylineOptions().width(lineWidth)
                    .add(new LatLng(prevEvent.getLatitude(), prevEvent.getLongitude()))
                    .add(new LatLng(event.getLatitude(), event.getLongitude())));

            buildFamilyTreeLinesRecursive(node.getLeftNode(), event,  events, baseLineWidth, depth + 1, lines);
            buildFamilyTreeLinesRecursive(node.getRightNode(),event,  events, baseLineWidth, depth + 1, lines);
        }


    }

    public ArrayList<PolylineOptions> buildFamilyTreeLines(String personID, EventModel selectedEvent, HashMap<String, ArrayList<EventModel>> events, float baseLineWidth) {
        ArrayList<PolylineOptions> lines = new ArrayList<>();

        FamilyTreeNode subtree = getSubtreeRecursive(root, personID);

        buildFamilyTreeLinesRecursive(subtree, selectedEvent, events, baseLineWidth, 1, lines);

        return lines;
    }
}
