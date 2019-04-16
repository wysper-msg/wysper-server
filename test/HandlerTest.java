import com.sun.org.apache.xml.internal.security.Init;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

public class HandlerTest {

  private DbWrapper testDB;
  private InitHandler init;
  private NMesgHandler nmesg;
  private SendHandler send;
  private PullHandler pull;

  private ArrayList<Message> messages;

  // PRECONDITIONS: DbWrapper tests completed successfully
  //                Message tests completed successfully
  @Before
  public void setup() {
    // init database
    testDB = new DbWrapper("testdb", true);
    // init handlers
    init = new InitHandler(testDB);
    nmesg = new NMesgHandler(testDB);
    send = new SendHandler(testDB);
    pull = new PullHandler(testDB);
    messages = new ArrayList<>();
    // insert some users into the database
    testDB.insertUser("Trevor");
    testDB.insertUser("Alex");
    testDB.insertUser("Connor");
    testDB.insertUser("Corey");
    testDB.insertUser("Aayushi");
    // insert some messages into the database
    testDB.insertMessage(new Message("Trevor", "Testing the server", new Timestamp(0)));
    testDB.insertMessage(new Message("Alex", "Looking good so far", new Timestamp(1000)));
    testDB.insertMessage(new Message("Connor", "Epoch is cool", new Timestamp(2000)));
    testDB.insertMessage(new Message("Corey", "Welcome to the 70s", new Timestamp(3000)));
    testDB.insertMessage(new Message("Aayushi", "Were the tests successful?", new Timestamp(4000)));
    // save messages that were inserted for easier testing
    messages.add(new Message("Trevor", "Testing the server", new Timestamp(0)));
    messages.add(new Message("Alex", "Looking good so far", new Timestamp(1000)));
    messages.add(new Message("Connor", "Epoch is cool", new Timestamp(2000)));
    messages.add(new Message("Corey", "Welcome to the 70s", new Timestamp(3000)));
    messages.add(new Message("Aayushi", "Were the tests successful?", new Timestamp(4000)));
  }

  // delete the database once the tests are done
  @After
  public void destroy() {
    testDB.cleanup(true);
    wipeDir(new File("./testdb/"));
    System.out.println();
  }

  private void wipeDir(File dir) {
    File[] contents = dir.listFiles();
    if (contents != null) {
      for (File file : contents) {
        wipeDir(file);
      }
    }
    dir.delete();
  }

