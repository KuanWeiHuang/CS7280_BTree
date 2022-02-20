
# CS7280 Project 1 - Btree 

This project is to implement 3 functions of B tree:
1. Lookup
2. Insert
3. Display

## Lookup

Check if a certain value is in the B tree, a helper method nodeLookup is used to search the 
node recursively. In nodeLookup, the program will do 3 things:
1. check if the value is in the current node, if yes, return true.
2. check if the current node is leaf, if yes and the value is not found, return false.
3. recursively call the nodeLookup method on the child pointer which is left to the first key value 
greater than the target value

## Insert

Insert a value into Btree, a helper method nodeInsert is used to insert recursively. In 
nodeInsert, the base case is when the current node is leaf, otherwise we have to deal with the value 
returned from the lower level. -2 means the value is found in the lower level, -1 means the value is 
inserted in the lower level. If the returned value is greater than 0, that means a new node is created 
and we need to insert the middle value to current node. 

Base case (leaf):

    1. check if the value is already exist.
    2. if the node size is less than default, insert and sort the array then return -1.
    3. the node is already full, we need to insert sort the array, split the node to 
       two parts and return the index(pointer) of newly created node. (the middle value
       is stored in the MIDDLE of originl node.)
    
Other case (internal):

    1. check if the value is already exist.
    2. find the child node we should visit and recursively call nodeLookup on it.
    3. check the status returned, if it's -2 or -1, we can return directly.
    4. there is a new child node created, we need to insert the middle value to the 
       current node.
    5. if current node is also full, we need to split it and return the newly created 
       node index to upper level.
    6. a special case is when current node is root, we have to create a new root in 
       this level since there is no upper level we can return to.


## Display

Display the tree or subtree under a specific node. BFS method is used to display the 
tree level by level. Nodes are separated by curly braces {} and Levels are separated by a seperation 
line ---. Pointers and values are separated by | and the value's space is wider than pointer's. If it's 
leaf node, only value space will be shown.  

![Display Example](/images/Display_example.png)

## Test

Test case 1: { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 }, NODESIZE = 5

![Test case 1](/images/Test_case_1.png)

Test case 2: { 10, 20, 30, 40, 50, 15, 60, 85, 95, 100, 11, 12, 13, 22, 32, 33, 34, 1, 2, 3, 4, 5, 6 }, NODESIZE = 5

![Test case 2](/images/Test_case_2.png)

Test case 3: { 10, 20, 30, 40, 50, 15, 60, 85, 95, 100, 11, 12, 13, 22, 32, 33, 34, 1, 2, 3, 4, 5, 6 }, NODESIZE = 4

![Test case 3](/images/Test_case_3.png)