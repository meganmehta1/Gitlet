package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static gitlet.Utils.*;

/** This class has the functions for
 * all the Gitlet commands.
 * It uses, changes, and serializes
 * Commit and Stage objects, and blobs
 * to the designated file paths.
 * @author Megan Mehta
 */

public class GitletRepo {
    /** Paths for all directories and File objects
     * to reference all of these directories and files. */
    static final String CURRENT_PATH = "./";

    /** Gitlet directory path as a String. */
    static final String GIT_PATH = ".gitlet/";
    /** Gitlet directory path File object. */
    static final File GITPATH = new File(GIT_PATH);

    /** Commit directory path as a String. */
    static final String COMMIT_PATH = ".gitlet/commits/";
    /** Commit directory path File object. */
    static final File COMMITPATH = new File(COMMIT_PATH);

    /** Stage directory path as a String. */
    static final String STAGE_PATH = ".gitlet/stages/";
    /** Stage directory path File object. */
    static final File STAGEPATH = new File(STAGE_PATH);

    /** Stage file directory path as a String. */
    static final String STAGE_PATH_FILE = ".gitlet/stages/stage.txt";
    /** Stage file directory path File object. */
    static final File STAGEPATHFILE = new File(STAGE_PATH_FILE);

    /** Blobs directory path as a String. */
    static final String BLOB_PATH = ".gitlet/blobs/";
    /** Blob directory path File object. */
    static final File BLOBPATH = new File(BLOB_PATH);

    /** Head directory path as a String. */
    static final String HEAD_PATH = ".gitlet/head/";
    /** Head directory path File object. */
    static final File HEADPATH = new File(HEAD_PATH);

    /** Head file directory path as a String. */
    static final String HEAD_FILE_PATH = ".gitlet/head/head.txt";
    /** Head file directory path File object. */
    static final File HEADFILEPATH = new File(HEAD_FILE_PATH);

    /** Branch directory path as a String. */
    static final String BRANCH_PATH = ".gitlet/branch/";
    /** Branch directory path File object. */
    static final File BRANCHPATH = new File(BRANCH_PATH);

    /** Branch file directory path as a String. */
    static final String BRANCH_FILE_PATH = ".gitlet/branch/branch.txt";
    /** Branch file directory path File object. */
    static final File BRANCHFILEPATH = new File(BRANCH_FILE_PATH);

    /** Current branch directory path Fas a String. */
    static final String CURRENT_BRANCH_PATH = ".gitlet/branch/currBranch/";
    /** Current branch directory path File object. */
    static final File CURRBRANCHPATH = new File(CURRENT_BRANCH_PATH);

    /** Current branch file directory path as a String. */
    static final String CURRENT_BRANCH_FILE_PATH =
            ".gitlet/branch/currBranch/currBranch.txt";
    /** Current branch file directory path File object. */
    static final File CURRBRANCHFILEPATH = new File(CURRENT_BRANCH_PATH);

    /*** Default standard branch name. */
    static final String BRANCHNAME = "master";

    public GitletRepo() {
    }

    public static void init() throws IOException {
        if (checkInitialized()) {
            System.out.println("A Gitlet version-control system "
                + "already exists in the current directory.");
        } else {
            new File(GIT_PATH).mkdir();
            new File(COMMIT_PATH).mkdir();

            new File(STAGE_PATH).mkdir();
            new File(STAGE_PATH_FILE).createNewFile();

            new File(BLOB_PATH).mkdir();

            new File(HEAD_PATH).mkdir();
            new File(HEAD_FILE_PATH).createNewFile();

            new File(BRANCH_PATH).mkdir();
            new File(BRANCH_FILE_PATH).createNewFile();


            TreeMap<String, String> blankBlob = new TreeMap<>();

            Commit initialCommit = new Commit(
                    "initial commit", null, blankBlob, BRANCHNAME);
            Utils.writeObject(Utils.join(COMMIT_PATH,
                    initialCommit.getMySHA() + ".txt"), initialCommit);

            String head = "";
            head = initialCommit.getMySHA();
            writeHEAD(head);

            Stage stage = new Stage();

            TreeMap<String, String> branchMap = new TreeMap<>();
            branchMap.put(BRANCHNAME, readHEAD());

            serializeHSB(head, stage, branchMap);

            Utils.writeContents(CURRBRANCHFILEPATH, BRANCHNAME);
        }

    }

