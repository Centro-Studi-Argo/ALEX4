/*   Copyright: Marie-Edith Bissey, Guido Ortona - 2007
*    Dipartimento di Politiche Pubbliche e Scelte Collettive
*    Universita' del Piemonte Orientale (Italia)
*    Contact: bissey@sp.unipmn.it

*    This file is part of ALEX4.1
*
*    ALEX4.1 is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    ALEX4.1 is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with ALEX4.1; if not, write to the Free Software
*    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package votingObjects;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Toolkit;
import java.text.*;
import java.io.*;

import classesWrittenByOthers.*;
import gui.*;
import actions.*;
import globals.*;

public class Coalition {
	Language language = Language.getInstance();
	private String nameOfCoalition;
	private String nameOfGroupOfCoalitions;
	private int indexOfGroupOfCoalitions;
	private LinkedList <Party> listOfParties = new LinkedList <Party> ();
	private HashMap <Integer,Party> mainParties = new HashMap <Integer,Party> ();

	// constructor
	public Coalition() {
		// default
	}
	
	public Coalition(String name) {
		this.nameOfCoalition = name;
	}

	public Coalition(String name,Party party) {
		this.nameOfCoalition = name;
		this.addParty(party);
	}

	public Coalition(String name,LinkedList <Party> parties) {
		this.nameOfCoalition = name;
		this.listOfParties = parties;
	}

	public void setNameOfCoalition(String name) {
		this.nameOfCoalition = name;
	}
	
	public void setNameOfGroupOfCoalitions(String name) {
		this.nameOfGroupOfCoalitions = name;
	}
	public void setIndexOfGroupOfCoalitions(int index) {
		this.indexOfGroupOfCoalitions = index;
	}
	
	public String getNameOfCoalition() {
		return nameOfCoalition;
	}

	public String getNameOfGroupOfCoalitions() {
		return nameOfGroupOfCoalitions;
	}

	public int getIndexOfGroupOfCoalitions() {
		return indexOfGroupOfCoalitions;
	}

	public void addParty(Party aParty) {
		listOfParties.add(aParty);
	}
	
	public LinkedList<Party> getListOfParties() {
		return listOfParties;
	}

	public ArrayList <Party> getArrayOfParties() {
		return new ArrayList <Party> (listOfParties);
	}

	public void addMainParty (int nameOfDistrict,Party party) {
		mainParties.put(new Integer(nameOfDistrict),party);
	}
	
	public HashMap<Integer,Party> getMainParties() {
		return mainParties;
	}

	public Party getMainParty (int nameOfDistrict) {
//System.out.println("1");
		if (mainParties.containsKey(new Integer(nameOfDistrict))) {
//System.out.println("2");
			return ((Party)mainParties.get(new Integer(nameOfDistrict)));
		} else {
//System.out.println("3");
			return null;
		}
	}

	public boolean containsParty(Party aParty) {
		int aname = aParty.getName();
		Iterator p = listOfParties.iterator();
		while (p.hasNext()) {
			Party party = (Party)p.next();
			int name = party.getName();
			if (aname == name) {
				return true;
			}
		}
		return false;
	}

	public boolean containsParty(int aname) {
		Iterator p = listOfParties.iterator();
		while (p.hasNext()) {
			Party party = (Party)p.next();
			int name = party.getName();
			if (aname == name) {
				return true;
			}
		}
		return false;
	}
	public void removeParties() {
		listOfParties = new LinkedList <Party> ();
		mainParties = new HashMap <Integer,Party> ();
	}

	public String toString() {
		String out="";
		out += "coalition_"+nameOfCoalition+"_group_"+nameOfGroupOfCoalitions+"\n";
		return out;
	}

	public String toStringDisplay() {
		String out="";
		out += "<b><i>"+language.getString("labels","coalition")+"</i></b>: "+nameOfCoalition+"<br>";
		out += language.getString("labels","parties")+": ";// + name of each party separated by commas+"\n";
		Iterator p = listOfParties.iterator();
		while (p.hasNext()) {
			Party pa = (Party)p.next();
			out+=pa.getNameParty()+", ";
		}
		// remove last "," and add "\n";
		out = out.substring(0,(out.length()-2));
		out += "<br>";
		return out;
	}
	public String toStringFull() {
		String out="";
		out += "coalition_"+nameOfGroupOfCoalitions+"_nameOfGroupOfCoalition\n";
		out += "coalition_"+nameOfCoalition+"\n";
		out += "coalition_"+nameOfCoalition+"_listOfParties:";// + index of each party separated by commas+"\n";
		Iterator p = listOfParties.iterator();
		while (p.hasNext()) {
			Party pa = (Party)p.next();
			out+=pa.getName()+",";
		}
		// remove last "," and add "\n";
		out = out.substring(0,(out.length()-2));
		out += "\n";
		out += "coalition_"+nameOfCoalition+"_mainParties:";// + [index of district , index of main party] separated by commas+"\n";
		Set pkeys = mainParties.keySet();
		p = pkeys.iterator();
		while (p.hasNext()) {
			Integer pkey = (Integer)p.next();
			Party pa = (Party)mainParties.get(pkey);
			out+="["+pkey+","+pa.getName()+"],";
		}
		// remove last "," and add "\n";
		out = out.substring(0,(out.length()-2));
		out += "\n";
		return out;
	}
//	public String toString() {
//		return (party+"_"+positionInList);
//	}
}// end class definition