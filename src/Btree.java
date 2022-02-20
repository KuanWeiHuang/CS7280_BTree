/*
 * CS7280 Special Topics in Database Management
 * Project 1: B-tree implementation.
 *
 * You need to code for the following functions in this program
 *   1. Lookup(int value) -> nodeLookup(int value, int node)
 *   2. Insert(int value) -> nodeInsert(int value, int node)
 *   3. Display(int node)
 *
 */

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

final class Btree {

  /* Size of Node. */
  private static final int NODESIZE = 5;

  private static final int MIDDLE = NODESIZE / 2;

  /* Node array, initialized with length = 1. i.e. root node */
  private Node[] nodes = new Node[1];

  /* Number of currently used nodes. */
  private int cntNodes;

  /* Pointer to the root node. */
  private int root;

  /* Number of currently used values. */
  private int cntValues;

  /*
   * B tree Constructor.
   */
  public Btree() {
    root = initNode();
    nodes[root].children[0] = createLeaf();
  }

  /*********** B tree functions for Public ******************/

  /*
   * Lookup(int value)
   *   - True if the value was found.
   */
  public boolean Lookup(int value) {
    return nodeLookup(value, root);
  }

  /*
   * Insert(int value)
   *    - If -1 is returned, the value is inserted and increase cntValues.
   *    - If -2 is returned, the value already exists.
   */
  public void Insert(int value) {
    if(nodeInsert(value, root) == -1) cntValues++;
  }


  /*
   * CntValues()
   *    - Returns the number of used values.
   */
  public int CntValues() {
    return cntValues;
  }

  /*
   * Display(int node)
   *    - Display the values and children of the node
   */
  public void Display(int node) {
    Node currentNode;

    // using BFS to display the B tree level by level
    Queue<Integer> q = new LinkedList<>();
    q.add(node);

    while (q.size() > 0) {
      int levelSize = q.size();
      StringBuilder sb1 = new StringBuilder();
      StringBuilder sb2 = new StringBuilder();

      for (int i = 0; i < levelSize; i++) {
        int nodeIndex = q.remove();
        currentNode = nodes[nodeIndex];
        // If it's leaf node, children will not be printed
        if (isLeaf(currentNode)) {
          sb1.append("{        Node ").append(nodeIndex).append("       }");
          sb2.append("{|");
          for (int j = 0; j < currentNode.size; j++) {
            sb2.append(" ").append(currentNode.values[j]).append(" |");
          }
          int sizeLeft = NODESIZE - currentNode.size;
          sb2.append("   |".repeat(sizeLeft)).append("}");
        } else {
          sb1.append("{             Node ").append(nodeIndex).append("              }");
          sb2.append("{|");
          for (int j = 0; j < currentNode.size; j++) {
            sb2.append(currentNode.children[j]).append("| ").append(currentNode.values[j]).append(" |");
            q.add(currentNode.children[j]);
          }
          sb2.append(currentNode.children[currentNode.size]).append("|");
          q.add(currentNode.children[currentNode.size]);
          int sizeLeft = NODESIZE - currentNode.size;
          sb2.append("   | |".repeat(sizeLeft)).append("}");
        }
      }
      System.out.println(sb1.toString());
      System.out.println(sb2.toString());
      System.out.println("-".repeat(50)); // the separation line between levels
    }
  }

  private void DisplayNode(int node) {
    Node currentNode = nodes[node];
    if (isLeaf(currentNode)) {
      System.out.printf("{        Node %d       }\n{|", node);
      for (int i = 0; i < currentNode.size; i++) {
        System.out.printf(" %d |", currentNode.values[i]);
      }
      int sizeLeft = NODESIZE - currentNode.size;
      System.out.print("   |".repeat(sizeLeft) + "}\n");
    } else {
      System.out.printf("{            Node %d            }\n{|", node);
      for (int i = 0; i < currentNode.size; i++) {
        System.out.printf("%d| %d |", currentNode.children[i], currentNode.values[i]);
      }
      System.out.printf("%d|", currentNode.children[currentNode.size]);
      int sizeLeft = NODESIZE - currentNode.size;
      System.out.print("   | |".repeat(sizeLeft) + "}\n");
    }
  }
  /*********** B-tree functions for Internal  ******************/

