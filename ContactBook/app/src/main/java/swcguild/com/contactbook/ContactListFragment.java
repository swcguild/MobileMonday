package swcguild.com.contactbook;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TimePicker;

/**
 * Displays a list of the contacts in the database and provides an 'add' button for creating
 * new contacts
 */
public class ContactListFragment extends ListFragment {

    // callback methods implemented in MainActivity
    public interface ContactListFragmentListener {
        // called when user clicks on a contact
        public void onContactSelected(Long rowID);
        // called when user clicks on add button
        public void onAddContact();
    }

    private ContactListFragmentListener listener;

    private ListView contactListView;
    private CursorAdapter contactAdapter;

    /**
     * Sets the ContactListListener when this fragment is attached
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (ContactListFragmentListener) activity;
    }

    /**
     * Removes the listener when this fragment is detached
     */
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    //This class extends ListFragment which already contains a ListView.  This means we don't
    // need to inflate our layout as we did in the other fragments (so we don't override onCreateView).
    // There are some tasks that must be done after our view is created, which is why we override
    // this method.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Save our state across config changes
        setRetainInstance(true);
        // This fragment has option menu items
        setHasOptionsMenu(true);

        // This sets the text that is to be displed when there are no contacts in the database
        setEmptyText(getResources().getString(R.string.no_contacts));

        // Configure the list view
        contactListView = getListView();
        // Set the listener that will respond when an item is clicked
        contactListView.setOnItemClickListener(viewContactListener);
        // only allow one item to be selected at a time
        contactListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // map each contact's name to a TextView in the ListView layout
        String[] from = new String[]{"name"};
        int[] to = new int[]{android.R.id.text1};
        // SimpleCursorAdapter constructor args are:
        // 1 - Context in which the ListView is running (i.e. MainActivity)
        // 2 - Resource Id of the layout that is used to display each item in the ListView
        // 3 - Cursor that provides access to the data - we supply null now (we'll specify it later)
        // 4 - String array of the column names to display
        // 5 - int array containing the corresponding GUI resource IDs
        // 6 - usually set to zero
        contactAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, from, to, 0);
        setListAdapter(contactAdapter);
    }

    // Responds when a user clicks on a contact's name in the ListView
    AdapterView.OnItemClickListener viewContactListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            listener.onContactSelected(id);
        }
    };

    // When this fragment is resumed, create a GetContactsTask and get all the contacts from
    // the database.
    public void onResume() {
        super.onResume();
        new GetContactsTask().execute((Object[]) null);
    }

    // When this fragment stops, close the Cursor and remove it from the contactAdapter
    public void onStop() {
        Cursor cursor = contactAdapter.getCursor();
        contactAdapter.changeCursor(null);

        if (cursor != null) {
            cursor.close();
        }

        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_contact_list_menu, menu);
    }

    // Responds when an option menu item is clicked - in our case we have only one item - 'add'
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_add:
                listener.onAddContact();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Performs database query outside of GUI thread - gets all contact in the database.
    private class GetContactsTask extends AsyncTask<Object, Object, Cursor> {
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        // Get all the contacts
        protected Cursor doInBackground(Object... params) {
            databaseConnector.open();
            return databaseConnector.getAllContacts();
        }

        // clean up
        protected void onPostExecute(Cursor result) {
            contactAdapter.changeCursor(result);
            databaseConnector.close();
        }

    }
}
