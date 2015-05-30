package joel.frutiger.turnupvolume;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    static final int STARTACTIVITY_REQUEST = 0;
    static final String LOG_TAG = "MainActivity";
    private static final String ACTION_CHECK = "joel.frutiger.turnupvolume.action.CHECK";
    private static final String EXTRA_ADDRESS = "joel.frutiger.turnupvolume.extra.Address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String address = preferences.getString("address", "empty");

        if(address.equals("empty")){
            Log.d(LOG_TAG, "Empty address starting StartActivity");
            //Starting StartActivity to let the User Input the Bitcoin Address
            Intent intent = new Intent(this, StartActivity.class);
            startActivityForResult(intent, STARTACTIVITY_REQUEST);
        }

        int balance = preferences.getInt("oldBalance", 0);
        int balanceInBits = balance / 100;
        TextView currentBalanceTextView = (TextView) findViewById(R.id.currentBalance);
        currentBalanceTextView.setText(Integer.toString(balanceInBits) + " Bits");

        Intent intent = new Intent(this, CheckBalanceIntentService.class);
        intent.setAction(ACTION_CHECK);
        boolean alarmUp = (PendingIntent.getService(this, 0,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp)
        {
            Log.d(LOG_TAG,"Service is running");
            changeUIRunning();

        }
        else{
            Log.d(LOG_TAG, "Service is stopped");
            changeUIStopped();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == STARTACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {

                Intent intent = new Intent(this, CheckBalanceIntentService.class);
                intent.setAction(ACTION_CHECK);
                PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

                AlarmManager alarmMgr = (AlarmManager)this.getSystemService(this.ALARM_SERVICE);

                alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES, pintent);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                int balance = preferences.getInt("oldBalance", 0);
                int balanceInBits = balance / 100;
                TextView currentBalanceTextView = (TextView) findViewById(R.id.currentBalance);
                currentBalanceTextView.setText(Integer.toString(balanceInBits) + " Bits");

                Log.d(LOG_TAG, "Result from StartActivity OK");
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(this.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is called when the Start Service Button has been clicked.
     * It starts the Service.
     * In MainActivty we get the result.
     * @param view          View from onClickEvent
     *
     */
    public void onClickStartStoppedService(View view){

        Intent intent = new Intent(this, CheckBalanceIntentService.class);
        intent.setAction(ACTION_CHECK);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(this.ALARM_SERVICE);

                /*alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        AlarmManager.INTERVAL_HALF_HOUR,
                        AlarmManager.INTERVAL_HALF_HOUR, pintent);*/
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pintent);
        changeUIRunning();
    }

    public void onClickStopStartedService(View view){
        Log.d(LOG_TAG, "onCLickStopStartedService called");
        Intent intent = new Intent(this, CheckBalanceIntentService.class);
        intent.setAction(ACTION_CHECK);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(this.ALARM_SERVICE);

        alarmMgr.cancel(pintent);
        changeUIStopped();

    }

    private void changeUIRunning(){
        TextView serviceStatusTextView = (TextView) findViewById(R.id.serviceStatusTextView);
        serviceStatusTextView.setText("Service is running...");
        serviceStatusTextView.setTextColor(Color.GREEN);
        //Change the UI in order to allow the User to start the Service
        View b = findViewById(R.id.stopService);
        b.setVisibility(View.VISIBLE);
        View a = findViewById(R.id.startService);
        a.setVisibility(View.INVISIBLE);
    }

    private void changeUIStopped(){
        TextView serviceStatusTextView = (TextView) findViewById(R.id.serviceStatusTextView);
        serviceStatusTextView.setText("Service is stopped...");
        serviceStatusTextView.setTextColor(Color.RED);

        //Change the UI in order to allow the User to start the Service
        View b = findViewById(R.id.startService);
        b.setVisibility(View.VISIBLE);
        View a = findViewById(R.id.stopService);
        a.setVisibility(View.INVISIBLE);
    }

}
