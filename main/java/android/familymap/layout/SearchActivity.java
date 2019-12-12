package android.familymap.layout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.familymap.R;
import android.familymap.data.DataCache;
import android.familymap.data.LifeEvent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import model.PersonModel;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final int PERSON_VIEW = 0;
    private static final int EVENT_VIEW = 1;

    private SearchRecyclerViewAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);

        RecyclerView recyclerView = findViewById(R.id.search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        adapter = new SearchRecyclerViewAdapter(new ArrayList<LifeEvent>(), new ArrayList<PersonModel>());
        recyclerView.setAdapter(adapter);


    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        DataCache cache = DataCache.getInstance();

        adapter.updateDataSet(cache.searchEvents(query), cache.searchPersons(query));
        return true;
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

    private class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewHolder> {
        ArrayList<LifeEvent> events;
        ArrayList<PersonModel> persons;

        public SearchRecyclerViewAdapter(ArrayList<LifeEvent> events, ArrayList<PersonModel> persons) {
            this.events = events;
            this.persons = persons;
        }

        public void updateDataSet(ArrayList<LifeEvent> events, ArrayList<PersonModel> persons) {
            this.events = events;
            this.persons = persons;
            notifyDataSetChanged();
        }



        @NonNull
        @Override
        public SearchRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.search_item, parent, false);

            return new SearchRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchRecyclerViewHolder holder, int position) {
            if (position < persons.size()) {
                holder.bind(persons.get(position));
            }
            else {
                holder.bind(events.get(position - persons.size()));
            }
        }

        @Override
        public int getItemCount() {
            return events.size() + persons.size();
        }
    }

    private class SearchRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView icon;
        TextView primaryInfo;
        TextView secondaryInfo;

        PersonModel person;
        LifeEvent event;



        int eventType;

        public SearchRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            eventType = -1;

            itemView.setOnClickListener(this);

            icon = itemView.findViewById(R.id.search_result_icon);
            primaryInfo = itemView.findViewById(R.id.primary_info);
            secondaryInfo = itemView.findViewById(R.id.secondary_info);

            person = null;
            event = null;
        }

        void bind(LifeEvent lifeEvent) {
            primaryInfo.setText(String.format("%s: %s, %s (%d)", lifeEvent.getEventType().toUpperCase(), lifeEvent.getCity(), lifeEvent.getCountry(), lifeEvent.getYear()));
            secondaryInfo.setText(String.format("%s %s", lifeEvent.getFirstName(), lifeEvent.getLastName()));

            icon.setImageResource(R.drawable.marker_48dp);
            icon.setColorFilter(lifeEvent.getArgbColor());

            eventType = EVENT_VIEW;
            event = lifeEvent;
            person = null;
        }

        void bind(PersonModel personModel) {
            primaryInfo.setText(String.format("%s, %s", personModel.getFirstName(), personModel.getLastName()));
            secondaryInfo.setText("");

            if (personModel.getGender().equals("m")) {
                icon.setImageResource(R.drawable.person_blue_48dp);
            }
            else if (personModel.getGender().equals("f")) {
                icon.setImageResource(R.drawable.person_pink_48dp);
            }
            else {
                icon.setImageResource(R.drawable.person_grey_48dp);
            }
            icon.clearColorFilter();

            eventType = PERSON_VIEW;
            person = personModel;
            event = null;
        }

        @Override
        public void onClick(View v) {
            if (eventType == PERSON_VIEW) {
                Intent intent = new Intent(getApplicationContext(), PersonActivity.class);
                intent.putExtra("personID", person.getPersonID());
                startActivity(intent);
            }
            else if (eventType == EVENT_VIEW){
                Intent intent = new Intent(getApplicationContext(), EventActivity.class);
                intent.putExtra("eventID", event.getEventID());
                startActivity(intent);
            }
        }
    }

        

}
