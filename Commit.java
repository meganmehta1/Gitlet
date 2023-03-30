package gitlet;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;
import java.io.Serializable;

/** Commit object that tracks all info that must be
 * held by a commmit for future reference.
 * @author Megan Mehta
 */

public class Commit implements Serializable {
    /***  Holds commit message.*/
    private String msg;

    /***  Holds timestamp.*/
    private String timestamp;

    /***  Holds SHA-1 (the commitID).*/
    private String mySHA;

    /***  Holds parent commit's SHA.*/
    private String parentSHA;

    /***  Holds <fileName, fileSHA> for each file tracked by this commit.*/
    private TreeMap<String, String> blobs;

    /***  Holds parent commit object, if it exists.*/
    private Commit parent;

    /***  Holds name of branch this commit is on.*/
    private String branch;

    /**
     * Constructor for the commit class.
     * @param message commit message
     * @param myParent parent commit, it not initial commit
     * @param blobMap TreeMap that holds all the blobs being tracked
     * @param myBranch branch this commit is on
     */
    public Commit(String message, Commit myParent,
                  TreeMap<String, String> blobMap, String myBranch) {
        this.msg = message;
        this.parent = myParent;
        this.blobs = blobMap;
        this.branch = myBranch;

        ZonedDateTime currDateTime = ZonedDateTime.now();
        this.timestamp = currDateTime.format
                (DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z"));

        this.mySHA = Utils.sha1(Utils.serialize(this));

        if (parent != null) {
            this.parentSHA = parent.mySHA;
        } else {
            this.parentSHA = null;
        }

    }

    public String getMsg() {
        return this.msg;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getMySHA() {
        return this.mySHA;
    }

    public String getParentSHA() {
        return this.parentSHA;
    }

    public Commit getParent() {
        return this.parent;
    }

    public void setParent(Commit setParent) {
        this.parent = setParent;
    }

    public TreeMap getBlobs() {
        return this.blobs;
    }

    public String getBranch() {
        return this.branch;
    }

    public void setBranch(String setBranch) {
        this.branch = setBranch;
    }
}
