/*  a Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node
*/

import java.util.*;
import java.io.*;
import java.awt.*;

public class Node {

  public static int count = 0;  // maintain unique id for each node
  public static ArrayList<Node> prevDefs = new ArrayList<Node>();
  public static Node func =  null;
  public static Node call =  null;

  private int id;

  private String kind;  // non-terminal or terminal category for the node
  private String info;  // extra information about the node such as
                        // the actual identifier for an I

  // references to children in the parse tree
  private Node first, second, third; 

  // stack of memories for all pending calls
  private static ArrayList<MemTable> memStack = new ArrayList<MemTable>();
  // convenience reference to top MemTable on stack
  private static MemTable table = new MemTable();

  // status flag that causes <stmts> nodes to abort asking second
  // to execute
  private static boolean returning = false;

  // value being returned
  private static double returnValue = 0;

  private static Node root;  // root of the entire parse tree

  private static Scanner keys = new Scanner( System.in );

  // construct a common node with no info specified
  public Node( String k, Node one, Node two, Node three ) {
    kind = k;  info = "";  
    first = one;  second = two;  third = three;
    id = count;
    count++;
  //  System.out.println( this );
  }

  // construct a node with specified info
  public Node( String k, String inf, Node one, Node two, Node three ) {
    kind = k;  info = inf;  
    first = one;  second = two;  third = three;
    id = count;
    count++;
   // System.out.println( this );
    if (kind.equals("def")) {
       prevDefs.add(this);
    }
    
  }

  // construct a node that is essentially a token
  public Node( Token token ) {
    kind = token.getKind();  info = token.getDetails();  
    first = null;  second = null;  third = null;
    id = count;
    count++;
  //  System.out.println( this );
  }

  public String toString() {
    return "#" + id + "[" + kind + "," + info + "]<" + nice(first) + 
              " " + nice(second) + ">";
  }

  public String nice( Node node ) {
     if ( node == null ) {
        return "-";
     }
     else {
        return "" + node.id;
     }
  }

  // produce array with the non-null children
  // in order
  private Node[] getChildren() {
    int count = 0;
    if( first != null ) count++;
    if( second != null ) count++;
    if( third != null ) count++;
    Node[] children = new Node[count];
    int k=0;
    if( first != null ) {  children[k] = first; k++; }
    if( second != null ) {  children[k] = second; k++; }
    if( third != null ) {  children[k] = third; k++; }

     return children;
  }

  //******************************************************
  // graphical display of this node and its subtree
  // in given camera, with specified location (x,y) of this
  // node, and specified distances horizontally and vertically
  // to children
  public void draw( Camera cam, double x, double y, double h, double v ) {

System.out.println("draw node " + id );

    // set drawing color
    cam.setColor( Color.black );

    String text = kind;
    if( ! info.equals("") ) text += "(" + info + ")";
    cam.drawHorizCenteredText( text, x, y );

    // positioning of children depends on how many
    // in a nice, uniform manner
    Node[] children = getChildren();
    int number = children.length;
System.out.println("has " + number + " children");

    double top = y - 0.75*v;

    if( number == 0 ) {
      return;
    }
    else if( number == 1 ) {
      children[0].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
    }
    else if( number == 2 ) {
      children[0].draw( cam, x-h/2, y-v, h/2, v );     cam.drawLine( x, y, x-h/2, top );
      children[1].draw( cam, x+h/2, y-v, h/2, v );     cam.drawLine( x, y, x+h/2, top );
    }
    else if( number == 3 ) {
      children[0].draw( cam, x-h, y-v, h/2, v );     cam.drawLine( x, y, x-h, top );
      children[1].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
      children[2].draw( cam, x+h, y-v, h/2, v );     cam.drawLine( x, y, x+h, top );
    }
    else {
      System.out.println("no Node kind has more than 3 children???");
      System.exit(1);
    }

  }// draw

  public static void error( String message ) {
    System.out.println( message );
    System.exit(1);
  }

