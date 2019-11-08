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

public class OwenIndex extends ShapleyValue//// implements ParliamentEventListener
	{
	// global variables for the class
	MainFrame mainFrame;
	private double majorityLevel;
	public String whatIndex = "OwenIndex";
	final HashMap <Party,String>aPrioriUnions = new  HashMap<Party,String>();
	HashMap <Integer,Double> owenIndex = new HashMap <Integer,Double> ();
	int sizeForIndex = 0;

	// constructor
	public OwenIndex(Parliament parl,HashMap<Integer,Integer>map) {
		super(parl,map);
System.out.println("CONSTRUCTING OWEN INDEX - and creating unions");
		Iterator <Party> p = arrayOfParties.iterator();
		// define the a-priori unions of parties
		// put default alphabet letter for all parties in the map
		mapForIndex = map;
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
//System.out.println("alphabet: "+alphabet);
		int i=0;
		while (p.hasNext()) {
			Party pa = p.next();
			Integer seats = mapForIndex.get(new Integer(pa.getName()));
//			if (seats.intValue()>0) {
//System.out.print("i "+i);
//System.out.println(" corresponds to "+alphabet.charAt(i));
				int modi = i/25;
//System.out.println("modi: "+modi);
				if (modi>=1) {
					i = i-(25*modi);
				}
//System.out.print("i "+i);
//System.out.println(" corresponds to "+alphabet.charAt(i));
				aPrioriUnions.put(pa,String.valueOf(alphabet.charAt(i)));
				++i;
//			}
		}
	}

	public OwenIndex(Parliament parl,HashMap<Integer,Integer>map,HashMap<Party,String>unions) {
		super(parl,map);
System.out.println("CONSTRUCTING OWEN INDEX - and passing unions");
		parliament=parl;
		arrayOfParties = parl.getArrayOfParties();
		majorityLevel = parl.getMajorityLevel();
		mapForIndex = map;
		Set <Party> keys = unions.keySet();
		Iterator <Party> p = keys.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			Integer seats = mapForIndex.get(new Integer(pa.getName()));
			aPrioriUnions.put(pa,unions.get(pa));
		}
	}
	
	
	/*
	 * insert here description of the owen index
	*/


	
	public JPanel parameters() {
		// define panel to ask for "a" parameter, and if necessary, government
System.out.println("create panel for owen parameters");
		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		panel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		JLabel groupLabel = new JLabel(language.getString("labels","group"));
		groupLabel.setToolTipText(language.getString("labels","groupToolTip"));
		panel.add(groupLabel,c);
		++c.gridy;
		c.gridx=0;
		Iterator <Party> p = arrayOfParties.iterator();
		while (p.hasNext()) {
			final Party pa = p.next();
			if (aPrioriUnions.containsKey(pa)) {
				JLabel name = new JLabel(pa.getNameParty());
				panel.add(name,c);
				++c.gridx;
				final String val = aPrioriUnions.get(pa);
				final JTextField valueGroup = new JTextField(val,1);
				valueGroup.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// check that newval is a one-character string
						String newval = valueGroup.getText();
						if (newval.length()>1) {
							JOptionPane.showMessageDialog(null,language.getString("labels","notALetterGroup"));						
						} else if (newval.length()==0) {
							valueGroup.setText(val);
						} else {
							valueGroup.setText(newval);
							aPrioriUnions.remove(pa);
							aPrioriUnions.put(pa,newval);
						}
					}
				});
				panel.add(valueGroup,c);
				c.gridx = 0;
				++c.gridy;
			}
		}
		return panel;
	}

	protected Object computeIndex() {
		valueFunction = getValueFunction();
//System.out.println("valueFunction: "+valueFunction.toString());
		// now get owen value: for each party
			// iterate over aPrioriUnions (K): set on one side the one containing the party (T) and on the other side the other unions (H)
			// adding the empty set to them
				// initialise sum0
				// iterate over the remaining unions (H)
				// - get M1 = h!(k-h-1)!/(k)!
				// - from the size of T, make up a combo of values, keep only thos containing the party pa's name (pname)
				// - initialise sum1
				// - for each of these elements of combo:
				// 		+ get M2 = (s-1)!(t-1)!/t!
				//		+ get v(H u S) from value function	(put the indexes in increasing order so that I can find them in valueFunction)
				//		+ get v(H u S\pa) from value function (put the indexes in increasing order so that I can find them in valueFunction)
				// 		+ get their difference D
				//		+ add M2 x D to sum1
				// - add M1 * sum1 to sum0
				// resulting sum0 at the end of the loops is the owen value for the party.
		String result = new String();
		Iterator <Party> p = restrictedParties.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			Integer pname = pa.getName();
//System.out.println("\nOWEN CONSIDERING PARTY "+pname+"\n");
//System.out.println("A priori unions "+aPrioriUnions.toString());
			String paUnion = aPrioriUnions.get(pa);
			ArrayList<Party> partiesInPaUnion = new ArrayList<Party>();
			partiesInPaUnion.add(pa);
			HashMap<String,ArrayList<Party>>remainingUnions = new HashMap<String,ArrayList<Party>>();
//			remainingUnions.put("empty",new ArrayList<Party>()); // make sure remaining unions contain the empty set
			Set<Party>keys = aPrioriUnions.keySet();
			Iterator<Party>ikeys = keys.iterator();
			while (ikeys.hasNext()) {
				Party ipa = ikeys.next();
				if (ipa!=pa) {
					String ipaUnion = aPrioriUnions.get(ipa);
//System.out.println("ipaUnion: "+ipaUnion+ " paUnion: "+paUnion);
					if (ipaUnion.compareTo(paUnion) == 0) {// same union
//System.out.println("same union");
						partiesInPaUnion.add(ipa);
					} else {// different union
//System.out.println("different union");
						if (remainingUnions.containsKey(ipaUnion)) {
							ArrayList <Party> l = remainingUnions.get(ipaUnion);
							l.add(ipa);
							remainingUnions.put(ipaUnion,l);
						} else {
							ArrayList <Party> l = new ArrayList<Party>();
							l.add(ipa);
							remainingUnions.put(ipaUnion,l);
						}
					}
				}
			}
//System.out.println("Parties in union: "+partiesInPaUnion.toString());
//System.out.println("Remaining unions: "+remainingUnions.toString());
			double sum0 = 0.0;
			int nbRemainingUnions = remainingUnions.size();
			Set <String> ukeys = remainingUnions.keySet();
			Iterator <String> iukeys = ukeys.iterator();
			ArrayList<String> keysOfRemainingUnions = new ArrayList<String>(); // to keep the keys always in the same order
			while (iukeys.hasNext()) {
				String key = iukeys.next();
				keysOfRemainingUnions.add(key);
			}
//System.out.println("keys of remaining unions: "+keysOfRemainingUnions.toString());
			for (int r=0;r<=nbRemainingUnions;++r) {
				String THr = new String();
//System.out.println("r = "+r+"; nbRemainingUnions = "+nbRemainingUnions);
				if (r>0) {//only if the function is not null do I combine unions
					Combo TH = new Combo();
					THr = TH.outputAllCombinations(nbRemainingUnions,r);
				}
//System.out.println("THr "+THr);
				String[] ATHr = THr.split(";");
				for (int q=0;q<ATHr.length;++q) {
					String rq = ATHr[q];
//System.out.println("rq "+rq);
					// this gives a combination of keys of remainingUnions
					// unite them together.
					String[] allq = rq.split(",");
					ArrayList<Party> H = new ArrayList<Party>();
					int h=0;
					if (r>0) {
						for (int o=0;o<allq.length;++o) {
							String union = keysOfRemainingUnions.get(new Integer(allq[o]).intValue()-1);
							++h;
							H.addAll(remainingUnions.get(union));
						}
					}
//System.out.println("\tH contains the parties "+H.toString());
//System.out.println("number of unions in H: h="+h);
					int k = remainingUnions.size()+1;// cannot use the size of aprioriUnions map because it is based on parties, not unions => nb of unions = nb of remaining unions (because the map is on unions) + 1 (to count the union I am considering at the moment)
//System.out.println("size of aPrioriUnions: k="+k);
					double M1 = (double)factorial(h)*factorial(k-h-1)/factorial(k);
//System.out.println("M1= "+h+"!("+k+"-"+h+"-1)!/("+k+")! = "+M1);
					int t = partiesInPaUnion.size();
//System.out.println("number of parties in unions: t="+t);
					ArrayList <String> unionsContainingPa = new ArrayList<String>();
					for (int i=1;i<=t;++i) {
						Combo TC = new Combo();
						String TCr = TC.outputAllCombinations(t,i);
//System.out.println("TCr "+TCr);
						String[] ATCr = TCr.split(";");
					// pa is in first position of array, so look for the substring "*1" in ATCr
						for (int j=0;j<ATCr.length;++j) {
							String c = ATCr[j];
//System.out.println("c "+c);
							String[] comb = c.split(",");
							for (int co=0;co<comb.length;++co) {
								String el = comb[co];
//System.out.println("el "+el);
								if (el.compareTo("1")==0) {
//System.out.println("el is 1");
									unionsContainingPa.add(c);
									break;
								}
							}
						}
					}
//System.out.println("all unions containing pa" + unionsContainingPa.toString());
					double sum1 = 0;
					Iterator <String> iu = unionsContainingPa.iterator();
					while (iu.hasNext()) {
						String ius = iu.next();
						String[] aius = ius.split(",");
						
						int s = aius.length;
//System.out.println("number of parties in union: s="+s);
						// get M2
						double M2 = (double)factorial(s-1)*factorial(t-s)/factorial(t);
//System.out.println("M2= "+M2);
						// get H u S and H u S\pa
						ArrayList <Integer> groupParties = new ArrayList<Integer>();
						for (int j=0;j<aius.length;++j) {
							String el = aius[j].replace("*","");
							Party piu = partiesInPaUnion.get((new Integer(el)).intValue()-1);
							groupParties.add(new Integer(piu.getName()));
						}
//System.out.println("groupParties before sorting (from aius) "+groupParties.toString());
						for (int j=0;j<H.size();++j) {
							Party piu = H.get(j);
							groupParties.add(new Integer(piu.getName()));
						}
//System.out.println("groupParties before sorting (from h) "+groupParties.toString());
						// order the two vectors and create the strings that will be found in the value function
						Collections.sort(groupParties);
//System.out.println("groupParties after sorting "+groupParties.toString());
						String HuS = new String();
						String HuSminusPa =  new String();
						for (int j=0; j<groupParties.size();++j) {
							Integer in = groupParties.get(j);
							HuS += "*" + in.toString() + "*,";
							if (in.intValue()!=pname) {
								HuSminusPa +="*" + in.toString() + "*,"; 
							}
						}
//System.out.println("HuS = "+HuS);
//System.out.println("HuSminusPa = "+HuSminusPa);
						HuS = HuS.substring(0,HuS.length()-1);
						if (HuSminusPa.length()>0) {
							HuSminusPa = HuSminusPa.substring(0,HuSminusPa.length()-1);
						}
//System.out.println("HuS = "+HuS);
//System.out.println("HuSminusPa = "+HuSminusPa);
						int vHuS = valueFunction.get(HuS).intValue();
						int vHuSminusPa = valueFunction.get(HuSminusPa).intValue();
//System.out.println("vHuS = "+vHuS);
//System.out.println("vHuSminusPa = "+vHuSminusPa);
						int D = vHuS - vHuSminusPa;
//System.out.println("D = "+D);
						sum1 += M2 * D;
//System.out.println("sum1 = M2 * D = "+M2+" * "+ D+" = "+sum1);
					}
					sum0+= M1 * sum1;
//System.out.println("sum0 +=  M1 * sum1 += "+M1+" * "+sum1+" +=" +sum0);
				}// end of loop on H
			}
			owenIndex.put(pname,new Double(sum0));
			
//System.out.println("party "+pa.getNameParty()+": "+aPrioriUnions.get(pa));
			DecimalFormat df = new DecimalFormat("##.####");
			String union = (aPrioriUnions.containsKey(pa)) ? aPrioriUnions.get(pa) : "-" ;
			result += pa.getNameParty()+" ("+union+"): "+df.format(owenIndex.get(pa.getName()))+" <br> ";
		}
		indexValue=owenIndex;
		setPowerMap(owenIndex);
//		result+="-----------------------------";
System.out.println("owenIndex: "+owenIndex.toString());
		return(result);	
	}// end of computeIndex;

	public HashMap<Integer,Double> getMapOfValues () {
		return owenIndex;
	}
	

}// end of class

