package gitlet;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

//need to differ Commit, and use corresponding directory to input and output.
public class IO {
    public static final int commitSHA1Length = Utils.UID_LENGTH + 1;
    public static final String commitString = "a";

    //split SHA1 to two file address.
    public static File[] splitSHA1(File aim ,String SHA1){
        //Split SHA1 as first 2 char and rest 39 char for 2 layers.
        File SHA1_DIR_L1 = Utils.join(aim, SHA1.substring(0,2)); //1st layer
        File SHA1_DIR_L2 = Utils.join(SHA1_DIR_L1, SHA1.substring(2));//2nd layer
        return new File[]{SHA1_DIR_L1, SHA1_DIR_L2};//[1st layer, 2nd layer]
    }
    //Check whether SHA1 is 41 length.
    public static void checkSHA1Length(String SHA1){
        if (SHA1.length() != commitSHA1Length) throw new GitletException("This SHA1 length is NOT 41");
    }
    public static String saveCommit (Commit.innerCommit commit){
        //add commitString at the end for commit type
        byte[] commit_s = Utils.serialize(commit);
        String SHA1 = Utils.sha1(commit_s) + commitString;

        File[] saveDIR = splitSHA1(Repository.Object_DIR, SHA1);
        if (!saveDIR[0].exists()) saveDIR[0].mkdir();
        if (saveDIR[1].exists()) throw new GitletException("Same SHA1 file is already existed.");

        //Utils.writeObject(saveDIR[1], commit);
        Utils.writeContents(saveDIR[1], commit_s);
        return SHA1;
    }
    public static Commit.innerCommit readCommit(String SHA1){
        if (SHA1 == null) return null;
        checkSHA1Length(SHA1);
        String last1 = SHA1.substring(SHA1.length()-1);
        File SHA1_DIR = splitSHA1(Repository.Object_DIR, SHA1)[1];
        //The last char should be commitString
        if (commitString.equals(last1)) {
            return Utils.readObject(SHA1_DIR, Commit.innerCommit.class);
        } else {
            throw new GitletException("This SHA1 does NOT belong to a Commit");
        }
    }
    /** Filter out all but plain folders. */
    private static final FilenameFilter PLAIN_FOLDERS =
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            };
    /** Returns a list of the names of all plain folders in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    public static List<String> plainFoldernamesIn(File dir) {
        String[] folders = dir.list(PLAIN_FOLDERS);
        if (folders == null) {
            return null;
        } else {
            Arrays.sort(folders);
            return Arrays.asList(folders);
        }
    }
    public static void copyFile(File blobDIR, File fileDIR){
        try {
            Files.copy(blobDIR.toPath(), fileDIR.toPath(), REPLACE_EXISTING);
        } catch (IOException excp) {
            throw new GitletException(excp.getMessage());
        }
    }
}
