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
package indexes;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.Toolkit;
import java.text.*;
import java.io.*;
import java.beans.*;

import classesWrittenByOthers.*;
import globals.*;
import parliaments.*;
import votingObjects.*;
import gui.*;

public class HollerIndex extends DeeganPackelIndex//// implements ParliamentEventListener
	{
	// global variables for the class
//	MainFrame mainFrame = main;
//	double majorityLevel;
//	HashMap<String,Integer>valueFunction = new HashMap<String,Integer>();
//	ArrayList <Party>restrictedParties = new ArrayList<Party>();
//	int nbParties = 0;
	HashMap <Integer,Integer> forHoller = new HashMap <Integer,Integer> ();
	HashMap <Integer,Double> hollerIndex = new HashMap <Integer,Double> ();
//	int sizeForIndex = 0;
//	LinkedList <Integer> orderedParties = new LinkedList <Integer> ();
//	LinkedList <String> minimumWinningCoalitions = new LinkedList<String>();

	// constructor


	public HollerIndex(Parliament parl, HashMap<Integer,Integer> map) {
		super(parl,map);
System.out.println("CONSTRUCTING Holler INDEX");
System.out.println("Minimum winning coalitions "+minimumWinningCoalitions.toString());
	}// end constructor
	
	protected Object computeIndex() {
		String result = new String();
		// loop on parties
		Iterator <Party> p = arrayOfParties.iterator();
		int sum0 = 0;
		while (p.hasNext()) {
			Party pa = p.next();
			int name = pa.getName();
			int sum = 0;
			// loop on minimumWinningCoalitions
			Iterator <String> m = minimumWinningCoalitions.iterator();
			while (m.hasNext()) {
				String wc = m.next();
				String[] els = wc.split(",");
				boolean isIn = false;
				for (int e=0;e<els.length;++e) {
					String el = els[e];
					int iel = (new Integer(el)).intValue();
					if (iel==name) {
						isIn=true;
						break;
					}
				}
				if (isIn==true) {
					sum += 1;
				}
			}
			sum0 += sum;
			forHoller.put(new Integer(name),new Integer(sum));
		}

		p = arrayOfParties.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			int name = pa.getName();
			int sum = (forHoller.get(new Integer(name))).intValue();
			double val = (double)sum/sum0;
			hollerIndex.put(new Integer(name),new Double(val));
			DecimalFormat df = new DecimalFormat("##.####");
			result += pa.getNameParty() +": "+df.format(val)+" <br> ";
		}
		indexValue=hollerIndex;
		setPowerMap(hollerIndex);
//		result+="-----------------------------";
System.out.println("hollerIndex: "+hollerIndex.toString());
		return(result);	
	}
	

}