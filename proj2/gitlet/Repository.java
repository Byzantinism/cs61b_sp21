package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Zebang Ge
 */
public class Repository {
    /**
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
    public static final File HEADSHA1_DIR = gitlet.Utils.join(Refs_DIR, "HEAD");
    public static final File HEADbranch_DIR = gitlet.Utils.join(Refs_DIR, "HEADbranch");
    //master branch = branches[0]
    public static final File branches_DIR = gitlet.Utils.join(Refs_DIR, "branches");
    //transient field
    //TempAreas
    public transient TreeMap<File, String> TempStaged;
    public transient TreeMap<File, String> TempRemoved;
    //Commit
    public transient TreeMap<String, String> branches; //Save corresponding branches' commit SHA1 value.
    public transient String headBranch;
    public transient String headSHA1;
    public transient Commit.innerCommit headCommit;

    public Repository(){
        TempStaged = readObject(Staged_DIR, TreeMap.class); //Maybe some errors.
        TempRemoved = readObject(Removed_DIR, TreeMap.class); //Maybe some errors.
        headBranch = Utils.readContentsAsString(Repository.HEADbranch_DIR);
        headSHA1 = Utils.readContentsAsString(HEADSHA1_DIR);
        headCommit = IO.readCommit(headSHA1); //Head commit.
        branches = Utils.readObject(Repository.branches_DIR, TreeMap.class);
    }
    /** Check whether .gitlet exists or not.
     *  needInit = 0: need .gitlet not exist.
     *  needInit = 1: need .gitlet     exist.*/
    public static boolean initCheck(int needInit){
        switch (needInit){
            case 0:
                if (GITLET_DIR.exists()){
                    System.out.print("A Gitlet version-control system already exists in the current directory.");
                    return false;
                }
                break;
            case 1:
                if (!GITLET_DIR.exists()){
                    System.out.print("Not in an initialized Gitlet directory.");
                    return false;
                }
                break;
        }
        return true;
    }
    public static void init (){
        if (!Repository.initCheck(0)) return;
        //create folders. Start form logs.
        Logs_DIR.mkdirs();
        //objects: commitMap + StagedMap + RemovedMap
        Object_DIR.mkdirs();
        Utils.writeObject(Staged_DIR, new TreeMap<File, String>());
        Utils.writeObject(Removed_DIR, new TreeMap<File, String>());
        //refs
        Refs_DIR.mkdirs();
        Utils.writeObject(branches_DIR, new TreeMap<String, String>());
        //create init commit.
        String SHA1 = Commit.initCommit();
    }
    public void add (String x){
        File xDir = gitlet.Utils.join(CWD, x);
        if (!xDir.exists()) {
            System.out.print("File does not exist.");
            return;
        }
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
    }
    public void rm (String x){
        File xDir = gitlet.Utils.join(CWD, x);
        boolean containedInStaged = TempStaged.containsKey(xDir);
        boolean containedInCurrentCommit = headCommit.blobMap.containsKey(xDir);
        if (!containedInStaged && !containedInCurrentCommit) {
            System.out.print("No reason to remove the file.");
            return;
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
            System.out.print("No changes added to the commit.");
            return;
        }
        if (message.matches("\s*") ){
            System.out.print("Please enter a commit message.");
            return;
        }
        String nextSHA1 = Commit.commit(message, headCommit, headSHA1, TempStaged, TempRemoved);
    }
    public void log (){
        Commit.innerCommit i = headCommit;
        String iSHA1 = headSHA1;
        while(i != null){
            printLog(i, iSHA1);
            iSHA1 = i.p1;
            i = IO.readCommit(i.p1);
        }
    }

