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

public class GovernabilityIndex4star extends GovernabilityIndex4
	{
	// constructor
	public GovernabilityIndex4star(Parliament parl) {
		super();
System.out.println("governability 4*");
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
//		majorityLevel = parl.getMajorityLevel();
		// register an observer for parliament
//		parl.addObserver(this);
	}

	/* How index is computed:
	 * 
	 * Fragnelli index, version 2 using owen power index, when parties outside the government are all in the same a priori union
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	*/


	HashMap<Party,String> getAPrioriUnions(LinkedList<Integer>gvt,Integer pname,ArrayList<Party>ap) {
//System.out.println("unions in 4*");
		HashMap <Party,String>unions = new HashMap<Party,String>();
		Iterator<Party>ip = ap.iterator();
		String charGvt = String.valueOf(alphabet.charAt(0));
		String charOut = String.valueOf(alphabet.charAt(1));
		while (ip.hasNext()) {
			// if party in government and not pname, set letter charGvt
			// else set letter at charOut
			Party pa = ip.next();
			Integer name = new Integer(pa.getName());
//System.out.println("name "+name+" pname "+pname);
			if ((name!=pname.intValue()) && gvt.contains(name)) {
//System.out.println("government");
				unions.put(pa,charGvt);
			} else {
//System.out.println("outside government");
				unions.put(pa,charOut);
			}
		}

		return unions;
	}
	
}// end of class

