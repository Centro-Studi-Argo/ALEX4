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

public class GovernabilityIndex3 extends GovernabilityIndex2//// implements ParliamentEventListener
	{
	// global variables for the class
	double a=0;
	int P=0;
	MainFrame mainFrame;
	DecimalField aField;
	int SP = 0;
//	double majorityLevel;
	// constructor
	public GovernabilityIndex3(Parliament parl) {
		super();
System.out.println("CONSTRUCTING GOVERNABILITY 3");
		parliament=parl;
		majorityLevel = parl.getMajorityLevel();
		mainFrame = MainFrame.getInstance();
		indexValue = computeIndex();
System.out.println("indexValue computed - end of constructor");
		// register an observer for parliament
//		parl.addObserver(this);
	}

	/* How index is computed:
	 * g = (S/N).(N/P)^a
	 * where
	 * S = share of seats for the parties in government
	 * N = number of parties in government for the current system
	 * P = number of parties in government for the one-district proportional to obtain the same share of seats as in the current system
	 * a = user-defined parameter, taking values from 0 to 1
	 * 
	 * 
	 * 
	*/

	protected Object computeIndex() {
System.out.println("GOVERNABILITY INDEX III");
		boolean majorityFound = parliament.getMajorityFound();
		int majority=0;
System.out.println("majorityFound "+majorityFound);
		if (majorityFound == true) {
			super.computeIndex();
			Double SoverN = super.getIndexVal();
System.out.println("soverN= "+SoverN);
System.out.println("size of parliament "+sizeOfParliament+" majorityLevel "+majorityLevel);
			// check whether the parties in government would have the majority in parliament with the proportional system
			majority = (sizeOfParliament*(int)majorityLevel/100);
			if (majorityLevel==50) {
				++majority;
			};
System.out.println("majority is "+majority);
		  	HashMap <Integer,Integer>proportionalAllocationOfSeats = parliament.getProportionalAllocationOfSeats();
			Iterator <Integer>psp = government.iterator();
			SP=0;
			while (psp.hasNext()) {
				Integer party = psp.next();
				Integer seats = proportionalAllocationOfSeats.get(party);
				SP+=seats.intValue();
			}
System.out.println("got SP = "+SP+" majority is "+majority);
			// define panel to ask for "a" parameter, and if necessary, government
			final JDialog dialog = new JDialog(mainFrame,true);
System.out.println("initialise dialog");
			JPanel panel = new JPanel();
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
			panel.setLayout(gridbag);
			c.gridx = 0;
			c.gridy = 0;
			JLabel title = new TitleLabel(language.getString("indexes","GovernabilityIndex3"));
			panel.add(title,c);
			++c.gridy;
			JLabel aparameter = new JLabel(language.getString("labels","aParameterXgovernability"));
			panel.add(aparameter,c);
			NumberFormat proportionFormat = NumberFormat.getNumberInstance();
			proportionFormat.setMinimumFractionDigits(0);
			proportionFormat.setMaximumFractionDigits(2);
			aField = new DecimalField(a,4,proportionFormat);
			++c.gridy;
			panel.add(aField,c);
			if (SP>=majority) {
System.out.println("SP>majority");
				P=government.size();
			} else {
System.out.println("no, show dialog");
				P=government.size();
				++c.gridy;
				JLabel gvtText = new JLabel(language.getString("labels","proportionalGovernmentXgovernability"));
				panel.add(gvtText,c);
				// create partity boxes
				final JLabel GmajorityField = new JLabel();
				ArrayList <Party>arrayOfParties = parliament.getArrayOfParties();
				for(int i = 0;i<proportionalAllocationOfSeats.size();++i) {
					Integer partyName = new Integer(i+1);
					Party party = (Party)arrayOfParties.get(i);
					String nameParty = party.getNameParty();
					int nbSeats = (proportionalAllocationOfSeats.get(partyName)).intValue();
					if (nbSeats>0) {
					++c.gridy;
					boolean isSelected = (government.contains(partyName));
					JCheckBox checkbox = new JCheckBox(nameParty,isSelected);
					if (isSelected) {
						checkbox.setEnabled(false);
					} else {
						checkbox.addActionListener(checkGvt(partyName,nbSeats,GmajorityField));
					}
					panel.add(checkbox,c);
					}
				}
				++c.gridy;
				panel.add(new JLabel(language.getString("labels","majority")),c);
				++c.gridx;
				GmajorityField.setText(language.getString("labels","majorityNotFound"));
				panel.add(GmajorityField,c);
				
			}
			++c.gridy;
			Action OK = new AbstractAction(language.getString("labels","ok")) {
				public void actionPerformed(ActionEvent e) {
					// check that "a" is between 0 and 1
					double newa = aField.getValue();
					if ((newa<0) || (newa>1)) {
						JOptionPane.showMessageDialog(null,language.getString("labels","aErrorMessage"));
					} else {
						a = newa;
						dialog.setVisible(false);
					}
				}
			};
			panel.add(new JButton(OK),c);

			dialog.getContentPane().add(panel);
			dialog.setLocation(mainFrame.getX()+mainFrame.WIDTH/2,mainFrame.getY()+mainFrame.HEIGHT/2);
			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			dialog.pack();
			dialog.setVisible(true);
System.out.println("S ("+S+") over N ("+N+"): "+SoverN+" N ("+N+") over P ("+P+"): "+((double)N/P)+" to the power of a="+a+" yields "+Math.pow(((double)N/P),a));
			Double indexValue = new Double (SoverN.doubleValue() * Math.pow(((double)N/P),a));
System.out.println("indexValue is "+indexValue);	
			parliament.setIndexValue(government,indexValue,"GovernabilityIndex3");
System.out.println("indexvalue set for parliament");
			DecimalFormat df = new DecimalFormat("##.####");
			return (df.format(indexValue));
//			return indexValue;
		} else {
			return new String(language.getString("labels","defaultGovernabilityLabel"));
		}
	}


	// action that listens to whether the majority is found
	// and creates the governement list
//	Action checkGvt (final Integer partyName,final int nbSeats,final JTextField GmajorityField) {
	Action checkGvt (final Integer partyName,final int nbSeats,final JLabel GmajorityField) {
		Action action= new AbstractAction (partyName.toString()) {
			public void actionPerformed(ActionEvent e) {
System.out.println("doing checkGvt");
				int majority=(int)(parliament.getSizeOfParliament()*majorityLevel/100);
				if (majorityLevel==50) {
					++majority;
				};
//				LinkedList <Integer> gvtG3 = (LinkedList)(parliament.getCurrentGovernment()).clone();
				LinkedList <Integer> gvtG3 = new LinkedList <Integer> ();
				Iterator <Integer> gvi = (parliament.getCurrentGovernment()).iterator();
				while (gvi.hasNext()) {
					Integer gve = gvi.next();
					gvtG3.add(gve);
				}
				boolean GmajorityFound;
            	JCheckBox cb = (JCheckBox)e.getSource();
				boolean isSelected = cb.isSelected();
System.out.println("party: "+partyName.toString()+" selected "+isSelected+" currentGovernment "+gvtG3.toString());			
				if (isSelected) {
					gvtG3.add(partyName);
					SP+=nbSeats;
					GmajorityFound = (SP>=majority) ? true : false;
//					P = gvtG3.size();
					++P;
				} else {
					gvtG3.remove(partyName);
					SP-=nbSeats;
					GmajorityFound = (SP>=majority) ? true : false;
//					P = gvtG3.size();
					--P;
				}
System.out.println("size of gvtG3 "+gvtG3.size()+" value of P "+P);
				GmajorityField.setText((GmajorityFound==true) ? language.getString("labels","majorityFound") : language.getString("labels","majorityNotFound"));
			}
		};
		return action;
	}


////////	// methods for events
////////	public void addParliamentEventListener(ParliamentEventListener listener) {
////////		listenerList.add(ParliamentEventListener.class,listener);
////////	}
////////
////////	public void removeParliamentEventListener(ParliamentEventListener listener) {
////////		listenerList.remove(ParliamentEventListener.class,listener);
////////	}
////////
////////	public void fireParliamentEvent(ParliamentEvent evt) {
////////		Object[] listeners = listenerList.getListenerList();
////////		for (int i = 0 ; i < listeners.length ; i+=2) {
////////			if (listeners[i] == ParliamentEventListener.class) {
////////				((ParliamentEventListener)listeners[i+1]).governmentHasChanged(evt);
////////			}
////////		}
////////	}
////////
//////// 	// update parties in government, set changed and notify the observers
////////	// if majorityFound is true, save the government in repository?
////////	public void updateParliament(LinkedList government,boolean majority,int sumOfVotes) {
////////		currentGovernment = government;
////////		majorityFound = majority;
////////		sumOfGovernmentVotes = sumOfVotes;
////////System.out.println("governments"+governments);
////////		// if a majority is found, add the currentGovernment in the map Governments as key
////////		// with sumOfgovernmentVotes as values (first element in a list)
////////		if (majorityFound == true) {
////////System.out.println("majority is found");
////////			if (!governments.containsKey(currentGovernment)) {
////////System.out.println("government "+currentGovernment+" does not exist yet, so save, sumOfGovernmentVotes = "+sumOfGovernmentVotes);
////////				LinkedList values = new LinkedList();
////////				values.add("sumOfGovernmentVotes="+(new Integer(sumOfGovernmentVotes)).toString());
////////				values.add("RepresentationIndex="+reprIndex.toString());
////////				governments.put(currentGovernment.toString(),values);
////////			}
////////System.out.println("there are "+currentGovernment.size()+" parties in the government, notify changes");
////////System.out.println("governments (after)"+governments);
////////		}
////////		fireParliamentEvent(new ParliamentEvent(this));
//////////		setChanged();
//////////		notifyObservers();
////////	}
////////

	
}// end of class