    private static void printLog(Commit.innerCommit i, String iSHA1){
        System.out.println("===");
        System.out.printf("commit %s%n",iSHA1);
        if (i.p2 != null) {
            System.out.printf("Merge: %s %s", i.p1.substring(0, 6), i.p2.substring(0, 6));
        }
        System.out.println("Date: " + dateFormat.format(i.timeStamp));
        System.out.printf(i.message + "%n%n");
    }
    public static void globalLog (){
        List<String> folderList = IO.plainFoldernamesIn(Repository.Object_DIR);
        for (String i: folderList){
            List<String> fileList = Utils.plainFilenamesIn(i);
            fileList.removeIf(next -> !IO.commitString.equals(next.substring(next.length() - 1)));
            for (String nextSHA1: fileList){
                Commit.innerCommit nextCommit = IO.readCommit(nextSHA1);
                printLog(nextCommit, nextSHA1);
            }
        }
    }
    public static void find (String message){
        List<String> folderList = IO.plainFoldernamesIn(Repository.Object_DIR);
        int count = 0;
        for (String i : folderList) {
            List<String> fileList = Utils.plainFilenamesIn(i);
            fileList.removeIf(next -> next.length() != IO.commitSHA1Length || !IO.commitString.equals(next.substring(next.length() - 1)));
            for (String nextSHA1 : fileList) {
                Commit.innerCommit nextCommit = IO.readCommit(nextSHA1);
                if (message.equals(nextCommit.message)) {
                    System.out.println(nextSHA1);
                    count += 1;
                }
            }
        }
        if (count == 0){
            System.out.println("Found no commit with that message.");
        }
    }
    private List<String> ScanUntracked (){
        //TODO: next task.
        List<String> filenames = Utils.plainFilenamesIn(CWD);
        for (File i: TempStaged.keySet()){
            filenames.remove(i.getName());
        }
        for (File i: headCommit.blobMap.keySet()){
            filenames.remove(i.getName());
        }
        return filenames;
    }
    private static void printTempAreaStatus (String section, TreeMap<File, String> Map){
        System.out.printf("=== %s ===%n", section);
        if (Map == null) { return;}
        for (File i: Map.keySet()){
            System.out.println(i.getName());
        }
        System.out.println();
    }
    public void status (){
        System.out.println("=== Branches ===");
        for (String branch: branches.keySet()){
            if (Commit.initBranchName.equals(branch)){
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        printTempAreaStatus("Staged Files", TempStaged);
        printTempAreaStatus("Removed Files", TempRemoved);
        //TODO: modified but not staged
        printTempAreaStatus("Modifications Not Staged For Commit", null);
        //Untracked Files
        System.out.println("=== Untracked Files ===");
        for (String i: ScanUntracked()){
            System.out.println(i);
        }
        System.out.println();
    }
    //TODO: may need to modify.
    private static void restoreCommit (String commitSHA1){
        //Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
        List<String> filenames = Utils.plainFilenamesIn(CWD);
        if (filenames != null){
            for (String i: filenames){
                Utils.restrictedDelete(Utils.join(CWD, i));
            }
        }
        //Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist.
        Commit.innerCommit branchCommit = IO.readCommit(commitSHA1);
        for (File i: branchCommit.blobMap.keySet()){
            File blobDIR = IO.splitSHA1(Object_DIR, branchCommit.blobMap.get(i))[1];
            try {
                Files.copy(blobDIR.toPath(), i.toPath());
            } catch (IOException excp) {
                throw new GitletException(excp.getMessage());
            }
        }
    }
    public void checkoutBranch (String branchName){
        String branchSHA1 = branches.get(branchName);
        if(branchSHA1 == null){
            System.out.print("No such branch exists.");
            return;
        }
        if(headSHA1.equals(branchSHA1)){
            System.out.print("No need to checkout the current branch.");
            return;
        }
        if(!ScanUntracked().isEmpty()){
            System.out.print("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }
        restoreCommit(branchSHA1);
        Utils.writeContents(Repository.HEADbranch_DIR, branchName);
        Utils.writeContents(Repository.HEADSHA1_DIR, branchSHA1);
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
            System.out.print("File does not exist in that commit.");
            return;
        }
        File blobDIR = IO.splitSHA1(Object_DIR, fileSHA1)[1];

        try {
            Files.copy(blobDIR.toPath(), fileDIR.toPath(), REPLACE_EXISTING);
        } catch (IOException excp) {
            throw new GitletException(excp.getMessage());
        }
    }
    public void createBranch (String branchName){
        if (branches.containsKey(branchName)){
            System.out.print("A branch with that name already exists.");
            return;
        }
        branches.put(branchName, branches.get(headBranch));
        Utils.writeObject(Repository.branches_DIR, branches);
    }
    public void rmBranch (String branchName){
        if (!branches.containsKey(branchName)){
            System.out.print("A branch with that name does not exist.");
            return;
        }
        if (headSHA1.equals(branches.get(branchName))){
            System.out.print("Cannot remove the current branch.");
            return;
        }
        branches.remove(branchName);
        Utils.writeObject(Repository.branches_DIR, branches);
    }
    //TODO: Could reset go to the commit not belonging to current branch?
    public void reset (String commitSHA1){
        File commitDIR = IO.splitSHA1(Object_DIR, commitSHA1)[1];
        if (!commitDIR.exists()){
            System.out.print("No commit with that id exists.");
            return;
        }
        if (!TempStaged.isEmpty() || !TempRemoved.isEmpty() || !ScanUntracked().isEmpty()){
            //TODO: maybe some errors. (If a working file is untracked in the current branch and would be overwritten by the reset
            System.out.print("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }
        restoreCommit(commitSHA1);
        branches.put(headBranch, commitSHA1);
        Utils.writeObject(Repository.branches_DIR, branches);
        Utils.writeContents(Repository.HEADSHA1_DIR, commitSHA1);
    }
    public static void merge (){}
}
