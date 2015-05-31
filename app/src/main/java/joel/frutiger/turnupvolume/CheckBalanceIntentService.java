package joel.frutiger.turnupvolume;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 *
 * helper methods.
 */
public class CheckBalanceIntentService extends IntentService {

    private static final String ACTION_CHECK = "joel.frutiger.turnupvolume.action.CHECK";
    private static final String LOG_TAG = "CheckBalanceIntent";

    /**
     * Starts this service to perform action Check with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCheck(Context context, String param1) {
        Intent intent = new Intent(context, CheckBalanceIntentService.class);
        intent.setAction(ACTION_CHECK);
        context.startService(intent);
    }


    public CheckBalanceIntentService() {
        super("CheckBalanceIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK.equals(action)) {
                handleActionCheck();
            }
        }
    }

    /**
     * Handle action Check in the provided background thread with the provided
     * parameters.
     * This method fetches the Bitcoin Address from the Preferences and queries blockchain.info for the Balance of the Address
     * Then it checks if enough satoshis have been added by comparing with the old Balance fetched also from the shared Preferences.
     * If all conditions are met, the Service sets the Ringtone Volume to maximum.
     */
    private void handleActionCheck() {

        URL url;
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String address = preferences.getString("address", "empty");

            //Checks the Validity of the Address
            // A Bitcoin address, or simply address, is an identifier of 26-35 alphanumeric characters, beginning with the number 1 or 3, that represents a possible destination for a Bitcoin payment.
            if (address.equals("empty") || address.length() < 26 || address.length() > 35){
                Log.d(LOG_TAG, "Address Empty or Incorrect Length");
                return;
            }
            else if (!(address.substring(0,1).equals("3") ^ address.substring(0,1).equals("1")) ){
                Log.d(LOG_TAG, "Address must start with a 3 or 1");
                return;
            }
            //Set URL
            url = new URL("https://blockchain.info/q/addressbalance/" + address);

            //Send HTTP Get request and get the result
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }

            //This is the Current Balance denominated in Satoshis
            int currentBalance = Integer.parseInt(total.toString());

            if (currentBalance != 0){
                //Get Balance form shared preferences
                int oldBalance = preferences.getInt("oldBalance", 0);

                //Get Increment to trigger the Volume increase
                int incrementBit = preferences.getInt("increment", 100);
                int incrementSatoshi = incrementBit * 100;

                //Check if sufficient Satoshis have been added
                if ((currentBalance - oldBalance) >= incrementSatoshi){
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    int maxvalue = audioManager.getStreamMaxVolume(audioManager.STREAM_RING);

                    //Check if Volume is already at Max
                    if (maxvalue != audioManager.getStreamVolume(AudioManager.STREAM_RING)){
                        //Set Volume to Max
                        audioManager.setStreamVolume(audioManager.STREAM_RING, maxvalue, 0);
                        Log.d(LOG_TAG, "Volume turned on");
                    }
                }
                //Set new Balance
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("oldBalance", currentBalance);
                editor.commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "ActionCalled");
    }
}
