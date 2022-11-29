package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
    public static final File Logs_DIR = gitlet.Utils.join(GITLET_DIR, "logs");
    public static final File Object_DIR = gitlet.Utils.join(GITLET_DIR, "objects");
    public static final File Staged_DIR = gitlet.Utils.join(Object_DIR, "Staged");
    public static final File Removed_DIR = gitlet.Utils.join(Object_DIR, "Removed");
    //public static final File commitMap_DIR = gitlet.Utils.join(Object_DIR, "commitMap");
    public static final File Refs_DIR = gitlet.Utils.join(GITLET_DIR, "refs");
    public static final File HEAD_DIR = gitlet.Utils.join(Refs_DIR, "HEAD");
    public static final File HEADbranch_DIR = gitlet.Utils.join(Refs_DIR, "HEADbranch");
    //master branch = branches[0]
    public static final File branches_DIR = gitlet.Utils.join(Refs_DIR, "branches");
    //transient field
    //TempAreas
    public transient TreeMap<File, String> TempStaged;
    public transient TreeMap<File, String> TempRemoved;
    public transient TreeMap ModificationsNotStaged;
    public transient TreeMap Untracked;
    //Commit
    public transient TreeMap<String, LinkedList<String>> branches; //Save corresponding branches' commit SHA1 value.
    public transient String headSHA1;
    public transient Commit.innerCommit headCommit;

    public Repository(){
        TempStaged = readObject(Staged_DIR, TreeMap.class); //Maybe some errors.
        TempRemoved = readObject(Removed_DIR, TreeMap.class); //Maybe some errors.
        headSHA1 = Utils.readContentsAsString(HEAD_DIR);
        headCommit = IO.readCommit(headSHA1); //Head commit.
    }
    /** Check whether .gitlet exists or not.
     *  needInit = 0: need .gitlet not exist.
     *  needInit = 1: need .gitlet     exist.*/
    public static void initCheck(int needInit){
        switch (needInit){
            case 0:
                if (GITLET_DIR.exists()){
                    throw new GitletException("A Gitlet version-control system already exists in the current directory.");
                }
                break;
            case 1:
                if (!GITLET_DIR.exists()){
                    throw new GitletException("Not in an initialized Gitlet directory.");
                }
                break;
        }
    }
    public static void init (){
        Repository.initCheck(0);
        //logs
        Logs_DIR.mkdirs();
        //objects: commitMap + StagedMap + RemovedMap
        Object_DIR.mkdirs();
        Utils.writeObject(Staged_DIR, new TreeMap<File, String>());
        Utils.writeObject(Removed_DIR, new TreeMap<File, String>());
        //Utils.writeObject(commitMap_DIR, new TreeMap<>());//TODO: think about commitMap type.
        //refs
        Refs_DIR.mkdirs();
        Utils.writeObject(branches_DIR, new TreeMap<String, LinkedList<String>>());
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
            Utils.restrictedDelete(xDir);
        }
    }
    //Delete certain item from TempMap.
    private static void popupTempArea(File Map_Dir, TreeMap<File, String> Map, File xDir){
        File oldSHA1_DIR = IO.splitSHA1(Object_DIR, Map.get(xDir))[1];
        Utils.restrictedDelete(oldSHA1_DIR);
        Map.remove(xDir);
        Utils.writeObject(Map_Dir,Map);
    }
    //Update certain item inside TempMap.
    private static void updateTempArea(File Map_Dir, TreeMap<File, String> Map,
                                       File xDir, String xSHA1, String fileContent){
        String oldSHA1 = Map.get(xDir);
        if (oldSHA1 != null) {
            File oldSHA1_DIR = IO.splitSHA1(Object_DIR, oldSHA1)[1];
            Utils.restrictedDelete(oldSHA1_DIR);
        }
        Map.put(xDir, xSHA1);
        Utils.writeObject(Map_Dir,Map);

        File[] newSHA1_DIR = IO.splitSHA1(Object_DIR, xSHA1);
        if (!newSHA1_DIR[0].exists()) newSHA1_DIR[0].mkdir();
        Utils.writeContents(newSHA1_DIR[1], fileContent);
    }
    public void commit (String message){
        if (TempStaged.isEmpty() && TempRemoved.isEmpty()){
            throw new GitletException("No changes added to the commit.");
        }
        //TODO: need to try work or NOT.
        if (message.matches("\s*") ){
            throw new GitletException("Please enter a commit message.");
        }
        String nextSHA1 = Commit.commit(message, headCommit, headSHA1, TempStaged, TempRemoved);
    }
    public static void log (){}
    public static void globalLog (){}
    public static void find (){}
    public static void status (){}
    public void checkoutBranch (String branchName){
        TreeMap<String, LinkedList<String>> branches = Utils.readObject(Repository.branches_DIR, TreeMap.class);
        LinkedList<String> HeadBranch = (LinkedList<String>) branches.get(branchName);
        if(HeadBranch == null){
            throw new GitletException("No such branch exists.");
        }
        String branchSHA1 = HeadBranch.getFirst();
        if(headSHA1.equals(branchSHA1)){
            throw new GitletException("No need to checkout the current branch.");
        }
        if(!TempStaged.isEmpty()){
            throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        //Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
        List<String> filenames = Utils.plainFilenamesIn(CWD);
        if (filenames != null){
            for (String i: filenames){
                Utils.restrictedDelete(Utils.join(CWD, i));
            }
        }
        //Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist.
        Commit.innerCommit branchCommit = IO.readCommit(branchSHA1);
        for (File i: branchCommit.blobMap.keySet()){
            File blobDIR = IO.splitSHA1(Object_DIR, branchCommit.blobMap.get(i))[1];
            try {
                Files.copy(blobDIR.toPath(), i.toPath());
            } catch (IOException excp) {
                throw new GitletException(excp.getMessage());
            }
        }
        //the given branch will now be considered the current branch (HEAD).
        Utils.writeContents(Repository.HEADbranch_DIR, branchName);
        Utils.writeContents(Repository.HEAD_DIR, branchSHA1);
    }
    public void checkoutFile (String fileName){ checkoutFileInCommit(headSHA1, fileName); }
    public void checkoutFileInCommit (String commitID, String fileName){
        Commit.innerCommit aimCommit;
        if (commitID.equals(headSHA1)){
            aimCommit = headCommit;
        } else {
            aimCommit = IO.readCommit(commitID);
        }

        File fileDIR = Utils.join(CWD, fileName);
        String fileSHA1 = aimCommit.blobMap.get(fileDIR);
        if (fileSHA1 == null){
            throw new GitletException("File does not exist in that commit.");
        }
        File blobDIR = IO.splitSHA1(Object_DIR, fileSHA1)[1];

        try {
            Files.copy(blobDIR.toPath(), fileDIR.toPath(), REPLACE_EXISTING);
        } catch (IOException excp) {
            throw new GitletException(excp.getMessage());
        }
    }
    public static void createBranch (String branchName){}
    private static void branch (String branchName, String commitSHA1){

    }
    public static void rmBranch (){}
    public static void reset (){}
    public static void merge (){}
}
