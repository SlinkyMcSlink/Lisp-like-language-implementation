/*
    This class provides a recursive descent parser 
    for Corgi (the new version),
    creating a parse tree which can be interpreted
    to simulate execution of a Corgi program
*/

import java.util.*;
import java.io.*;

public class Parser {

   private Lexer lex;

   public Parser( Lexer lexer ) {
      lex = lexer;
   }

   public Node parseDefs() {
    //  System.out.println("-----> parsing <defs>:");
      Node first = parseDef();
      Token token = lex.getNextToken();
      if ( token.isKind("eof") ) {
         return new Node( "defs", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseDefs();
         return new Node("defs", first, second, null );
      }
   }

   public Node parseDef() {
    //  System.out.println("-----> parsing <def>:");
      Token token = lex.getNextToken();
      errorCheck(token, "Lparen");
      token = lex.getNextToken();
      errorCheck(token, "define");
      token = lex.getNextToken();
      errorCheck(token, "Lparen");
      Token name = lex.getNextToken();
      errorCheck(name, "name");
      token = lex.getNextToken();
      if (token.isKind("Rparen")) {
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck(token, "Rparen");
         return new Node("def", name.getDetails(), null, first, null );
      }
      else {
         lex.putBackToken( token );
         Node first = parseParams();
         token = lex.getNextToken();
         errorCheck(token, "Rparen");
         Node second = parseExpr();
         token = lex.getNextToken();
         errorCheck(token, "Rparen");
         return new Node("def", name.getDetails(), first, second, null );
      }
   }

   public Node parseParams() {
  //    System.out.println("-----> parsing <params>:");
      Token name = lex.getNextToken();
      errorCheck(name, "name");
      Token token = lex.getNextToken();
      if (token.isKind("Rparen")) {
         lex.putBackToken( token );
         return new Node("params", name.getDetails(), null, null, null );
      }
      else {
         lex.putBackToken( token );
         Node first = parseParams();
         return new Node("params", name.getDetails(), first, null, null );
      }
   }

   public Node parseExpr() {
   //   System.out.println("-----> parsing <expr>:");
      Token token = lex.getNextToken();
      if (token.isKind("name")) {
         return new Node("name", token.getDetails(), null, null, null );
      }
      else if (token.isKind("number")) {
         return new Node("number", token.getDetails(), null, null, null );
      }
      else  {
         lex.putBackToken( token );
         return parseList();
      }
   }

   public Node parseList() {
   //   System.out.println("-----> parsing <list>:");
      Token token = lex.getNextToken();
      errorCheck(token, "Lparen");
      token = lex.getNextToken();
      if (token.isKind("Rparen")) {
         return new Node("list", null, null, null );
      }
      else {
         lex.putBackToken( token );
         Node first = parseItems();
         token = lex.getNextToken();
         errorCheck(token, "Rparen");
         return new Node("list", first, null, null );
      }
   }

   public Node parseItems() {
  //    System.out.println("-----> parsing <items>:");
      Node first = parseExpr();
      Token token = lex.getNextToken();
      if (token.isKind("Rparen")) {
         lex.putBackToken( token );
         return new Node("items", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseItems();
         return new Node("items", first, second, null );
      }
 
   }


  // check whether token is correct kind
  private void errorCheck( Token token, String kind ) {
    if( ! token.isKind( kind ) ) {
      System.out.println("Error:  expected " + token + 
                         " to be of kind " + kind );
      System.exit(1);
    }
  }

  // check whether token is correct kind and details
  private void errorCheck( Token token, String kind, String details ) {
    if( ! token.isKind( kind ) || 
        ! token.getDetails().equals( details ) ) {
      System.out.println("Error:  expected " + token + 
                          " to be kind= " + kind + 
                          " and details= " + details );
      System.exit(1);
    }
  }

}
