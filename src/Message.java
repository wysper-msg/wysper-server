package src;

import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Message data that needs to be seen on the screen
 */
public class Message{
    String username;                // Username of the sender
    String body;             // Message body
    Timestamp timestamp;    // Time the message was sent

    /**
     * Standard message constructor
     * Adds timestamp on creation
     * @param username username of sender
     * @param text message body
     */
    public Message(String username, String text) {
        this.username = username;
        this.body = text;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Message constructor with a manual time
     * Adds timestamp on creation
     * @param username username of sender
     * @param text message body
     * NOTE: need another way to pass time manually, not sure how to use Timestamp library
     *             to get a time from a string
     */
    public Message(String username, String text, Timestamp time) {
        this.username = username;
        this.body = text;
        this.timestamp = time;
    }

    /**
     * @param j a json representation of a message
     */
    public Message(JSONObject j) {
        if (j.containsKey("username")) {
            this.username = j.get("username").toString();
        }
        if (j.containsKey("timestamp")) {
            this.timestamp = Timestamp.valueOf(j.get("timestamp").toString());
        }
        else {
            this.timestamp = new Timestamp(System.currentTimeMillis());
        }
        if (j.containsKey("body")) {
            this.body = j.get("body").toString();
        }
    }

    /**
     * @param size of the space string to make
     * @return a string of size size of all spaces
     */
    public String makeSpaceString(int size) {
        String ret = new String();
        for (int i = 0; i < size; i++) {
            ret += " ";
        }
        return ret;
    }
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
                    ret += bodyArray[i].substring(ind, ind+wrapLen-1) + "-\n";
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
     * @return a json representation of the message
     */
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("username", this.username);
        j.put("body", this.body);
        j.put("timestamp", this.timestamp.toString());

        return j;
    }

    /**
     * @return the username who created the message
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @return the body of the message
     */
    public String getBody() {
        return this.body;
    }

    /**
     * @return a LocalDateTime object of when the message was created
     */
    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public static void main(String[] args) {
        System.out.println("Message main");
        String s2 = "What if I pass a loooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooong " +
                "string?";

        String s3 = "Hello from wysper";
        Message msg2 = new Message("Tom", s3);
        Message msg1 = new Message("Tom", s2);
        Message msg3 = new Message("Corey", "What's up");
        Message msg4 = new Message("Tom", "Looks like the text wraps!");
        System.out.println(msg2);
        System.out.println(msg3);
        System.out.println(msg1);
        System.out.println(msg4);
    }


}