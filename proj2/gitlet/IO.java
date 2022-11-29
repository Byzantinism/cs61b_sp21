package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

// need to differ Commit and blob, and use corresponding directory to input and output.
public class IO {
    public static final int commitSHA1Length = Utils.UID_LENGTH + 1;

    //split SHA1 to two file address.
    public static File[] splitSHA1(File aim ,String SHA1){
        //Split SHA1 as first 2 char and rest 39 char for 2 layers.
        File SHA1_DIR_L1 = Utils.join(aim, SHA1.substring(0,2)); //1st layer
        File SHA1_DIR_L2 = Utils.join(SHA1_DIR_L1, SHA1.substring(2));//2nd layer
        return new File[]{SHA1_DIR_L1, SHA1_DIR_L2};//[1st layer, 2nd layer]
    }

    //Check whether SHA1 is 41 length.
    private static void checkSHA1Length(String SHA1){
        if (SHA1.length() != commitSHA1Length) throw new GitletException("This SHA1 length is NOT 41");
    }

    public static String saveCommit (Commit.innerCommit commit){
        //add "x" at the end for commit type
        byte[] commit_s = Utils.serialize(commit);
        String SHA1 = Utils.sha1(commit_s) + "X";

        File[] saveDIR = splitSHA1(Repository.Object_DIR, SHA1);
        if (!saveDIR[0].exists()) saveDIR[0].mkdir();
        if (saveDIR[1].exists()) throw new GitletException("Same SHA1 file is already existed.");

        //Utils.writeObject(saveDIR[1], commit);
        Utils.writeContents(saveDIR[1], commit_s);
        return SHA1;
    }

    public static String saveBlob (File blob){
        String blobContent = Utils.readContentsAsString(blob);
        //add "p" at the end for blob type
        String SHA1 = Utils.sha1(blobContent) + "p";

        File[] saveDIR = splitSHA1(Repository.Object_DIR, SHA1);
        if (!saveDIR[0].exists()) saveDIR[0].mkdir();
        if (saveDIR[1].exists()) throw new GitletException("Same SHA1 file is already existed.");

        //TODO: change below line to save blob file to aim address by using copy method.
        Utils.writeContents(saveDIR[1], blobContent);
        //TODO: ???????read blobMap inside this commit and add blob SHA1 and its origin file name.

        return SHA1;
    }

    public static Commit.innerCommit readCommit(String SHA1){
        checkSHA1Length(SHA1);
        String last1 = SHA1.substring(SHA1.length()-1);
        File SHA1_DIR = splitSHA1(Repository.Object_DIR, SHA1)[1];
        if (!SHA1_DIR.exists()){
            throw new GitletException("No commit with that id exists.");
        }
        //The last char should be "X"
        if ("X".equals(last1)) {
            return Utils.readObject(SHA1_DIR, Commit.innerCommit.class);
        } else {
            throw new GitletException("This SHA1 does NOT belong to a Commit");
        }
    }

    public static String readBlob(String SHA1){
        checkSHA1Length(SHA1);
        String last1 = SHA1.substring(SHA1.length()-1);
        File SHA1_DIR = splitSHA1(Repository.Object_DIR, SHA1)[1];
        //The last char should be "p"
        if ("p".equals(last1)) {
            return Utils.readContentsAsString(SHA1_DIR);
        } else {
            throw new GitletException("This SHA1 should NOT belong to a Commit");
        }
    }
}
