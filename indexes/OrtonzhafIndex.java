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

public class OrtonzhafIndex extends BanzhafIndex//// implements ParliamentEventListener
	{
 	DecimalField aField;
	double a;

	public OrtonzhafIndex(Parliament parl, HashMap<Integer,Integer> map) {
		super(parl, map);
	}

	public OrtonzhafIndex(Parliament parl, HashMap<Integer,Integer> map, double a) {
		super(parl, map);
                this.a = a;
System.out.println("OK, ortonzhaf created");
	}


	public JPanel parameters() {
		// define panel to ask for "a" parameter, and if necessary, government
//System.out.println("create panel for owen parameters");
		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		panel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		JLabel aparameter = new JLabel(language.getString("labels","aParameterXgovernability"));
		panel.add(aparameter,c);
		++c.gridy;
		NumberFormat proportionFormat = NumberFormat.getNumberInstance();
		proportionFormat.setMinimumFractionDigits(0);
		proportionFormat.setMaximumFractionDigits(2);
		aField = new DecimalField(a,4,proportionFormat);
		++c.gridy;
		aField.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// check that newval is a one-character string
				double newa = aField.getValue();
				if ((newa<0) || (newa>1)) {
					JOptionPane.showMessageDialog(null,language.getString("labels","aErrorMessage"));
				} else {
					a = newa;
				}
			}
		});
		panel.add(aField,c);
		return panel;
	}



	
	protected Object computeIndex() {
		Object obj = super.computeIndex();
// System.out.println("Ortonzhaf: "+obj);
// System.out.println("ortonzhaf - powermap: "+powerMap);
		return(obj);	
		
	}
	
        public HashMap <String,Integer> returnValueFunction() {
            return valueFunction;
        }
        
        public HashMap <Integer,Double> returnPowerMap() {
            return powerMap;
        }
        
	protected HashMap <String,Integer> getValueFunction() {
//System.out.println("using ortonzhaf getValueFunction");
//System.out.println("got array of parties");
		HashMap <Integer,Integer> forShapley = new HashMap<Integer,Integer>();
//System.out.println("map for ortonzhaf index when creating value "+mapForIndex.toString());

		restrictedParties = new ArrayList<Party>();
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



//System.out.println("created forShapley");
//System.out.println("got seats in parliament");
//System.out.println("got its size nbparties= "+nbParties);		
		// get the value at which majority is obtained
//System.out.println("majorityLevel "+ majorityLevel);
		majorityForIndex = (int)Math.round((sizeForIndex*majorityLevel/100));
//		if (majorityLevel==50) {
//			++majorityForIndex;
//		}
////System.out.println("majority in seats: "+majorityForIndex);


		HashMap<String,Integer>vf = new HashMap<String,Integer>();
		// put the "null" set in vf
		vf.put("",new Integer(0));
		// then get all the sets
		for (int i=1;i<=nbParties; ++i) {
//System.out.println("get combo");
			Combo ci = new Combo();
//System.out.println("done");
			String res = ci.outputAllCombinations ( nbParties, i );
//System.out.println("risultato combo: "+res);
			String[] arrayRes = res.split(";");
//System.out.println("arrayRes "+arrayRes.toString());
			for (int j=0;j<arrayRes.length;++j) {
//System.out.println("j = "+j);
				String c = arrayRes[j];
//System.out.println("c = "+c);
				String[] pn = c.split(",");
//System.out.println("pn "+pn.toString());
				int sum = 0;
				String listParties = new String();
				int oldIndex=0;
				int broken = 0;
				for (int k=0;k<pn.length;++k) {
					String el = pn[k].replace("*","");
					int index = (new Integer(el)).intValue();
					if (k==0) {
						oldIndex = index;
					} else {
						if (index == (oldIndex+1)) {
							oldIndex = index;
						} else {
							broken=1;
							break;
						}
					}
//System.out.println("oldIndex: "+oldIndex+" index: "+index);
					Party pa = restrictedParties.get(index-1);
//System.out.println("get name party: "+pa.getName());
					Integer name = new Integer(pa.getName());
					Integer val = mapForIndex.get(name);
					sum+=val.intValue();
					listParties += "*"+name.toString() + "*,";
				}
				if (broken==0) {
					listParties = listParties.substring(0,listParties.length()-1);
//System.out.println(c+"="+listParties+"="+sum);
//System.out.println("listParties "+ listParties);
					vf.put(listParties, ((sum>majorityForIndex) ? new Integer(1) : new Integer(0)));
				}
			}
		}
//System.out.println("ortonzhaf: vf");
//System.out.println(vf);
		return vf;
	}

	public double getDifference(Integer pname,int val1, HashMap<String,Integer>vf,String stminus) {
//System.out.println("using ortonzhaf getDifference - majority for index = "+majorityForIndex);
		int seatsParty = (mapForIndex.get(pname)).intValue();
//System.out.println("party "+pname+" coalition without party"+stminus);
		int sum = seatsParty;
		if (stminus.length()>2) {
			String[] si = stminus.split(",");
			for (int i=0;i<si.length;++i) {
				String sii = si[i];
				Integer partyName = new Integer(sii.replace("*",""));
				sum+= ((mapForIndex.get(partyName)).intValue());
			}
		}
		int sumOthers = sum - seatsParty;
		int val2 = (sumOthers>majorityForIndex) ? 1 : 0;
		double diff = (double) Math.abs(val1-val2) ;
//System.out.println("\t seatsParty "+seatsParty+" sum coalizione "+sum+" sumOthers "+sumOthers+" a "+a);
//System.out.println("\t val1 "+val1+" val2 "+val2+" -> diff: "+diff);
		return diff * Math.pow((double)seatsParty/sum,a);
	}

}
