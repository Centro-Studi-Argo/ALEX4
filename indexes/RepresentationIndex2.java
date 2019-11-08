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

public class RepresentationIndex2 extends Index
	{
	// global variables for the class
	
	// constructor
	public RepresentationIndex2(Parliament parl) {
		super(parl);
	}

	/* How index is computed:
	 * r = 1-X/T
	 * X = total number of seats in excess of those obtained with the one-district proportional
	 * T = total number of seats
	 */

	protected Object computeIndex() {
		HashMap <Integer,Integer> currentAllocationOfSeats = parliament.getAllocationOfSeats();
		HashMap <Integer,Integer> proportionalAllocationOfSeats = parliament.getProportionalAllocationOfSeats();
		
		// get maximum value of proportional allocation of seats and corresponding party
		// NOTE: if more than one party has the highest value, the first one found in map is the one that gets all the seats
		Collection <Integer> seats = proportionalAllocationOfSeats.values();
		int maxSeats = (Collections.max(seats)).intValue();

		// create the index
		Set <Integer> parties = proportionalAllocationOfSeats.keySet();
		Iterator <Integer> i = parties.iterator();
		int X = 0;
		int T = 0;
		while (i.hasNext()) {
			Integer party = i.next();
			int Sji= (currentAllocationOfSeats.get(party)).intValue();
			int Sppi = (proportionalAllocationOfSeats.get(party)).intValue();
			X+=Math.abs(Sji - Sppi);
			T+=Sji;
		}// end of while
		Double toReturn = new Double((double)1 - ((double)X/(2*T)));
		parliament.setRepresentationValue(toReturn);
		DecimalFormat df = new DecimalFormat("##.####");
		return df.format(toReturn);
	}
}// end of class