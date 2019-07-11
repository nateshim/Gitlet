package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author
 */
public class Main {
    private static ArrayList<Branch> listOfBranches = new ArrayList<Branch>();
    private static ArrayList<String> removedFiles = new ArrayList<String>();
    private static ArrayList<Commit> repository = new ArrayList<Commit>();
    private static File directoryPath = new File(System.getProperty("user.dir"));
    private static File gitletDir = new File(directoryPath, ".gitlet");
    private static ArrayList<File> sa = new ArrayList<File>();
    private static ArrayList<File> marked = new ArrayList<File>();
    private static ArrayList<File> tracked = new ArrayList<File>();
    private static Branch currBranch = new Branch(new ArrayList<Commit>(), null);

    public static void init() {
        if (gitletDir.exists()) {
            System.out.println("A gitlet version-control system already exists"
                    + " in the current directory.");
            System.exit(0);
        } else {
            Commit latest = new Commit(null, "initial commit");
            Branch master = new Branch(new ArrayList<Commit>(), "master");
            master.getBranch().add(latest);
            repository.add(latest);
            listOfBranches.add(master);
            currBranch = listOfBranches.get(0);
            //create .gitlet directory
            gitletDir.mkdir();
            //serialize staging area
            Serialized.saveStagingArea(sa);
            //serialize listofbranches
            Serialized.saveCurrBranch(currBranch);
            Serialized.savemarked(marked);
            Serialized.savelistofBranches(listOfBranches);
            Serialized.saveremovedFiles(removedFiles);
            Serialized.saverepository(repository);
        }
    }

