package gitlet;

import ucb.junit.textui;
//import org.junit.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * The suite of all JUnit tests for the gitlet package.
 *
 * @author
 */
public class UnitTest {

    /**
     * Run the JUnit tests in the loa package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /**
     * A dummy test to avoid complaint.
     */
    @Test
    public void placeholderTest() {
    }
/*
    @org.junit.Test
    public void testConstructor() {
        Measurement x = new Measurement();
        assertTrue(x.getFeet() == 0);
        assertTrue(x.getInches() == 0);
        Measurement y = new Measurement(1, 12);
        assertTrue(x.getInches() == 0);
        assertTrue(x.getFeet() == 2);
    }

    @Test
    public void testGetFeet() throws Exception {
        Measurement x = new Measurement(5);
        assertTrue(x.getFeet() == 5);
    }

    @Test
    public void testGetInches() throws Exception {
        Measurement x = new Measurement(1,2);
        assertTrue(x.getInches() == 2);
    }

    @Test
    public void testPlus() throws Exception {
        Measurement x = new Measurement(1, 2);
        Measurement y = new Measurement(3, 4);
        Measurement z = new Measurement(4, 6);
        Measurement xPlusY = x.plus(y);
        assertTrue(xPlusY.getFeet() == z.getFeet());
        assertTrue(xPlusY.getInches() == z.getInches());
    }

    @Test
    public void testMinus() throws Exception {
        Measurement x = new Measurement(1, 2);
        Measurement y = new Measurement(3, 4);
        Measurement z = new Measurement(2, 2);
        Measurement yMinusX = y.minus(x);
        assertTrue(yMinusX.getFeet() == z.getFeet());
        assertTrue(yMinusX.getInches() == z.getInches());
    }

    @Test
    public void testMultiple() throws Exception {
        Measurement x = new Measurement(1, 2);
        Measurement z = new Measurement(3, 6);
        Measurement xTimesThree = x.multiple(3);
        assertTrue(xTimesThree.getFeet() == z.getFeet());
        assertTrue(xTimesThree.getInches() == z.getInches());
    }

    @Test
    public void testToString() throws Exception {
        Measurement x = new Measurement(1, 2);
        assertTrue(x.toString() == "1'2\"");
    }
    */
}
