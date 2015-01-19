package swcguild.com.contactbook;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.Objects;

/**
 * Allows the user to create new contacts or edit existing contacts
 */
public class AddEditFragment extends Fragment {

    private AddEditFragmentListener listener;
    // the displayed contact's database row id
    private long rowID;
    // holds arguments for editing a contact
    private Bundle contactInfoBundle;

    // UI component that holds/displays the contact name
    private EditText nameEditText;
    // UI component that holds/displays the contact phone
    private EditText phoneEditText;
    // UI component that holds/displays the contact email
    private EditText emailEditText;

    // This interface contains the definitions of callback methods for this screen.  These methods
    // are implemented in the Main Activity of the app
    public interface AddEditFragmentListener {
        public void onAddEditCompleted(long rowID);
    }

    /**
     * Sets the AddEditFragmentListener when this fragment is attached to the main activity
     * @param activity
     */
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (AddEditFragmentListener) activity;
    }

    /**
     * Removes the AddEditFragmentListener when this fragment is detached
     */
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * Called when the AddEditFragmentListener's view needs to be created
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // We want to save the fragment's state across configuration changes
        setRetainInstance(true);
        // This fragments has option menu items
        setHasOptionsMenu(true);

        View view =
                inflater.inflate(R.layout.fragment_add_edit, container, false);

        // Get references to all of our edit text fields
        nameEditText = (EditText) view.findViewById(R.id.nameEditText);
        phoneEditText = (EditText) view.findViewById(R.id.phoneEditText);
        emailEditText = (EditText) view.findViewById(R.id.emailEditText);

        // This will be null if we're adding a new contact, otherwise it will contain the values
        // for the specified existing contact
        contactInfoBundle = getArguments();

        // Set contact values into the edit text fields
        if (contactInfoBundle != null) {
            rowID = contactInfoBundle.getLong(MainActivity.ROW_ID);
            nameEditText.setText(contactInfoBundle.getString("name"));
            phoneEditText.setText(contactInfoBundle.getString("phone"));
            emailEditText.setText(contactInfoBundle.getString("email"));
        }

        // set Save Contact Button's onclick listener
        Button saveContactButton =
                (Button) view.findViewById(R.id.saveContactButton);
        saveContactButton.setOnClickListener(saveContactButtonClicked);

        return view;
    }

    private void saveContact() {
        // get db connector
        DatabaseConnector databaseConnector =
                new DatabaseConnector(getActivity());

        if (contactInfoBundle == null) {
            // insert
            rowID = databaseConnector.insertContact(
                    nameEditText.getText().toString(),
                    phoneEditText.getText().toString(),
                    emailEditText.getText().toString());
        } else {
            databaseConnector.updateContact(rowID,
                    nameEditText.getText().toString(),
                    phoneEditText.getText().toString(),
                    emailEditText.getText().toString());
        }
    }

    /**
     * This code responds when the Save button is clicked
     */
    OnClickListener saveContactButtonClicked = new OnClickListener() {
        public void onClick(View v) {
            // if the required 'name' field is filled in, create an AsyncTask to save the contact
            // to the database - if 'name' is empty, display an error dialog
            if (nameEditText.getText().toString().trim().length() != 0) {
                AsyncTask<Object, Object, Object> saveContactTask =
                        new AsyncTask<Object, Object, Object>() {
                            @Override
                            protected Objects doInBackground(Object... params) {
                                saveContact();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object result) {
                                // hide the kb
                                InputMethodManager imm = (InputMethodManager)
                                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                                listener.onAddEditCompleted(rowID);
                            }
                        }; // end AsyncTask
                saveContactTask.execute((Object[]) null);
            } else {
                // Name field is blank - display error message
                DialogFragment errorSaving = new DialogFragment() {
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.error_message);
                        builder.setPositiveButton(R.string.ok, null);
                        return builder.create();
                    }
                };

                errorSaving.show(getFragmentManager(), "error saving contact");
            }
        }
    };
}