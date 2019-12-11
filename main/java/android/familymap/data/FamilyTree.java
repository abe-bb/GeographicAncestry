package android.familymap.data;

import android.app.Person;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import model.EventModel;
import model.PersonModel;

public class FamilyTree {
    private FamilyTreeNode root;
    private PersonModel spouse;

    public FamilyTree(PersonModel basePerson) {
        root = new FamilyTreeNode(basePerson);
    }

    public void fillTree(PersonModel[] persons) {
        fillTreeRecursive(persons, root);

        if (root.getPerson().getSpouseID() != null) {
            for (PersonModel person : persons) {
                if (root.getPerson().getSpouseID().equals(person.getPersonID())) {
                    spouse = person;
                }
            }
        }

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
        if (spouse.getPersonID().equals(personID)) {
            return spouse;
        }
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

        if (males) {
            if (root.getPerson().getGender().equals("m")) {
                people.add(root.getPerson());
            }
            if (spouse != null && spouse.getGender().equals("m")) {
                people.add(spouse);
            }
        }
        if (females) {
            if (root.getPerson().getGender().equals("f")) {
                people.add(root.getPerson());
            }
            if (spouse != null && spouse.getGender().equals("f")) {
                people.add(spouse);
            }
        }

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

    private void buildFamilyTreeLinesRecursive(FamilyTreeNode node, EventModel prevEvent, HashMap<String, LinkedList<EventModel>> events, float baseLineWidth, int depth, ArrayList<PolylineOptions> lines) {
        if (node == null) {
            return;
        }
        LinkedList<EventModel> personEvents = events.get(node.getPerson().getPersonID());

        EventModel event = prevEvent;
        if (personEvents != null) {
            event = DataCache.getBirthOrFirstEvent(node.getPerson().getPersonID(), personEvents);

            float lineWidth = baseLineWidth - (5 * depth);

            lineWidth =  (lineWidth < 2) ? 2 : lineWidth;

            lines.add(new PolylineOptions().width(lineWidth)
                    .add(new LatLng(prevEvent.getLatitude(), prevEvent.getLongitude()))
                    .add(new LatLng(event.getLatitude(), event.getLongitude())));

        }

        buildFamilyTreeLinesRecursive(node.getLeftNode(), event,  events, baseLineWidth, depth + 1, lines);
        buildFamilyTreeLinesRecursive(node.getRightNode(),event,  events, baseLineWidth, depth + 1, lines);



    }

    public ArrayList<PolylineOptions> buildFamilyTreeLines(String personID, EventModel selectedEvent, HashMap<String, LinkedList<EventModel>> events, float baseLineWidth) {
        ArrayList<PolylineOptions> lines = new ArrayList<>();

        FamilyTreeNode subtree = getSubtreeRecursive(root, personID);

        if (subtree == null) {
            return lines;
        }

        buildFamilyTreeLinesRecursive(subtree.getRightNode(), selectedEvent, events, baseLineWidth, 0, lines);
        buildFamilyTreeLinesRecursive(subtree.getLeftNode(), selectedEvent, events, baseLineWidth, 0, lines);

        return lines;
    }

    public LinkedList<FamilyMember> buildDirectFamilyList(String personID) {
        LinkedList<FamilyMember> familyMembers = buildDirectFamilyListRecursive(personID, root, null, null);

        if (familyMembers == null) {
            familyMembers = new LinkedList<>();
        }

//        Add rootPerson's spouse if needed;
        if (root.getPerson().getPersonID().equals(personID) && spouse != null) {
            FamilyMember familyMember = new FamilyMember("Spouse", spouse.getFirstName(), spouse.getLastName(), spouse.getGender(), spouse.getPersonID());
            familyMembers.add(familyMember);
        }
//        Add root person as spouse's spouse if needed;
        else if (spouse != null && spouse.getPersonID().equals(personID) && root.getPerson() != null) {
            FamilyMember familyMember = new FamilyMember("Spouse", root.getPerson().getFirstName(), root.getPerson().getLastName(), root.getPerson().getGender(), root.getPerson().getPersonID());
            familyMembers.add(familyMember);
        }

        return familyMembers;
    }

    private LinkedList<FamilyMember> buildDirectFamilyListRecursive(String personID, FamilyTreeNode node, PersonModel child, FamilyTreeNode spouse) {
        if (node == null) {
            return null;
        }

        LinkedList<FamilyMember> directFamily;
        if (node.getPerson().getPersonID().equals(personID)) {
            directFamily = new LinkedList<>();

            if (node.getLeftNode() != null) {
                PersonModel person = node.getLeftNode().getPerson();
                directFamily.add(new FamilyMember("Father", person.getFirstName(), person.getLastName(), person.getGender(), person.getPersonID()));
            }
            if (node.getRightNode() != null) {
                PersonModel person = node.getRightNode().getPerson();
                directFamily.add(new FamilyMember("Mother", person.getFirstName(), person.getLastName(), person.getGender(), person.getPersonID()));
            }
            if (spouse != null) {
                PersonModel person = spouse.getPerson();
                directFamily.add(new FamilyMember("Spouse", person.getFirstName(), person.getLastName(), person.getGender(), person.getPersonID()));
            }
            if (child != null) {
                directFamily.add(new FamilyMember("Child", child.getFirstName(), child.getLastName(), child.getGender(), child.getPersonID()));
            }
            return directFamily;
        }

        directFamily = buildDirectFamilyListRecursive(personID, node.getLeftNode(), node.getPerson(), node.getRightNode());

        if (directFamily != null) {
            return directFamily;
        }

        directFamily = buildDirectFamilyListRecursive(personID, node.getRightNode(), node.getPerson(), node.getLeftNode());

        return directFamily;

    }

}