  /** Test Superclass methods */
  @Test
  public void parseTest() {
    // build input
    String testString = "{\"test\":\"test\"}";
    // build expected result
    JSONObject expected = new JSONObject();
    expected.put("test", "test");
    try {
      // test
      JSONObject result = init.parse(testString);
      // verify
      assertEquals(expected, result);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void marshalMessagesTest() {
    // build input
    ArrayList<Message> msgList = new ArrayList<>();
    msgList.add(new Message("Trevor", "test 1", new Timestamp(0)));
    msgList.add(new Message("Alex", "test 2", new Timestamp(1000)));
    // build expected result
    JSONArray msgs = new JSONArray();
    msgs.add(new Message("Trevor", "test 1", new Timestamp(0)).toJSON());
    msgs.add(new Message("Alex", "test 2", new Timestamp(1000)).toJSON());
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    JSONObject result = init.marshalMessages(msgList);
    // verify
    assertEquals(expected, result);
  }

  /** Test NMesgHandler accessDB method */
  @Test
  public void NMesgGetAllTest() {
    // build input
    String req = "5";
    int numMsgs = Integer.parseInt(req);
    // build expected results
    JSONArray msgs = new JSONArray();
    for (int i = 0; i < numMsgs && i < messages.size(); i++) {
      msgs.add(messages.get(i).toJSON());
    }
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    String result = nmesg.accessDB(req);
    // verify
    assertEquals(expected.toString(), result);
  }

  @Test
  public void NMesgGetMoreThanAllTest() {
    // build input
    String req = "69";
    int numMsgs = Integer.parseInt(req);
    // build expected results
    JSONArray msgs = new JSONArray();
    for (int i = 0; i < numMsgs && i < messages.size(); i++) {
      msgs.add(messages.get(i).toJSON());
    }
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    String result = nmesg.accessDB(req);
    // verify
    assertEquals(expected.toString(), result);
  }

  @Test
  public void NMesgGetLessThanAllTest() {
    // build input
    String req = "2";
    int numMsgs = Integer.parseInt(req);
    // build expected results
    JSONArray msgs = new JSONArray();
    Collections.reverse(messages);
    for (int i = 0; i < numMsgs && i < messages.size(); i++) {
      msgs.add(messages.get(i).toJSON());
    }
    Collections.reverse(msgs);
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    String result = nmesg.accessDB(req);
    // verify
    assertEquals(expected.toString(), result);
  }

  @Test
  public void NMesgGetNoneTest() {
    // build input
    String req = "0";
    int numMsgs = Integer.parseInt(req);
    // build expected results
    JSONArray msgs = new JSONArray();
    for (int i = 0; i < numMsgs && i < messages.size(); i++) {
      msgs.add(messages.get(i).toJSON());
    }
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    String result = nmesg.accessDB(req);
    // verify
    assertEquals(expected.toString(), result);
  }

  @Test
  public void NMesgGetNegativeTest() {
    // build input
    String req = "-1";
    int numMsgs = Integer.parseInt(req);
    // build expected results
    JSONArray msgs = new JSONArray();
    for (int i = 0; i < numMsgs && i < messages.size(); i++) {
      msgs.add(messages.get(i).toJSON());
    }
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    String result = nmesg.accessDB(req);
    // verify
    assertEquals(expected.toString(), result);
  }

  /** Test InitHandler accessDB method */
  @Test
  public void InitHandlerExistingUserTest() {
    // build input
    String user = "Trevor";
    // build expected output
    JSONArray msgs = new JSONArray();
    for (Message msg : messages) {
      msgs.add(msg.toJSON());
    }
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    String result = init.accessDB(user);
    // verify
    assertEquals(expected.toString(), result);
    assertEquals(5, testDB.getLastRead(user));
  }

  @Test
  public void InitHandlerNewUserTest() {
    // build input
    String user = "Johnny";
    // build expected output
    JSONArray msgs = new JSONArray();
    for (Message msg : messages) {
      msgs.add(msg.toJSON());
    }
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    String result = init.accessDB(user);
    // verify
    assertEquals(expected.toString(), result);
    assertEquals(5, testDB.getLastRead(user));
  }

  /** Test SendHandler accessDB method */
  @Test
  public void SendHandlerTest() {
    // build input
    Message msg = new Message("Johnny", "I'm new here!", new Timestamp(10000));
    // build expected output
    JSONArray msgs = new JSONArray();
    msgs.add(msg.toJSON());
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    try {
      // test
      String result = send.accessDB(msg.toJSON().toString());
      // verify
      assertEquals("", result);
      assertEquals(expected.toString(), nmesg.accessDB("1"));
    } catch (Exception e) {
      fail();
    }
  }

  /** Test PullHandler accessDB method */
  @Test
  public void PullHandlerAllMessagesTest() {
    // build input
    String user = "Trevor";
    // build expected output
    JSONArray msgs = new JSONArray();
    for (Message msg : messages) {
      msgs.add(msg.toJSON());
    }
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    String result = pull.accessDB(user);
    // verify
    assertEquals(expected.toString(), result);
  }

  @Test
  public void PullHandlerNoMessagesTest() {
    // build input
    String user = "Trevor";
    // build expected output
    JSONArray msgs = new JSONArray();
    JSONObject expected = new JSONObject();
    expected.put("messages", msgs);
    // test
    testDB.updateUsersLastRead(user);
    String result = pull.accessDB(user);
    // verify
    assertEquals(expected.toString(), result);
  }
}
