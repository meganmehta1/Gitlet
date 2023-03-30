package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid little
 * version-control system for my stupid little 61B grade
 * for my stupid little CS degree.
 * This class has the function calls depending on what command is
 * read in, and has error messages for all invalid commands.
 *  @author Megan Mehta
 */
public class Main {
    public static void main(String... args) throws IOException {
        if (args.length <= 0) {
            System.out.println("Please enter a command.");
        } else {
            File cwd = Utils.join(System.getProperty("user.dir"));
            switch (args[0]) {
            case "init": {
                if (validArgs(1, args)) {
                    GitletRepo.init();
                }
                break;
            }
            case "add": {
                if (validArgs(2, args)) {
                    GitletRepo.add(args[1]);
                }
                break;
            }
            case "commit": {
                if (validArgs(2, args)) {
                    GitletRepo.commit(args[1]);
                }
                break;
            }
            case "rm": {
                if (validArgs(2, args)) {
                    GitletRepo.remove(args[1]);
                }
                break;
            }
            case "log": {
                if (validArgs(1, args)) {
                    GitletRepo.log();
                }
                break;
            }
            case "global-log": {
                if (validArgs(1, args)) {
                    GitletRepo.globalLog();
                }
                break;
            }
            case "find": {
                if (validArgs(2, args)) {
                    GitletRepo.find(args[1]);
                }
                break;
            }
            case "status": {
                if (validArgs(1, args)) {
                    GitletRepo.status();
                }
                break;
            }
            default:
                main2(args);
            }
        }
        System.exit(0);
    }

    public static void main2(String[] args) throws IOException {
        switch (args[0]) {
        case "checkout": {
            if (validArgs(2, args) || validArgs(3, args)
                    || validArgs(4, args)) {
                GitletRepo.checkout(args);
            }
            break;
        }
        case "branch": {
            if (validArgs(2, args)) {
                GitletRepo.branch(args[1]);
            }
            break;
        }
        case "rm-branch": {
            if (validArgs(2, args)) {
                GitletRepo.removeBranch(args[1]);
            }
            break;
        }
        case "reset": {
            if (validArgs(2, args)) {
                GitletRepo.reset(args[1]);
            }
            break;
        }
        case "merge": {
            if (validArgs(2, args)) {
                GitletRepo.merge(args[1]);
            }
            break;
        }
        default:
            System.out.println("No command with that name exists.");
        }
    }

    public static boolean validArgs(int expected, String... args) {
        return (args.length == expected);
    }

}
