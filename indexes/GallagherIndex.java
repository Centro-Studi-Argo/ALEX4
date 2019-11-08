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

import classesWrittenByOthers.*;
import globals.*;
import parliaments.*;

public class GallagherIndex extends Index
	{
	// global variables for the class
	
	// constructor
	public GallagherIndex(Parliament parl) {
		super(parl);
	}

	/* How index is computed:
	 * G= sqrt( 0.5 * (sum_{i=1 to n} (v_i - s_i)^2
	 * where:
	 * - i indexes a loop on parties
	 * - v_i is the percentage of votes obtained by party i
	 * - s_i is the percentage of seats obtained by party i
	 */

	protected Object computeIndex() {
		HashMap <Integer,Integer> currentAllocationOfSeats = parliament.getAllocationOfSeats();
		HashMap <Integer,Integer> currentVotes = parliament.getVotesForParliament();
		HashMap <Integer,Double> percentageSeats = new HashMap <Integer,Double> ();
		HashMap <Integer,Double> percentageVotes = new HashMap <Integer,Double> ();
		// get percentages of votes and seats
		Set <Integer> k = currentAllocationOfSeats.keySet();
		Iterator <Integer> i = k.iterator();
		int totSeats = 0;
		int totVotes = 0;
		while (i.hasNext()) {
			Integer p = i.next();
			int vals = (currentAllocationOfSeats.get(p)).intValue();
			int valv = (currentVotes.get(p)).intValue();
			totSeats += vals;
			totVotes += valv;
		}
		
System.out.println("tot seats "+totSeats+" tot votes "+totVotes);
		i = k.iterator();
		while (i.hasNext()) {
			Integer p = i.next();
			int vals = (currentAllocationOfSeats.get(p)).intValue();
			int valv = (currentVotes.get(p)).intValue();
			double percs = (double)vals*100/totSeats;
			double percv = (double)valv*100/totVotes;
System.out.println("party "+p+" perc seats "+percs+" perc votes "+percv);
			percentageSeats.put(p,new Double(percs));
			percentageVotes.put(p,new Double(percv));
		}
		// create the index
		double sum = 0;
		i = k.iterator();
		while (i.hasNext()) {
			Integer p = i.next();
			double percs = (percentageSeats.get(p)).doubleValue();
			double percv = (percentageVotes.get(p)).doubleValue();
			double diff = Math.pow((percv-percs),2);
System.out.println("party "+p+" squared diff "+diff);
			sum += diff;
		}// end of while
		Double toReturn = Math.sqrt(sum/2);
		parliament.setRepresentationValue(toReturn);
		DecimalFormat df = new DecimalFormat("##.####");
		return df.format(toReturn);
	}
}// end of class