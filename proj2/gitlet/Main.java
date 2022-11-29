package gitlet;

import java.util.ResourceBundle;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Zebang Ge
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // what if args is empty?
        if (args.length == 0){throw new GitletException("Please enter a command.");}
        String firstArg = args[0];
        // TODO: handle the `init` command
        if (firstArg.equals("init")){
            if (args.length > 1) throw new GitletException("Incorrect operands.");
            Repository.init();
            return;
        }
        // Following gitlet commands need .gitlet folder.
        // TODO: If a user inputs a command with the wrong number or format of operands, print the message Incorrect operands. and exit.
        Repository.initCheck(1);
        Repository work = new Repository();
        switch(firstArg) {
            case "add":
                if (args.length > 2 || args.length == 1) throw new GitletException("Incorrect operands.");
                work.add(args[1]);
                break;
            case "rm":
                if (args.length > 2 || args.length == 1) throw new GitletException("Incorrect operands.");
                work.rm(args[1]);
                break;
            case "commit":
                if (args.length > 2 || args.length == 1) throw new GitletException("Incorrect operands.");
                work.commit(args[1]);
                break;
            case "log":

                break;
            case "global-log":

                break;
            case "find":

                break;
            case "status":

                break;
            case "checkout":
                if (args.length == 2){
                    //checkout [branch name]
                    work.checkoutBranch(args[1]);
                } else if (args.length == 3 && args[1].equals("--")){
                    //checkout -- [file name]
                    work.checkoutFile(args[2]);
                } else if (args.length == 4 && args[1].length() == IO.commitSHA1Length && args[2].equals("--")){
                    //checkout [commit id] -- [file name]
                    work.checkoutFileInCommit(args[1], args[3]);
                } else {
                    throw new GitletException("Incorrect operands.");
                }
                break;
            case "branch":

                break;
            case "rm-branch":

                break;
            case "reset":

                break;
            case "merge":

                break;
            default:
                throw new GitletException("No command with that name exists.");
        }
    }
}
