package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import static gitlet.Utils.*;

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
    public static final File Refs_DIR = gitlet.Utils.join(GITLET_DIR, "refs");
    public static final File HEADSHA1_DIR = gitlet.Utils.join(Refs_DIR, "HEAD");
    public static final File HEADbranch_DIR = gitlet.Utils.join(Refs_DIR, "HEADbranch");
    public static final File branches_DIR = gitlet.Utils.join(Refs_DIR, "branches");
    //transient field
    public transient TreeMap<File, String> TempStaged;
    public transient TreeMap<File, String> TempRemoved;
    public transient TreeMap<String, String> branches; //Save corresponding branches' commit SHA1 value.
    public transient String headBranch;
    public transient String headSHA1;
    public transient Commit.innerCommit headCommit;

    public Repository(){
        TempStaged = readObject(Staged_DIR, TreeMap.class);
        TempRemoved = readObject(Removed_DIR, TreeMap.class);
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
        popupTempArea(Removed_DIR, TempRemoved, xDir); //TODO: should not delete file in Object folder.
        //TempRemoved.remove(xDir); Utils.writeObject(Removed_DIR, TempRemoved);
        if (xSHA1.equals(headCommit.blobMap.get(xDir))){
            popupTempArea(Staged_DIR, TempStaged, xDir);
        } else if (!xSHA1.equals(TempStaged.get(xDir))){
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
        } else {
            //containedInCurrentCommit = true.
            TempRemoved.put(xDir,"Delete");
            Utils.writeObject(Removed_DIR, TempRemoved);
            Utils.restrictedDelete(xDir);
        }
    }
    //Delete certain item from TempMap.
    private static void popupTempArea(File Map_Dir, TreeMap<File, String> Map, File xDir){
        String oldSHA1 = Map.get(xDir);
        if (oldSHA1 != null){
            File oldSHA1_DIR = IO.splitSHA1(Object_DIR, oldSHA1)[1];
            oldSHA1_DIR.delete();
            Map.remove(xDir);
            Utils.writeObject(Map_Dir,Map);
        }
    }
    //Update certain item inside TempMap.
    private static void updateTempArea(File Map_Dir, TreeMap<File, String> Map,
                                       File xDir, String xSHA1, String fileContent){
        String oldSHA1 = Map.get(xDir);
        if (oldSHA1 != null) {
            File oldSHA1_DIR = IO.splitSHA1(Object_DIR, oldSHA1)[1];
            oldSHA1_DIR.delete();
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
        String nextSHA1 = Commit.commit(message, headCommit, headSHA1, null, TempStaged, TempRemoved);
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
            System.out.printf("Merge: %s %s%n", i.p1.substring(0, 6), i.p2.substring(0, 6));
        }
        System.out.println("Date: " + dateFormat.format(i.timeStamp));
        System.out.printf(i.message + "%n%n");
    }

    private static List<String> scanCommit(){
        List<String> folderList = IO.plainFoldernamesIn(Repository.Object_DIR);
        List<String> commitList = new ArrayList<>();
        for (String i: folderList){
            List<String> fileList = Utils.plainFilenamesIn(Utils.join(Object_DIR, i));
            for (String next: fileList){
                if (next != null && next.length() == (IO.commitSHA1Length -2) && IO.commitString.equals(next.substring(next.length() - 1))){
                    commitList.add(i + next);//Combine first 2 and last 39 digits.
                }
            }
        }
        return commitList;
    }
    public static void globalLog (){
        List<String> fileList = scanCommit();
        for (String nextSHA1: fileList){
                Commit.innerCommit nextCommit = IO.readCommit(nextSHA1);
                printLog(nextCommit, nextSHA1);
        }
    }
    public static void find (String message){
        int count = 0;
        List<String> fileList = scanCommit();
        for (String nextSHA1 : fileList) {
                Commit.innerCommit nextCommit = IO.readCommit(nextSHA1);
                if (message.equals(nextCommit.message)) {
                    System.out.println(nextSHA1);
                    count += 1;
                }
        }
        if (count == 0){
            System.out.println("Found no commit with that message.");
        }
    }
    private List<String> ScanUntracked (TreeMap<File, String> map,Commit.innerCommit futureCommit){
        List<String> filenames = new ArrayList<>(Utils.plainFilenamesIn(CWD));
         for (File i: headCommit.blobMap.keySet()){
            filenames.remove(i.getName());
        }
         if (map != null){
             for (File ii: map.keySet()){
                 filenames.remove(ii.getName());
             }
         }
         if (futureCommit != null) {
             filenames.removeIf(i -> !futureCommit.blobMap.containsKey(Utils.join(CWD, i)));
         }
        return filenames;
    }
    private static void printTempAreaStatus (String section, TreeMap<File, String> Map){
        System.out.printf("=== %s ===%n", section);
        if (Map == null) { return; }
        for (File i: Map.keySet()){
            System.out.println(i.getName());
        }
        System.out.println();
    }
    public void status (){
        System.out.println("=== Branches ===");
        for (String branch: branches.keySet()){
            if (headBranch.equals(branch)){
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
        System.out.println();
        //Untracked Files
        System.out.println("=== Untracked Files ===");
        for (String i: ScanUntracked(TempStaged, null)){
            System.out.println(i);
        }
        System.out.println();
    }

    private static void restoreCommit (String commitSHA1){
        //TODO: need to modify.
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
        if(!branches.containsKey(branchName)){
            System.out.print("No such branch exists.");
            return;
        }
        if(headBranch.equals(branchName)){
            System.out.print("No need to checkout the current branch.");
            return;
        }
        String branchSHA1 = branches.get(branchName);
        Commit.innerCommit futureCommit = IO.readCommit(branchSHA1);
        if(!ScanUntracked(null, futureCommit).isEmpty()){
            System.out.print("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }
        if(!headSHA1.equals(branchSHA1)){restoreCommit(branchSHA1);}
        Utils.writeContents(Repository.HEADbranch_DIR, branchName);
        Utils.writeContents(Repository.HEADSHA1_DIR, branchSHA1);
        Utils.writeObject(Repository.Staged_DIR, new TreeMap<File, String>());
        Utils.writeObject(Repository.Removed_DIR, new TreeMap<File, String>());
    }
    public void checkoutFile (String fileName){ checkoutFileInCommit(headSHA1, fileName); }
    public void checkoutFileInCommit (String commitID, String fileName){
        Commit.innerCommit aimCommit;
        if (commitID.length() != IO.commitSHA1Length || !IO.splitSHA1(Repository.Object_DIR, commitID)[1].exists()){
            System.out.println("No commit with that id exists.");
            return;
        } else if (commitID.equals(headSHA1)){
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
        IO.copyFile(blobDIR, fileDIR);
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
        if (headBranch.equals(branchName)){
            System.out.print("Cannot remove the current branch.");
            return;
        }
        branches.remove(branchName);
        Utils.writeObject(Repository.branches_DIR, branches);
    }

    public void reset (String commitSHA1){
        //TODO: Could reset go to the commit not belonging to current branch?
        if (commitSHA1.length() != IO.commitSHA1Length || !IO.splitSHA1(Repository.Object_DIR, commitSHA1)[1].exists()){
            System.out.print("No commit with that id exists.");
            return;
        }
        Commit.innerCommit futureCommit = IO.readCommit(commitSHA1);
        if (!ScanUntracked(null, futureCommit).isEmpty()){
            System.out.print("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }
        restoreCommit(commitSHA1);
        branches.put(headBranch, commitSHA1);
        Utils.writeObject(Repository.branches_DIR, branches);
        Utils.writeContents(Repository.HEADSHA1_DIR, commitSHA1);
        Utils.writeObject(Repository.Staged_DIR, new TreeMap<File, String>());
        Utils.writeObject(Repository.Removed_DIR, new TreeMap<File, String>());
    }
    private String findSplitPoint(String branchSHA1){
        List<String> SHA1Pool = new ArrayList<>();
        SHA1Pool.add(headSHA1);
        SHA1Pool.add(branchSHA1);
        Queue<String> SHA1Q = new LinkedList<>();
        SHA1Q.offer(headSHA1);
        SHA1Q.offer(branchSHA1);
        Commit.innerCommit nextCommit;
        while (!SHA1Q.isEmpty()){
            String nextSHA1 = SHA1Q.poll();
            nextCommit = IO.readCommit(nextSHA1);
            //deal with the parents of nextCommit.
            if (SHA1Pool.contains(nextCommit.p1)){
                return nextCommit.p1;
            } else if (nextCommit.p1 != null) {
                SHA1Pool.add(nextCommit.p1);
                SHA1Q.offer(nextCommit.p1);
            }
            if (SHA1Pool.contains(nextCommit.p2)){
                return nextCommit.p2;
            } else if (nextCommit.p2 != null) {
                SHA1Pool.add(nextCommit.p2);
                SHA1Q.offer(nextCommit.p2);
            }
        }
        return null;
    }

    /**
     *
     * @param conflictFile: aimFile
     * @param headFlag: 1 = file existed. 0 = file not existed.
     * @param otherFlag: 1 = file existed. 0 = file not existed.
     */
    private void dealConflict (Commit.innerCommit otherBranchHead, File conflictFile, int headFlag, int otherFlag){
        String headContent, otherContent;
        if (headFlag == 1) {
            File headBlob = IO.splitSHA1(Object_DIR, headCommit.blobMap.get(conflictFile))[1];
            headContent = Utils.readContentsAsString(headBlob);
        } else { headContent = null;}
        if (otherFlag == 1){
            File otherBlob = IO.splitSHA1(Object_DIR, otherBranchHead.blobMap.get(conflictFile))[1];
            otherContent = Utils.readContentsAsString(otherBlob);
        } else { otherContent = null;}
        String newContent = "<<<<<<< HEAD" + System.lineSeparator() + headContent + "=======" + System.lineSeparator() + otherContent + ">>>>>>>";
        Utils.writeContents(conflictFile, newContent);
    }

    private boolean updateMergeFiles(Commit.innerCommit otherBranchHead, Commit.innerCommit splitpointCommit){
        //TODO: Then, if the merge encountered a conflict, print the message Encountered a merge conflict. on the terminal (not the log).
        boolean existedInHead, existedInOther, HeadSame, OtherSame, conflictFlag = false;
        for (File i: splitpointCommit.blobMap.keySet()){
            String iSHA1 = splitpointCommit.blobMap.get(i);
            existedInHead = headCommit.blobMap.containsKey(i);
            existedInOther = otherBranchHead.blobMap.containsKey(i);
            HeadSame = OtherSame = false; //TODO: think about this init value.
            if (existedInHead) { HeadSame = iSHA1.equals(headCommit.blobMap.get(i));}
            if (existedInOther) { OtherSame = iSHA1.equals(otherBranchHead.blobMap.get(i));}
            if ((!existedInHead && !existedInOther) || (!existedInHead && OtherSame) || (HeadSame && !existedInOther)){
                TempRemoved.put(i, "Delete");
                Utils.restrictedDelete(i);
            } else if (HeadSame && !OtherSame){
                TempStaged.put(i, otherBranchHead.blobMap.get(i));
                File blobDIR = IO.splitSHA1(Object_DIR,otherBranchHead.blobMap.get(i))[1];
                IO.copyFile(blobDIR, i);
            } else if(!HeadSame && !existedInOther){
                //deal conflict. Head modified, other delete.
                conflictFlag = true;
                dealConflict(otherBranchHead, i, 1, 0);
            } else if (!existedInHead && !OtherSame){
                //deal conflict. Head delete, other modified.
                conflictFlag = true;
                dealConflict(otherBranchHead, i, 0, 1);
            } else if (!HeadSame && !OtherSame) {
                //deal conflict. Both head and other modified.
                conflictFlag = true;
                dealConflict(otherBranchHead, i, 1, 1);
            }
            headCommit.blobMap.remove(i);
            otherBranchHead.blobMap.remove(i);
        }
        //Not in splitepoint.
        if (!headCommit.blobMap.isEmpty()) {
            for (File ii: headCommit.blobMap.keySet()){
                if (otherBranchHead.blobMap.containsKey(ii) && !headCommit.blobMap.get(ii).equals(otherBranchHead.blobMap.get(ii))){
                    //TODO: deal conflict. Both head and other modified.
                    conflictFlag = true;
                }
                otherBranchHead.blobMap.remove(ii);
            }
        }
        if (!otherBranchHead.blobMap.isEmpty()){
            for (File ii: otherBranchHead.blobMap.keySet()) {
                TempStaged.put(ii, otherBranchHead.blobMap.get(ii));
                File blobDIR = IO.splitSHA1(Object_DIR,otherBranchHead.blobMap.get(ii))[1];
                IO.copyFile(blobDIR, ii);
            }
        }
        return  conflictFlag;
    }
    public void merge (String branchName){
        //TODO: If merge would generate an error because the commit that it does has no changes in it, just let the normal commit error message for this go through.
        if (!TempStaged.isEmpty() || !TempRemoved.isEmpty()){
            System.out.print("You have uncommitted changes.");
            return;
        } else if (!branches.containsKey(branchName)){
            System.out.print("A branch with that name does not exist.");
            return;
        } else if (headBranch.equals(branchName)){
            System.out.print("Cannot merge a branch with itself.");
            return;
        }
        String branchSHA1 = branches.get(branchName);
        Commit.innerCommit otherBranchHead = IO.readCommit(branchSHA1);
        if (!ScanUntracked(null, otherBranchHead).isEmpty()){
            System.out.print("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }
        String splitpointSHA1 = findSplitPoint(branchSHA1);
                if (branchSHA1.equals(splitpointSHA1)){
            System.out.print("Given branch is an ancestor of the current branch.");
            return;
        } else if (headSHA1.equals(splitpointSHA1)){
            checkoutBranch(branchName);
            System.out.print("Current branch fast-forwarded.");
            return;
        }
        Commit.commit("Merged " + branchName + " into " + headBranch + ".",
                headCommit, headSHA1, branchSHA1, TempStaged, TempRemoved);
        Commit.innerCommit splitpointCommit = IO.readCommit(splitpointSHA1);
        boolean conflictFlag = updateMergeFiles(otherBranchHead, splitpointCommit);
        if (conflictFlag){ System.out.print("Encountered a merge conflict.");}
    }
}
