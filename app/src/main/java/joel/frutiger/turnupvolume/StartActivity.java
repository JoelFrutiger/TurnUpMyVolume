package joel.frutiger.turnupvolume;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class StartActivity extends Activity {

    private static final String LOG_TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    /**
     * This method is called when the Start Button has been clicked.
     * It starts the Service and starts MainActivity.
     * In MainActivty we get the result. Also Checks the Validity of the Bitcoin Address
     *
     * @param view          View from onClickEvent
     * @see MainActivity    Activity Result returned
     */
    public void onClickStartService(View view){

        //Getting the Address from User input
        EditText editText   = (EditText)findViewById(R.id.editText);
        String address = editText.getText().toString();

        //Checks the Validity of the Address
        // A Bitcoin address, or simply address, is an identifier of 26-35 alphanumeric characters, beginning with the number 1 or 3, that represents a possible destination for a Bitcoin payment.
        if (address.equals("empty") || address.length() < 26 || address.length() > 35){
            CharSequence text = "Address Empty or Incorrect Length";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
            Log.d(LOG_TAG, "Address Empty or Incorrect Length");
            return;
        }
        else if (!(address.substring(0,1).equals("3") ^ address.substring(0,1).equals("1")) ){
            CharSequence text = "Address must start with a 3 or 1";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
            Log.d(LOG_TAG, "Address must start with a 3 or 1");
            return;
        }
        //Saving the Address in the shared prefferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("address", "1EzwoHtiXB4iFwedPr49iywjZn2nnekhoj");
        editor.commit();

        //Starts MainActivity
        Intent returnIntent = new Intent();
        setResult(RESULT_OK,returnIntent);
        finish();
    }

}
