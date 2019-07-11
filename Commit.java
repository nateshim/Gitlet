package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Commit implements Serializable {
    // Constructor
    public Commit(String parentId, String myMessage) { //might also have branch argument
        parent = parentId;
        message = myMessage;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(this);
            objectStream.close();
            sha = Utils.sha1(stream.toByteArray());
        } catch (IOException excp) {
            System.out.println("Internal error serializing commit.");
        }
        LocalDate localDate = LocalDate.now();
        String formattedDate = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        LocalTime localTime = LocalTime.now();
        String formattedTime = localTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
        commitDate = formattedDate + " " + formattedTime.substring(0, 8);
    }

// Fields
    /**
     * The SHA-1 identifier of my parent, or null if I am the initial commit.
     */
    private final String parent;
    /**
     * My log message.
     */
    private final String message;
    /**
     * My timestamp. (java.util.Date)
     */
    private String commitDate;
    /**
     * A mapping of file names to the SHA-1's of their blobs.
     */             //key == name  value == SHA-1(HashCode)
    private HashMap<String, String> contents = new HashMap<>();
    private String sha;


    // Methods
    public String toString() {
        return message;
    }

    public HashMap<String, String> getContents() {
        return contents;
    }

    public void setContents(HashMap<String, String> h) {
        contents = h;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getCommitDate() {
        return commitDate;
    }

    /**
     * Get SHA-1 identifier of my parent, or null if I am the initial commit.
     */
    public String getParent() {
        return parent;
    }

    /**
     * Finalize me and write me to my repository.
     */
    public String getSHA() {
        return sha;
    }

    @Override
    public boolean equals(Object b) {
        Commit c = (Commit) b;
        return this.getSHA().equals(c.getSHA());
    }

}
