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

public class OrtonzhafIndex2 extends OrtonzhafIndex//// implements ParliamentEventListener
	{
 	DecimalField aField;
	double b;

	public OrtonzhafIndex2(Parliament parl, HashMap<Integer,Integer> map) {
		super(parl,map);
	}

	public JPanel parameters() {
		// define panel to ask for "b" parameter, and if necessary, government
// System.out.println("create panel for owen parameters");
		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		panel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		JLabel aparameter = new JLabel(language.getString("labels","bParameterOrtonzhaf2"));
		panel.add(aparameter,c);
		++c.gridy;
		NumberFormat proportionFormat = NumberFormat.getNumberInstance();
		proportionFormat.setMinimumFractionDigits(0);
		proportionFormat.setMaximumFractionDigits(2);
		aField = new DecimalField(b,4,proportionFormat);
		++c.gridy;
		aField.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// check that newval is a one-character string
				double newb = aField.getValue();
				if ((newb<0) || (newb>1)) {
					JOptionPane.showMessageDialog(null,language.getString("labels","aErrorMessage"));
				} else {
					b = newb;
				}
			}
		});
		panel.add(aField,c);
		return panel;
	}



	
	protected Object computeIndex() {

                Object obj = new Object();
                OrtonzhafIndex oi1 = new OrtonzhafIndex(parliament,mapForIndex,0);
                oi1.computeIndex();
                HashMap <Integer,Double> ozPowerMap = oi1.returnPowerMap();
                HashMap <String,Integer> ozValueFunction = oi1.returnValueFunction();
                HashMap <String, HashMap <Integer,Double> > ozPerCoalitionPerParty = oi1.returnMapPerCoalitionPerParty();
//System.out.println("\n--------\n"+ozPerCoalitionPerParty+"\n--------\n");
// System.out.println("Powermap: "+powerMap);
// // System.out.println("Ortonzhaf value function: "+ozValueFunction);
// System.out.println("Array of parties: "+arrayOfParties);
// System.out.println("Map for index: "+mapForIndex);
                HashMap <Integer,Double> partyPower = new HashMap<Integer,Double>(); //
                Iterator <Party> p = arrayOfParties.iterator();
                double sumAll = 0.0;
                String result = new String("b="+b+"<br>");
		while (p.hasNext()) {
			Party pa = p.next();
			Integer name = new Integer(pa.getName());
result+="\r<b>CONSIDERING PARTY "+name.intValue()+"</b><br>\r";
			Integer ival = mapForIndex.get(name);
			int seats = ival.intValue();
// System.out.println("which has "+seats);
//                         Double dval = ozPowerMap.get(name);
//                         double oi1Power = dval.doubleValue();
// System.out.println("which has "+seats+" and an oz1 power of "+oi1Power);
                        double sumParty = 0.0;
			Set <String> keys = ozValueFunction.keySet();
//result+="allkeys "+keys+"<br>";
            Iterator <String> s = keys.iterator();
			while (s.hasNext()) {
				String st = s.next();
//result+="coalition: "+st+"<br>";
				// does string contains party?
				if (st.contains("*"+name+"*")) {
// System.out.println("contains the party");
//result+="coalition: "+st+"<br>\n";
					int val1 = ozValueFunction.get(st).intValue();
                                        if (val1==1) {// coalizione vincente
// System.out.println("val1 = "+val1);
                                            double oi1Power = ((ozPerCoalitionPerParty.get(st)).get(name)).doubleValue();
                                            int P =0;
                                            if (oi1Power>0) {
                                                P=1;
                                            }
                                            int seatsCoalition=0;
                                            String[] partiesInCoalition = st.split(",");
                                            for (int c=0;c<partiesInCoalition.length;++c) {
                                                String stminus = (partiesInCoalition[c]).replace("*","");
                                                ival = mapForIndex.get(new Integer(stminus));
// System.out.println("seats of party "+ival);
                                                seatsCoalition += ival.intValue();
                                            }
//result+="seatsCoalition = "+seatsCoalition+", seats party "+seats+". oi1Power "+oi1Power+" quindi P="+P+"<br>\n";
//                                            double oz2i = (double)b*oi1Power + (double)(1-b)*seats/seatsCoalition;
                                            double oz2i = (double)b*P + (double)(1-b)*seats/seatsCoalition;
//result+="oz2i = "+b+"*"+P+"+(1-"+b+")*"+seats+"/"+seatsCoalition+"="+oz2i+"<br>\n";
                                            sumParty+= oz2i;
                                            sumAll += oz2i;
//result+="sumParty = "+sumParty+"<br>\n\r";
                                        }
// System.out.println("val1 = "+val1);
				}
			}
                        
			partyPower.put(name,new Double(sumParty));
		}
//System.out.println("at the end: powerParty "+partyPower.toString());
result+="\r<b>FINAL RESULT</b><br>\n\r";
                HashMap <Integer,Double> OZvalue = new HashMap <Integer,Double> ();
		p = arrayOfParties.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			Integer name = new Integer(pa.getName());
                	Double powerP = partyPower.get(name);
                        double val = powerP.doubleValue()/sumAll;
//result+="power: "+powerP.doubleValue()+" sumall "+sumAll+" value of index "+val+"<br>";
//System.out.println("party "+pa.getNameParty()+": "+aPrioriUnions.get(pa));
                        OZvalue.put(name,val);
                        DecimalFormat df = new DecimalFormat("##.####");
                        result += pa.getNameParty()+": "+df.format(val)+" <br> ";
                }
		indexValue=OZvalue;
		setPowerMap(OZvalue);
		return(result);	
                
		
	}
	

}
