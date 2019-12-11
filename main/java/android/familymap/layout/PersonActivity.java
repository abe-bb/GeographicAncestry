package android.familymap.layout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.familymap.R;
import android.familymap.data.DataCache;
import android.familymap.data.FamilyMember;
import android.familymap.data.LifeEvent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import model.EventModel;
import model.PersonModel;

public class PersonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        String personID = getIntent().getStringExtra("personID");

        DataCache cache = DataCache.getInstance();

        PersonModel person = cache.getPersonByID(personID);

        TextView firstName = findViewById(R.id.first_name_field);
        TextView lastName = findViewById(R.id.last_name_field);
        TextView gender = findViewById(R.id.gender_field);

        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());

        if (person.getGender().equals("m")) {
            gender.setText(R.string.male_gender);
        }
        else if (person.getGender().equals("f")) {
            gender.setText(R.string.female_gender);
        }
        else {
            gender.setText(R.string.unknown_gender);
        }

        ExpandableListView expandableListView = findViewById(R.id.expandable_list_view);


        LinkedList<LifeEvent> lifeEvents = cache.getLifeEvents(personID);
        LinkedList<FamilyMember> familyMembers = cache.getDirectFamilyMembers(personID);

        expandableListView.setAdapter(new ExpandableListAdapter(lifeEvents, familyMembers));

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

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private static final int LIFE_EVENTS_GROUP_POSITION = 0;
        private static final int FAMILY_GROUP_POSITION = 1;
        private static final int NUM_GROUPS = 2;

        private final List<LifeEvent> lifeEvents;
        private final List<FamilyMember> familyMembers;

        public ExpandableListAdapter(List<LifeEvent> lifeEvents, List<FamilyMember> familyMembers) {
            this.lifeEvents = lifeEvents;
            this.familyMembers = familyMembers;
        }

        @Override
        public int getGroupCount() {
            return NUM_GROUPS;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    return lifeEvents.size();
                case FAMILY_GROUP_POSITION:
                    return familyMembers.size();
                default:
                    throw new RuntimeException("Invalid group position");

            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    return lifeEvents;
                case FAMILY_GROUP_POSITION:
                    return familyMembers;
                default:
                    throw new RuntimeException("Invalid group position");
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    return lifeEvents.get(childPosition);
                case FAMILY_GROUP_POSITION:
                    return familyMembers.get(childPosition);
                default:
                    throw new RuntimeException("Invalid group position");
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_group, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.list_title);

            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    textView.setText(R.string.life_events_list_group);
                    break;
                case FAMILY_GROUP_POSITION:
                    textView.setText(R.string.family_list_group);
                    break;
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            ImageView icon = itemView.findViewById(R.id.list_item_icon);
            TextView primary = itemView.findViewById(R.id.primary_info);
            TextView secondary = itemView.findViewById(R.id.secondary_info);

            switch (groupPosition) {
                case LIFE_EVENTS_GROUP_POSITION:
                    final LifeEvent event = (LifeEvent) getChild(groupPosition, childPosition);
                    primary.setText(String.format("%s: %s, %s (%d)", event.getEventType().toUpperCase(), event.getCountry(), event.getCity(), event.getYear()));
                    secondary.setText(String.format("%s %s", event.getFirstName(), event.getLastName()));

                    icon.setImageResource(R.drawable.marker_48dp);
                    icon.setColorFilter(event.getArgbColor());

                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), EventActivity.class);
                            intent.putExtra("eventID", event.getEventID());
                            startActivity(intent);
                        }
                    });
                    break;

                case FAMILY_GROUP_POSITION:
                    final FamilyMember familyMember = (FamilyMember) getChild(groupPosition, childPosition);
                    primary.setText(String.format("%s %s", familyMember.getFirstName(), familyMember.getLastName()));
                    secondary.setText(familyMember.getRelationship());

                    if (familyMember.getGender().equals("m")) {
                        icon.setImageResource(R.drawable.person_blue_48dp);
                    }
                    else if (familyMember.getGender().equals("f")) {
                        icon.setImageResource(R.drawable.person_pink_48dp);
                    }
                    else {
                        icon.setImageResource(R.drawable.person_grey_48dp);
                    }

                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), PersonActivity.class);
                            intent.putExtra("personID", familyMember.getPersonID());
                            startActivity(intent);
                        }
                    });
                    break;
            }

            return itemView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