    public static void add(String fileName) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        File addFile = Utils.join(CURRENT_PATH, fileName);
        String blobHash;
        Stage stage = readStage();

        if (addFile.exists()) {
            blobHash = Utils.sha1(Utils.readContents(addFile));

            Utils.writeContents(Utils.join(BLOBPATH,
                    blobHash + ".txt"), Utils.readContents(addFile));

            Commit curr = currCommit();
            if (curr.getBlobs().get(fileName) != null
                    && curr.getBlobs().get(fileName).equals(blobHash)) {
                if (stage.getRemovedFiles().containsKey(fileName)) {
                    stage.getRemovedFiles().remove(fileName);
                }
                if (stage.getAddedFiles().containsKey(fileName)) {
                    stage.getAddedFiles().remove(fileName);
                }
            } else {
                stage.addToFile(fileName, blobHash);
            }
        } else {
            System.out.println("File does not exist.");
        }

        writeStage(stage);
    }

    public static void commit(String msg) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        Stage stage = readStage();
        TreeMap<String, String> branchMap = readBranchMap();
        String currBranch = Utils.readContentsAsString(CURRBRANCHPATH);

        if (stage.getAddedFiles().isEmpty()
            && stage.getRemovedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
        } else {
            Commit curr = currCommit();
            if (curr != null) {
                TreeMap<String, String> blobCopies = curr.getBlobs();
                for (String file: stage.getAddedFiles().keySet()) {
                    if (blobCopies.containsKey(file)) {
                        blobCopies.replace(file,
                            stage.getAddedFiles().get(file));
                    } else {
                        blobCopies.put(file, stage.getAddedFiles().get(file));
                    }
                }

                for (String file: stage.getRemovedFiles().keySet()) {
                    blobCopies.remove(file);
                }

                stage.clear();

                Commit newCommit = new Commit(msg, curr,
                        blobCopies, currBranch);

                String head = newCommit.getMySHA();

                if (branchMap.containsKey(currBranch)) {
                    branchMap.replace(currBranch, head);
                } else {
                    branchMap.put(currBranch, head);
                }

                serializeHSB(head, stage, branchMap);
                Utils.writeContents(CURRBRANCHFILEPATH, currBranch);
                Utils.writeObject(Utils.join(COMMIT_PATH,
                        newCommit.getMySHA() + ".txt"), newCommit);
            }
        }
    }

    public static void remove(String fileName) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        Stage stage = readStage();
        Commit currCommit = currCommit();

        if ((!stage.getAddedFiles().containsKey(fileName)
                && !stage.getRemovedFiles().containsKey(fileName))
                && !currCommit.getBlobs().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
        } else if (currCommit.getBlobs().containsKey(fileName)) {
            stage.addToRemoveStage(fileName,
                    currCommit.getBlobs().get(fileName).toString());
            restrictedDelete(fileName);
        }

        if (stage.getAddedFiles().containsKey(fileName)) {
            stage.getAddedFiles().remove(fileName);
        }

        writeStage(stage);
    }

    public static void log() {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        String currBranch = Utils.readContentsAsString(CURRBRANCHPATH);
        Commit curr = currCommit();
        while (curr != null) {
            System.out.println("===");
            System.out.println("commit " + curr.getMySHA());
            System.out.println("Date: " + curr.getTimestamp());
            System.out.println(curr.getMsg());
            System.out.println();
            if (curr.getParent() != null && curr.getParent().
                    getBranch().equals(curr.getBranch())) {
                curr = curr.getParent();
            } else {
                break;
            }
        }
    }

    public static void globalLog() {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        for (File f: COMMITPATH.listFiles()) {
            Commit curr = Utils.readObject(f, Commit.class);
            System.out.println("===");
            System.out.println("commit " + curr.getMySHA());
            System.out.println("Date: " + curr.getTimestamp());
            System.out.println(curr.getMsg());
            System.out.println();
        }
        return;
    }

    public static void find(String msg) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        boolean flag = false;
        for (File f: COMMITPATH.listFiles()) {
            Commit curr = Utils.readObject(f, Commit.class);
            if (curr.getMsg().equals(msg)) {
                System.out.println(curr.getMySHA());
                flag = true;
            }
        }

        if (!flag) {
            System.out.println("Found no commit with that message.");
        }
        return;
    }

    public static void status() {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        TreeMap<String, String> branchMap = readBranchMap();
        Stage stage = readStage();
        String head = readHEAD();
        Commit currCommit = currCommit();

        System.out.println("=== Branches ===");
        List<String> branchMapSorted = new ArrayList<>();
        for (String bName: branchMap.keySet()) {
            branchMapSorted.add(bName);
        }
        Collections.sort(branchMapSorted);
        for (String bName: branchMapSorted) {
            if (branchMap.get(BRANCHNAME).equals(head)) {
                System.out.println("*" + bName);
            } else {
                System.out.println(bName);
            }
        }
        System.out.println();

        List<String> stagedFilesSorted = new ArrayList<>();
        for (String fileName: stage.getAddedFiles().keySet()) {
            stagedFilesSorted.add(fileName);
        }
        Collections.sort(stagedFilesSorted);
        System.out.println("=== Staged Files ===");
        for (String stagedFiles: stagedFilesSorted) {
            System.out.println(stagedFiles);
        }
        System.out.println();

        List<String> stageFilesRemovedSorted = new ArrayList<>();
        for (String fileName: stage.getRemovedFiles().keySet()) {
            stageFilesRemovedSorted.add(fileName);
        }
        Collections.sort(stagedFilesSorted);
        System.out.println("=== Removed Files ===");
        for (String stagedFiles: stageFilesRemovedSorted) {
            System.out.println(stagedFiles);
        }
        System.out.println();

        List<String> blobNames = new ArrayList<>();
        List<String> cwdFiles = Utils.plainFilenamesIn(CURRENT_PATH);
        for (Object bName: currCommit.getBlobs().keySet()) {
            blobNames.add(bName.toString());
        }

        System.out.println("=== Modifications Not Staged For Commit ===");
        if (!cwdFiles.isEmpty()) {
            for (String cwdFile : cwdFiles) {
                String cwdFileSHA = Utils.sha1(cwdFile);
                String blobSHA = (String) currCommit.getBlobs().get(cwdFile);

                if (blobNames.isEmpty()) {
                    System.out.println();
                } else {
                    if (blobNames.contains(cwdFile)) {
                        if (!blobSHA.equals(cwdFileSHA)
                                && !stagedFilesSorted.contains(cwdFile)
                                && !stageFilesRemovedSorted.contains(cwdFile)) {
                            System.out.println(cwdFile + "(modified)");
                        }
                    } else if (!blobSHA.equals(cwdFileSHA)
                            && stagedFilesSorted.contains(cwdFile)) {
                        System.out.println(cwdFile + "(modified)");
                    }
                }
            }
        } else {
            for (String fileName : blobNames) {
                if (!cwdFiles.contains(fileName)
                        && !stageFilesRemovedSorted.contains(fileName)) {
                    System.out.println(fileName + "(deleted)");
                } else if (stagedFilesSorted.contains(fileName)
                        && !cwdFiles.contains(fileName)) {
                    System.out.println(fileName);
                }
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String cwdFile : cwdFiles) {
            if (!stage.getRemovedFiles().containsKey(cwdFile)
                    && !stage.getAddedFiles().containsKey(cwdFile)
                    && !blobNames.contains(cwdFile)) {
                System.out.println(cwdFile);
            }
        }
        System.out.println();
    }

    public static void checkout(String... args) throws IOException {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        Stage stage = readStage();
        TreeMap<String, String> branchMap = readBranchMap();
        String head = readHEAD();

        if (args.length == 3 && args[1].equals("--")) {
            String fileName = args[2];
            checkout1(fileName, stage, branchMap, head);
        } else if (args.length == 4 && args[2].equals("--")) {
            String commitID = args[1];
            String fileName = args[3];
            checkout2(commitID, fileName, stage, branchMap, head);
        } else if (args.length == 2) {
            String branchNameArg = args[1];
            checkout3(branchNameArg, stage, branchMap, head);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    public static void checkout1(String fileName, Stage stage,
                     TreeMap<String, String> branchMap, String head) {
        Commit curr = currCommit();
        if (curr.getBlobs().containsKey(fileName)) {
            String fileContents = Utils.readContentsAsString(Utils.join
                    (BLOBPATH, curr.getBlobs().get(fileName) + ".txt"));
            Utils.writeContents(Utils.join
                    (CURRENT_PATH, fileName), fileContents);
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static void checkout2(String commitID, String fileName,
                 Stage stage, TreeMap<String, String> branchMap, String head) {
        boolean flag = false;

        if (commitID.length() == 8) {
            commitID = findShortUID(commitID);
        }

        if (!Utils.plainFilenamesIn(COMMIT_PATH).contains(commitID + ".txt")) {
            System.out.println("No commit with that id exists.");
            return;
        }

        for (File f : COMMITPATH.listFiles()) {
            Commit curr = Utils.readObject(f, Commit.class);

            if (curr.getMySHA().equals(commitID)) {
                flag = true;
                if (curr.getBlobs().containsKey(fileName)) {
                    String fileContents = Utils.readContentsAsString(Utils.join
                            (BLOBPATH, curr.getBlobs().get(fileName) + ".txt"));
                    Utils.writeContents(
                        Utils.join(CURRENT_PATH, fileName), fileContents);
                } else {
                    System.out.println(
                        "File does not exist in that commit.");
                }
                break;
            }
        }
        if (!flag) {
            System.out.println("No commit with that id exists.");
        }
    }

    public static void checkout3(String branchNameArg, Stage stage,
                 TreeMap<String, String> branchMap, String head) {
        if (!branchMap.containsKey(branchNameArg)) {
            System.out.println("No such branch exists.");
        } else {
            Commit branchHeadCommit = Utils.readObject(Utils.join
                    (COMMIT_PATH, branchMap.get(branchNameArg)
                            + ".txt"), Commit.class);
            String currBranch = Utils.readContentsAsString(CURRBRANCHFILEPATH);
            if (branchNameArg.equals(currBranch)) {
                System.out.println("No need to checkout the current branch.");
            } else {
                for (String fileName: plainFilenamesIn(CURRENT_PATH)) {
                    if (!currCommit().getBlobs().containsKey(fileName)
                            && !stage.getAddedFiles().containsKey(fileName)
                            && !stage.getRemovedFiles().containsKey(fileName)
                            && branchHeadCommit.getBlobs().
                            containsKey(fileName)) {
                        System.out.println("There is an untracked file in the"
                            + " way; delete it, or add and commit it first.");
                        break;
                    }
                }
            }
            List<String> branchFiles = new ArrayList<>(branchHeadCommit.
                getBlobs().keySet());
            List<String> cwdFiles = Utils.plainFilenamesIn(CURRENT_PATH);
            for (String cwdFile : cwdFiles) {
                if (!branchFiles.contains(cwdFile)) {
                    Utils.restrictedDelete(cwdFile);
                } else {
                    String fileContents = Utils.readContentsAsString(Utils.join
                            (BLOBPATH, branchHeadCommit.getBlobs().
                                    get(cwdFile) + ".txt"));
                    Utils.writeContents(Utils.join
                            (CURRENT_PATH, cwdFile), fileContents);
                }
            }
            for (String branchFile: branchFiles) {
                if (!cwdFiles.contains(branchFile)) {
                    String fileContents = Utils.readContentsAsString(Utils.join
                            (BLOBPATH, branchHeadCommit.getBlobs().
                                    get(branchFile) + ".txt"));
                    Utils.writeContents(Utils.join
                            (CURRENT_PATH, branchFile), fileContents);
                }
            }
            stage.clear();
            head = branchHeadCommit.getMySHA();
            currBranch = branchNameArg;
            branchHeadCommit.setBranch(currBranch);
            if (branchMap.containsKey(branchNameArg)) {
                branchMap.replace(branchNameArg, head);
            } else {
                branchMap.put(branchNameArg, head);
            }
            writeCommit(branchHeadCommit);
            Utils.writeContents(CURRBRANCHFILEPATH, currBranch);
            serializeHSB(head, stage, branchMap);
        }
    }

    public static void branch(String branchNameArg) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        TreeMap<String, String> branchMap = readBranchMap();

        if (branchMap.containsKey(branchNameArg)) {
            System.out.println("A branch with that name already exists.");
        } else {
            branchMap.put(branchNameArg, readHEAD());
        }

        writeBranchMap(branchMap);
    }

    public static void removeBranch(String branchNameArg) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        TreeMap<String, String> branchMap = readBranchMap();
        Commit currCommit = currCommit();

        if (!branchMap.containsKey(branchNameArg)) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchNameArg.equals(currCommit.getBranch())) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branchMap.remove(branchNameArg);
        }

        writeBranchMap(branchMap);
    }

    public static void reset(String commitID) throws IOException {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }

        TreeMap<String, String> branchMap = readBranchMap();
        Commit currCommit = null;
        Stage stage = readStage();
        boolean flag = false;

        for (File f : COMMITPATH.listFiles()) {
            Commit curr = Utils.readObject(f, Commit.class);
            if (curr.getMySHA().equals(commitID)) {
                currCommit = curr;
                flag = true;
                break;
            }
        }

        if (!flag) {
            System.out.println("No commit with that id exists.");
        } else {
            for (String fileName: plainFilenamesIn(CURRENT_PATH)) {
                if (!currCommit().getBlobs().containsKey(fileName)
                        && !stage.getAddedFiles().containsKey(fileName)
                        && !stage.getRemovedFiles().containsKey(fileName)
                        && currCommit.getBlobs().containsKey(fileName)) {
                    System.out.println("There is an untracked file in the "
                            + "way; delete it, or add and commit it first.");
                    break;
                }
            }

            List<String> fileNames = new ArrayList<>();
            for (Object fileName: currCommit.getBlobs().keySet()) {
                fileNames.add(fileName.toString());
            }
            for (String fileName : fileNames) {
                String[] args = new String[]
                    { "checkout", commitID, "--", fileName };
                checkout(args);
            }

            stage.clear();
            writeStage(stage);
        }
    }

    public static void merge(String fileName) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        return;
    }

    public static Commit currCommit() {
        String currHEAD = readHEAD();
        for (File f : COMMITPATH.listFiles()) {
            Commit curr = Utils.readObject(f, Commit.class);
            if (curr.getMySHA().equals(currHEAD)) {
                return curr;
            }
        }
        return null;
    }

    public static void writeCommit(Commit commit) {
        Utils.writeObject(Utils.join(COMMIT_PATH,
                commit.getMySHA() + ".txt"), commit);
    }

    public static String readHEAD() {
        if (HEADFILEPATH.exists()) {
            return Utils.readContentsAsString(HEADFILEPATH);
        } else {
            return "";
        }
    }
    public static void writeHEAD(String head) {
        if (HEADFILEPATH.exists()) {
            Utils.writeContents(HEADFILEPATH, head);
        }
    }

    public static Stage readStage() {
        if (STAGEPATHFILE.exists()) {
            return Utils.readObject(STAGEPATHFILE, Stage.class);
        } else {
            Stage stage = new Stage();
            return stage;
        }
    }
    public static void writeStage(Stage stage) {
        if (STAGEPATHFILE.exists()) {
            Utils.writeObject(STAGEPATHFILE, stage);
        }
    }

    public static TreeMap<String, String> readBranchMap() {
        if (BRANCHFILEPATH.exists()) {
            return Utils.readObject(BRANCHFILEPATH, TreeMap.class);
        } else {
            TreeMap<String, String> branchMap = new TreeMap<>();
            return branchMap;
        }
    }
    public static void writeBranchMap(TreeMap<String, String> branchMap) {
        if (BRANCHFILEPATH.exists()) {
            Utils.writeObject(BRANCHFILEPATH, branchMap);
        }
    }

    public static void serializeHSB(String head, Stage stage,
                                    TreeMap<String, String> branchMap) {
        writeHEAD(head);
        writeStage(stage);
        writeBranchMap(branchMap);
    }

    public static void serializeAll(String head, Stage stage,
        TreeMap<String, String> branchMap, Commit commit) {
        writeCommit(commit);
        serializeHSB(head, stage, branchMap);
    }

    public static boolean checkInitialized() {
        return GITPATH.exists();
    }

    public static String findShortUID(String commitID) {
        String longCommitID = "";
        String shortCommitID = "";
        for (File f: COMMITPATH.listFiles()) {
            Commit curr = Utils.readObject(f, Commit.class);
            shortCommitID = curr.getMySHA().substring(0, 8);
            if (shortCommitID.equals(commitID)) {
                longCommitID = curr.getMySHA();
                break;
            }
        }
        return longCommitID;
    }
}
