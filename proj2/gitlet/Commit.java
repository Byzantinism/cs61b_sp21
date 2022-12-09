package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  @author Zebang Ge
 */
public class Commit {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    private static String initBranchName = "master";
    public static class InnerCommit implements Serializable {
        public String message;
        public Date timeStamp;
        public String p1, p2;
        public TreeMap<File, String> blobMap;

        public InnerCommit(String message, Date currentTime, String p1, String p2){
            this.message = message;
            this.timeStamp = currentTime;
            this.p1 = p1;
            this.p2 = p2;
            this.blobMap = new TreeMap<>();
        }
    }

    private static String saveProcess (String branchName, InnerCommit commit){
        String SHA1 = IO.saveCommit(commit);
        //Update branches Map.
        TreeMap<String, String> branches = Utils.readObject(Repository.branches_DIR, TreeMap.class);
        branches.put(branchName, SHA1);
        Utils.writeObject(Repository.branches_DIR, branches);
        //Update HEAD
        Utils.writeContents(Repository.HEADbranch_DIR, branchName); //do NOT delete this line because of init Commit.
        Utils.writeContents(Repository.HEADSHA1_DIR, SHA1);
        return SHA1;
    }

    public static String initCommit (){
        //Init Timestamp: 00:00:00 UTC, Thursday, 1 January 1970
        InnerCommit init = new InnerCommit("initial commit", new Date(0), null, null);
        String SHA1 = saveProcess(initBranchName, init);
        return SHA1;
    }

    public static String commit (String message, InnerCommit headCommit,
                                 String headSHA1, String p2,
                                 TreeMap<File, String> TempStaged,
                                 TreeMap<File, String> TempRemoved){
        headCommit.message = message;
        headCommit.timeStamp = new Date();
        headCommit.p1 = headSHA1;
        headCommit.p2 = p2;

        //2. check staged/Removed Map and save.
        for (File i: TempStaged.keySet()){
            headCommit.blobMap.put(i, TempStaged.get(i));
        }
        for (File i: TempRemoved.keySet()){
            headCommit.blobMap.remove(i);
        }
        Utils.writeObject(Repository.Staged_DIR, new TreeMap<File, String>());
        Utils.writeObject(Repository.Removed_DIR, new TreeMap<File, String>());
        //3. save current Commit.
        String HeadBranch = Utils.readContentsAsString(Repository.HEADbranch_DIR);
        String SHA1 = saveProcess(HeadBranch, headCommit);
        return SHA1;
    }
}