  /*
   * nodeLookup(int value, int pointer)
   *    - True if the value was found in the specified node.
   *    - False otherwise
   */
  private boolean nodeLookup(int value, int pointer) {
    Node currentNode = nodes[pointer];

    // check if the value equals to existing values
    // if the element is already greater than the value,
    // we can stop earlier since the array is sorted
    for (int i = 0; i < currentNode.size; i++) {
      if (value == currentNode.values[i]) {
        return true;
      }
      if (value < currentNode.values[i]) {
        if (isLeaf(currentNode)) { // the node is leaf and we cannot go to next level to find the value
          return false;
        } else { // go to the child pointer left to the first key value which greater than the target value
          return nodeLookup(value, currentNode.children[i]);
        }
      }
    }

    if (isLeaf(currentNode)) {
      return false;
    }
    
    return nodeLookup(value, currentNode.children[currentNode.size]);
  }

  /*
   * nodeInsert(int value, int pointer)
   *    return:
   *    - -2 if the value already exists in the specified node
   *    - -1 if the value is inserted into the node and no more executions needed
   *    - new node index (> 0) if the value is inserted into the node and new node is created
   */
  private int nodeInsert(int value, int pointer) {
    Node currentNode = nodes[pointer];
    // Base case: the node is leaf node
    if (isLeaf(currentNode)) {
      // if the value already exists, don't insert it and return -2
      if (findValue(currentNode.values, value, currentNode.size)) {
        return  -2;
      } else if (currentNode.size < NODESIZE) { // if the leaf has spaces, insert the value and sort the array
        currentNode.values[currentNode.size++] = value;
        Arrays.sort(currentNode.values, 0, currentNode.size);
        return -1;
      } else { // if the leaf has no space, split the node and uplift the middle value to parent
        // insert the value to a new temp array and sort it
        int[] temp = new int[NODESIZE + 1];
        System.arraycopy(currentNode.values, 0, temp, 0, NODESIZE);
        temp[NODESIZE] = value;
        Arrays.sort(temp);

        currentNode.size = MIDDLE; // decrease the original node size to MIDDLE (NODESIZE / 2)
        // copy the sorted values into original value including the middle value which will be used after returning to upper level
        System.arraycopy(temp, 0, currentNode.values, 0, MIDDLE + 1);
        // create a new leaf and put the sorted value greater than middle value
        int newLeafIndex = createLeaf();
        Node newLeaf = nodes[newLeafIndex];
        int j = 0;
        for (int i = MIDDLE + 1; i <= NODESIZE; i++) {
          newLeaf.values[j++] = temp[i];
          newLeaf.size++;
        }

        return newLeafIndex;
      }
    }

    // if current node is not leaf, go to the next level to insert the value
    int childIndex = -1;
    for (int i = 0; i < currentNode.size; i++) {
      if (value == currentNode.values[i]) { // if the value is already in the node, return -2
        return -2;
      }
      if (value < currentNode.values[i]) {
        childIndex = i;
        break;
      }
    }
    // set the childIndex if it's not set during iteration
    if (childIndex == -1) {
        childIndex = currentNode.size;
    }

    int status = nodeInsert(value, currentNode.children[childIndex]);
    if (status < 0) {
      return status;
    } else {
      int childPointer = status;
      // get the middle value from the original child
      Node originalChild = nodes[currentNode.children[childIndex]];
      int middleValue = originalChild.values[MIDDLE];

      // if current node has space
      if (currentNode.size < NODESIZE) {
        // move the values greater than middle value to the right by 1 slot
        for (int j = currentNode.size; j > childIndex; j--) {
          currentNode.values[j] = currentNode.values[j - 1];
        }
        currentNode.values[childIndex] = middleValue;
        currentNode.size++;
        // move the children pointer next to middle value to the right by 1 slot
        for (int j = currentNode.size; j > childIndex + 1; j--) {
          currentNode.children[j] = currentNode.children[j - 1];
        }
        currentNode.children[childIndex + 1] = childPointer;

        return -1;
      } else { // current node has no space
        // create temp arrays to store one more element
        int[] tempValues = new int[NODESIZE + 1];
        int[] tempChildren = new int[NODESIZE + 2];
        int i = 0;
        while (i < childIndex) {
          tempValues[i] = currentNode.values[i];
          tempChildren[i] = currentNode.children[i];
          i++;
        }
        tempValues[i] = middleValue;
        tempChildren[i] = currentNode.children[i];
        tempChildren[i + 1] = childPointer;
        i++;
        while (i <= NODESIZE) {
          tempValues[i] = currentNode.values[i - 1];
          tempChildren[i + 1] = currentNode.children[i];
          i++;
        }

        // decrease current node's size to MIDDLE
        currentNode.size = MIDDLE;

        // create a new node with values greater than middle value and child pointers next to middle value
        int newNodeIndex = initNode();
        Node newNode = nodes[newNodeIndex];
        int j = 0;
        for (i = MIDDLE + 1; i <= NODESIZE; i++) {
          newNode.values[j] = tempValues[i];
          newNode.children[j] = tempChildren[i];
          j++;
          newNode.size++;
        }
        newNode.children[j] = tempChildren[i];

        if (pointer != root) {
          return newNodeIndex;
        } else { // special case: if the current node is root, we have to create a new root to store the middle value
          int newRootIndex = initNode();
          Node newRoot = nodes[newRootIndex];
          newRoot.values[0] = tempValues[MIDDLE];
          newRoot.children[0] = root;
          newRoot.children[1] = newNodeIndex;
          newRoot.size++;
          root = newRootIndex;
          return -1;
        }
      }
    }
  }

