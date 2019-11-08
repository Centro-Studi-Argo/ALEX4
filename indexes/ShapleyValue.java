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

public class ShapleyValue extends Index//// implements ParliamentEventListener
	{
	// global variables for the class
//	MainFrame mainFrame = main;
	double majorityLevel;
	HashMap<String,Integer>valueFunction = new HashMap<String,Integer>();
	ArrayList <Party>restrictedParties = new ArrayList<Party>();
	int nbParties = 0;
	HashMap <Integer,Double> shapleyValue = new HashMap <Integer,Double> ();
	int sizeForIndex = 0;
	int majorityForIndex = 0;
        HashMap <String,HashMap <Integer,Double> > shapleyPerCoalitionPerParty = new HashMap <String,HashMap<Integer,Double>> ();
//	// constructor
//	public ShapleyValue(Parliament parl) {
//		super();
//System.out.println("CONSTRUCTING SHAPLEY INDEX");
//		parliament=parl;
//		majorityLevel = parl.getMajorityLevel();
////		mainFrame = MainFrame.getInstance();
//		// create a map <string,integer> containing as key the possible grouping of parties and as value whether they get majority or not
//		// keep only the parties that have seats in the parliament
//		// get the distribution of seats in parliament
//		arrayOfParties = parl.getArrayOfParties();
//		mapForIndex = parl.getAllocationOfSeats();
//		Iterator <Party> p = arrayOfParties.iterator();
//		while (p.hasNext()) {
//			Party pa = p.next();
//			Integer name = new Integer(pa.getName());
//			Integer val = seatsInParliament.get(name);
//			if (val.intValue()>0) {
//				restrictedParties.add(pa);
////System.out.println("party with positive seats: "+name);
//			}
//		}
//System.out.println("got restrictedParties which has "+restrictedParties.size()+" parties");
//
//		nbParties = restrictedParties.size();
//
//
////		indexValue = computeIndex();
////System.out.println("indexValue computed - end of constructor");
//		// register an observer for parliament
////		parl.addObserver(this);
//	}

	public ShapleyValue(Parliament parl, HashMap<Integer,Integer> map) {
		super();
// System.out.println("CONSTRUCTING SHAPLEY INDEX");
		parliament=parl;
		majorityLevel = parl.getMajorityLevel();
//		mainFrame = MainFrame.getInstance();
		// create a map <string,integer> containing as key the possible grouping of parties and as value whether they get majority or not
		// keep only the parties that have seats in the parliament
		// get the distribution of seats in parliament
		arrayOfParties = parl.getArrayOfParties();
		mapForIndex = map;
// System.out.println("map for index "+mapForIndex.toString());
//		sizeForIndex = 0;
//		Iterator <Party> p = arrayOfParties.iterator();
//		while (p.hasNext()) {
//			Party pa = p.next();
//			Integer name = new Integer(pa.getName());
//			Integer val = mapForIndex.get(name);
//			sizeForIndex+=val.intValue();
////			if (val.intValue()>0) {
//				restrictedParties.add(pa);
////System.out.println("party with positive seats: "+name);
////			}
//		}
//System.out.println("got restrictedParties which has "+restrictedParties.size()+" parties");
//
//		nbParties = restrictedParties.size();


//		indexValue = computeIndex();
//System.out.println("indexValue computed - end of constructor");
		// register an observer for parliament
//		parl.addObserver(this);
	}
	
	protected Object computeIndex() {

//System.out.println("Total number of subgroups "+valueFunction.size());
//System.out.println("valueFunction in computeIndex: "+valueFunction);
		valueFunction = getValueFunction();
//System.out.println("value function in computeIndex shapley :"+valueFunction.toString());
		// now get shapley value
		String result = new String();
		Iterator <Party> p = arrayOfParties.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			Integer pname = pa.getName();
// System.out.println("SHAPLEY CONSIDERING PARTY "+pname);
			double val = 0.0;
			Set <String> keys = valueFunction.keySet();
			Iterator <String> s = keys.iterator();
			while (s.hasNext()) {
				String st = s.next();
// System.out.println("string : "+st);
				// does string contains party?
				if (st.contains("*"+pname+"*")) {
// System.out.println("contains the party");
					int val1 = valueFunction.get(st).intValue();
// System.out.println("val1 = "+val1);
					// get string without the party and correct commas
					String stminus = st.replace("*"+pname+"*","");
// System.out.println("stminus 1 "+ stminus);
					if (stminus.length()>0) {
						stminus = stminus.replace(",,",",");
						if ((stminus.substring(0,1)).compareTo(",")==0) {
							stminus = stminus.substring(1);
						}
						if ((stminus.substring(stminus.length()-1)).compareTo(",")==0) {
							stminus = stminus.substring(0,stminus.length()-1);
						}
					}
// System.out.println("stminus 2 "+ stminus);
					double diff = getDifference(pname,val1,valueFunction,stminus);
//						double diff = (double) Math.abs(val1-val2) ;
//System.out.println("result from getDifference: diff = "+diff);
					if (diff>0) {
//System.out.println("getting the factorial");
						// get the factorial ((s-1)!(n-s)!)/n!) and multipy by val
						int si = (st.split(",")).length;
//System.out.println("si = "+si);
						double fact = computeCoefficient(si,nbParties);
//System.out.println("factorial = "+fact);
						diff *= fact;
//System.out.println("diff becomes = "+diff);
					}
                                        if (shapleyPerCoalitionPerParty.containsKey(st)) {
                                            HashMap <Integer,Double> hm = shapleyPerCoalitionPerParty.get(st);
                                            hm.put(pname,new Double(diff));
                                            shapleyPerCoalitionPerParty.remove(st);
                                            shapleyPerCoalitionPerParty.put(st,hm);
                                        } else {
                                            HashMap <Integer,Double> hm = new HashMap <Integer,Double>();
                                            hm.put(pname,new Double(diff));
                                            shapleyPerCoalitionPerParty.put(st,hm);
                                        }
					val+=diff;
//System.out.println("val becomes = "+val);
				}
			}
			shapleyValue.put(pname,new Double(val));
			
//System.out.println("party "+pa.getNameParty()+": "+aPrioriUnions.get(pa));
			DecimalFormat df = new DecimalFormat("##.####");
//			if (result.length()==0) {
//				result = "<html>"+pa.getNameParty()+": "+df.format(val)+" <br> ";
//			} else {
				result += pa.getNameParty()+": "+df.format(val)+" <br> ";
//			}
		}
		indexValue=shapleyValue;
		setPowerMap(shapleyValue);
//		result+="-----------------------------";
// System.out.println("shapleyValue: "+shapleyValue.toString());
		return(result);	
	}

	protected HashMap <String,Integer> getValueFunction() {
//System.out.println("got array of parties");
		HashMap <Integer,Integer> forShapley = new HashMap<Integer,Integer>();
// System.out.println("map for index when creating value "+mapForIndex.toString());

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
// System.out.println("got restrictedParties which has "+restrictedParties.size()+" parties");

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
// System.out.println("majority in seats: "+majorityForIndex);


		HashMap<String,Integer>vf = new HashMap<String,Integer>();
		// put the "null" set in vf
		vf.put("",new Integer(0));
		// then get all the sets
		for (int i=1;i<=nbParties; ++i) {
//System.out.println("get combo");
			Combo ci = new Combo();
//System.out.println("done");
			String res = ci.outputAllCombinations ( nbParties, i );
// System.out.println("risultato combo: "+res);
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
				for (int k=0;k<pn.length;++k) {
					String el = pn[k].replace("*","");
					int index = (new Integer(el)).intValue();
//System.out.println("index: "+index);
					Party pa = restrictedParties.get(index-1);
//System.out.println("get name party: "+pa.getName());
					Integer name = new Integer(pa.getName());
					Integer val = mapForIndex.get(name);
					sum+=val.intValue();
					listParties += "*"+name.toString() + "*,";
				}
				listParties = listParties.substring(0,listParties.length()-1);
//System.out.println(c+"="+listParties+"="+sum);
				vf.put(listParties, ((sum>majorityForIndex) ? new Integer(1) : new Integer(0)));			
			}
		}
