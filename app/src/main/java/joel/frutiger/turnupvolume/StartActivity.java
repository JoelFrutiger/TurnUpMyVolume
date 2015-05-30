package joel.frutiger.turnupvolume;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class StartActivity extends Activity {

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
     * In MainActivty we get the result.
     *
     * @param view          View from onClickEvent
     * @see MainActivity    Activity Result returned
     */
    public void onClickStartService(View view){


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();
        //temp

        editor.putString("address", "1EzwoHtiXB4iFwedPr49iywjZn2nnekhoj");
        editor.commit();

        //Starts MainActivity
        Intent returnIntent = new Intent();
        setResult(RESULT_OK,returnIntent);
        finish();
    }

}
