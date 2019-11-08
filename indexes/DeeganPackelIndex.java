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

public class DeeganPackelIndex extends Index//// implements ParliamentEventListener
	{
	// global variables for the class
//	MainFrame mainFrame = main;
	double majorityLevel;
	HashMap<String,Integer>valueFunction = new HashMap<String,Integer>();
	ArrayList <Party>restrictedParties = new ArrayList<Party>();
	int nbParties = 0;
	HashMap <Integer,Double> deeganPackelIndex = new HashMap <Integer,Double> ();
	int sizeForIndex = 0;
	LinkedList <Integer> orderedParties = new LinkedList <Integer> ();
	LinkedList <String> minimumWinningCoalitions = new LinkedList<String>();

	// constructor


	public DeeganPackelIndex(Parliament parl, HashMap<Integer,Integer> map) {
		super();
System.out.println("CONSTRUCTING DEEGAN PACKEL INDEX");
		parliament=parl;
		majorityLevel = parl.getMajorityLevel();
//		mainFrame = MainFrame.getInstance();
		// create a map <string,integer> containing as key the possible grouping of parties and as value whether they get majority or not
		// keep only the parties that have seats in the parliament
		// get the distribution of seats in parliament
		arrayOfParties = parl.getArrayOfParties();
		mapForIndex = map;
//System.out.println("map for index "+mapForIndex.toString());

		// get majority
		sizeForIndex = 0;
		Iterator <Party> p = arrayOfParties.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			Integer name = new Integer(pa.getName());
			Integer val = mapForIndex.get(name);
			sizeForIndex+=val.intValue();
//			if (val.intValue()>0) {
				restrictedParties.add(pa);
//			}
		}
//System.out.println("got restrictedParties which has "+restrictedParties.size()+" parties");

		nbParties = restrictedParties.size();
  		int majorityForIndex = (int)(sizeForIndex*majorityLevel/100);
		if (majorityLevel==50) {
			++majorityForIndex;
		}
System.out.println("majorityLevel: "+majorityLevel+" majority in seats: "+majorityForIndex);

		// order the parties according to the number of seats they get, in decreasing order (first is the party with most seats
		GenericParliament gp = new GenericParliament();
		orderedParties = gp.sortMap(mapForIndex,"descending",mapForIndex);
		int t = orderedParties.size();
//System.out.println("ordered parties "+orderedParties);
		// get the set of minimum winning coalitions
		for (int i=1;i<=t;++i) {
			Combo TC = new Combo();
			String TCr = TC.outputAllCombinations(t,i);
//System.out.println("Tcr "+ TCr);
			String[] ATCr = TCr.split(";");
			// for each element in combo, get list of corresponding parties in "orderedParties" then their value (seats/votes) from
			// "mapForIndex". Confront with majority: if >=: winning coalition else, ignore
			// if the coalition is winning, need to check whether it is minimal: remove last element of list (ie smallest party since
			// they are in decreasing order). Look into "minimumWinningCoalitions". If it contains the list minus the last element,
			// ignore coalition as it is not minimum. If not, add to minimumWinning coalitions.
			for (int j=0;j<ATCr.length;++j) {
				String c = ATCr[j];
				int sum = 0;
				String minCoalitions = new String();
				String[] comb = c.split(",");
				
				for (int co=0;co<comb.length;++co) {
					String el = comb[co];
					int idx = (new Integer(el)).intValue()-1;
					Integer partyName = orderedParties.get(idx);
					int partyValue = (mapForIndex.get(partyName)).intValue();
//System.out.println("element: "+el+", idx: "+idx+", party name: "+partyName.toString()+", partyValue:" + partyValue);
					sum += partyValue;
					if (minCoalitions.length()==0) {
						minCoalitions += partyName.toString();
					} else {
						minCoalitions += "," + partyName.toString();
					}
//System.out.println("minCoalitions "+minCoalitions);
					if (minimumWinningCoalitions.contains(minCoalitions)) {
//System.out.println("already contained, exit loop");
						break;
					}
				}
//System.out.println("sum is "+sum+" majority is "+majorityForIndex);
				if (!minimumWinningCoalitions.contains(minCoalitions)) {
//System.out.println("cReduced is not in minimunWinningCoalitions");
					if (sum>=majorityForIndex) {
//System.out.println("sum is greater than majority, add it");
						minimumWinningCoalitions.add(minCoalitions);
					}
				} 
			} // end for on ATCr
		}// end for i
System.out.println("Minimum winning coalitions "+minimumWinningCoalitions.toString());
	}// end constructor
	
	protected Object computeIndex() {
		String result = new String();
		int K = minimumWinningCoalitions.size();
		// loop on parties
		Iterator <Party> p = arrayOfParties.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			int name = pa.getName();
			double sum = 0;
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
					sum += (double)1/(els.length);
				}
			}
			double val = ((double)1/K)*sum;
			deeganPackelIndex.put(new Integer(name),new Double(val));
			DecimalFormat df = new DecimalFormat("##.####");
			result += pa.getNameParty() +": "+df.format(val)+" <br> ";
		}
		indexValue=deeganPackelIndex;
		setPowerMap(deeganPackelIndex);
//		result+="-----------------------------";
System.out.println("deeganPackelIndex: "+deeganPackelIndex.toString());
		return(result);	
	}
	
	// allow to display the a-priori unions
	public Object recallParameters() {
		String o = new String();
		Iterator <String> i = minimumWinningCoalitions.iterator();
		while (i.hasNext()) {
			String si = i.next();
			String sia[] = si.split(",");
			java.util.List <String> list = Arrays.asList(sia);
			Collections.sort(list);
			String mv = new String();
			for (int s=0;s<list.size();++s) {
				Integer pname = new Integer(list.get(s));
				Party pa = arrayOfParties.get((pname.intValue())-1);
				if (mv.length()==0) {
					mv+="["+pa.getNameParty();
				} else {
					mv+=","+pa.getNameParty();
				}
			}
			mv+="]";
			if (o.length()==0) {
				o+="<i>"+language.getString("labels","minimumWinningCoalitions")+"<br>"+mv;
			} else {
				o+=" ; "+mv;
			}
		}
		o+="</i>";
		return o;
	}

}