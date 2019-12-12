package android.familymap;

import android.content.Context;
import android.content.SharedPreferences;
import android.familymap.data.DataCache;
import android.familymap.data.FamilyTree;
import android.familymap.data.FamilyTreeNode;
import android.familymap.data.LifeEvent;
import android.familymap.data.ServerAccessError;
import android.familymap.data.ServerProxy;

import androidx.test.platform.app.InstrumentationRegistry;

import static junit.framework.TestCase.*;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import model.EventModel;
import model.PersonModel;
import netabs.AuthResult;
import netabs.UserLoginRequest;

public class TestCache {
    @BeforeClass
    public static void setup() {
        ServerProxy.initializeServer(TestProxy.SERVER_HOST, TestProxy.SERVER_PORT);
    }

    @Test
    public void validateParents() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        FamilyTree familyTree = cache.getFamilyTree();
        assertNotNull(familyTree);

        FamilyTreeNode node  = familyTree.getRoot();
        assertNotNull(node);
        assertEquals(node.getPerson().getAssociatedUsername(), TestProxy.VALID_USERNAME);

        FamilyTreeNode father = node.getLeftNode();
        FamilyTreeNode mother = node.getRightNode();

        if (node.getPerson().getFatherID() != null) {
            assertEquals(node.getLeftNode().getPerson().getPersonID(), father.getPerson().getPersonID());
        }
        if (node.getPerson().getMotherID() != null) {
            assertEquals(node.getRightNode().getPerson().getPersonID(), mother.getPerson().getPersonID());
        }

    }

    @Test
    public void validateAncestors() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        FamilyTree familyTree = cache.getFamilyTree();
        assertNotNull(familyTree);

        FamilyTreeNode node  = familyTree.getRoot();
        assertNotNull(node);
        assertEquals(node.getPerson().getAssociatedUsername(), TestProxy.VALID_USERNAME);

        ArrayList<FamilyTreeNode> nodes = new ArrayList<>();


        FamilyTreeNode father = node.getLeftNode();
        FamilyTreeNode mother = node.getRightNode();

        if (node.getPerson().getFatherID() != null) {
            assertEquals(node.getLeftNode().getPerson().getPersonID(), father.getPerson().getPersonID());
            nodes.add(father);
        }
        if (node.getPerson().getMotherID() != null) {
            assertEquals(node.getRightNode().getPerson().getPersonID(), mother.getPerson().getPersonID());
            nodes.add(mother);
        }

        for (int i = 0; i < nodes.size(); i++) {
            FamilyTreeNode currentNode = nodes.get(i);

            FamilyTreeNode currentPersonsFather = currentNode.getLeftNode();
            FamilyTreeNode currentPersonsMother = currentNode.getRightNode();

            if (currentNode.getPerson().getFatherID() != null) {
                assertEquals(currentNode.getPerson().getFatherID(),
                        currentPersonsFather.getPerson().getPersonID());
                nodes.add(currentPersonsFather);
            }
            if (currentNode.getPerson().getMotherID() != null) {
                assertEquals(currentNode.getPerson().getMotherID(),
                        currentPersonsMother.getPerson().getPersonID());
                nodes.add(currentPersonsMother);
            }


        }

    }

    @Test
    public void testFiltersEverythingEnabled() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        SharedPreferences preferences = appContext.getSharedPreferences(  appContext.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("mother_side_switch", true);
        editor.putBoolean("father_side_switch", true);
        editor.putBoolean("male_switch", true);
        editor.putBoolean("female_switch", true);
        editor.commit();

        cache.getEvents(preferences);

        assertEquals(cache.getFilteredEventsMap().size(), cache.getEventsMap().size());
    }

    @Test
    public void testFiltersMalesDisabled() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        SharedPreferences preferences = appContext.getSharedPreferences(  appContext.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("mother_side_switch", true);
        editor.putBoolean("father_side_switch", true);
        editor.putBoolean("male_switch", false);
        editor.putBoolean("female_switch", true);
        editor.commit();

        cache.getEvents(preferences);

        for (List<EventModel> events : cache.getEvents(preferences)) {
            for (EventModel event: events) {
                assertEquals("f", cache.getPersonByID(event.getPersonID()).getGender());
            }
        }
    }

    @Test
    public void testFiltersAllEventsHidden() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        SharedPreferences preferences = appContext.getSharedPreferences(appContext.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("mother_side_switch", true);
        editor.putBoolean("father_side_switch", true);
        editor.putBoolean("male_switch", false);
        editor.putBoolean("female_switch", false);
        editor.commit();

        cache.getEvents(preferences);

        for (List<EventModel> events : cache.getEvents(preferences)) {
            assertTrue(events.isEmpty());
        }
    }

    @Test
    public void validateLifeEventOrder() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        ArrayList<FamilyTreeNode> nodes = new ArrayList<>();
        nodes.add(cache.getFamilyTree().getRoot());
        if (cache.getFamilyTree().getSpouse() != null) {
            nodes.add(new FamilyTreeNode(cache.getFamilyTree().getSpouse()));
        }

        HashMap<String, LinkedList<EventModel>> eventMap = cache.getEventsMap();

        for (int i = 0; i < nodes.size(); i++) {
            FamilyTreeNode node = nodes.get(i);
            if (node.getLeftNode() != null) {
                nodes.add(node.getLeftNode());
            }
            if (node.getRightNode() != null) {
                nodes.add(node.getRightNode());
            }

            LinkedList<EventModel> lifeEvents = eventMap.get(node.getPerson().getPersonID());

            EventModel prevEvent = lifeEvents.isEmpty() ? null : lifeEvents.get(0);
            for (int j = 1; j < lifeEvents.size(); j++) {
                EventModel currentEvent = lifeEvents.get(j);

                if (j == 1 && prevEvent.getEventType().toLowerCase().equals("birth")) {
                    prevEvent = currentEvent;
                    continue;
                }

                assertTrue(prevEvent.getYear() <= currentEvent.getYear());
            }
        }
    }

    @Test
    public void testPersonSearch() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        ArrayList<PersonModel> persons = cache.searchPersons(TestProxy.VALID_PERSON_SEARCH);
        assertEquals(TestProxy.VALID_PERSON_SEARCH_RESULT, persons.size());
    }

    @Test
    public void testPersonSearchEmptyResult() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        ArrayList<PersonModel> persons = cache.searchPersons(UUID.randomUUID().toString());
        assertTrue(persons.isEmpty());
    }

    @Test
    public void testEventSearch() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        SharedPreferences preferences = appContext.getSharedPreferences(appContext.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("mother_side_switch", true);
        editor.putBoolean("father_side_switch", true);
        editor.putBoolean("male_switch", true);
        editor.putBoolean("female_switch", true);
        editor.commit();

        cache.getEvents(preferences);

        ArrayList<LifeEvent> persons = cache.searchEvents(TestProxy.VALID_EVENT_SEARCH);

        assertEquals(TestProxy.VALID_EVENT_SEARCH_RESULT, persons.size());
    }

    @Test
    public void testEventSearchEmptyResult() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(TestProxy.VALID_USERNAME, TestProxy.VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());
        proxy.fillDataCache();

        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        SharedPreferences preferences = appContext.getSharedPreferences(appContext.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("mother_side_switch", true);
        editor.putBoolean("father_side_switch", true);
        editor.putBoolean("male_switch", true);
        editor.putBoolean("female_switch", true);
        editor.commit();

        cache.getEvents(preferences);

        ArrayList<LifeEvent> persons = cache.searchEvents(UUID.randomUUID().toString());
        assertTrue(persons.isEmpty());
    }
}
