package android.familymap.data;

// Singleton. Needed to be accessible from many different classes. May be refactored into another
// class later if I find a better way to implement this.


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import model.AuthTokenModel;
import model.EventModel;
import model.PersonModel;
import netabs.AuthResult;
import netabs.UserEventResult;
import netabs.UserLoginRequest;
import netabs.UserPersonResult;
import netabs.UserRegisterRequest;

public class ServerProxy {
    private static String TAG = "ServerProxy";
    private static ServerProxy server;

    private String serverHost;
    private int serverPort;

    private ServerProxy(String serverHost, String serverPort) {
        this.serverHost = serverHost;
        this.serverPort = Integer.parseInt(serverPort);
    }

    public static void initializeServer(String serverHost, String serverPort) {
        server = new ServerProxy(serverHost, serverPort);

    }

    public static ServerProxy getInstance() {
        return server;
    }

    public AuthResult loginUser(UserLoginRequest request) throws ServerAccessError {
        validatePort();
        try {
            URL url = new URL("http", serverHost, serverPort, "/user/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            OutputStream requestBody = connection.getOutputStream();
            OutputStreamWriter requestBodyWriter = new OutputStreamWriter(requestBody);

            Gson gson = new Gson();
            gson.toJson(request, requestBodyWriter);

            requestBodyWriter.close();

            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream responseBody = connection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody);

                AuthTokenModel result  = gson.fromJson(responseBodyReader, AuthTokenModel.class);

//                unable to parse server response
                if (result.getPersonID() == null || result.getAuthToken() == null || result.getUserName() == null) {
                    throw new ServerAccessError("Login Error: Bad username or password");
                }

                return new AuthResult(result);
            }
            else {
                throw new ServerAccessError("Login Error: Bad HTTP Resposne Code");
            }
        }
//        Gson could not parse an AuthResult from  Server response (likely error result)
        catch (JsonSyntaxException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Login Error");
        }
//        Failed to build a URL. Invalid User input
        catch (MalformedURLException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Login Error: invalid server host");
        }
//        Could not communicate with Server
        catch (IOException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Login Error: Could not communicate with server");
        }

    }

    public AuthResult registerUser(UserRegisterRequest request) throws ServerAccessError {
        validatePort();
        try {
            URL url = new URL("http", serverHost, serverPort, "/user/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            OutputStream requestBody = connection.getOutputStream();
            OutputStreamWriter requestBodyWriter = new OutputStreamWriter(requestBody);

            Gson gson = new Gson();
            gson.toJson(request, requestBodyWriter);

            requestBodyWriter.close();

            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream responseBody = connection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody);

                AuthTokenModel result  = gson.fromJson(responseBodyReader, AuthTokenModel.class);

//                unable to parse server response
                if (result.getPersonID() == null || result.getAuthToken() == null || result.getUserName() == null) {
                    throw new ServerAccessError("Register Error: Username already taken");
                }

                return new AuthResult(result);
            }
            else {
                throw new ServerAccessError("Login Error: Bad HTTP Resposne Code");
            }
        }
//        Gson could not parse an AuthResult from  Server response (likely error result)
        catch (JsonSyntaxException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Register Error");
        }
//        Failed to build a URL. Invalid User input
        catch (MalformedURLException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Register Error: invalid server host");
        }
//        Could not communicate with Server
        catch (IOException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Register Error: Could not communicate with server");
        }
    }

    public void fillDataCache() throws ServerAccessError {
        DataCache cache = DataCache.getInstance();
        cache.buildCache(getPerson(), getUserPersons(), getUserEvents());
    }

//    Depends on an AuthToken existing in the Database
    private PersonModel getPerson() throws ServerAccessError {
        try {
            DataCache cache = DataCache.getInstance();
            URL url = new URL("http", serverHost, serverPort, "/person/" + cache.getAuthToken().getPersonID());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", cache.getAuthToken().getAuthToken());
            connection.setRequestMethod("GET");

            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream responseBody = connection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody);

                Gson gson = new Gson();
                PersonModel result = gson.fromJson(responseBodyReader, PersonModel.class);
                return result;
            }
            else {
                throw new ServerAccessError("Person Error: Bad HTTP Resposne Code");
            }
        }
//        Gson could not parse an AuthResult from  Server response (likely error result)
        catch (JsonSyntaxException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Error retrieving data");
        }
//        Failed to build a URL. Invalid User input
        catch (MalformedURLException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Error retrieving data. Invalid URL.");
        }
//        Could not communicate with Server
        catch (IOException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Error retrieving data. Could not communicate with server.");
        }

    }


    private PersonModel[] getUserPersons() throws ServerAccessError {
        try {
            DataCache cache = DataCache.getInstance();
            URL url = new URL("http", serverHost, serverPort, "/person");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", cache.getAuthToken().getAuthToken());
            connection.setRequestMethod("GET");

            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream responseBody = connection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody);

                Gson gson = new Gson();
                UserPersonResult result = gson.fromJson(responseBodyReader, UserPersonResult.class);
                return result.getData();
            }
            else {
                throw new ServerAccessError("Person Error: Bad HTTP Resposne Code");
            }
        }
//        Gson could not parse an AuthResult from  Server response (likely error result)
        catch (JsonSyntaxException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Error retrieving data");
        }
//        Failed to build a URL. Invalid User input
        catch (MalformedURLException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Error retrieving data. Invalid URL.");
        }
//        Could not communicate with Server
        catch (IOException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Error retrieving data. Could not communicate with server.");
        }


    }

    private EventModel[] getUserEvents() throws ServerAccessError {
//        TODO: implement this
        try {
            DataCache cache = DataCache.getInstance();
            URL url = new URL("http", serverHost, serverPort, "/event");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", cache.getAuthToken().getAuthToken());
            connection.setRequestMethod("GET");

            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream responseBody = connection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody);

                Gson gson = new Gson();
                UserEventResult result = gson.fromJson(responseBodyReader, UserEventResult.class);
                return result.getData();
            }
            else {
                throw new ServerAccessError("Person Error: Bad HTTP Resposne Code");
            }
        }
//        Gson could not parse an AuthResult from  Server response (likely error result)
        catch (JsonSyntaxException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Error retrieving data");
        }
//        Failed to build a URL. Invalid User input
        catch (MalformedURLException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Error retrieving data. Invalid URL.");
        }
//        Could not communicate with Server
        catch (IOException e) {
            Log.d(TAG, null, e);
            throw new ServerAccessError("Error retrieving data. Could not communicate with server.");
        }
    }

    private void validatePort() throws ServerAccessError {
        if (serverPort < 0 || serverPort > 65535) {
            throw new ServerAccessError(String.format("error: invalid port number: %d", serverPort));
        }
    }
}
