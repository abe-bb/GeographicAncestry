package android.familymap;

import android.familymap.data.DataCache;
import android.familymap.data.ServerAccessError;
import android.familymap.data.ServerProxy;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert.*;
import org.junit.runner.RunWith;

import java.util.UUID;

import model.EventModel;
import model.PersonModel;
import netabs.AuthResult;
import netabs.UserEventRequest;
import netabs.UserEventResult;
import netabs.UserLoginRequest;
import netabs.UserRegisterRequest;

import static junit.framework.TestCase.*;

@RunWith(AndroidJUnit4.class)
public class TestProxy {
    public final static String SERVER_HOST = "192.168.2.7";
    public final static String SERVER_PORT = "8008";
    public final static String VALID_USERNAME = "username";
    public final static String VALID_PASSWORD = "password";
    public final static String INVALID_AUTH_TOKEN = "invalidToken";
    public final static String VALID_PERSON_SEARCH = "asdf";
    public final static int VALID_PERSON_SEARCH_RESULT = 1;
    public final static String VALID_EVENT_SEARCH ="united";
    public final static int VALID_EVENT_SEARCH_RESULT = 16;



    @BeforeClass
    static public void setup() {
        ServerProxy.initializeServer(SERVER_HOST, SERVER_PORT);
    }

    @Test
    public void testRegister() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();

        String username = UUID.randomUUID().toString();
        UserRegisterRequest request = new UserRegisterRequest(username, "test", "test",
                "first", "last", "m");

        AuthResult result = proxy.registerUser(request);
        assertNotNull(result);
        assertEquals(result.getAuthToken().getUserName(), username);
    }

    @Test(expected = ServerAccessError.class)
    public void testRegisterInvalidGender() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();

        String username = UUID.randomUUID().toString();
        UserRegisterRequest request = new UserRegisterRequest(username, "test", "test",
                "first", "last", "plant");
        proxy.registerUser(request);
    }

    @Test
    public void testLogin() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();

        UserLoginRequest request = new UserLoginRequest(VALID_USERNAME, VALID_PASSWORD);

        AuthResult result = proxy.loginUser(request);
        assertNotNull(result);
        assertEquals(result.getAuthToken().getUserName(), VALID_USERNAME);
    }

    @Test(expected = ServerAccessError.class)
    public void testLoginBadPassword() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();

        UserLoginRequest request = new UserLoginRequest(VALID_USERNAME, null);
        proxy.loginUser(request);

    }

    @Test
    public void retrieveEvents() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(VALID_USERNAME, VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());

        UserEventRequest request = new UserEventRequest(authResult.getAuthToken());
        PersonModel[] persons = proxy.getUserPersons();

        assertNotNull(persons);
    }

    @Test(expected = ServerAccessError.class)
    public void retrieveEventsInvalidAuthToken() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(VALID_USERNAME, VALID_PASSWORD));
        authResult.getAuthToken().setAuthToken(INVALID_AUTH_TOKEN);
        cache.setAuthToken(null);

        proxy.getUserEvents();
    }

    @Test
    public void retrievePersons() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(VALID_USERNAME, VALID_PASSWORD));
        cache.setAuthToken(authResult.getAuthToken());

        UserEventRequest request = new UserEventRequest(authResult.getAuthToken());
        EventModel[] events = proxy.getUserEvents();

        assertNotNull(events);
    }

    @Test(expected = ServerAccessError.class)
    public void retrievePersonsInvalidAuthToken() throws ServerAccessError {
        ServerProxy proxy = ServerProxy.getInstance();
        DataCache cache = DataCache.getInstance();

        AuthResult authResult = proxy.loginUser(new UserLoginRequest(VALID_USERNAME, VALID_PASSWORD));
        authResult.getAuthToken().setAuthToken(INVALID_AUTH_TOKEN);
        cache.setAuthToken(null);

        proxy.getUserEvents();
    }
}
