package android.familymap.data;

public class LifeEvent {
    private String eventID;
    private String eventType;
    private String country;
    private String city;
    private int year;

    private String firstName;
    private String lastName;

    private int argbColor;

    LifeEvent() {}

    public LifeEvent(String eventID, String eventType, String country, String city, int year, String firstName, String lastName, int argbColor) {
        this.eventID = eventID;
        this.eventType = eventType;
        this.country = country;
        this.city = city;
        this.year = year;
        this.firstName = firstName;
        this.lastName = lastName;
        this.argbColor = argbColor;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getArgbColor() {
        return argbColor;
    }

    public void setArgbColor(int argbColor) {
        this.argbColor = argbColor;
    }
}
