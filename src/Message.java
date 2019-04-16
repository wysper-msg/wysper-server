import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Message datatype for representing messages that users have sent to the server */
public class Message {
  private String username; // Username of the sender
  private String body; // Message body
  private Timestamp timestamp; // Time the message was sent

  /**
   * Standard message constructor. Adds timestamp on creation
   *
   * @param username username of sender
   * @param text message body
   */
  Message(String username, String text) {
    this.username = username;
    this.body = text;
    this.timestamp = new Timestamp(System.currentTimeMillis());
  }

  /**
   * Message constructor with a manual time Adds timestamp on creation
   *
   * @param username username of sender
   * @param text message body
   */
  Message(String username, String text, Timestamp time) {
    this.username = username;
    this.body = text;
    this.timestamp = time;
  }

  /**
   * Message constructor with a message in JSON form
   *
   * @param j a json representation of a message
   */
  Message(JSONObject j) {
    if (j.containsKey("username")) {
      this.username = j.get("username").toString();
    }
    if (j.containsKey("timestamp")) {
      this.timestamp = Timestamp.valueOf(j.get("timestamp").toString());
    } else {
      this.timestamp = new Timestamp(System.currentTimeMillis());
    }
    if (j.containsKey("body")) {
      this.body = j.get("body").toString();
    }
  }

  /**
   * Create a string of spaces of a given length
   *
   * @param size of the space string to make
   * @return a string of size size of all spaces
   */
  private String makeSpaceString(int size) {
    String ret = new String();
    for (int i = 0; i < size; i++) {
      ret += " ";
    }
    return ret;
  }

  /**
   * toString implementation
   *
   * @return String form of this method
   */
  @Override
  public String toString() {
    int wrapLen = 50;
    int userPad = 10;
    String[] bodyArray = this.body.split("\\s+");

    String ret = "";
    ret += this.username;
    ret += makeSpaceString(userPad - this.username.length());

    int lineCount = 0;
    for (int i = 0; i < bodyArray.length; i++) {
      // if this word fits on the current line
      if (bodyArray[i].length() + lineCount < wrapLen - 1) {
        ret += bodyArray[i] + " ";
        lineCount += bodyArray[i].length() + 1;
      }
      // if we can fit this word on a newline
      else if (bodyArray[i].length() < wrapLen - 1) {
        ret += "\n" + makeSpaceString(userPad) + bodyArray[i] + " ";
        lineCount = bodyArray[i].length() + 1;
      }
      // if this word is larger than wrapLen
      else {
        int ind = wrapLen - lineCount - 1;
        ret += bodyArray[i].substring(0, wrapLen - lineCount - 1) + "-\n";
        ret += makeSpaceString(userPad);
        while (bodyArray[i].substring(ind).length() > wrapLen - 1) {
          ret += bodyArray[i].substring(ind, ind + wrapLen - 1) + "-\n";
          ret += makeSpaceString(userPad);
          ind += wrapLen;
        }
        ret += bodyArray[i].substring(ind) + " ";
        lineCount = bodyArray[i].substring(ind).length() + 1;
      }
    }
    ret += makeSpaceString(wrapLen - lineCount + 2);
    ret += new SimpleDateFormat("hh:mm:ss").format(timestamp);

    return ret;
  }

  /**
   * Build a JSON representation of this message
   *
   * @return a json representation of the message
   */
  JSONObject toJSON() {
    JSONObject j = new JSONObject();
    j.put("username", this.username);
    j.put("body", this.body);
    j.put("timestamp", this.timestamp.toString());

    return j;
  }

  /**
   * Get the username from this message
   *
   * @return the username who created the message
   */
  String getUsername() {
    return this.username;
  }

  /**
   * Get the message body
   *
   * @return the body of the message
   */
  String getBody() {
    return this.body;
  }

  /**
   * Get the time that this message was created
   *
   * @return a Timestamp object of when the message was created
   */
  Timestamp getTimestamp() {
    return this.timestamp;
  }
}
