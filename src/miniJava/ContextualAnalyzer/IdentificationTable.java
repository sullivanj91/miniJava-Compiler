/*
 * @(#)IdentificationTable.java                2.0 1999/08/11
 *
 * Copyright (C) 1999 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package miniJava.ContextualAnalyzer;

import miniJava.AbstractSyntaxTrees.*;

public final class IdentificationTable {

  private int level;
  private IdEntry latest;

  public IdentificationTable () {
    level = 0;
    latest = null;
  }

  // Opens a new level in the identification table, 1 higher than the
  // current topmost level.

  public void openScope () {
    level ++;
  }

  // Closes the topmost level in the identification table, discarding
  // all entries belonging to that level.

  public void closeScope () {

    IdEntry entry, local;

    // Presumably, idTable.level > 0.
    entry = this.latest;
    while (entry.level > this.level || (entry.level == this.level && this.level > 2)) {
      local = entry;
      entry = local.previous;
    }
    this.level--;
    this.latest = entry;
  }

  // Makes a new entry in the identification table for the given identifier
  // and attribute. The new entry belongs to the current level.
  // duplicated is set to to true iff there is already an entry for the
  // same identifier at the current level.

  public void enter (String id, Declaration attr) {

    IdEntry entry = this.latest;
    boolean present = false, searching = true;

    // Check for duplicate entry ...
    while (searching) {
    	if (this.level > 2 && entry.level > 1 && entry.id.equals(id)){
    			present = true;
        		searching = false;
        		attr = entry.attr; 
    	}
    	else if (entry == null || entry.level < this.level)
    		searching = false;
    	else if (entry.id.equals(id)) {
    		if (id.equals("System") || id.equals("_PrintStream")){
    			searching = false;
    		}else{
    			present = true;
        		searching = false;
        		attr = entry.attr;
    		}    		
      } 
    	else
        entry = entry.previous;
    }

    attr.duplicated = present;
    // Add new entry ...
    entry = new IdEntry(id, attr, this.level, this.latest);
    this.latest = entry;
  }

  // Finds an entry for the given identifier in the identification table,
  // if any. If there are several entries for that identifier, finds the
  // entry at the highest level, in accordance with the scope rules.
  // Returns null iff no entry is found.
  // otherwise returns the attribute field of the entry found.

  public Declaration retrieve (String id) {

    IdEntry entry;
    Declaration attr = null;
    boolean present = false, searching = true;

    entry = this.latest;
    while (searching) {
      if (entry == null)
        searching = false;
      else if (entry.id.equals(id)) {
        present = true;
        searching = false;
        attr = entry.attr;
      } else
        entry = entry.previous;
    }

    return attr;
  }
  public Declaration retrieveClass (String id) {

	    IdEntry entry;
	    Declaration attr = null;
	    boolean present = false, searching = true;

	    entry = this.latest;
	    while (searching) {
	      if (entry == null)
	        searching = false;
	      else if (entry.id.equals(id) && entry.level == 0) {
	        present = true;
	        searching = false;
	        attr = entry.attr;
	      } else
	        entry = entry.previous;
	    }

	    return attr;
	  }

}