   // ===============================================================
   //   execute/evaluate nodes
   // ===============================================================

  // ask this node to execute itself
  // (for nodes that don't return a value)

    
   // compute and return value produced by this node
   public Value evaluate() {

      if (kind.equals("name")) {
         debug("Evaluating name. [Variable look up: " + info + "  --->  " + table.retrieve( info ) + "] -- End Name evaluation");
         return new Value(table.retrieve( info )); 
      }
      else if (kind.equals("number")) {
         Value a = new Value(Double.parseDouble( info ));
         debug("Evaluating number. [Number used: " + a + "] -- End Number evaluation" );
         return a;
      }
      else if (kind.equals("list")) {
        // debug("Evaluating list. [ ");
         if (first.first.kind.equals("name")) {
            String funcName;
            Node firstArg = null;
            Node secondArg = null;
            Node thirdArg = null;
            funcName = first.first.info;
            if (first.second != null) {
               firstArg = first.second;
               if (first.second.second != null) {
                  secondArg = first.second.second;
                  if (first.second.second.second != null) {
                     thirdArg = first.second.second.second;
                  }
               }
            }

            if (funcName.equals("if")) {
               Value temp;
               double b = firstArg.first.evaluate().getNumber();
                  debug("If question (!= 0): " + b);
               if ( b != 0) {
                  temp = secondArg.first.evaluate();
                  debug("If evaluated, returning: " + temp);
                  return temp;
               }
               else {
                  temp = thirdArg.first.evaluate();
                  debug("Else evaluated, returning: " + temp);
                  return temp;
               }    
            }
            else if (funcName.equals("plus")) {
               debug("Evaluating plus.");
               double x = firstArg.first.evaluate().getNumber();
               debug("First number to add: " + x);
               double y = secondArg.first.evaluate().getNumber();
               debug("Second number to add: " + y);
               Value temp = new Value(x+y);
               debug("Sum of " + x + " and " + y + ": " + temp);
               return temp;
            }
            else if (funcName.equals("minus") ){
               debug("Evaluating minus.");
               double x = firstArg.first.evaluate().getNumber();
               debug("First number to subtract: " + x);
               double y = secondArg.first.evaluate().getNumber();
               debug("second number to subtract: " + y);
               Value temp = new Value(x-y);
               debug("Difference of " + x + " and " + y +  ": " + temp);
               return temp;
            }
            else if (funcName.equals("times") ){
               debug("Evaluating times.");
               double x = firstArg.first.evaluate().getNumber();
               debug("First number to multiply: " + x);
               double y = secondArg.first.evaluate().getNumber();
               debug("second number to multiply: " + x);
               Value temp = new Value(x*y);
               debug("Product of " + x + " and " + y  + ": " + temp);
               return temp;
            }
            else if (funcName.equals("div") ){
               debug("Evaluating div.");
               double x = firstArg.first.evaluate().getNumber();
                debug("First number to divide: " + x);
               double y = secondArg.first.evaluate().getNumber();
                debug("Second number to divide: " + y);
               Value temp = new Value(x/y);
               debug("Quotient  of " + x + " and " + y  + ": " + temp);
               return temp;
            }
            else if (funcName.equals("lt") ){
               debug("Evaluating lt.");
               double x = firstArg.first.evaluate().getNumber();
               debug("First number to compare (<): " + x);
               double y = secondArg.first.evaluate().getNumber();
               debug("Second number to compare (<): " + y);
               if (x < y) {
                  debug(x + " is less than " + y + ", returning 1.");
                  return new Value(1.0);
               }
               else {
                 debug(x + " is NOT less than " + y + ", returning 0.");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("le") ){
               debug("Evaluating le.");
               double x = firstArg.first.evaluate().getNumber();
               debug("First number to compare (<=): " + x);
               double y = secondArg.first.evaluate().getNumber();
               debug("Second number to compare (<=): " + y);
               if (x <= y) {
                  debug(x + " is less than or equal to " + y + ", returning 1.");
                  return new Value(1.0);
               }
               else {
                 debug(x + " is NOT less than or equal to " + y + ", returning 0.");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("eq") ){
               debug("Evaluating eq.");
               double x = firstArg.first.evaluate().getNumber();
               debug("First number to compare (==): " + x);
               double y = secondArg.first.evaluate().getNumber();
               debug("Second number to compare (==): " + y);
               if (x == y) {
                  debug(x + " is equal to " + y + ", returning 1.");
                  return new Value(1.0);
               }
               else {
                   debug(x + " is NOT equal to " + y + ", returning 0.");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("ne") ){
               debug("Evaluating ne.");
               double x = firstArg.first.evaluate().getNumber();
               debug("First number to compare (!=): " + x);
               double y = secondArg.first.evaluate().getNumber();
               debug("Second number to compare (!=): " + y);
               if (x != y) {
                  debug(x + " is NOT equal to " + y + ", returning 1.");
                  return new Value(1.0);
               }
               else {
                  debug(x + " is equal to " + y + ", returning 0.");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("and") ){
               debug("Evaluating and.");
               double x = firstArg.first.evaluate().getNumber();
               debug("First number to compare (&&): " + x);
               double y = secondArg.first.evaluate().getNumber();
               debug("Second number to compare (&&): " + y);
               if (x != 0 && y != 0) {
                  debug(x + " and " + y + " are both NON ZERO, returning 1.");
                  return new Value(1.0);
               }
               else {
                  debug("Either x: " + x + ", y: " + y + ", or both is/are Zero, returning 0.");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("or") ){
               debug("Evaluating or.");
               double x = firstArg.first.evaluate().getNumber();

               double y = secondArg.first.evaluate().getNumber();
               debug("Second number to compare (||): " + y);
               if (x != 0 || y != 0) {
                  debug("Either x: " + x + ", y: " + y + ", or both is/are NON ZERO, returning 1.");
                  return new Value(1.0);
               }
               else {
                  debug(x + " and " + y + " are both Zero, returning 0.");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("not") ){
               debug("Evaluating not.");
               double x = firstArg.first.evaluate().getNumber();
               debug("Number to not: " + x);
               if (x == 0) {
                  debug("Noting 0, returning 1.");
                  return new Value(1.0);
               }
               else {
                  debug("Noting 1, returning 0");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("ins") ){
               debug("Evaluating ins.");
               Value x = firstArg.first.evaluate();
               debug("Number to insert: " + x);
               Value y = secondArg.first.evaluate();
               debug("List to insert " + x + " to: " + y);
               y  = y.insert(x);
               debug("List after inserting " + x + ": " + y);
               return y;
  
            }
            else if (funcName.equals("first") ){
               debug("Evaluating first.");
               Value y = firstArg.first.evaluate();
               debug("List: " + y);
               Value temp = y.first();
               debug("First of the list (" + y + "): " + temp);
               return temp;
            }
            else if (funcName.equals("rest") ){
               debug("Evaluating rest.");
               Value y = firstArg.first.evaluate();
               debug("List: " + y);
               Value temp = y.rest();
               debug("Rest of the list (" + y + "): " + temp);
               return temp;
            }
            else if (funcName.equals("null") ){
               debug("Evaluating null.");
               Value x = firstArg.first.evaluate();
               debug("Value to determine if Null: " + x);
               if (x.isEmpty()) {
                  debug(x + " is empty, returning 1.");
                  return new Value(1.0);
               }
               else {
                  debug(x + " is NOT empty, returning 0.");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("num") ){
               debug("Evaluating num.");
               Value x = firstArg.first.evaluate();
               debug("Value to determine if Number: " + x);
               if (x.isNumber()) {
                   debug(x + " is a number, returning 1.");
                  return new Value(1.0);
               }
               else {
                  debug(x + " is NOT a number, returning 0.");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("list") ){
               debug("Evaluating list.");
               Value x = firstArg.first.evaluate();
               debug("Value to determine if List: " + x);
               if (!x.isNumber()) {
                   debug(x + " is a List, returning 1.");
                  return new Value(1.0);
               }
               else {
                  debug(x + " is NOT a list, returning 0.");
                  return new Value(0.0);
               }
            }
            else if (funcName.equals("read") ){
               debug("Evaluating read.");
               System.out.println("Enter number: ");
               String a = keys.nextLine();
               debug("Number entered : " + a);
               return new Value(a);
            }
            else if (funcName.equals("write") ){
               debug("Evaluating write.");
               Value x = firstArg.first.evaluate();
               System.out.print(x + " ");
               return new Value(-1.0);
            }
            else if (funcName.equals("nl") ){
               System.out.println();
               return new Value(-1.0);
            }
            else if (funcName.equals("quote")) {
              debug("Evaluating quote.");
              Node current = firstArg.first.first;
           //   debug("Current: " + current);
              ArrayList<String> temp = new ArrayList<String>();
              while (current != null) {
                temp.add(current.first.info);
                current = current.second;
              }
              debug("Total number of elements: " + temp.size());
              Value val = new Value();
              for (int i = 0; i < temp.size(); i++) {
              //  System.out.println("Adding: " + temp.get(i));
                val = val.insert(new Value(temp.get(i))); 
              }
              debug("Returning as Value: " + val);
              return val;
            }
            else if (funcName.equals("quit") ){
               System.out.print("Halting...");
               System.exit(0);
               return new Value(-1.0);
            }
            else { // Pre-defined
               boolean found = false;
               int ind = -1;
               for (int i = 0; i < prevDefs.size(); i++) {
                  if (funcName.equals(prevDefs.get(i).info)) {
                     found = true;
                     ind = i;
                  }
               }
               if (found == true) {
                  System.out.println("Executing function: " + prevDefs.get(ind).info);
                  func = prevDefs.get(ind);
                  debug("Func Name: " + func.info);
                  Node parOne = null;
                  Node parTwo = null;
                  if (func.first != null) {
                     debug("Loading params.");
                     parOne = func.first;
                     debug("parOne info: " + parOne.info);
                     Value x = firstArg.first.evaluate();
                     debug("Value to store with " + parOne.info + " : " + x);
                     table.store(parOne.info, x);
                     if (func.first.first != null) {
                        parTwo = func.first.first;
                        debug("ParTwo info: " + parTwo.info);
                        Value y = secondArg.first.evaluate();
                        debug("Value to store with " + parTwo.info + ": " + y);
                        table.store(parTwo.info, y);
                    }
                  } 
                  Value tempola = func.second.evaluate();
                  debug("Func [" + func.info + "] evaluated value: " + tempola); 
                  return tempola;
               }
               else {
                  System.out.println();
                  error("Error, " + funcName + " is not a recognized function." );
                  return new Value(-1.0);
               }
          
            }
         }
         else {
            System.out.println();
            error("Error, " + first.first.info + " is not a recognized function." );
            return new Value(-1.0);
         }
      }
      else {
         error("Evaluating unknown kind of node [" + kind + "]" );
         return new Value(-1.0);
      }

   }// evaluate

   private final static String[] bif0 = { "input", "nl" };
   private final static String[] bif1 = { "sqrt", "cos", "sin", "atan", 
                             "round", "trunc", "not" };
   private final static String[] bif2 = { "lt", "le", "eq", "ne", "pow",
                                          "or", "and"
                                        };

   // return whether target is a member of array
   private static boolean member( String target, String[] array ) {
      for (int k=0; k<array.length; k++) {
         if ( target.equals(array[k]) ) {
            return true;
         }
      }
      return false;
   }

   // given a funcCall node, and for convenience its name,
   // locate the function in the function defs and
   // create new memory table with arguments values assigned
   // to parameters
   // Also, return root node of body of the function being called
   
   public static void debug(String a) {
      System.out.println(a);
   }

}// Node