// System.out.println("Shapley: vf");
// System.out.println(vf);
		return vf;
	}
	

// Evaluate n!
    public static long factorial( int n ) {
        if( n <= 1 )     // base case
            return 1;
        else
            return n * factorial( n - 1 );
    }
	
//	public SemiCircle graph (HashMap <Integer,Double>map) {
//		// change the shapleyvalue map to hold integers instead of doubles: * by 100
//		Set <Integer> k = map.keySet();
//		Iterator i = k.iterator();
//		HashMap <Integer,Integer>forGraph = new HashMap<Integer,Integer>();
//		while (i.hasNext()) {
//			Integer inti = i.next();
//			double val = (map.get(inti)).doubleValue();
//			int vali = (int)(val*100);
//			forGraph.put(inti,vali);
//		}
//		Dimension dimDesk = mainFrame.getScrollableDesktop().getSize();
//		int height = (int)(dimDesk.height * 0.3);
//		int width = (int)(dimDesk.width * 0.3);
//		return (new SemiCircle(forGraph,new Dimension(size,size),arrayOfParties) );
//	}

	public double computeCoefficient (int si,int nbParties) {
		return  (double)(factorial(si-1) * factorial(nbParties-si))/factorial(nbParties);
	}

	public double getDifference(Integer pname,int val1, HashMap<String,Integer>vf,String stminus) {
		int val2=0;
		if (vf.containsKey(stminus)) {
			val2 = vf.get(stminus).intValue();
// System.out.println("val2 = "+val2);
		}
		
		return (double) Math.abs(val1-val2) ;
	}
        public HashMap <String, HashMap <Integer,Double> > returnMapPerCoalitionPerParty () {
            return shapleyPerCoalitionPerParty;
        }
}