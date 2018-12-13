package com.example.android.pokedex;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE=1;
    // TODO: 13-Dec-2018 -- change the REQUEST_URL value with the actual server address
    static final String REQUEST_URL = "0.0.0.0:5000";
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            try {
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                String encodedImage = encodeImage(selectedImage);
                predictPokemonName(encodedImage);
            }
            catch(FileNotFoundException fofe){
                Toast.makeText(this, "Unable to get Bitmap of captured Image", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void predictPokemonName(String eImage)
    {
        PokedexAsyncTask task = new PokedexAsyncTask();
        task.execute(eImage);
    }

    private String encodeImage(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private class PokedexAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            URL url = createUrl(REQUEST_URL+params[0]);
            String pokemonName = "";
            try {
                pokemonName = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }
            return pokemonName;
        }

        @Override
        protected void onPostExecute(String pokemonName) {
            updateUI(pokemonName);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO: 13-Dec-2018 -- make the progress bar
        }
    }

    private void updateUI(String pokemonName)
    {
        // TODO: 13-Dec-2018 -- complete the function to display all the content that arerequired by the page
        // TODO:                OR what we can do is make a new UI for all these things usig .xml layout file
    }

    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    private String makeHttpRequest(URL url) throws IOException {
        String pokemonName = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(20000 /* milliseconds */);
            urlConnection.setConnectTimeout(25000 /* milliseconds */);
            urlConnection.connect();
            if(urlConnection.getResponseCode()==200) {
                inputStream = urlConnection.getInputStream();
                pokemonName = readFromStream(inputStream);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Unable to connect to server", Toast.LENGTH_LONG).show();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return pokemonName;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
