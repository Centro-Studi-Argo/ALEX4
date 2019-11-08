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

public class GovernabilityIndex extends Index
	{
	// global variables for the class
//	private double majorityLevel;
	// constructor
	public GovernabilityIndex(Parliament parl) {
		super(parl);
//		majorityLevel = parl.getMajorityLevel();
		// register an observer for parliament
//		parl.addObserver(this);
	}

	/* How index is computed:
	 * This index is made up of two sums:
	 * Gj = A + B (4)
	 * . A = 1/(C + 1) where C is the number of parties in the government such
	 *   that if they leave the government, it loses the majority (these parties are said
	 *   to be crucial)
	 * . B = [n/(m/2)] * [1/C - 1/(C + 1)] where
	 * 		 n=number of seats above the majority level
	 * 		 m=total number of seats
	 * 		 C=total number of crucial parties
	*/

	protected Object computeIndex() {
System.out.println("GOVERNABILITY INDEX I");
		HashMap <Integer,Integer> currentAllocationOfSeats = parliament.getAllocationOfSeats();
		LinkedList <Integer> government = parliament.getPartiesInGovernment();
		boolean majorityFound = parliament.getMajorityFound();
		if (majorityFound == true) {
			double sumA = 0;
			double sumB = 0;		
			int nbCrucialParties = 0;
			int sizeOfParliament = 0; // found from the current allocation of seats map (for VAP, it is not the real size of parliament)
			Set <Integer> keys = currentAllocationOfSeats.keySet();
			Iterator <Integer> c = keys.iterator();
			while (c.hasNext()) {
				Integer key = c.next();
				Integer value = currentAllocationOfSeats.get(key);
				sizeOfParliament += value.intValue();
			}
System.out.println("\tsize of Parliament "+sizeOfParliament);
			int majority = (sizeOfParliament*((int)majorityLevel)/100);
System.out.println("\tmajority in seats: "+majority);
			if (majorityLevel==50.0) {
				++majority;
			};
System.out.println("\tquindi maggioranza: "+majority);
			int nbSeatsInGovernment = 0;
System.out.println("\tnumero di seggi del governo all'inizio della funzione (deve essere 0): "+nbSeatsInGovernment);
			// for each party in the government
			// add its seats to nbSeatsInGovernment
			Iterator <Integer> i = government.iterator();
			while (i.hasNext()) {
				Integer party = i.next();
				nbSeatsInGovernment += (currentAllocationOfSeats.get(party)).intValue();
			}

System.out.println("\tnumber of seats in government: "+nbSeatsInGovernment);
System.out.println("\tnumero di partiti cruciali all'inizio della funzione (deve essere 0): "+nbCrucialParties);

			// find whether each party in government is crucial and if so, increment nbCrucialParties
			i = government.iterator();
			while (i.hasNext()) {
				Integer party = i.next();
				int seats = (currentAllocationOfSeats.get(party)).intValue();
System.out.println("\tparty "+party+" has seats "+seats);
				if ((nbSeatsInGovernment-seats) < majority) {
System.out.println("\t quindi e' maggiore");
					++nbCrucialParties;
System.out.println("\tnumber of crucial parties becomes: "+nbCrucialParties);
				}
			}
System.out.println("\tnumber of crucial parties alla fine: "+nbCrucialParties);
			int nbSeatsAboveMajority = nbSeatsInGovernment - majority;
System.out.println("\tnb seats above majority "+nbSeatsAboveMajority);
			sumA = (double)1/(nbCrucialParties+1);
System.out.println("\tvalue of A: "+sumA);
			sumB = (((double)1/nbCrucialParties)-((double)1/(nbCrucialParties+1)))*((double)nbSeatsAboveMajority/majority);
System.out.println("\tvalue of B: "+sumB);
			Double indexValue = new Double(sumA + sumB);
System.out.println("\tvalue of index: "+indexValue);
			parliament.setIndexValue(government,indexValue,"GovernabilityIndex");
			DecimalFormat df = new DecimalFormat("##.####");
			return df.format(indexValue);
		} else {
			return new String(language.getString("labels","defaultGovernabilityLabel"));
		}
	}


}// end of class

