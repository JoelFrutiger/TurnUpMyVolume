package joel.frutiger.turnupvolume;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    private static final int STARTACTIVITY_REQUEST = 0;
    private static final String LOG_TAG = "MainActivity";
    private static final String ACTION_CHECK = "joel.frutiger.turnupvolume.action.CHECK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the view
        setContentView(R.layout.activity_main);

        //Getting the Addresss from shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String address = preferences.getString("address", "empty");

        //If Address is empty we call StartActivity to let the user input the Address
        if(address.equals("empty")){
            Log.d(LOG_TAG, "Empty address starting StartActivity");
            //Starting StartActivity to let the User Input the Bitcoin Address
            Intent intent = new Intent(this, StartActivity.class);
            startActivityForResult(intent, STARTACTIVITY_REQUEST);
        }

        //Getting the Balance stored in the shared preferences
        int balance = preferences.getInt("oldBalance", 0);
        int balanceInBits = balance / 100;

        //Setting the TextView
        TextView currentBalanceTextView = (TextView) findViewById(R.id.currentBalance);
        currentBalanceTextView.setText(Integer.toString(balanceInBits) + " Bits");

        //Starting a pending Intent in order to check if the alarm is already set
        Intent intentpend = new Intent(this, CheckBalanceIntentService.class);
        intentpend.setAction(ACTION_CHECK);
        boolean alarmUp = (PendingIntent.getService(this, 123,
                intentpend,
                PendingIntent.FLAG_NO_CREATE) != null);

        //If alarm is set initiate approriate UI changes
        if (alarmUp)
        {
            Log.d(LOG_TAG,"Service is running");
            changeUIRunning();

        }
        //If alarm is not set initiate appropriate UI changes
        else{
            Log.d(LOG_TAG, "Service is stopped");
            changeUIStopped();
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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

        //Start Settings Activity
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *  Called when an activity that is launched exits, giving you the requestCode you started it with.
     *  The resultCode it returned, and any additional data from it.
     *  Here we set the Alarm Manager to Start the Service every 15 Minutes.
     *  Also getting the Balance to Display on the UI.
     * @param requestCode           Request Code
     * @param resultCode            Result OK oder Result Canceled
     * @param data                  The Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == STARTACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                //Set the Intent to start the Service
                Intent intent = new Intent(this, CheckBalanceIntentService.class);
                intent.setAction(ACTION_CHECK);
                PendingIntent pintent = PendingIntent.getService(this, 123, intent, 0);

                //Start the Alarm Manager to call the Service every 15 minutes
                AlarmManager alarmMgr = (AlarmManager)this.getSystemService(ALARM_SERVICE);
                alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES, pintent);

                //Initial Check
                initialCheck();

                //Get the Balance form shared preferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                int balance = preferences.getInt("oldBalance", 0);

                //Satoshis to bits conversion
                int balanceInBits = balance / 100;

                //Set the textView
                TextView currentBalanceTextView = (TextView) findViewById(R.id.currentBalance);
                currentBalanceTextView.setText(Integer.toString(balanceInBits) + " Bits");
                changeUIRunning();
                Log.d(LOG_TAG, "Result from StartActivity OK");
            }
        }
    }

    /**
     * This method is called when the Start Service Button has been clicked.
     * It starts the Alarm Manager.
     * In MainActivty we get the result.
     * @param view          View from onClickEvent
     *
     */
    public void onClickStartStoppedService(View view){
        //Set the Intent to start the Service
        Intent intent = new Intent(this, CheckBalanceIntentService.class);
        intent.setAction(ACTION_CHECK);
        PendingIntent pintent = PendingIntent.getService(this, 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Start the Alarm Manager to call the Service every 15 minutes
        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pintent);

        //InitialCheck
        initialCheck();

        //Initiate approriate UI changes
        changeUIRunning();

        Log.d(LOG_TAG, "onCLickStartStoppedService called");
    }

    /**
     * This method is called when the Stop Service Button has been clicked.
     * It stops the AlarmManager.
     * In MainActivty we get the result.
     * @param view          View from onClickEvent
     *
     */
    public void onClickStopStartedService(View view){
        //Set the Intent
        Intent intent = new Intent(this, CheckBalanceIntentService.class);
        intent.setAction(ACTION_CHECK);
        PendingIntent pintent = PendingIntent.getService(this, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Cancle Alarm for the Intent
        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pintent);
        //Initiate approriate UI changes
        changeUIStopped();

        Log.d(LOG_TAG, "onCLickStopStartedService called");
    }

    /**
     * This method Changes the UI to reflect the Service Status and allows to Stop the Service.
     */
    private void changeUIRunning(){
        //Set the TextView
        TextView serviceStatusTextView = (TextView) findViewById(R.id.serviceStatusTextView);
        serviceStatusTextView.setText("Service is running...");
        serviceStatusTextView.setTextColor(Color.GREEN);

        //Change the UI in order to allow the User to stop the Service
        View b = findViewById(R.id.stopService);
        b.setVisibility(View.VISIBLE);
        View a = findViewById(R.id.startService);
        a.setVisibility(View.INVISIBLE);
    }

    /**
     * This method Changes the UI to reflect the Service Status and allows to Start the Service.
     */
    private void changeUIStopped(){
        //Set the TextView
        TextView serviceStatusTextView = (TextView) findViewById(R.id.serviceStatusTextView);
        serviceStatusTextView.setText("Service is stopped...");
        serviceStatusTextView.setTextColor(Color.RED);

        //Change the UI in order to allow the User to start the Service
        View b = findViewById(R.id.startService);
        b.setVisibility(View.VISIBLE);
        View a = findViewById(R.id.stopService);
        a.setVisibility(View.INVISIBLE);
    }

    /**
     * This method fetches the Bitcoin Address from the Preferences and queries blockchain.info for the Balance of the Address
     * Then it checks if enough satoshis have been added by comparing with the old Balance fetched also from the shared Preferences.
     * If all conditions are met, the Service sets the Ringtone Volume to maximum.
     * Gets called when getting the Result from StartActivity and when Pressing the Start Service Button.
     */
    private void initialCheck(){
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
                String incrementBitString = preferences.getString("increment", "100");
                int incrementBitInt = Integer.parseInt(incrementBitString);
                int incrementSatoshi = incrementBitInt * 100;


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
            CharSequence text = "Failed to Connect";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
            e.printStackTrace();
        }
    }

}
