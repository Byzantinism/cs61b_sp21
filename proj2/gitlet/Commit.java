package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Zebang Ge
 */
public class Commit {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    //Class property
    public File commitAddress;
    public String commitSHA1;
    //Instance property
    //public innerCommit commit;

    public static class innerCommit implements Serializable {
        public String message;
        public Date timeStamp;
        public String p1, p2;
        public TreeMap<File, String> blobMap;

        public innerCommit (String message, Date currentTime, String p1, String p2){
            this.message = message;
            this.timeStamp = currentTime;
            this.p1 = p1;
            this.p2 = p2;
            this.blobMap = new TreeMap<>();
        }
    }

    private static String saveProcess (String branchName, innerCommit commit){
        String SHA1 = IO.saveCommit(commit);

        //Update branches Map.
        TreeMap<String, LinkedList<String>> branches = Utils.readObject(Repository.branches_DIR, TreeMap.class);
        LinkedList<String> HeadBranch = branches.get(branchName);
        if (HeadBranch == null){
            LinkedList<String> newCommit = new LinkedList<>();
            newCommit.addFirst(SHA1);
            branches.put(branchName, newCommit);
        } else {
            HeadBranch.addFirst(SHA1);
        }
        Utils.writeObject(Repository.branches_DIR, branches);

        //Update HEAD
        Utils.writeContents(Repository.HEADbranch_DIR, branchName);
        Utils.writeContents(Repository.HEAD_DIR, SHA1);

        //TODO: fill log part.
        return SHA1;
    }

    public static String initCommit (){
        //Init Timestamp: 00:00:00 UTC, Thursday, 1 January 1970
        innerCommit init = new innerCommit("initial commit",new Date(0), null, null);
        String SHA1 = saveProcess("master", init);
        return SHA1;
    }

    public static String commit (String message, innerCommit headCommit, String headSHA1,
                                 TreeMap<File, String> TempStaged,
                                 TreeMap<File, String> TempRemoved){
        //TODO: corner cases.
        headCommit.message = message;
        headCommit.timeStamp = new Date();
        headCommit.p1 = headSHA1;

        //2. check staged/Removed Map.
        for (File i: TempStaged.keySet()){
            headCommit.blobMap.put(i, TempStaged.remove(i));
        }
        for (File i: TempRemoved.keySet()){
            headCommit.blobMap.remove(i);
        }

        //3. save current Commit.
        String HeadBranch = Utils.readContentsAsString(Repository.HEADbranch_DIR);
        String SHA1 = saveProcess(HeadBranch, headCommit);
        return SHA1;
    }
}
