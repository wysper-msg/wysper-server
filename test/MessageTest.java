import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.sql.Time;
import java.sql.Timestamp;

import static org.junit.Assert.*;

public class MessageTest {

    Message msg;

    /**
     * PRECONDITIONS: This constructor must be implemented correctly
     */
    @Before
    public void setup() {
        msg = new Message("trevor", "test", new Timestamp(0));
    }

    /**
     * Test constructors
     * PRECONDITION: toString must be implemented correctly
     */
    @Test
    public void JSONConstructorTest() {
        JSONObject input = new JSONObject();
        input.put("username", "trevor");
        input.put("body", "test");
        input.put("timestamp", new Timestamp(0).toString());
        Message expected = msg;
        Message result = new Message(input);
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void JSONConstructorWithNoTimestampTest() {
        JSONObject input = new JSONObject();
        input.put("username", "trevor");
        input.put("body", "test");
        Message expected = new Message("trevor", "test", new Timestamp(System.currentTimeMillis()));
        Message result = new Message(input);
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void NoTimestampConstructorTest() {
        // caveat: since the timestamp is determined to millisecond accuracy in the constructor,
        // it is impossible to make an expected result exactly what it should be. ToString compares
        // to the second, though, so that should be good enough
        Message expected = new Message("trevor", "test", new Timestamp(System.currentTimeMillis()));
        Message result = new Message("trevor", "test");
        assertEquals(expected.toString(), result.toString());
    }

    /**
     * Test getters
     */

    @Test
    public void getUsernameTest() {
        String expected = "trevor";
        String result = msg.getUsername();
        assertEquals(expected, result);
    }

    @Test
    public void getBodyTest() {
        String expected = "test";
        String result = msg.getBody();
        assertEquals(expected, result);
    }

    @Test
    public void getTimestampTest() {
        Timestamp expected = new Timestamp(0);
        Timestamp result = msg.getTimestamp();
        assertEquals(expected, result);
    }

    /**
     * Test converters
     */

    @Test
    public void toStringTest() {
        String expected = "trevor    test                                                07:00:00";
        String result = msg.toString();
        assertEquals(expected, result);
    }

    @Test
    public void toJSONTest() {
        JSONObject expected = new JSONObject();
        expected.put("username", "trevor");
        expected.put("body", "test");
        expected.put("timestamp", new Timestamp(0).toString());
        JSONObject result = msg.toJSON();
        assertEquals(expected, result);
    }
}