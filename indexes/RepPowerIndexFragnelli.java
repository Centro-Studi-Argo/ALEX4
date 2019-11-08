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
import votingObjects.*;

public class RepPowerIndexFragnelli extends Index
	{
	// global variables for the class

	Index powerIndex;
	
	// constructor
	public RepPowerIndexFragnelli(Parliament parl,Index pi) {
		super();
		parliament = parl;
		powerIndex = pi;
		arrayOfParties = parl.getArrayOfParties();
	}

	/* How index is computed: // this is the Ortona version of the index
	 * S = sum over all parties of the difference between power computed on seats and
	 * the power computed on the votes in the parliament
	 * index is 1 - S/2
	 * use the current power index to obtain the values
	 */

	protected Object computeIndex() {
		HashMap<Integer,Double> ivs = powerIndex.getPowerMap();
		HashMap<Integer,Double> indexValueSeats = new HashMap<Integer,Double>();  // necessary because otherwise the value changes when I compute index on votes
		Set<Integer>keys = ivs.keySet();
		Iterator<Integer>ik = keys.iterator();
		while (ik.hasNext()) {
			Integer vik = ik.next();
			indexValueSeats.put(vik,ivs.get(vik));
		}
System.out.println("index on seats (first)"+ indexValueSeats.toString());
		HashMap<Integer,Integer> votes = parliament.getVotesForParliament();
System.out.println("\n------------\nvotes "+votes.toString()+"\n---------------------\n");
		powerIndex.setMapForIndex(votes);
		Object res = powerIndex.computeIndex();
		HashMap<Integer,Double> indexValueVotes = powerIndex.getPowerMap();
System.out.println("index on votes "+indexValueVotes.toString());
System.out.println("index on seats (then)"+ indexValueSeats.toString());
		
		double sum = 0.0;
		Iterator <Party> p = arrayOfParties.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			Integer name = new Integer(pa.getName());
			double indSeats  = (indexValueSeats.get(name)).doubleValue();
			double indVotes  = (indexValueVotes.get(name)).doubleValue();
			sum += Math.abs(indVotes - indSeats);
System.out.println("indSeats = "+indSeats+" indVotes = "+indVotes + " sum = "+sum);
		}

		
		
		Double toReturn = new Double((double)1 - ((double)sum/2));
//		parliament.setRepresentationValue(toReturn);
		DecimalFormat df = new DecimalFormat("##.####");
		return df.format(toReturn);
	}
}// end of class