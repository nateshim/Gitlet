package gitlet;

import java.io.Serializable;
import java.util.ArrayList;

public class Branch implements Serializable, Comparable {

    //Fields
    private ArrayList<Commit> branch;
    private String name;
    private int head;

    //Constructor
    public Branch(ArrayList<Commit> thisBranch, String thisBranchName) {
        branch = thisBranch;
        name = thisBranchName;
        head = 0;
    }

    //Methods
    public String getName() {
        return name;
    }

    public ArrayList<Commit> getBranch() {
        return branch;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int val) {
        head = val;
    }

    @Override
    public int compareTo(Object b) {
        Branch br = (Branch) b;
        return this.getName().compareTo(br.getName());
    }

    public String toString() {
        return name;
    }
}
