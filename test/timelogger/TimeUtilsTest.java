package timelogger;

import junit.framework.TestCase;

/**
 *
 * @author jdalbey
 */
public class TimeUtilsTest extends TestCase
{
    public TimeUtilsTest(String testName)
    {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testOne()
    {
        assertEquals(1, TimeUtils.getDelta("1000", "1001"));
        assertEquals(59, TimeUtils.getDelta("1000", "1059"));
        assertEquals(60, TimeUtils.getDelta("1000", "1100"));
        assertEquals(61, TimeUtils.getDelta("1000", "1101"));
        assertEquals(1, TimeUtils.getDelta("1358", "1359"));
        assertEquals(3, TimeUtils.getDelta("1358", "1401"));
        assertEquals(42, TimeUtils.getDelta("1358", "1440"));
    }

    public void testTwo()
    {
        assertEquals(1, TimeUtils.getDelta("0001", "0000"));
        assertEquals(59, TimeUtils.getDelta("0000", "0059"));
        assertEquals(59, TimeUtils.getDelta("2300", "2359"));
        assertEquals(60, TimeUtils.getDelta("2259", "2359"));
        assertEquals(121, TimeUtils.getDelta("1100", "1301"));
    }

    public void testInvalid()
    {
        assertEquals(0, TimeUtils.getDelta("2260", "2300"));
        assertEquals(0, TimeUtils.getDelta("2359", "2400"));
        assertEquals(0, TimeUtils.getDelta("2525", "2626"));
    }
}
