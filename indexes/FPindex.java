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

public class FPindex extends Index {//// implements ParliamentEventListener{
    // global variables for the class
    double majorityLevel;
    HashMap<String,Integer>valueFunction = new HashMap<String,Integer>();
    ArrayList <Party>restrictedParties = new ArrayList<Party>();
    int nbParties = 0;
    HashMap <Integer,Double> FPindexMap = new HashMap <Integer,Double> ();
    int sizeForIndex = 0;
    LinkedList <String> contiguousCoalitions = new LinkedList<String>();
    final HashMap <String,HashMap<String,Object>> FPparameters= new HashMap<String,HashMap<String,Object>>();
    ArrayList<String> usedCoalitions = new ArrayList <String> ();
    HashMap <String,Double> stAlpha = new HashMap <String,Double> ();
    HashMap <String,HashMap<Integer,Double>> stBeta = new HashMap <String,HashMap<Integer,Double>> ();
    
    public FPindex(Parliament parl, HashMap<Integer,Integer> map) {
	super();
//System.out.println("CONSTRUCTING FP INDEX");
	parliament=parl;
	majorityLevel = parl.getMajorityLevel();
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
	}
//System.out.println("got restrictedParties which has "+restrictedParties.size()+" parties");
	nbParties = arrayOfParties.size();
  	int majorityForIndex = (int)(sizeForIndex*majorityLevel/100);
	if (majorityLevel==50) {
            ++majorityForIndex;
	}
//System.out.println("majorityLevel: "+majorityLevel+" majority in seats: "+majorityForIndex);

	// get the set of contiguous coalitions
	for (int i=1;i<=nbParties;++i) {
            Combo TC = new Combo();
            String TCr = TC.outputAllCombinations(nbParties,i);
//System.out.println("Tcr "+ TCr);
            String[] ATCr = TCr.split(";");

            // for each element in combo, get list of corresponding parties in "arrayOfParties" 
            // then their value (seats/votes) from
            // "mapForIndex". 
            // if the two parties are not contiguous ([index at n+1] does not equal 
            // [index at n] +1 , do not use coalition
            for (int j=0;j<ATCr.length;++j) {
                String c = ATCr[j];
//System.out.println("c "+c);
                int sum = 0;
                String coalitions = new String();
                String[] comb = c.split(",");
                boolean isContiguous = true;
//System.out.println("number of elements in comb: "+comb.length);
                for (int co=0;co<comb.length;++co) {
                    String el = comb[co];
                    int idx = (new Integer(el)).intValue()-1;
//System.out.println("element el "+el+" corresponds to index "+idx);
                    if (co<=comb.length-2) {
                        String elnext = new String();
                        elnext = comb[co+1];
                        int idxnext = (new Integer(elnext)).intValue()-1;
//System.out.println("element elnext "+elnext+" corresponds to index "+idxnext);
                        if (idxnext != idx+1) {
//System.out.println("which is not contiguous to el");
                            isContiguous = false;
                            break;
                        }
                    }

                    if (isContiguous==true) {
//System.out.println("index is contiguous, put in coalitions");
                        Integer partyName = arrayOfParties.get(idx).getName();
			int partyValue = (mapForIndex.get(partyName)).intValue();
                        sum+=partyValue;
                        if (coalitions.length()==0) {
                            coalitions += partyName.toString();
                        } else {
                            coalitions += "," + partyName.toString();
                        }
                    }
                }
                // only contiguous winning coalitions
                if ((isContiguous == true) && (sum>=majorityForIndex)) {
//System.out.println("all indexes are contiguous, add to contiguous coalitions");
                    contiguousCoalitions.add(coalitions);
                }
            } // end for on ATCr
        }// end for i
//System.out.println("contiguous coalitions "+contiguousCoalitions.toString());

    }// end constructor
	
    public JPanel parameters() {

        final NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(3);
        
        // define panel to ask for "a" parameter, and if necessary, government
        //System.out.println("create panel for FP index parameters");
        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        panel.setLayout(gridbag);
        c.gridx = 0;
        c.gridy = 0;
        // titles
        panel.add(new TitleLabel(language.getString("labels","contiguousCoalitions")),c);
        ++c.gridx;
        panel.add(new TitleLabel("alpha"),c);
        ++c.gridx;
        panel.add(new TitleLabel("beta"),c);
        c.gridx=0;
        ++c.gridy;
        Iterator <String> st = contiguousCoalitions.iterator();
        while (st.hasNext()) {
            final String coa = st.next();
            String[] coas = coa.split(",");
            String cn = new String();
            HashMap <Integer,Double> beta = new HashMap <Integer,Double>();
            for (int i=0;i<coas.length;++i) {
                int p = ((new Integer((coas[i]))).intValue())-1;
                Party pa = arrayOfParties.get(p);
                String n = pa.getNameParty();
                beta.put(new Integer(pa.getName()),new Double(1));
                if (cn.length()==0) {
                    cn+=n;
                } else {
                    cn+=","+n;
                }
            }
            final String coa_name = new String(cn);
            // add to FPparameters
            HashMap <String,Object> vals = new HashMap<String,Object>();
            vals.put(new String("name"),new String(coa_name));
            vals.put(new String("used"),new Boolean(true)); 
            vals.put(new String("alpha"),new Double(1));
            vals.put(new String("beta"),beta);
            FPparameters.put(new String(coa),vals);
            // checkbox and name of coalition
            boolean selected = ((Boolean)(FPparameters.get(coa)).get("used")).booleanValue();
            final JCheckBox checkCoa = new JCheckBox(coa_name,selected);
            checkCoa.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // check that newval is a one-character string
                    boolean chosen = checkCoa.isSelected();
                    HashMap <String,Object> vals = FPparameters.get(coa);
                    vals.remove("used");
                    vals.put(new String("used"),new Boolean(chosen));
                    FPparameters.remove(coa);
                    FPparameters.put(new String(coa),vals);
//System.out.println("FPparameters: "+FPparameters);
                }
            });
            panel.add(checkCoa,c);
            // decimal field for alpha
            ++c.gridx;
            final double alphaval = ((Double)(FPparameters.get(coa)).get("alpha")).doubleValue();
            final DecimalField alphaField = new DecimalField(alphaval,5,nf);
            alphaField.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // check that newval is a one-character string
                    double newval = alphaField.getValue();
                    alphaField.setValue(newval);
                    HashMap <String,Object> vals = FPparameters.get(coa);
                    vals.remove("alpha");
                    vals.put(new String("alpha"),new Double(newval));
                    FPparameters.remove(coa);
                    FPparameters.put(new String(coa),vals);
