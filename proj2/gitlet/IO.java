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
    public static final int COMMITSHA1LENGTH = Utils.UID_LENGTH + 1;
    public static final String COMMITSTRING = "a";

    //split SHA1 to two file address.
    public static File[] splitSHA1(File aim, String sha1) {
        //Split SHA1 as first 2 char and rest 39 char for 2 layers.
        File sha1DirL1 = Utils.join(aim, sha1.substring(0, 2)); //1st layer
        File sha1DirL2 = Utils.join(sha1DirL1, sha1.substring(2)); //2nd layer
        return new File[]{sha1DirL1, sha1DirL2}; //[1st layer, 2nd layer]
    }
    //Check whether SHA1 is 41 length.
    public static void checkSHA1Length(String sha1) {
        if (sha1.length() != COMMITSHA1LENGTH) {
            throw new GitletException("This SHA1 length is NOT 41");
        }
    }
    public static String saveCommit(Commit.InnerCommit commit) {
        //add COMMITSTRING at the end for commit type
        byte[] commitS = Utils.serialize(commit);
        String SHA1 = Utils.sha1(commitS) + COMMITSTRING;

        File[] saveDIR = splitSHA1(Repository.Object_DIR, SHA1);
        if (!saveDIR[0].exists()) {
            saveDIR[0].mkdir();
        }
        if (saveDIR[1].exists()) {
            throw new GitletException("Same SHA1 file is already existed.");
        }
        Utils.writeContents(saveDIR[1], commitS);
        return SHA1;
    }
    public static Commit.InnerCommit readCommit(String sha1) {
        if (sha1 == null) {
            return null;
        }
        checkSHA1Length(sha1);
        String last1 = sha1.substring(sha1.length() - 1);
        File sha1Dir = splitSHA1(Repository.Object_DIR, sha1)[1];
        //The last char should be COMMITSTRING
        if (COMMITSTRING.equals(last1)) {
            return Utils.readObject(sha1Dir, Commit.InnerCommit.class);
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
    public static void copyFile(File blobDIR, File fileDIR) {
        try {
            Files.copy(blobDIR.toPath(), fileDIR.toPath(), REPLACE_EXISTING);
        } catch (IOException excp) {
            throw new GitletException(excp.getMessage());
        }
    }

    public static String shortCommitId(String id) {
        File folder = Utils.join(Repository.Object_DIR, id.substring(0, 2));
        List<String> fileList = Utils.plainFilenamesIn(folder);
        String needMatch = id.substring(2);
        int matchLength = id.length() - 2;
        if (fileList != null) {
            for (String i : fileList) {
                if (needMatch.equals(i.substring(0, matchLength)) && (i.length() == COMMITSHA1LENGTH - 2)) {
                    return id.substring(0, 2) + i;
                }
            }
        }
        return null;
    }
}
