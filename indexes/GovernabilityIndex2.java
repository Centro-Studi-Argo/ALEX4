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

public class GovernabilityIndex2 extends Index
	{
	// global variables for the class
	double S=0;
	int N=0;
	int sizeOfParliament = 0;
	LinkedList <Integer> government = new LinkedList<Integer>();
	Double indexVal = new Double(-1);
	
	// constructor
	public GovernabilityIndex2() {
	};
	public GovernabilityIndex2(Parliament parl) {
		super(parl);
		// register an observer for parliament
//		parl.addObserver(this);
	}

	/* How index is computed:
	 * S = share of seats obtained by the parties in the government
	 * N = number of parties in the government
	 * 
	 * g = S/N
	 * 
	*/

	protected Object computeIndex() {
System.out.println("GOVERNABILITY INDEX II");
		government = parliament.getPartiesInGovernment();
		HashMap <Integer,Integer> currentAllocationOfSeats = parliament.getAllocationOfSeats();
		boolean majorityFound = parliament.getMajorityFound();
		if (majorityFound == true) {
			N = government.size();

			Set <Integer> keys = currentAllocationOfSeats.keySet();
			Iterator <Integer> c = keys.iterator();
			while (c.hasNext()) {
				Integer key = c.next();
				Integer value = currentAllocationOfSeats.get(key);
				sizeOfParliament += value.intValue();
			}

			// for each party in the government
			// add its seats to nbSeatsInGovernment
			Iterator <Integer> i = government.iterator();
			while (i.hasNext()) {
				Integer party = i.next();
				S += (currentAllocationOfSeats.get(party)).intValue();
			}
			S /= sizeOfParliament;
			Double indexValue = new Double(S/N);
			indexVal = indexValue;
			parliament.setIndexValue(government,indexValue,"GovernabilityIndex2");
			DecimalFormat df = new DecimalFormat("##.####");
			return (df.format(indexValue));
		} else {
			return new String(language.getString("labels","defaultGovernabilityLabel"));
		}
	}

	protected Double getIndexVal() {
		return indexVal;
	}
	
}// end of class

