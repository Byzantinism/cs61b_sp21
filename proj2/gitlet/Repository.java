package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Zebang Ge
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    //Time Stamp format as "Thu Nov 9 17:01:33 2017 -0800"
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = gitlet.Utils.join(CWD, ".gitlet");
    public static final File TempArea_DIR = gitlet.Utils.join(GITLET_DIR, "TempArea");
    public static final File Staged_DIR = gitlet.Utils.join(TempArea_DIR, "Staged");
    public static final File Removed_DIR = gitlet.Utils.join(TempArea_DIR, "Removed");
    public static final File Logs_DIR = gitlet.Utils.join(GITLET_DIR, "logs");
    public static final File Object_DIR = gitlet.Utils.join(GITLET_DIR, "objects");
    public static final File commitMap_DIR = gitlet.Utils.join(Object_DIR, "commitMap");
    public static final File Refs_DIR = gitlet.Utils.join(GITLET_DIR, "refs");
    public static final File HEAD_DIR = gitlet.Utils.join(Refs_DIR, "HEAD");
    //master branch = branches[0]
    public static final File branches_DIR = gitlet.Utils.join(Refs_DIR, "branches");
    //TempArea for cache


    //transient field
    //TempAreas
    public transient TreeMap<File, String> TempStaged;
    public transient TreeMap<File, String> TempRemoved;
    public transient TreeMap ModificationsNotStaged;
    public transient TreeMap Untracked;
    //Commit
    public transient TreeMap commitMap; //TODO: data type is not decided.
    public transient TreeMap branches; //Save corresponding branches' commit SHA1 value.
    public transient Commit.innerCommit headCommit;

    /* TODO: fill in the rest of this class. */
    /** Check whether .gitlet exists or not.
     *  needInit = 0: need .gitlet not exist.
     *  needInit = 1: need .gitlet     exist.
     */

    public Repository(){
        //TODO: think about this.
        TempStaged = readObject(Staged_DIR, TreeMap.class); //Maybe some errors.
        TempRemoved = readObject(Removed_DIR, TreeMap.class); //Maybe some errors.

        headCommit = IO.readCommit(Utils.readContentsAsString(HEAD_DIR)); //Head commit.
    }
    public static void initCheck(int needInit){
        switch (needInit){
            case 0:
                if (GITLET_DIR.exists()){
                    throw new GitletException("A Gitlet version-control system already exists in the current directory.");
                }
            case 1:
                if (!GITLET_DIR.exists()){
                    throw new GitletException("Not in an initialized Gitlet directory.");
                }
        }
    }
    public static void init (){
        Repository.initCheck(0);
        // TODO: create other necessary files. Maybe should do in for loop.
        //TempArea folder + StagedMap and RemovedMap
        TempArea_DIR.mkdirs();
        Utils.writeObject(Staged_DIR, new TreeMap<File, String>());
        Utils.writeObject(Removed_DIR, new TreeMap<File, String>());
        //logs
        Logs_DIR.mkdirs();
        //TODO: need more code.
        //objects
        Object_DIR.mkdirs();
        Utils.writeObject(commitMap_DIR, new TreeMap<>());//TODO: think about commitMap type.
        //refs
        Refs_DIR.mkdirs();
        Utils.writeObject(branches_DIR, new TreeMap());
        //Folder structure is done. Now create init commit.
        String SHA1 = Commit.initCommit();
    };
    public void add (String x){
        File xDir = gitlet.Utils.join(CWD, x);
        if (!xDir.exists()) throw new GitletException("File does not exist.");
        String fileContent = Utils.readContentsAsString(xDir);
        String xSHA1 = Utils.sha1(fileContent);

        if (TempRemoved.containsKey(xDir)) {
            popupTempArea(Removed_DIR, TempRemoved, xDir);
        }

        if (xSHA1.equals(headCommit.blobMap.get(xDir))){
            popupTempArea(Staged_DIR, TempStaged, xDir);
            return;
        }
        if (!xSHA1.equals(TempStaged.get(xDir))){
            updateTempArea(Staged_DIR,TempStaged, xDir, xSHA1, fileContent);
        }
    };
    public void rm (String x){
        File xDir = gitlet.Utils.join(CWD, x);
        boolean containedInStaged = TempStaged.containsKey(xDir);
        boolean containedInCurrentCommit = headCommit.blobMap.containsKey(xDir);
        if (!containedInStaged && !containedInCurrentCommit) {
            throw new GitletException("No reason to remove the file.");
        }
        if (containedInStaged){
            popupTempArea(Staged_DIR, TempStaged, xDir);
        }
        if (containedInCurrentCommit){
            TempRemoved.put(xDir,"Delete");
            xDir.delete();
        }
    };
    //Delete certain item from TempMap.
    private static void popupTempArea(File Map_Dir, TreeMap<File, String> Map, File xDir){
        File oldSHA1_DIR = IO.splitSHA1(Map_Dir, Map.get(xDir))[2];
        oldSHA1_DIR.delete();
        Map.remove(xDir);
        Utils.writeObject(Map_Dir,Map);
    }
    //Update certain item inside TempMap.
    private static void updateTempArea(File Map_Dir, TreeMap<File, String> Map,
                                       File xDir, String xSHA1, String fileContent){
        File oldSHA1_DIR = IO.splitSHA1(Map_Dir, Map.get(xDir))[2];
        oldSHA1_DIR.delete();
        Map.put(xDir, xSHA1);
        Utils.writeObject(Map_Dir,Map);
        File newSHA1_DIR = IO.splitSHA1(Map_Dir, xSHA1)[2];
        Utils.writeObject(newSHA1_DIR, fileContent);
    }
    public void commit (String message){
        if (TempStaged.isEmpty() && TempRemoved.isEmpty()){
            throw new GitletException("No changes added to the commit.");
        }
        //TODO: need to try work or NOT.
        if (message.matches("\s*") ){
            throw new GitletException("Please enter a commit message.");
        }
        String headSHA1 = Utils.readContentsAsString(HEAD_DIR);
        String nextSHA1 = Commit.commit(message, headCommit, headSHA1, TempStaged, TempRemoved);
    }
    private static void realCommit (){}
    public static void log (){}
    public static void globalLog (){}
    public static void find (){}
    public static void status (){}
    public static void checkout (){}
    public static void createBranch (String branchName){}
    private static void branch (String branchName, String commitSHA1){}
    public static void rmBranch (){}
    public static void reset (){}
    public static void merge (){}
}
