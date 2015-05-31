package joel.frutiger.turnupvolume;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

/**
 * Created by chef on 5/31/15.
 */
public class TestCase extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mFirstTestActivity;
    private TextView mFirstTestText;

    public TestCase() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFirstTestActivity = getActivity();
        mFirstTestText =
                (TextView) mFirstTestActivity
                        .findViewById(R.id.currentBalance);
    }

    public void testMyFirstTestTextView_labelText() {
        final String expected = "0 Bits";
        final String actual = mFirstTestText.getText().toString();
        assertEquals(expected, actual);
    }

}
