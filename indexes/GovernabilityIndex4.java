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

public class GovernabilityIndex4 extends Index
	{
	// global variables for the class
//	private double majorityLevel;
	protected String alphabet = "abcdefghijklmnopqrstuvwxyz";
	ArrayList <Party> restrictedParties = new ArrayList <Party> ();
	HashMap <Integer,Integer> currentAllocationOfSeats = new HashMap <Integer,Integer> ();

	// constructor
	public GovernabilityIndex4(){
		super();
	};
	public GovernabilityIndex4(Parliament parl) {
		super();
		parliament = parl;
		arrayOfParties = parliament.getArrayOfParties();
		Iterator <Party> p = arrayOfParties.iterator();
		currentAllocationOfSeats = parliament.getAllocationOfSeats();
		while (p.hasNext()) {
			Party pa = p.next();
			Integer name = new Integer(pa.getName());
			Integer seats = currentAllocationOfSeats.get(name);
//			if (seats.intValue()>0) {
				restrictedParties.add(pa);
//			}
			
		}
		indexValue = computeIndex();
		
//System.out.println("1 alphabet is "+alphabet);
//		majorityLevel = parl.getMajorityLevel();
		// register an observer for parliament
//		parl.addObserver(this);
	}

	/* How index is computed:
	 * 
	 * Fragnelli index, version 1 using owen power index, when parties outside the government are all in separate a priori unions
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	*/

	protected Object computeIndex() {
System.out.println("GOVERNABILITY INDEX IV");
		LinkedList <Integer> government = parliament.getPartiesInGovernment();
		boolean majorityFound = parliament.getMajorityFound();
		HashMap <Integer,Double> owenValuesPerParty = new HashMap<Integer,Double>();
		if (majorityFound == true) {
System.out.println("majority is found");
			// for each party in government, compute the owen index when it is not in government
			// i.e. loss of power when leaving the government
			Iterator<Integer> ig = government.iterator();
//System.out.println("created iterator on parties in government");
			while (ig.hasNext()) {
				Integer name = ig.next();
System.out.println("CONSIDERING PARTY "+name.toString());
				// create aPrioriUnions map for this case
//System.out.println("2 alphabet is "+alphabet);
				HashMap<Party,String>aPrioriUnions = getAPrioriUnions(government,name,restrictedParties);
System.out.println("A priori Union for party "+name.toString()+" is: "+aPrioriUnions.toString());
				// create owen index
				OwenIndex owen = new OwenIndex(parliament,currentAllocationOfSeats,aPrioriUnions);
				// compute index and get value for current party
				Object obj = owen.computeIndex();
				HashMap<Integer,Double> owenValues = owen.getMapOfValues();
System.out.println("OwenValues "+owenValues.toString());
				owenValuesPerParty.put(name,owenValues.get(name));
			}
			// compute the value of the index as 1 minus sum of owenValuesPerParty divided by size of government
			Set keys = owenValuesPerParty.keySet();
			Iterator<Integer> ik = keys.iterator();
			double sum = 0.0;
			while (ik.hasNext()) {
				Integer pname = ik.next();
				Double oval = owenValuesPerParty.get(pname);
				sum+=oval.doubleValue();
System.out.println("party "+pname+" has owen value "+oval+" so sum becomes "+sum);
			}
			
			Double indexValue = new Double(1-sum/arrayOfParties.size());
System.out.println("\tvalue of index: "+indexValue);
			parliament.setIndexValue(government,indexValue,"GovernabilityIndex4");
			DecimalFormat df = new DecimalFormat("##.####");
			return df.format(indexValue);
		} else {
			return new String(language.getString("labels","defaultGovernabilityLabel"));
		}
	}

	HashMap<Party,String> getAPrioriUnions(LinkedList<Integer>gvt,Integer pname,ArrayList<Party>ap) {
//System.out.println("creating unions for governement: "+gvt.toString()+" and ref party "+pname.toString());
		HashMap <Party,String>unions = new HashMap<Party,String>();
		Iterator<Party>ip = ap.iterator();
//System.out.println("created iterator on ap");
//System.out.println("3 alphabet is "+alphabet);
		String charGvt = String.valueOf(alphabet.charAt(0));
//System.out.println("got charGvt = "+charGvt);
		int n = 1;
		while (ip.hasNext()) {
			// if party in government and not pname, set letter charGvt
			// else set letter at nth position in alphabet, and increase n
			Party pa = ip.next();
			Integer name = new Integer(pa.getName());
//System.out.println("party "+name.toString());
			if ((name.intValue()!=pname.intValue()) && gvt.contains(name)) {
//System.out.println("is in the government, but not the party under consideration");
				unions.put(pa,charGvt);
			} else {
//System.out.println("is not in the government, or is the party under consideration");
				if (n<=25) {
					unions.put(pa,String.valueOf(alphabet.charAt(n)));
				} else {
					unions.put(pa,(String.valueOf(alphabet.charAt(n))).toUpperCase());
				}
				++n;
			}
		}

		return unions;
	}
	
}// end of class