//System.out.println("FPparameters: "+FPparameters);
                }
            });
            panel.add(alphaField,c);
            // decimal fields for beta
            ++c.gridx;
            final HashMap <Integer,Double> betaval = (HashMap)(FPparameters.get(coa)).get("beta");
            final JPanel betaPanel = new JPanel();
            int nbetas = betaval.size();
            betaPanel.setLayout(new GridLayout(1,nbetas));
            Set <Integer> keysbeta = betaval.keySet();
            Iterator <Integer> pbeta = keysbeta.iterator();
            while (pbeta.hasNext()) {
                final Integer key = pbeta.next();
                final DecimalField betaField = new DecimalField(betaval.get(key),5,nf);
                betaField.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // check that newval is a one-character string
                        double newval = betaField.getValue();
                        String[] elscoa = coa.split(",");
                        betaField.setValue(newval);
                        HashMap <String,Object> vals = FPparameters.get(coa);
                        HashMap <Integer,Double> tempbeta = (HashMap)vals.get("beta");
                        vals.remove("beta");
                        tempbeta.remove(key);
                        tempbeta.put(key,new Double(newval));
                        vals.put(new String("beta"),tempbeta);
                        FPparameters.remove(coa);
                        FPparameters.put(new String(coa),vals);
//System.out.println("FPparameters: "+FPparameters);
                    }
                });
                betaPanel.add(betaField);
            } 
            panel.add(betaPanel,c);       
            c.gridx=0;
            ++c.gridy;
        }
        return panel;
    }


    protected Object computeIndex() {
        // first standardize the values in the FPparameters map
        
        HashMap <String,Double> unstAlpha = new HashMap <String,Double> ();
        
        Iterator <String> cc = contiguousCoalitions.iterator();
        double sumAlphas = 0;
        while (cc.hasNext()) {
            String coa = cc.next();
            HashMap <String,Object> vals = FPparameters.get(coa);
            boolean used = ((Boolean)vals.get("used")).booleanValue();
            if (used==true) {
                usedCoalitions.add(coa);
                unstAlpha.put(coa,(Double)vals.get("alpha"));
                sumAlphas += ((Double)vals.get("alpha")).doubleValue();
                HashMap <Integer,Double> unstBetas = (HashMap)vals.get("beta");
//System.out.println("unstBetas "+unstBetas.toString());
                HashMap <Integer,Double> sstBetas = new HashMap <Integer,Double>();
                double sumBetas = 0;
                Set <Integer> keys = unstBetas.keySet();
                Iterator <Integer> ibeta = keys.iterator();
                while (ibeta.hasNext()) {
                    Integer kbeta = ibeta.next();
                    sumBetas += (unstBetas.get(kbeta)).doubleValue();
                }
//System.out.println("sumBetas "+sumBetas);
                ibeta = keys.iterator();
                while (ibeta.hasNext()) {
                    Integer kbeta = ibeta.next();
                    double v = (unstBetas.get(kbeta)).doubleValue()/sumBetas;
                    sstBetas.put(kbeta,new Double(v));
                 }
//System.out.println("sstBetas "+sstBetas.toString());
                stBeta.put(coa,sstBetas);
            }
        }
        int n = unstAlpha.size();
        cc = usedCoalitions.iterator();
        while (cc.hasNext()) {
            String coa = cc.next();
            double v = ((unstAlpha.get(coa)).doubleValue())/sumAlphas;
            stAlpha.put(coa,new Double(v));
        }
        
//      valueFunction = getValueFunction();
        // now get FP index
        String result = new String("risultato dell'indice FP");
        Iterator <Party> p = arrayOfParties.iterator();
        while (p.hasNext()) {
            Party pa = p.next();
            Integer pname = pa.getName();
            // System.out.println("FP CONSIDERING PARTY "+pname);
            double val = 0.0;

            cc = usedCoalitions.iterator();
            while (cc.hasNext()) {
                String scoa = cc.next();
                String[] coa = scoa.split(",");
                for (int i=0;i<coa.length;++i) {
                    int pacoa = (new Integer(coa[i])).intValue();
                    if (pacoa==pname.intValue()) {// party is in the coalition
                        double alpha = stAlpha.get(scoa).doubleValue();
                        HashMap <Integer,Double> betas = stBeta.get(scoa);
                        double beta = (betas.get(pname)).doubleValue();
                        val += alpha * beta;
                    }
                }
            }

//System.out.println("party "+pname+" val "+val);
            FPindexMap.put(pname,new Double(val));
			
            DecimalFormat df = new DecimalFormat("##.####");
            if (result.length()==0) {
                result = "<html><br>"+pa.getNameParty()+": "+df.format(val)+" <br> ";
            } else {
                result +="<br>"+ pa.getNameParty()+": "+df.format(val);
            }
        }
        indexValue=FPindexMap;
        setPowerMap(FPindexMap);
//	result+="-----------------------------";
//System.out.println("FPindexMap: "+FPindexMap.toString());
        return(result);	
    }


	public Object recallParameters() {
            String o = new String();
            Iterator <String> cc = contiguousCoalitions.iterator();
            DecimalFormat df = new DecimalFormat("##.####");
            o+="<br><table border><tr><th>"+language.getString("labels","contiguousCoalitions")+" ("+language.getString("labels","used")+")</th>";
            o+=" </th><th> Alpha </th><th> Beta</th></tr>";
            while (cc.hasNext()) {
                String coa = cc.next();
                if (usedCoalitions.contains(coa)) {
                    String nameCoa = (String)(FPparameters.get(coa)).get("name");
                    String alpha = df.format(stAlpha.get(coa)).toString();
                    HashMap <Integer,Double> mapBeta = stBeta.get(coa);
                    String [] vcoa = coa.split(",");
                    String beta = new String();
                    for (int i = 0;i<vcoa.length;++i) {
                        Integer key = new Integer(vcoa[i]);
                        Double bval = mapBeta.get(key);
                        if (beta.length()==0) {
                            beta += df.format(bval).toString();
                        } else {
                            beta += " / " + df.format(bval).toString();
                        }
                    }
                    o+= "<tr><td>"+nameCoa + " </td><td> " + alpha + " </td><td> " + beta + "</tr>";
                }
            }
            o+="</table>";
            return o;
        }


}