package joel.frutiger.turnupvolume;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by chef on 5/31/15.
 */
public class TestCase
        extends ActivityInstrumentationTestCase2<StartActivity> {


    private StartActivity mStartActivity;
    private Button mStartButton;
    private EditText mEditTextAddress;
    private TextView mHead;

    public TestCase() {
        super(StartActivity.class);
    }

    /**
     * Sets up the test fixture for this test case. This method is always called before every test
     * run.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //Sets the initial touch mode for the Activity under test. This must be called before
        //getActivity()
        setActivityInitialTouchMode(true);

        //Get a reference to the Activity under test, starting it if necessary.
        mStartActivity = getActivity();

        //Get references to all views
        mStartButton = (Button) mStartActivity.findViewById(R.id.button2);
        mEditTextAddress = (EditText) mStartActivity.findViewById(R.id.editText);
        mHead = (TextView) mStartActivity.findViewById(R.id.textView5);
    }

    /**
     * Tests the preconditions of this test fixture.
     */
    @MediumTest
    public void testPreconditions() {
        assertNotNull("mStartActivity is null", mStartActivity);
        assertNotNull("mStartButton is null", mStartButton);
        assertNotNull("mHead is null", mHead);
        assertNotNull("mEdidTextAddress is null", mEditTextAddress);
    }

    @MediumTest
    public void testClickMeButton_layout() {
        //Retrieve the top-level window decor view
        final View decorView = mStartActivity.getWindow().getDecorView();

        //Verify that the mClickMeButton is on screen
        ViewAsserts.assertOnScreen(decorView, mStartButton);

        //Verify width and heights
        final ViewGroup.LayoutParams layoutParams = mStartButton.getLayoutParams();
        assertNotNull(layoutParams);
        assertEquals(layoutParams.width, WindowManager.LayoutParams.WRAP_CONTENT);
        assertEquals(layoutParams.height, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @MediumTest
    public void testClickMeButton_labelText() {
        //Verify that mClickMeButton uses the correct string resource
        final String expectedNextButtonText = mStartActivity.getString(R.string.start);
        final String actualNextButtonText = mStartButton.getText().toString();
        assertEquals(expectedNextButtonText, actualNextButtonText);
    }

    @MediumTest
    public void testInfoTextView_layout() {
        //Retrieve the top-level window decor view
        final View decorView = mStartActivity.getWindow().getDecorView();

        //Verify that the mInfoTextView is on screen and is not visible
        ViewAsserts.assertOnScreen(decorView, mHead);
        assertTrue(View.VISIBLE == mHead.getVisibility());
    }

}