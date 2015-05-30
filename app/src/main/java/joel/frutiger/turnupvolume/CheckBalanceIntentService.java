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
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CheckBalanceIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CHECK = "joel.frutiger.turnupvolume.action.CHECK";
    private static final String LOG_TAG = "CheckBalanceIntent";


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
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
     * Handle action Foo in the provided background thread with the provided
     * parameters.
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

            //url = new URL("https://blockchain.info/q/addressbalance/1EzwoHtiXB4iFwedPr49iywjZn2nnekhoj");
            url = new URL("https://blockchain.info/q/addressbalance/" + address);
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


                int oldBalance = preferences.getInt("oldBalance", 0);

                if (oldBalance == 0){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("oldBalance", currentBalance);
                    editor.commit();
                }
                else{
                    int increment = preferences.getInt("increment", 10000);
                    //Check if sufficient Satoshis have been added
                    if ((currentBalance - oldBalance) >= increment){
                        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                        int maxvalue = audioManager.getStreamMaxVolume(audioManager.STREAM_RING);
                        //Check if Volume is already at Max
                        if (maxvalue != audioManager.getStreamVolume(AudioManager.STREAM_RING)){
                            audioManager.setStreamVolume(audioManager.STREAM_RING, maxvalue, 0);
                            Log.d(LOG_TAG, "Volume turned on");
                        }

                    }
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("oldBalance", currentBalance);
                    editor.commit();

                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "ActionCalled");

    }

}
