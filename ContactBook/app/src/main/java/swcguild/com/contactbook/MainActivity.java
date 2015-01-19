package swcguild.com.contactbook;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.FragmentTransaction;


// This is the main activity for our app.  It implements the callback methods for all of our
// fragments
public class MainActivity extends ActionBarActivity
        implements ContactListFragment.ContactListFragmentListener,
        DetailsFragment.DetailsFragmentListener,
        AddEditFragment.AddEditFragmentListener {

    public static final String ROW_ID = "row_id";

    // For displaying the contact list
    ContactListFragment contactListFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If the Activity is being restored we don't need to recreate the UI - just return
        if (savedInstanceState != null) {
            return;
        }

        // ContactListFragment is always displayed
        contactListFragment = new ContactListFragment();

        // add the fragment to the FrameLayout
        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, contactListFragment);
        // This causes the ContactListFragment to be displayed
        transaction.commit();
    }

    // =============================================================================================
    // The following methods are required because our Main Activity implements the
    // ContactListFragmentListener, DetailsFragment Listener, and the AddEditFragmentListener
    // =============================================================================================

    // Updates the GUI after a new or existing contact is saved
    @Override
    public void onAddEditCompleted(long rowID) {
        getFragmentManager().popBackStack();
    }

    // Pops the Details fragment off the stack when the Back to List button is clicked
    // so that the list of all contacts is displayed
    @Override
    public void onBackButtonClick() {
        getFragmentManager().popBackStack();
    }

    // Displays DetailsFragment for selected contact
    @Override
    public void onContactSelected(Long rowID) {
        displayContact(rowID, R.id.fragmentContainer);
    }

    // Displays AddEditFragment when 'add' button is pressed
    @Override
    public void onAddContact() {
        displayAddEditFragment(R.id.fragmentContainer, null);
    }

    // removes the top of the back stack after a contact has been deleted
    @Override
    public void onContactDeleted() {
        // remove the top of the back stack
        getFragmentManager().popBackStack();
    }

    // Displays AddEditFragment for editing an existing contact
    @Override
    public void onEditContact(Bundle arguments) {
        displayAddEditFragment(R.id.fragmentContainer, arguments);
    }

    // =============================================================================================
    // Helper Methods
    // =============================================================================================

    // Creates a new DetailsFragment and displays it
    private void displayContact(long rowID, int viewID) {
        // Create a new DetailsFragment
        DetailsFragment detailsFragment = new DetailsFragment();
        // Create and pass arguments - in this case just the row id
        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowID);
        detailsFragment.setArguments(arguments);

        // Replace given viewID with detailsFragment
        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewID, detailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Creates a new AddEditFragment and displays it
    private void displayAddEditFragment(int viewID, Bundle arguments) {
        AddEditFragment addEditFragment = new AddEditFragment();

        // if arguments is not null it means we're editing an existing contact
        if (arguments != null) {
            addEditFragment.setArguments(arguments);
        }

        // Replace viewID with addEditFragment
        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
