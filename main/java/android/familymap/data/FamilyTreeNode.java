package android.familymap.data;

import java.util.LinkedList;

import model.PersonModel;

public class FamilyTreeNode {
    private PersonModel person;
    private FamilyTreeNode leftNode;
    private FamilyTreeNode rightNode;

    public FamilyTreeNode(PersonModel person) {
        this.person = person;
        this.leftNode = null;
        this.rightNode = null;
    }

    public PersonModel getPerson() {
        return person;
    }

    public FamilyTreeNode getLeftNode() {
        return leftNode;
    }

    public void setLeftNode(FamilyTreeNode leftNode) {
        this.leftNode = leftNode;
    }

    public FamilyTreeNode getRightNode() {
        return rightNode;
    }

    public void setRightNode(FamilyTreeNode rightNode) {
        this.rightNode = rightNode;
    }


}
