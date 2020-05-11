/*
  store variable names with stored value
*/

import java.util.ArrayList;

public class MemTable {

  private ArrayList<String> names;
  private ArrayList<Double> values;
  private ArrayList<Value> vals;

  public MemTable() {
    names = new ArrayList<String>();
    vals = new ArrayList<Value>();
  }

  public String toString() {
    String s = "----\n";
    for( int k=0; k<names.size(); k++ ) {
      s += names.get(k) + " " + values.get(k) + "\n";
    }
    return s;
  }

  public int size() {
    return names.size();
  }

  // store value for name, adding name if not already
  // there
  public void store( String name, double value ) {

     int loc = findName( name );
   
     if ( loc < 0 ) {// add new pair
        names.add( name );
        values.add( value );
     }
     else {// change value for existing pair
        values.set( loc, value );
     } 

  }// store
  public void store( String name, Value value ) {

     int loc = findName( name );
   
     if ( loc < 0 ) {// add new pair
        names.add( name );
        vals.add( value );
     }
     else {// change value for existing pair
        vals.set( loc, value );
     } 

  }// store

  // retrieve value for given name
  public Value retrieve( String name ) {

     int loc = findName( name );

     if ( loc >= 0 ) {// add new pair
        return vals.get( loc );
     }
     else {
        System.out.println("variable [" + name + "] not found");
        System.exit(1);
        return new Value(0);
     }
  
  }// retrieve

  // return index of name in names, or -1 if
  // not found
  private int findName( String name ) {
     // locate name
     int loc = -1;
     for (int k=0; k<names.size() && loc<0; k++) {
        if ( names.get(k).equals(name) ) {
           loc = k;
        }
     }

     return loc;
  }// findName

}
