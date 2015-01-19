package swcguild.com.contactbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Displays the information for one contact.  Contains menus items that allow the user to update
 * or delete the contact.
 */
public class DetailsFragment extends Fragment {

    private DetailsFragmentListener listener;

    // the displayed contact's database row id
    private long rowID = -1;
    // UI component that holds/displays the contact name
    private TextView nameTextView;
    // UI component that holds/displays the contact phone
    private TextView phoneTextView;
    // UI component that holds/displays the contact email
    private TextView emailTextView;

    // This interface contains the definitions of callback methods for this screen.  These methods
    // are implemented in the Main Activity of the app
    public interface DetailsFragmentListener {
        public void onContactDeleted();
        public void onEditContact(Bundle arguments);
        public void onBackButtonClick();
    }

    /**
     * Sets the DetailsFragmentListener when this fragment is attached to the main activity
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (DetailsFragmentListener) activity;
    }

    /**
     * Removes the DetailsFragmentListener when this fragment is detached
     */
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * Called when the DetailsFragmentListener's view needs to be created
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // We want to save the fragment's state across configuration changes
        setRetainInstance(true);

        // if we're being restored grab the saved row ID
        if (savedInstanceState != null) {
            rowID = savedInstanceState.getLong(MainActivity.ROW_ID);
        } else {
            // Grab the rowID out of the Bundle of arguments
            Bundle arguments = getArguments();
            if (arguments != null) {
                rowID = arguments.getLong(MainActivity.ROW_ID);
            }
        }

        // inflate the layout
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        // This fragment has option menu items for editing and deleting a contact
        setHasOptionsMenu(true);

        // Get references to all of our edit text fields
        nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        phoneTextView = (TextView) view.findViewById(R.id.phoneTextView);
        emailTextView = (TextView) view.findViewById(R.id.emailTextView);

        // Set up Back button - this button allows us to go back to the list view from the details
        // screen.  Here we create the button and set its onClick listener
        Button backButton = (Button) view.findViewById(R.id.backButton);
        backButton.setOnClickListener(backButtonClicked);

        return view;
    }

    /**
     * When our fragment resumes it creates an asynchronous task (AsyncTask) that will retrieve the
     * specified contact from the database.  It must create a new AsyncTask each time because each
     * AsyncTask can only be executed once.
     */
    public void onResume() {
        super.onResume();
        new LoadContactTask().execute(rowID);
    }

    /**
     * This method is called when ever the configuration of the device changes (for example if the
     * device goes from portrait to landscape mode).  The state of the UI components is automatically
     * saved but we are responsible for saving any application specific data.  In our case, we
     * want to save the rowID of the contact currently being displayed.
     * @param outState data structure containing the state of the fragment
     */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MainActivity.ROW_ID, rowID);
    }

    /**
     * Displays this fragment's option menus (Edit and Delete)
     * @param menu
     * @param inflater
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }

    /**
     * This method is called when one of the option menu items is selected
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                // create a Bundle with the contact data to edit
                Bundle arguments = new Bundle();
                arguments.putLong(MainActivity.ROW_ID, rowID);
                arguments.putCharSequence("name", nameTextView.getText());
                arguments.putCharSequence("phone", phoneTextView.getText());
                arguments.putCharSequence("email", emailTextView.getText());
                // pass the Bundle to the listener
                listener.onEditContact(arguments);
                return true;
            case R.id.action_delete:
                // just call the delete method - no Bundle needed
                deleteContact();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Asynchronously performs db query outside GUI thread
     */
    private class LoadContactTask extends AsyncTask<Long, Object, Cursor> {

        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        /**
         * Opens the database and retrieves the information for the specified contact.
         * RowID of the requested contact is passed in as the first parameter.
         * @param params
         * @return
         */
        protected Cursor doInBackground(Long... params) {
            databaseConnector.open();
            return databaseConnector.getOneContact(params[0]);
        }

        /**
         * Uses the given Cursor to display the requested contact's information
         * @param result
         */
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);
            // Move to the first item in the result
            result.moveToFirst();

            // get the col index for each data item
            int nameIndex = result.getColumnIndex("name");
            int phoneIndex = result.getColumnIndex("phone");
            int emailIndex = result.getColumnIndex("email");

            // fill the TextViews with data
            nameTextView.setText(result.getString(nameIndex));
            phoneTextView.setText(result.getString(phoneIndex));
            emailTextView.setText(result.getString(emailIndex));

            // clean up
            result.close();
            databaseConnector.close();
        }
    }

    // delete a contact
    private void deleteContact() {
        // display a confirm/delete DialogFragment
        confirmDelete.show(getFragmentManager(), "confirm delete");
    }

    /**
     * Confirmation dialog for deleting a contact
     */
    private DialogFragment confirmDelete = new DialogFragment() {

        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);
            // if OK is clicked, delete the contact using an AsyncTask
            builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int button) {
                    final DatabaseConnector databaseConnector = new DatabaseConnector((getActivity()));

                    AsyncTask<Long, Object, Object> deleteTask = new AsyncTask<Long, Object, Object>() {
                        @Override
                        protected Object doInBackground(Long... params) {
                            databaseConnector.deleteContact(params[0]);
                            return null;
                        }

                        protected void onPostExecute(Object result) {
                            listener.onContactDeleted();
                        }
                    };
                    deleteTask.execute(new Long[]{rowID});
                } // end onClick
            } // end anonymous inner class
            ); // end call to method setPositiveButton

            // if Cancel is clicked, do nothing except dismissing the dialog
            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create(); // return the alert dialog
        }
    }; // end dialogFragment

    // OnClickListener for the back button - will just return to the list screen.
    View.OnClickListener backButtonClicked = new View.OnClickListener() {
        public void onClick(View v) {
            listener.onBackButtonClick();
        }
    };
} // end class