  /*
   *  findValue(int[] values, int target, int size)
   *    find target value in a integer array
   *    - return True if the value is found
   *    - else return False
   */
  private boolean findValue(int[] values, int target, int size) {
    for(int i = 0; i < size; i++) {
      if (values[i] == target) {
        return true;
      }
      if (values[i] > target) {
        return false;
      }
    }
    return false;
  }


  /*********** Functions for accessing node  ******************/

  /*
   * isLeaf(Node node)
   *    - True if the specified node is a leaf node.
   *         (Leaf node -> a missing children)
   */
  boolean isLeaf(Node node) {
    return node.children == null;
  }

  /*
   * initNode(): Initialize a new node and returns the pointer.
   *    - return node pointer
   */
  int initNode() {
    Node node = new Node();
    node.values = new int[NODESIZE];
    node.children =  new int[NODESIZE + 1];

    checkSize();
    nodes[cntNodes] = node;
    return cntNodes++;
  }

  /*
   * createLeaf(): Creates a new leaf node and returns the pointer.
   *    - return node pointer
   */
  int createLeaf() {
    Node node = new Node();
    node.values = new int[NODESIZE];

    checkSize();
    nodes[cntNodes] = node;
    return cntNodes++;
  }

  /*
   * checkSize(): Resizes the node array if necessary.
   */
  private void checkSize() {
    if(cntNodes == nodes.length) {
      Node[] tmp = new Node[cntNodes << 1];
      System.arraycopy(nodes, 0, tmp, 0, cntNodes);
      nodes = tmp;
    }
  }
}

/*
 * Node data structure.
 *   - This is the simplest structure for nodes used in B-tree
 *   - This will be used for both internal and leaf nodes.
 */
final class Node {
  /* Node Values (Leaf Values / Key Values for the children nodes).  */
  int[] values;

  /* Node Array, pointing to the children nodes.
   * This array is not initialized for leaf nodes.
   */
  int[] children;

  /* Number of entries
   * (Rule in B Trees:  d <= size <= 2 * d).
   */
  int size;
}
