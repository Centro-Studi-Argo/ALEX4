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
import com.tomtessier.scrollabledesktop.*;

public class MyersonIndex extends ShapleyValue//// implements ParliamentEventListener
	{
	// global variables for the class
////	MainFrame mainFrame = main;
//	private double majorityLevel;
//	HashMap<String,Integer>valueFunction = new HashMap<String,Integer>();
//	ArrayList <Party>restrictedParties = new ArrayList<Party>();
//	int nbParties = 0;
//	HashMap <Integer,Integer> mapForIndex	= new HashMap <Integer,Integer> ();
//	HashMap <Integer,Double> shapleyValue = new HashMap <Integer,Double> ();
	JTable connections;
	JScrollPane connectionsPane = new JScrollPane();

	
	// constructor
	public MyersonIndex(Parliament parl,HashMap<Integer,Integer>map) {
		super(parl,map);
                
System.out.println("DONE constructing MYERSON");
System.out.println(mapForIndex);
	}
	public JPanel parameters() {
		// define panel to ask for "a" parameter, and if necessary, government
System.out.println("create panel for myerson parameters");
		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		panel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		Iterator <Party> p = arrayOfParties.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			Integer name = new Integer(pa.getName());
			Integer val = mapForIndex.get(name);
//			if (val.intValue()>0) {
				restrictedParties.add(pa);
//			}
		}
		connections = new JTable (new TableData(restrictedParties));
		connectionsPane = new JScrollPane(connections);
		connectionsPane.setPreferredSize(new Dimension((connections.getColumnCount()+1)*50,connections.getRowHeight()*(connections.getRowCount()+2)));
		panel.add(connectionsPane,c);
		return panel;
	}

	protected Object computeIndex() {
System.out.println("myserson: compute index");
		String result = (String)super.computeIndex();
		return result;
	}

	public Object recallParameters() {
//System.out.println("myerson: define table to return");
//		JPanel fullpanel = new JPanel(new GridLayout(2,1));
//		fullpanel.add(connectionsPane);
//		return fullpanel;
		String o = new String();
		int cols = connections.getColumnCount();
		int rows = connections.getRowCount();
		o+="<table border=\"1\">";
		// headers
		o+="<tr>";
		for (int i=0;i<cols;++i) {
			o+="<th>"+connections.getColumnName(i)+"</th>";
		}
		o+="</tr>";
		// contents
		for (int r=0;r<rows;++r) {
			o+="<tr>";
			for (int c=0;c<cols;++c) {
				if (c==0) {
					o+="<th>";
				} else {
					o+="<td align=\"center\">";
				}
				Object val = connections.getValueAt(r,c);
				if (val instanceof Boolean) {
					if (((Boolean)val).booleanValue()==true) {
						o+="x";
					} else {
						o+=" ";
					}
				} else {
					o+=val.toString();
				}
				if (c==0) {
					o+="</th>";
				} else {
					o+="</td>";
				}
			}
			o+="</tr>";
		}
		o+="</table>";
		return o;
	}
	
    protected HashMap <String,Integer> getValueFunction() {
System.out.println("CONNECTIONS "+connections);
        //System.out.println("got array of parties");
        HashMap <Integer,Integer> forMyerson = new HashMap<Integer,Integer>();
        // System.out.println("created forMyerson");
        // System.out.println("got seats in parliament");
        // System.out.println("got its size nbparties= "+nbParties);		
        // get the value at which majority is obtained
        // System.out.println("majorityLevel "+ majorityLevel);

        sizeForIndex = 0;
        Iterator <Party> p = arrayOfParties.iterator();
        restrictedParties = new ArrayList<Party>();
        while (p.hasNext()) {
            Party pa = p.next();
            Integer name = new Integer(pa.getName());
            Integer val = mapForIndex.get(name);
System.out.println("seats of party: "+val.intValue());
            sizeForIndex+=val.intValue();
            if (val.intValue()>0) {
                restrictedParties.add(pa);
            }
        }
        nbParties = restrictedParties.size();
        // System.out.println("Array of parties "+ arrayOfParties.toString());
        // System.out.println("restricted parties "+ restrictedParties.toString());
        int majorityForIndex = (int)(sizeForIndex*majorityLevel/100);
System.out.println("sizeForIndex "+sizeForIndex+" majorityLevel "+majorityLevel);
        if (majorityLevel==50) {
            ++majorityForIndex;
        }
System.out.println("majority in seats in getValueFunction Myerson: "+majorityForIndex);


        HashMap<String,Integer>vf = new HashMap<String,Integer>();
        // put the "null" set in vf
        vf.put("",new Integer(0));
        // then get all the sets
        for (int i=1;i<=nbParties; ++i) {
// System.out.println("get combo");
            Combo ci = new Combo();
        //System.out.println("done");
            String res = ci.outputAllCombinations ( nbParties, i );
        // System.out.println("nbParties "+nbParties);
        // System.out.println("risultato combo: "+res);
            String[] arrayRes = res.split(";");
//System.out.println("arrayRes "+arrayRes.toString());
            for (int j=0;j<arrayRes.length;++j) {
// System.out.println("j = "+j);
                String c = arrayRes[j];
System.out.println("c = "+c);
                String[] pn = c.split(",");
                // for each element in pn, need to see if there is a connection in connection table
                // (all boxes checked), otherwise, compute the value using the sum of existing values for
                // individual elements, or a mix.
                String listParties = new String();	// index used later in valueFunction map
                String in = new String(); // comma separated list of indexes of parties which are in a connection
                String out = new String(); // comma separated list of indexes of parties which are NOT in a connection
                int valAll=0;// sum of values of all parties considered
                String listOut = "";
                for (int ro = 0;ro<=pn.length -1 ;++ro) {
                    String row = pn[ro].replace("*","");	// row of the table to read the connections
System.out.println("row "+row);
                    int index = (new Integer(row)).intValue();
                    Party pa = restrictedParties.get(index-1);
                    int name = pa.getName();
                    listParties += "*"+name + "*,";
// System.out.println("so listParties becomes: "+listParties);
                    valAll += ((mapForIndex.get(name)).intValue()>majorityForIndex) ? 1 : 0;
// System.out.println("and vallAll becomes "+valAll);
                    if (pn.length>1) {
                        for (int k= ro+1;k<pn.length;++k) {
                            String el = pn[k].replace("*","");
// System.out.println("el "+el);
                            int rintable = (new Integer(row)).intValue()-1;
                            int cintable = (new Integer(el)).intValue() ;
                            int valInTable = (((Boolean)connections.getValueAt(rintable,cintable)).booleanValue()) ? 1 : 0; // read the connections: if true, add el to "in", if false, add el to "out"
// System.out.println("value at row "+rintable+" and column "+cintable+" is "+valInTable);
                            if (valInTable>0) {
// System.out.println("add row and el to in, checking that it does not already contain the element el or row");
                                
                                if (in.length()==0) {
                                    in+=row;
                                    in+=","+el;
                                } else {
                                    in += (isIn(row,in)==false) ? ","+row : "";
                                    in += (isIn(el,in)==false) ? ","+el : "";
                                }
                            }
                        }
                    }
                }
// System.out.println("add parties that are not in 'in' to 'out'");
                // get parties that are not connected in "out". 
                // listout: if such a list has already been found, get its value from value function
                // instead of recomputing it.
                for (int ro = 0;ro<=pn.length -1 ;++ro) {
                    String row = pn[ro].replace("*","");
                    if (isIn(row,in)==false) {
                       if (out.length()==0) {
                            out+=row;
                            listOut += "*"+row+"*";
                        } else {
                            out+=","+row;
                            listOut += ",*"+row+"*";
                        }
                    }
                
                 }
System.out.println("at the end, in is "+in);
System.out.println("at the end, out is "+out);
                // now get the value according to the contents of "in" and "out"
                // if "in" is empty, get the value for each party and sum
                // (note that in all cases, the value is (seats in parliament > majority) ? 1 : 0 )
                // if "out" is empty, get the value for the set of parties
                // else, get the V1 = value for the set of parties in "in" + value for each party in "out"
                // and V2 = sum of the values for each party in the set, the value we want is the max of V1 and V2
                int val = 0;
                // sum of seats for the parties in "in"
                int valIn=0;
                if (in.length()>0) {
    System.out.println("sum elements in in");
                    int sum = 0;
                    String[] sin = in.split(",");
                    for (int ii=0;ii<sin.length;++ii) {
                        Party pa = restrictedParties.get((new Integer(sin[ii])).intValue()-1);
                        int name = pa.getName();
                        sum += (mapForIndex.get(name)).intValue();
    System.out.println("with party "+name+" sum becomes "+sum+" majority is "+majorityForIndex);
                    }
                    valIn = (sum>=majorityForIndex) ? 1 : 0;
    System.out.println("so valIn is "+valIn);
                }
                // sum of seats for the parties in "out"
                int valOut=0;
                if (out.length()>0) {
    System.out.println("get valOut as sum of value of elements in out");
                    if (vf.containsKey(listOut)) {
                        valOut = (vf.get(listOut)).intValue();
                    } else {
                        valOut=0;
                        String[] sout = out.split(",");
                        for (int io=0;io<sout.length;++io) {
                            Party pa = restrictedParties.get((new Integer(sout[io])).intValue()-1);
                            int name = pa.getName();
                            valOut+= (((mapForIndex.get(name)).intValue())>=majorityForIndex) ? 1:0;
                        }
                    }
    System.out.println("so valOut is "+valOut);
                }
                listParties = listParties.substring(0,listParties.length()-1);
                // get val
                if (in.length()==0) {
    System.out.println("nothing in in, so put valAll : "+listParties+" -> "+ valAll);
                    vf.put(listParties, new Integer(valAll));			
                } else if (out.length()==0) {
    System.out.println("nothing in out, so put valIn : "+listParties+" -> "+ valIn);
                    vf.put(listParties, new Integer(valIn));
                } else {
                    int valIO = valIn + valOut;
    System.out.println("else get valIO = "+valIO);
                    if (valIO>valAll) {
    System.out.println("put valIO: "+listParties+" -> "+ valIO);
                        vf.put(listParties,new Integer(valIO));
                    } else {
    System.out.println("put valAll: "+listParties+" -> "+ valAll);
                        vf.put(listParties, new Integer(valAll));			
                    }
                }
            }


        }
System.out.println("Myerson value function: "+vf.toString());
        return vf;
    }

    private boolean isIn (String val, String set) {
       String[] vect = set.split(",");
       boolean ii = false;
        for (int ai=0;ai<vect.length;++ai) {
            if (vect[ai].equals(val)) {
                ii = true;
                break;
            }
        }
        return ii;    
    }
    
	class TableData extends AbstractTableModel {
		String[] parties;
		Object[][] vector;
		public TableData(ArrayList <Party> arrayOfParties) {
			parties = new String[arrayOfParties.size()+1];
			// names of columns = names of all parties
			parties[0] = language.getString("labels","party");
			vector = new Object[arrayOfParties.size()][arrayOfParties.size()+1];
			for (int p=0; p<arrayOfParties.size(); ++p) {
				Party party = arrayOfParties.get(p);
				parties[p+1] = party.getNameParty();
			}
			for (int i=0;i<arrayOfParties.size();++i) {
				Party partyr = arrayOfParties.get(i);
				vector[i][0] = partyr.getNameParty();
				for (int p=0; p<arrayOfParties.size(); ++p) {
					vector[i][p+1] = new Boolean(false);
					if (i==p) {
						vector[i][p+1] = new Boolean(true);
					}
				}
			}
		}	

		
		public int getColumnCount() {
			return parties.length;
		}
	
		public int getRowCount() {
			return vector.length;
		}
	
		public String getColumnName(int col) {
			return parties[col];
		}

		public boolean isCellEditable(int r,int c) {
			if ((c>0) && (c!=(r+1))) {
				return true;
			} else {
				return false;
			}
		}
		
		public Object getValueAt(int r,int c) {
//System.out.println("r = "+r+" c = "+c);
			return vector[r][c];
		}

		public void setValueAt(Object val,int r,int c) {
			if (val instanceof Boolean) {
				boolean bval = ((Boolean)val).booleanValue();
				vector[r][c] = new Boolean(bval);
				fireTableCellUpdated(r,c);
				vector[c-1][r+1] = new Boolean(bval);
				fireTableCellUpdated(c-1,r+1);
			} else {
				vector[r][c] = val;
//				fireTableCellUpdated(r,c);
			}
		}
		
		public Class getColumnClass(int c) {
			return getValueAt(0,c).getClass();
		}

	
	}
}