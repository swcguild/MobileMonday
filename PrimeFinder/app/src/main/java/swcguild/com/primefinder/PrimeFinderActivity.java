package swcguild.com.primefinder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class PrimeFinderActivity extends Activity {

    // this is the input box control where the number will be entered
    // by the user.  we'll initialize it in the onCreate method and
    // access it in our application logic
    private TextView numberInputView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prime_finder);
        // initialize our TextView control variable
        numberInputView= (TextView) findViewById(R.id.inputNumber);
        // find our button and initialize our variable
        Button myButton  = (Button) findViewById(R.id.button);
        // add the on click listener to our button
        myButton.setOnClickListener(buttonListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.prime_finder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(View v) {
            String answer = "Yes!";
            // get the entered string value from the numberInputView
            String numString = numberInputView.getText().toString();
            // if the user entered a value, go ahead and process it
            // if not, display an error message
            if (numString != null && !numString.isEmpty()) {
                // now parse the String into an int - we've locked the input
                // so that it only accepts numbers so there is no possibility of
                // a number format exception
                int num = Integer.parseInt(numString);
                if (num%2 == 0) {
                    // it's even so it can't be prime...
                    answer = "Nope!  It's even...";
                } else {
                    // we can start at 3 and we only have to go up to the square root of the
                    // number to check for prime
                    for (int i = 3; i * i <= num; i += 2) {
                        if (num%i == 0) {
                            // it has at least one factor so it can't be prime
                            answer = "Nope! It has factors!";
                            // go ahead and quit - we don't need to find all of the factors
                            // if we find one, we're done...
                            break;
                        }
                    }
                }
            } else {
                // user did not enter a value
                answer = "Hey! I said enter a number!";
            }
            Toast.makeText(PrimeFinderActivity.this, answer, Toast.LENGTH_LONG).show();
        }
    };
}
