package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

/**
 * Creates a staging area for files that
 * need to be added or removed in the
 * next commit.
 * @author Megan Mehta
 */

public class Stage implements Serializable {
    /** Holds the <fileName, blobSHA> for files to be
     * added in the next commit.*/
    private TreeMap<String, String> addedFiles;

    /** Holds the <fileName, blobSHA> for files to be
     * removed in the next commit.*/
    private TreeMap<String, String> removedFiles;

    public Stage() {
        addedFiles = new TreeMap<>();
        removedFiles = new TreeMap<>();
    }

    public void clear() {
        addedFiles = new TreeMap<>();
        removedFiles = new TreeMap<>();
    }

    public TreeMap<String, String> getAddedFiles() {
        return this.addedFiles;
    }

    public TreeMap<String, String> getRemovedFiles() {
        return this.removedFiles;
    }

    public void addToFile(String fileName, String blobSHA) {
        addedFiles.put(fileName, blobSHA);
    }

    public void addToRemoveStage(String fileName, String blobSHA) {
        removedFiles.put(fileName, blobSHA);
    }
}
