package gitlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;


public class Serialized {

    public static File loadFile(String sha) {
        File myFile = null;
        File listOfBranchesFile = new File(sha);
        if (listOfBranchesFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(listOfBranchesFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                myFile = (File) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading File.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading File.";
                System.out.println(msg);
            }
        }
        return myFile;
    }

    public static void savemarked(ArrayList<File> marked) {
        if (marked == null) {
            return;
        }
        try {
            File markedFile = new File(".gitlet/marked.ser");
            FileOutputStream fileOut = new FileOutputStream(markedFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(marked);
        } catch (IOException e) {
            String msg = "IOEception while saving marked.";
            System.out.println(msg);
        }
    }

    public static ArrayList<File> loadmarked() {
        ArrayList<File> marked = null;
        File markedFile = new File(".gitlet/marked.ser");
        if (markedFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(markedFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                marked = (ArrayList<File>) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading marked.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading marked.";
                System.out.println(msg);
            }
        }
        return marked;
    }

    public static void saveCurrBranch(Branch b) {
        if (b == null) {
            return;
        }
        try {
            File branchFile = new File(".gitlet/currBranch.ser");
            FileOutputStream fileOut = new FileOutputStream(branchFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(b);
        } catch (IOException e) {
            String msg = "IOEception while saving currBranch.";
            System.out.println(msg);
        }
    }

    public static Branch loadCurrBranch() {
        Branch b = null;
        File branchFile = new File(".gitlet/currBranch.ser");
        if (branchFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(branchFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                b = (Branch) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading currBranch.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading currBranch.";
                System.out.println(msg);
            }
        }
        return b;
    }

    public static void saverepository(ArrayList<Commit> repository) {
        if (repository == null) {
            return;
        }
        try {
            File repositoryFile = new File(".gitlet/repository.ser");
            FileOutputStream fileOut = new FileOutputStream(repositoryFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(repository);
        } catch (IOException e) {
            String msg = "IOEception while saving repository.";
            System.out.println(msg);
        }
    }

    public static ArrayList<Commit> loadrepository() {
        ArrayList<Commit> repository = null;
        File repositoryFile = new File(".gitlet/repository.ser");
        if (repositoryFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(repositoryFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                repository = (ArrayList<Commit>) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading repository.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading repository.";
                System.out.println(msg);
            }
        }
        return repository;
    }

    public static void savelistofBranches(ArrayList<Branch> listOfBranches) {
        if (listOfBranches == null) {
            return;
        }
        try {
            File mylistofBranchesFile = new File(".gitlet/listOfBranches.ser");
            FileOutputStream fileOut = new FileOutputStream(mylistofBranchesFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(listOfBranches);
        } catch (IOException e) {
            String msg = "IOEception while saving listOfBranches.";
            System.out.println(msg);
        }
    }

    public static ArrayList<Branch> loadlistOfBranches() {
        ArrayList<Branch> listOfBranches = null;
        File listOfBranchesFile = new File(".gitlet/listOfBranches.ser");
        if (listOfBranchesFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(listOfBranchesFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                listOfBranches = (ArrayList<Branch>) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading listOfBranches.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading listOfBranches.";
                System.out.println(msg);
            }
        }
        return listOfBranches;
    }

    public static void saveremovedFiles(ArrayList<String> removedFiles) {
        if (removedFiles == null) {
            return;
        }
        try {
            File myremovedFilesFile = new File(".gitlet/removedFiles.ser");
            FileOutputStream fileOut = new FileOutputStream(myremovedFilesFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(removedFiles);
        } catch (IOException e) {
            String msg = "IOEception while saving removedFiles.";
            System.out.println(msg);
        }
    }

    public static ArrayList<String> loadremovedFiles() {
        ArrayList<String> removedFiles = null;
        File removedFilesFile = new File(".gitlet/removedFiles.ser");
        if (removedFilesFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(removedFilesFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                removedFiles = (ArrayList<String>) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading removedFiles.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading removedFiles.";
                System.out.println(msg);
            }
        }
        return removedFiles;
    }

    public static void saveStagingArea(ArrayList<File> stagingArea) {
        if (stagingArea == null) {
            return;
        }
        try {
            File stagingAreaFile = new File(".gitlet/stagingArea.ser");
            FileOutputStream fileOut = new FileOutputStream(stagingAreaFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(stagingArea);
        } catch (IOException e) {
            String msg = "IOEception while saving stagingArea.";
            System.out.println(msg);
        }
    }

    public static ArrayList<File> loadStagingArea() {
        ArrayList<File> stagingArea = null;
        File stagingAreaFile = new File(".gitlet/stagingArea.ser");
        if (stagingAreaFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(stagingAreaFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                stagingArea = (ArrayList<File>) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading Staging Area.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading Staging Area.";
                System.out.println(msg);
            }
        }
        return stagingArea;
    }
}
