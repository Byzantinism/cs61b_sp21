package gitlet;

import java.util.ResourceBundle;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Zebang Ge
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    private static boolean argsCheck (String[] args, int length){
        if (args.length != length) {
            System.out.print("Incorrect operands.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        // what if args is empty?
        if (args.length == 0){
            System.out.print("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        //handle the `init` command
        if (firstArg.equals("init")){
            if (!argsCheck(args, 1)) return;
            Repository.init();
            return;
        }
        // Following gitlet commands need .gitlet folder.
        // TODO: If a user inputs a command with the wrong number or format of operands, print the message Incorrect operands. and exit.
        if (!Repository.initCheck(1)) return;
        Repository work = new Repository();
        switch(firstArg) {
            case "add":
                if (!argsCheck(args, 2)) return;
                work.add(args[1]);
                break;
            case "rm":
                if (!argsCheck(args, 2)) return;
                work.rm(args[1]);
                break;
            case "commit":
                if (!argsCheck(args, 2)) return;
                work.commit(args[1]);
                break;
            case "log":
                if (!argsCheck(args, 1)) return;
                work.log();
                break;
            case "global-log":
                if (!argsCheck(args, 1)) return;
                Repository.globalLog();
                break;
            case "find":
                if (!argsCheck(args, 2)) return;
                Repository.find(args[1]);
                break;
            case "status":
                if (!argsCheck(args, 1)) return;
                work.status();
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
                    System.out.print("Incorrect operands.");
                }
                break;
            case "branch":
                if (!argsCheck(args, 2)) return;
                work.createBranch(args[2]);
                break;
            case "rm-branch":
                if (!argsCheck(args, 2)) return;
                work.rmBranch(args[2]);
                break;
            case "reset":
                if (!argsCheck(args, 2)) return;
                work.reset(args[2]);
                break;
            case "merge":
                //TODO: wait for finished.
                break;
            default:
                System.out.print("No command with that name exists.");
        }
    }
}
