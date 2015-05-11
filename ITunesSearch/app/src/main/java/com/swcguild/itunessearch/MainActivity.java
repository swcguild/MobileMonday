package com.swcguild.itunessearch;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private static final String ITUNES_SEARCH_URL = "https://itunes.apple.com/search?term=";

    private TextView resultsDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultsDisplay = (TextView) findViewById(R.id.result_txt);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // This is the onClick handler registered for the Search button in activity_main.xml
    public void searchITunes(View view) {

        // We know the button was clicked - now get the search term from the EditText
        // control
        EditText searchTxt = (EditText) findViewById(R.id.search_edit);
        String searchTerm = searchTxt.getText().toString();

        // make sure that the user typed something into the search box before clicking the
        // Search button
        if (searchTerm.isEmpty()) {
            resultsDisplay.setText("It appears that you haven't entered a Search Query...Please enter one now!");
            return;
        }

        // Now get the URL ready and encode it
        try {
            String encodedSearchTerm = URLEncoder.encode(searchTerm, "UTF-8");
            String searchUrl = ITUNES_SEARCH_URL + encodedSearchTerm;

            new GetResults().execute(searchUrl);

        } catch (UnsupportedEncodingException ex) {
            resultsDisplay.setText("He's dead, Jim...sorry, we couldn't encode your search.");
        }
    }

    // asynchronous task for fetching our search results
    private class GetResults extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... searchUrl) {
            StringBuilder searchBuilder = new StringBuilder();

            try {
                for (String currentUrl : searchUrl) {
                    HttpClient searchClient = new DefaultHttpClient();

                    HttpGet searchGet = new HttpGet(currentUrl);

                    HttpResponse searchResponse = searchClient.execute(searchGet);

                    StatusLine searchStatus = searchResponse.getStatusLine();

                    if (searchStatus.getStatusCode() == 200) {
                        HttpEntity searchEntity = searchResponse.getEntity();
                        InputStream searchContent = searchEntity.getContent();

                        InputStreamReader searchInput = new InputStreamReader(searchContent);
                        BufferedReader searchReader = new BufferedReader(searchInput);

                        String lineIn;
                        while ((lineIn = searchReader.readLine()) != null) {
                            searchBuilder.append(lineIn);
                        }

                    } else {
                        resultsDisplay.setText("He's dead, Jim...error code " + searchStatus.getStatusCode() + " returned from server.");
                    }
                }
            } catch (IOException ex) {
                resultsDisplay.setText("He's dead, Jim...error fetching your data.");
            }

            return searchBuilder.toString();
        }

        protected void onPostExecute(String result) {
            StringBuilder searchResultsBuilder = new StringBuilder();

            try {
                JSONObject resultsObject = new JSONObject(result);
                JSONArray resultsArray = resultsObject.getJSONArray("results");

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject resultItemObject = resultsArray.getJSONObject(i);
                    String wrapperType = resultItemObject.getString("wrapperType");
                    if (wrapperType.equals("track")) {
                        searchResultsBuilder.append(resultItemObject.getString("trackName"));
                        if (resultItemObject.getString("kind").equals("feature-movie")) {
                            searchResultsBuilder.append("\n");
                            searchResultsBuilder.append("DESCRIPTION:\n");
                            searchResultsBuilder.append(resultItemObject.getString("longDescription"));
                        } else if (resultItemObject.getString("kind").equals("song")) {
                            searchResultsBuilder.append("\n");
                            searchResultsBuilder.append("ALBUM:\n");
                            searchResultsBuilder.append(resultItemObject.getString("collectionName"));
                            searchResultsBuilder.append("\n");
                            searchResultsBuilder.append("TRACK PRICE: ");
                            searchResultsBuilder.append(resultItemObject.getString(("trackPrice")));
                        }
                        searchResultsBuilder.append("\n\n");
                    } else if (wrapperType.contentEquals("audiobook")) {
                        searchResultsBuilder.append("NAME: ");
                        searchResultsBuilder.append(resultItemObject.getString("collectionName"));
                        searchResultsBuilder.append("\n");
                        searchResultsBuilder.append(resultItemObject.getString("description"));
                        searchResultsBuilder.append("\n\n");
                    }
                }
            } catch (JSONException ex) {
                resultsDisplay.setText("He's dead, Jim...error parsing JSON returned from server.");
            }

            if (searchResultsBuilder.length() > 0) {
                resultsDisplay.setText(searchResultsBuilder.toString());
            } else {
                resultsDisplay.setText("No results for for your search.  Please try something else.");
            }
        }

    }
}