    public static void add(String filename) {
        File file = new File(directoryPath, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        sa = Serialized.loadStagingArea();
        removedFiles = Serialized.loadremovedFiles();
        repository = Serialized.loadrepository();
        marked = Serialized.loadmarked();
        Commit latestCommit = repository.get(repository.size() - 1);
        //if the file had been marked to be removed, delete that mark
        if (removedFiles.contains(filename)) {
            removedFiles.remove(filename);
            marked.remove(file);
            Serialized.savemarked(marked);
        }
        if (latestCommit.getContents().containsKey(filename)) {
            if (!latestCommit.getContents().get(filename)
                    .equals(Utils.sha1(Utils.readContents(file)))) {
                sa.add(file); //add
            }
        } else {
            sa.add(file);
        }
        Serialized.saveStagingArea(sa);
        Serialized.saveremovedFiles(removedFiles);
    }

    public static void commit(String message) {
        sa = Serialized.loadStagingArea();
        marked = Serialized.loadmarked();
        //create a commit with the correct parent and message, empty contents
        if (sa.isEmpty() && marked.isEmpty()) { //all files of recent commit are in workingdirectory
            System.out.print("No changes added to the commit.");
            System.exit(0);
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        listOfBranches = Serialized.loadlistOfBranches();
        repository = Serialized.loadrepository();
        removedFiles = Serialized.loadremovedFiles();
        currBranch = Serialized.loadCurrBranch();
        Commit parent = currBranch.getBranch().get(currBranch.getBranch()
                .size() - 1);
        Commit newCommit = new Commit(parent.getSHA(), message);
        newCommit.setContents(parent.getContents());
        int i;
        for (i = 0; i < listOfBranches.size(); i++) {
            if (listOfBranches.get(i).getName().equals(currBranch.getName())) {
                listOfBranches.get(i).getBranch().add(newCommit);
                listOfBranches.get(i).setHead(listOfBranches.get(i).getHead() + 1);
                break;
            }
        }
        File f = new File(gitletDir, newCommit.getSHA());
        f.mkdir(); //creating new commit folder
        for (String s: newCommit.getContents().keySet()) { //for all already existing files
            //removed files case
            if (!removedFiles.contains(s)) {
                File blob = new File(f, s); //creating new file in new commit folder
                Utils.writeContents(blob, Utils.readContents(new File(".gitlet/"
                        + parent.getSHA(), s)));
                //write parent file into new file
            }
        }
        for (int x = 0; x < sa.size(); x++) {
            //sa.get(x) = staged file
            //read contents of sa.get(x) to change into byte[], then sha this
            //then put into hashmap
            newCommit.getContents().put(sa.get(x).getName(),
                    Utils.sha1(Utils.readContents(sa.get(x))));
            File blob = new File(f, sa.get(x).getName());
            Utils.writeContents(blob, Utils.readContents(sa.get(x)));
        }
        repository.add(newCommit);
        currBranch = listOfBranches.get(i);
        sa.clear();
        removedFiles.clear();
        marked.clear();
        //serialize cleared staging area
        Serialized.saveStagingArea(sa);
        //serialize listofbranches
        Serialized.savemarked(marked);
        Serialized.savelistofBranches(listOfBranches);
        Serialized.saverepository(repository);
        Serialized.saveremovedFiles(removedFiles);
        Serialized.saveCurrBranch(currBranch);
    }

    public static void rm(String filename) {
        removedFiles = Serialized.loadremovedFiles();
        listOfBranches = Serialized.loadlistOfBranches();
        currBranch = Serialized.loadCurrBranch();
        marked = Serialized.loadmarked();
        int counter = currBranch.getBranch().size();
        Commit current = currBranch.getBranch().get(counter - 1);
        sa = Serialized.loadStagingArea();
        File file = new File(directoryPath, filename);
        if (current.getContents().containsKey(filename)) {
            if (sa.contains(file)) {
                sa.remove(file);
            }
            current.getContents().remove(filename);
            marked.add(file);
            removedFiles.add(filename);
            Utils.restrictedDelete(file);
        } else if (sa.contains(file)) {
            sa.remove(file);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        Serialized.savemarked(marked);
        Serialized.saveStagingArea(sa);
        Serialized.savelistofBranches(listOfBranches);
        Serialized.saveremovedFiles(removedFiles);
        Serialized.saveCurrBranch(currBranch);
    }

    public static void log() {
        listOfBranches = Serialized.loadlistOfBranches();
        currBranch = Serialized.loadCurrBranch();
        ArrayList<Commit> curr = currBranch.getBranch();
        for (int x = currBranch.getHead(); x >= 0; x--) {
            System.out.println("===");
            System.out.println("Commit " + curr.get(x).getSHA());
            System.out.println(curr.get(x).getCommitDate());
            System.out.println(curr.get(x).toString());
            System.out.println();
        }
    }

    public static void globalLog() {
        repository = Serialized.loadrepository();
        for (int x = repository.size() - 1; x >= 0; x--) {
            System.out.println("===");
            System.out.println("Commit " + repository.get(x).getSHA());
            System.out.println(repository.get(x).getCommitDate());
            System.out.println(repository.get(x).toString());
            System.out.println();
        }
    }

    public static void find(String message) {
        int counter = 0;
        repository = Serialized.loadrepository();
        for (int x = 0; x < repository.size(); x++) {
            if (repository.get(x).toString().equals(message)) {
                counter++;
                System.out.println(repository.get(x).getSHA());
            }
        }
        if (counter == 0) {
            System.out.println("Found no commit with that message.");
        }
        System.exit(0);
    }

    public static void status() {
        sa = Serialized.loadStagingArea();
        currBranch = Serialized.loadCurrBranch();
        listOfBranches = Serialized.loadlistOfBranches();
        removedFiles = Serialized.loadremovedFiles();
        Collections.sort(sa);
        Collections.sort(listOfBranches);
        Collections.sort(removedFiles);
        //Collections.sort(directoryPath);
        System.out.println("=== Branches ===");
        for (int x = 0; x < listOfBranches.size(); x++) {
            if (currBranch.getName().equals(listOfBranches.get(x).getName())) {
                System.out.println("*" + listOfBranches.get(x).getName());
            } else {
                System.out.println(listOfBranches.get(x).getName());
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (int x = 0; x < sa.size(); x++) {
            System.out.println(sa.get(x).getName());
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (int x = 0; x < removedFiles.size(); x++) {
            System.out.println(removedFiles.get(x));
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        Serialized.savelistofBranches(listOfBranches);
        Serialized.saveStagingArea(sa);
        Serialized.saveremovedFiles(removedFiles);
    }

    public static void checkoutBranch(String branchname) {
        listOfBranches = Serialized.loadlistOfBranches();
        repository = Serialized.loadrepository();
        sa = Serialized.loadStagingArea();
        currBranch = Serialized.loadCurrBranch();
        int indexofbranch = -1;
        for (int x = 0; x < listOfBranches.size(); x++) {
            if (listOfBranches.get(x).getName().equals(branchname)) {
                indexofbranch = x;
            }
        }
        if (indexofbranch == -1) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        List<String> wdFiles = Utils.plainFilenamesIn(directoryPath);
        Commit c = currBranch.getBranch().get(currBranch.getHead());
        //head commit of current branch
        Branch givenbranch = listOfBranches.get(indexofbranch);
        if (givenbranch.getName().equals(currBranch.getName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit commit = givenbranch.getBranch().get(givenbranch.getHead()); //main commit: f&g
        //head commit of given branch
        for (String s : wdFiles) {
            File file = new File(".gitlet/" + c.getSHA(), s); // /wd/.gitlet/alt CURRENT
            File f = new File(".gitlet/" + commit.getSHA(), s); // /wd/.gitlet/main GIVEN
            File fwd = new File(directoryPath, s);
            if (!file.exists() && f.exists() && !Utils.sha1(Utils.readContents(f))
                    .equals(Utils.sha1(Utils.readContents(fwd)))) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                System.exit(0);
            }
        }
        commit.getContents().forEach((k, v) -> { //for each hash in latest given commit
            try {
                Files.copy(Paths.get(".gitlet/" + commit.getSHA() + "/" + k),
                        Paths.get(k), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                File f = new File(directoryPath, k);
            }
        });
<<<<<<< HEAD
        currBranch = givenbranch; //master
        if (currBranch != listOfBranches.get(0)) {
            currBranch.getBranch().add(0, repository.get(0));
        }
=======
        //if file in current is not in given, delete; else, overwrite/create new
        c.getContents().forEach((k, v) -> { //for each hash in latest current commit
            if (!commit.getContents().containsKey(k)) {
                File delete = new File(directoryPath, k);
                Utils.restrictedDelete(delete);
            }
        });
        currBranch = givenbranch;
>>>>>>> 256bae170ec7a5ab3e9a7229815f8371005642c2
        Serialized.saveCurrBranch(currBranch);
        Serialized.savelistofBranches(listOfBranches);
        sa.clear();
        Serialized.saveStagingArea(sa);
    }

    public static void checkoutFileName(String filename) {
        listOfBranches = Serialized.loadlistOfBranches();
        currBranch = Serialized.loadCurrBranch();
        int counter = currBranch.getBranch().size();
        Commit headCommit = currBranch.getBranch().get(counter - 1);
        if (headCommit.getContents().containsKey(filename)) {
            try {
                Files.copy(Paths.get(".gitlet/" + headCommit.getSHA() + "/" + filename),
                        Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("IO Exception.");
            }
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Serialized.saveCurrBranch(currBranch);
        Serialized.savelistofBranches(listOfBranches);
    }

    public static void checkoutCommitID(String commitid, String filename) {
        repository = Serialized.loadrepository();
        String fullid = "";
        int index = -1;
        for (int x = 0; x < repository.size(); x++) {
            if (repository.get(x).getSHA().substring(0, 8).equals(commitid.substring(0, 8))) {
                index = x;
                fullid = repository.get(x).getSHA();
            }
        }
        if (index == -1) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else if (!repository.get(index).getContents().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
<<<<<<< HEAD
            if (commitid.length() < 40) {
                try {
                    Files.copy(Paths.get(".gitlet/" + commitid.substring(0, 6) + "/" + filename),
                            Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("IO Exception.");
                }
            } else {
                try {
                    Files.copy(Paths.get(".gitlet/" + commitid + "/" + filename),
                            Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("IO Exception.");
                }
=======
            try {
                Files.copy(Paths.get(".gitlet/" + fullid + "/" + filename),
                        Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("IO Exception.");
>>>>>>> 256bae170ec7a5ab3e9a7229815f8371005642c2
            }
        }
        Serialized.saverepository(repository);
    }


    public static void branch(String branchname) {
        listOfBranches = Serialized.loadlistOfBranches();
        currBranch = Serialized.loadCurrBranch();
        for (Branch b : listOfBranches) {
            if (b.getName().equals(branchname)) {
                System.out.println("A branch with that name already exists.");
                System.exit(0);
            }
        }
        Branch newBranch = new Branch(new ArrayList<Commit>(), branchname);
        for (Commit c: currBranch.getBranch()) {
            newBranch.getBranch().add(c);
        }
        newBranch.setHead(newBranch.getBranch().size() - 1);
        listOfBranches.add(newBranch);
        Serialized.savelistofBranches(listOfBranches);
    }

    public static void rmBranch(String branchname) {
        listOfBranches = Serialized.loadlistOfBranches();
        currBranch = Serialized.loadCurrBranch();
        if (branchname.equals(currBranch.getName())) {
            System.out.println("Cannot remove the current branch");
            System.exit(0);
        }
        for (Branch b : listOfBranches) {
            if (branchname.equals(b.getName())) {
                listOfBranches.remove(branchname);
                Serialized.savelistofBranches(listOfBranches);
            }
        }
        System.out.println("A branch with that name does not exist.");
        System.exit(0);
    }

    public static void reset(String commitid) {
        repository = Serialized.loadrepository();
        listOfBranches = Serialized.loadlistOfBranches();
        currBranch = Serialized.loadCurrBranch();
        sa = Serialized.loadStagingArea();
        int index = -1;
        for (int x = 0; x < repository.size(); x++) {
            if (repository.get(x).getSHA().equals(commitid)) {
                index = x;
            }
        }
        if (index == -1) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = repository.get(index);
        Commit curr = currBranch.getBranch().get(currBranch.getBranch().size() - 1);
        for (String s : Utils.plainFilenamesIn(directoryPath)) {
            File file = new File(".gitlet/" + curr.getSHA(), s); // /wd/.gitlet/alt CURRENT
            File f = new File(".gitlet/" + commit.getSHA(), s); // /wd/.gitlet/main GIVEN
            File fwd = new File(directoryPath, s);
            if (!file.exists() && f.exists() && !Utils.sha1(Utils.readContents(f))
                    .equals(Utils.sha1(Utils.readContents(fwd)))) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                System.exit(0);
            }
        }
<<<<<<< HEAD
        commit.getContents().forEach((k, v) -> {
            if (commitid.length() < 40) {
                try {
                    Files.copy(Paths.get(".gitlet/" + commitid.substring(0, 6) + "/" + k),
                            Paths.get(k), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    File f = new File(directoryPath, k);
                    Utils.restrictedDelete(f);
                }
            } else {
                try {
                    Files.copy(Paths.get(".gitlet/" + commitid + "/" + k),
                            Paths.get(k), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    File f = new File(directoryPath, k);
                    Utils.restrictedDelete(f);
                }
=======
        commit.getContents().forEach((k, v) -> { //for each hash in latest given commit
            try {
                Files.copy(Paths.get(".gitlet/" + commit.getSHA() + "/" + k),
                        Paths.get(k), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                File f = new File(directoryPath, k);
>>>>>>> 256bae170ec7a5ab3e9a7229815f8371005642c2
            }

        });
        //if file in current is not in given, delete; else, overwrite/create new
        curr.getContents().forEach((k, v) -> { //for each hash in latest current commit
            if (!commit.getContents().containsKey(k)) {
                File delete = new File(directoryPath, k);
                Utils.restrictedDelete(delete);
            }
        });
        int i;
        for (i = 0; i < listOfBranches.size(); i++) {
            if (listOfBranches.get(i).getName().equals(currBranch.getName())) {
                break;
            }
        }
        for (int x = 0; x < currBranch.getBranch().size(); x++) {
            if (currBranch.getBranch().get(x).getSHA().equals(commitid)) {
                currBranch.setHead(x); //set it on the list of branches
                listOfBranches.get(i).setHead(x);
            }
        }
        sa.clear();
        Serialized.saveCurrBranch(currBranch);
        Serialized.savelistofBranches(listOfBranches);
        Serialized.saveStagingArea(sa);
        Serialized.saverepository(repository);
    }

    public static void end(boolean encounteredConflict, Branch givenBranch) {
        sa = Serialized.loadStagingArea();
        if (!encounteredConflict) {
            commit("Merged " + currBranch.getName() + " with " + givenBranch.getName() + ".");
            System.exit(0);
        } else {
            System.out.println("Encountered a merge conflict.");
            System.exit(0);
        }
    }
    public static void mergeFailCases(Branch givenBranch) {
        sa = Serialized.loadStagingArea();
        removedFiles = Serialized.loadremovedFiles();
        if (!sa.isEmpty() || !removedFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (givenBranch == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (givenBranch.equals(currBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Serialized.saveremovedFiles(removedFiles);
        Serialized.saveStagingArea(sa);
    }

<<<<<<< HEAD
    public static boolean mergeConflict(boolean encounteredConflict, Commit splitPoint,
                                        HashMap<String, String> filesInGiven,
                                        HashMap<String, String> filesInCurr) {
        ArrayList<File> toPrintCurr = new ArrayList<File>();
        ArrayList<File> toPrintGiven = new ArrayList<File>();
        for (String x : splitPoint.getContents().keySet()) {
            try {
                if (!splitPoint.getContents().containsValue(filesInGiven.get(x)) && !splitPoint.getContents().containsValue(filesInCurr.get(x)) && !filesInGiven.get(x).equals(filesInCurr.get(x))) {
                    File filecurr = new File(directoryPath, filesInCurr.get(x));
                    File filegiven = new File(directoryPath, filesInGiven.get(x));
                    toPrintCurr.add(filecurr);
                    toPrintGiven.add(filegiven);
                } else if (!splitPoint.getContents().containsValue(filesInCurr.get(x)) && !splitPoint.getContents().equals(filesInGiven.get(x))
                        || !splitPoint.getContents().containsValue(filesInGiven.get(x)) && !splitPoint.getContents().equals(filesInCurr.get(x))) {
                    File filecurr = new File(directoryPath, filesInCurr.get(x));
                    File filegiven = new File(directoryPath, filesInGiven.get(x));
                    toPrintCurr.add(filecurr);
                    toPrintGiven.add(filegiven);
                }
            } catch (NullPointerException e) {
                File f = new File(directoryPath, x);
            }
        }
        for (String x : filesInCurr.keySet()) {
            if (splitPoint.getContents().get(x) == null && (!filesInCurr.get(x).equals(filesInGiven.get(x)))) {
                File filecurr = new File(directoryPath, filesInCurr.get(x));
                File filegiven = new File(directoryPath, filesInGiven.get(x));
                toPrintCurr.add(filecurr);
                toPrintGiven.add(filegiven);
            }
        }
        System.out.println("<<<<<<< HEAD");
        for (int x = 0; x < toPrintCurr.size(); x++) {
            byte[] bcurr = Utils.readContents(toPrintCurr.get(x));
            printConflict(bcurr);
        }
        System.out.println("=======");
        for (int x = 0; x < toPrintGiven.size(); x++) {
            byte[] bcurr = Utils.readContents(toPrintGiven.get(x));
            printConflict(bcurr);
        }
        System.out.println(">>>>>>>\n");
        encounteredConflict = true;
        return encounteredConflict;
    }

=======
    public static boolean mergeConflict(Commit splitPoint,
                                        Commit currfiles, Commit givenfiles) {
        boolean encounteredConflict = false;
        ArrayList<File> toPrintCurr = new ArrayList<File>();
        ArrayList<File> toPrintGiven = new ArrayList<File>();
        HashMap<String, String> filesInCurr = currfiles.getContents();
        HashMap<String, String> filesInGiven = givenfiles.getContents();
        File filecurr;
        File filegiven;
        for (String x : splitPoint.getContents().keySet()) {
            if (filesInCurr.get(x) != null && filesInGiven.get(x) != null) { //avoiding npe's
                filecurr = new File(".gitlet/" + currfiles.getSHA(), x);
                filegiven = new File(".gitlet/" + givenfiles.getSHA(), x);
                if (filecurr.exists() && filegiven.exists()
                        && !splitPoint.getContents().get(x).equals(filesInGiven.get(x))
                        && !splitPoint.getContents().get(x).equals(filesInCurr.get(x))
                        && !filesInGiven.get(x).equals(filesInCurr.get(x))) {
                    toPrintCurr.add(filecurr);
                    toPrintGiven.add(filegiven);
                    encounteredConflict = true;
                }
            } else if (!splitPoint.getContents().containsValue(filesInCurr.get(x))
                    && !splitPoint.getContents().get(x).equals(filesInGiven.get(x))
                    || !splitPoint.getContents().containsValue(filesInGiven.get(x))
                    && !splitPoint.getContents().get(x).equals(filesInCurr.get(x))) {
                //contents of one are changed, other is deleted
                filecurr = new File(".gitlet/" + currfiles.getSHA(), x);
                filegiven = new File(".gitlet/" + givenfiles.getSHA(), x);
                try {
                    filecurr.createNewFile();
                } catch (IOException i) {
                    System.out.println("IOException");
                }
                try {
                    filegiven.createNewFile();
                } catch (IOException o) {
                    System.out.println("IOException");
                }
                if  (filecurr.exists() && filegiven.exists()) {
                    toPrintCurr.add(filecurr);
                    toPrintGiven.add(filegiven);
                    encounteredConflict = true;
                }
            }
        }
        //file absent in split point, different contents in given and current
        for (String x : filesInCurr.keySet()) {
            if (filesInCurr.get(x) != null && filesInGiven.get(x) != null) {
                filecurr = new File(".gitlet/" + currfiles.getSHA(), x);
                filegiven = new File(".gitlet/" + givenfiles.getSHA(), x);
                if (filecurr.exists() && filegiven.exists()
                        && splitPoint.getContents().get(x) == null
                        && (!filesInCurr.get(x).equals(filesInGiven.get(x)))) {
                    toPrintCurr.add(filecurr);
                    toPrintGiven.add(filegiven);
                    encounteredConflict = true;
                }
            }
        }
        if (encounteredConflict) {
            for (int x = 0; x < toPrintCurr.size(); x++) {
                String curr = "";
                String given = "";
                try {
                    curr = new String(Utils.readContents(toPrintCurr.get(x)), "UTF-8");
                    given = new String(Utils.readContents(toPrintGiven.get(x)), "UTF-8");
                } catch (IOException e) {
                    System.out.println("IOException");
                }
                String combined = "<<<<<<< HEAD\n" + curr + "=======\n" + given + ">>>>>>>\n";
                byte[] bytes = combined.getBytes();
                File f = new File(directoryPath, toPrintCurr.get(x).getName());
                Utils.writeContents(f, bytes);
            }
        }
        return encounteredConflict;
    }

    public static void splitPointFailCases(Commit splitPoint, Branch givenBranch) {
        if (splitPoint == null) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        } else if (splitPoint.equals(givenBranch.getBranch()
                .get(givenBranch.getBranch().size() - 1))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPoint.equals(currBranch.getBranch()
                .get(currBranch.getBranch().size() - 1))) {
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

>>>>>>> 256bae170ec7a5ab3e9a7229815f8371005642c2
    public static void merge(String branchname) {
        boolean encounteredConflict = false;
        sa = Serialized.loadStagingArea();
        removedFiles = Serialized.loadremovedFiles();
        listOfBranches = Serialized.loadlistOfBranches();
        currBranch = Serialized.loadCurrBranch();
        Branch givenBranch = null;
        Commit splitPoint = null;
        for (Branch b : listOfBranches) {
            if (branchname.equals(b.getName())) {
                givenBranch = b;
            }
        }
        mergeFailCases(givenBranch);
        for (Commit x : givenBranch.getBranch()) {
            if (currBranch.getBranch().contains(x)) {
                splitPoint = x;
            }
        }
<<<<<<< HEAD
        if (splitPoint == null) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (splitPoint.equals(givenBranch.getBranch().get(givenBranch.getBranch().size() - 1))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPoint.equals(currBranch.getBranch()
                .get(currBranch.getBranch().size() - 1))) {
            currBranch = givenBranch;
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } else {
            Commit currfiles = currBranch.getBranch().get(currBranch.getBranch().size() - 1);
            Commit givenfiles = givenBranch.getBranch().get(givenBranch.getBranch().size() - 1);
            //head commit of given branch
            HashMap<String, String> filesInGiven = givenfiles.getContents();
            //get files that are in files but not split point
            HashMap<String, String> filesInCurr = currfiles.getContents();
            for (String k : filesInGiven.keySet()) {
                try {
                    //modified in given branch
                    if (!filesInGiven.get(k).equals(splitPoint.getContents().get(k))) {
                        //unmodified in current branch
                        if (filesInCurr.get(k).equals(splitPoint.getContents().get(k))) {
                            checkoutCommitID(givenfiles.getSHA(), k);
                            File f = new File(directoryPath, k);
                            sa.add(f);
                        }
                    }
                } catch (NullPointerException e) {
                    //not present at split point
                    if (filesInCurr.get(k) == null) {
=======
        splitPointFailCases(splitPoint, givenBranch);
        Commit currfiles = currBranch.getBranch().get(currBranch.getBranch().size() - 1);
        Commit givenfiles = givenBranch.getBranch().get(givenBranch.getBranch().size() - 1);
        HashMap<String, String> filesInGiven = givenfiles.getContents();
        HashMap<String, String> filesInCurr = currfiles.getContents();
        List<String> wdFiles = Utils.plainFilenamesIn(directoryPath);
        for (String s : wdFiles) {
            File file = new File(".gitlet/" + currfiles.getSHA(), s);
            File f = new File(".gitlet/" + givenfiles.getSHA(), s);
            File fwd = new File(directoryPath, s);
            if (!file.exists() && f.exists() && !Utils.sha1(Utils.readContents(f))
                    .equals(Utils.sha1(Utils.readContents(fwd)))) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                System.exit(0);
            }
        }
        for (String k : filesInGiven.keySet()) {
            try {
                if (!filesInGiven.get(k).equals(splitPoint.getContents().get(k))) {
                    if (filesInCurr.get(k).equals(splitPoint.getContents().get(k))) {
>>>>>>> 256bae170ec7a5ab3e9a7229815f8371005642c2
                        checkoutCommitID(givenfiles.getSHA(), k);
                        File f = new File(directoryPath, k);
                        sa.add(f);
                    }
                }
<<<<<<< HEAD
            }
            //any files present at split point, unmodified in currbranch,
            //and absent in given should be removed
            for (String k : splitPoint.getContents().keySet()) {
                //loaded in merge
                Serialized.saveCurrBranch(currBranch);
                Serialized.saveremovedFiles(removedFiles);
                Serialized.savelistofBranches(listOfBranches);
                Serialized.saveStagingArea(sa);
                try {
                    //unmodified in curr branch
                    if (filesInCurr.get(k).equals(splitPoint.getContents().get(k))) {
                        //absent in given
                        if (filesInGiven.get(k) == null) {
                            rm(k);
                            sa = Serialized.loadStagingArea();
                            listOfBranches = Serialized.loadlistOfBranches();
                            removedFiles = Serialized.loadremovedFiles();
                            currBranch = Serialized.loadCurrBranch();
                        }
                    }
                } catch (NullPointerException e) {
                    File f = new File(directoryPath, k);
=======
            } catch (NullPointerException e) {
                if (filesInCurr.get(k) == null) {
                    checkoutCommitID(givenfiles.getSHA(), k);
                    File f = new File(directoryPath, k);
                    sa.add(f);
                }
            }
        }
        for (String k : splitPoint.getContents().keySet()) {
            Serialized.saveCurrBranch(currBranch);
            Serialized.saveremovedFiles(removedFiles);
            Serialized.savelistofBranches(listOfBranches);
            Serialized.saveStagingArea(sa);
            try {
                if (filesInCurr.get(k).equals(splitPoint.getContents().get(k))) {
                    if (filesInGiven.get(k) == null) {
                        rm(k);
                        sa = Serialized.loadStagingArea();
                        listOfBranches = Serialized.loadlistOfBranches();
                        removedFiles = Serialized.loadremovedFiles();
                        currBranch = Serialized.loadCurrBranch();
                    }
>>>>>>> 256bae170ec7a5ab3e9a7229815f8371005642c2
                }
            } catch (NullPointerException e) {
                File f = new File(directoryPath, k);
            }
            //Any files in conflict should be replaced but the result should not be staged
            encounteredConflict = mergeConflict(encounteredConflict, splitPoint, filesInGiven, filesInCurr);
        }
        encounteredConflict = mergeConflict(splitPoint, currfiles, givenfiles);
        Serialized.savelistofBranches(listOfBranches);
        Serialized.saverepository(repository);
        Serialized.saveStagingArea(sa);
        end(encounteredConflict, givenBranch);
    }

<<<<<<< HEAD
    public static void printConflict(byte[] currBranchContent) {
        for (int x = 0; x < currBranchContent.length; x++) {
            System.out.println(currBranchContent[x]);
        }

    }


=======
>>>>>>> 256bae170ec7a5ab3e9a7229815f8371005642c2
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (!args[0].equals("init") && !gitletDir.exists()) {
            System.out.println("Not in an initialized gitlet directory.");
            System.exit(0);
        }
        switch (args[0]) {
            case "init":
                incorrectOperands(args.length, 1);
                init();
                break;
            case "add":
                incorrectOperands(args.length, 2);
                add(args[1]);
                break;
            case "commit":
                incorrectOperands(args.length, 2);
                commit(args[1]);
                break;
            case "rm":
                incorrectOperands(args.length, 2);
                rm(args[1]);
                break;
            case "log":
                incorrectOperands(args.length, 1);
                log();
                break;
            case "global-log":
                incorrectOperands(args.length, 1);
                globalLog();
                break;
            case "find":
                incorrectOperands(args.length, 2);
                find(args[1]);
                break;
            case "status":
                incorrectOperands(args.length, 1);
                status();
                break;
            case "checkout":
                if (args.length < 2 || args.length > 4) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args.length == 2) {
                    checkoutBranch(args[1]); //need to finish checkout(branch name)
                } else if (args.length == 3 && args[1].equals("--")) {
                    checkoutFileName(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    checkoutCommitID(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                break;
            case "branch":
                incorrectOperands(args.length, 2);
                branch(args[1]); //need to finish branch(branch name)
                break;
            case "rm-branch":
                incorrectOperands(args.length, 2);
                rmBranch(args[1]); //need to finish rm-branch(branch name)
                break;
            case "reset":
                incorrectOperands(args.length, 2);
                reset(args[1]); //need to finish reset(commit id)
                break;
            case "merge":
                incorrectOperands(args.length, 2);
                merge(args[1]); //need to finish merge
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
                break;
        }
    }

    private static void incorrectOperands(int args, int length) {
        if (args != length) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

}