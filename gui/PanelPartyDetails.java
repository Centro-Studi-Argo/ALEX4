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
package gui;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Toolkit;
import java.text.*;

import globals.*;
import classesWrittenByOthers.*;
import votingObjects.*;
import actions.*;
import com.tomtessier.scrollabledesktop.*;

public class PanelPartyDetails extends JPanel {
	/*
	 * variables set in constructor
	 */
	private ParametersParties parametersParties;
	private Party party;
	private boolean last;
	Language language = Language.getInstance();
	int numberUninominalDistricts;
	int numberVotersPerDistrict;

	/*
	 * objects used to create the panel
	 */

	private TextField namePartyField;
	private JLabel namePartyLabel;
	private String nameParty;

	private DecimalField shareField;
	private JLabel shareLabel;
	private double share;

	private WholeNumberField concentrationField;
	private JLabel concentrationLabel;
	private int concentration=0;
	private JLabel detLabel;
	private JLabel detField;
	private DecimalField coefficientField;
	private JLabel coefficientLabel;
	private double coefficient=1;
	private DecimalField distanceField;
	private JLabel distanceLabel;
	private double distance=0;
	
	private JCheckBox majorField;
	private JLabel majorLabel;
	private boolean major=false;

	private ArrayList <Party> parties;

	public PanelPartyDetails (final ParametersParties parametersParties, final Party party, boolean last,final HashMap <String,Object> generalParameters) {
		super();
		this.parametersParties = parametersParties;
		this.party = party;
		this.last = last;
		numberUninominalDistricts=((Integer)generalParameters.get("numberUninominalDistricts")).intValue();
		numberVotersPerDistrict=((Integer)generalParameters.get("numberVoters")).intValue();

		parties = parametersParties.getParties();

		// for the name
		namePartyField = new TextField(party.getNameParty());
		namePartyLabel = new JLabel(language.getString("labels","nameParty"));
		namePartyLabel.setToolTipText(language.getString("labels","namePartyToolTip"));
		namePartyField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String namePartyValue = namePartyField.getText();
				party.setNameParty(namePartyValue);
			}
		});
		
		// for the share of votes
		NumberFormat proportionFormat=NumberFormat.getNumberInstance();
		proportionFormat.setMinimumFractionDigits(0);
		proportionFormat.setMaximumFractionDigits(2);
		shareField=new DecimalField(party.getShare(),4,proportionFormat);
		shareField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				double newShare = shareField.getValue();
				int totalVoters = ((Integer)generalParameters.get("totalNumberVoters")).intValue();
				int votes =  (int)(newShare*totalVoters)/100;
				
				int val1 = (int)(party.getCoefficient()*votes/numberUninominalDistricts);
				int val2 = val1*party.getConcentration();
				if (newShare<0) {
System.out.println("1");
					JOptionPane.showMessageDialog(null,language.getString("labels","negativeShare"));
					shareField.setValue(share);
				} else if (party.getMajor()==true) {
					if (val2>votes) {
System.out.println("2");
						Object[] arguments = new Object[] {new Integer(val1),new Integer(val2),new Integer(votes)};
						JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","notEnoughVoters"),arguments));
						shareField.setValue(share);
					} else if (val1>numberVotersPerDistrict) {
	System.out.println("3 ATTENTION: THE MESSAGE IS WRONG: THE PB IS NOT MAXIMUM SHARE, RATHER SOMETHING ALONG NOT ENOUGH VOTERS OR MAXIMUM COEFFICIENT");
						Object[] arguments = new Object[] {new Double((double)numberVotersPerDistrict/party.getCoefficient())};
						JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","maximumShare"),arguments));
						shareField.setValue(share);
						
					}
				} else {
System.out.println("4");
					// save the current values of sumOfShares and oldSumOfShares in case the new value
					// entered is not acceptable
					double currentOldSumOfShares = parametersParties.getOldSumOfShares();
					double currentSumOfShares = parametersParties.getNewSumOfShares();
					// update sumOfShares and oldSumOfSharess with the new value of the quote entered
					parametersParties.updateSumOfShares(share,newShare);
					double oldSumOfShares = parametersParties.getOldSumOfShares();
					double newSumOfShares = parametersParties.getNewSumOfShares();
					// if the sum has changed, find the value to put in the field for the last party
					// in case this value is negative (sum of quotes greater than 100),
					// show an error message, reset the share value in the field for the new party
					// to its old value, and reset the values for sumOfShares and oldSumOfShares
					// in caratteristiche partiti too.
					if (oldSumOfShares != newSumOfShares) {
						double value = 100-newSumOfShares;
						if ((value >= 0)&&(value <= 100)) {
							((parametersParties.getLastPanel()).getShareField()).setValue(100-newSumOfShares);
							(parametersParties.getLastParty()).setShare(100-newSumOfShares);
						} else {
System.out.println("5");
							Object[] arguments = new Object[] {new Double(newShare),party.getNameParty(),new Double(Math.abs(value))};
							JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","sumOfShares"),arguments));
							shareField.setValue(share);
							newShare = share;
							parametersParties.setSumOfShares(currentSumOfShares,currentOldSumOfShares);
						}
						// all is fine, set share to its new value.for the party
						party.setShare(newShare);
						shareField.setValue(newShare);
						share = newShare;
					}
				}
			}
		});

		if (last==false) { 
			shareLabel = new SubMenuLabel(language.getString("labels","share"));
			shareLabel.setToolTipText(language.getString("labels","shareToolTip"));
		} else { // last party, share for last party is not modifiable
			shareLabel = new unSelectedMenuLabel(language.getString("labels","share"));
			shareField.setEnabled(false);
		}

		// for the distance
		distanceField = new DecimalField(distance,4,proportionFormat);
		distanceLabel = new SubMenuLabel(language.getString("labels","distance"));
		distanceLabel.setToolTipText(language.getString("labels","distanceToolTip"));
		distanceField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				double nuovaDistance = distanceField.getValue();
				if (nuovaDistance < 0) {
					JOptionPane.showMessageDialog(null,language.getString("labels","negativeDistance"));
					distanceField.setValue(distance);
				} else if (nuovaDistance > 100){
					JOptionPane.showMessageDialog(null,language.getString("labels","distanceTooLarge"));
					distanceField.setValue(distance);
				} else {
					distanceField.setValue(nuovaDistance);
					party.setDistance(nuovaDistance);
				}
			}
		});

		// for the "major"
		majorLabel = new SubMenuLabel(language.getString("labels","major"));
		majorLabel.setToolTipText(language.getString("labels","majorToolTip"));
		majorField = new JCheckBox(language.getString("labels","majorComment"));
		majorField.setSelected(major);
		// (this is enough as there is only one check box)
		majorField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox)e.getSource();
                major = (boolean)cb.isSelected();

				// enable the fields for concentration and coefficient only if major is true
				if (major == true) {
					detLabel.setForeground(Color.black);
					concentrationField.setEnabled(true);
					concentrationLabel.setForeground(Color.black);
					coefficientField.setEnabled(true);
					coefficientLabel.setForeground(Color.black);
					party.setMajor(true);
				} else {
					detLabel.setForeground(Color.gray);
					concentrationField.setEnabled(false);
					concentrationLabel.setForeground(Color.gray);
					coefficientField.setEnabled(false);
					coefficientLabel.setForeground(Color.gray);
					party.setMajor(false);
				}
             }
	       });
		
		// for the concentration
		concentrationField = new WholeNumberField(0,1);
		concentrationField.setValue(concentration);
		concentrationField.setEnabled(false);
		concentrationLabel = new unSelectedMenuLabel(language.getString("labels","concentration"));
		detLabel = new unSelectedMenuLabel(language.getString("labels","concentrationComment"));
		detField = new unSelectedMenuLabel(" ");
		concentrationField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				int newConcentration=concentrationField.getValue();
				Iterator <Party> ip = parties.iterator();
				int sum=0;
				while (ip.hasNext()) {
					Party p = ip.next();
					if (p.getName()==party.getName()) {
						sum += newConcentration;
					} else {
						sum += p.getConcentration();
					}
				}
				int val1 = (int)(party.getCoefficient()*party.getVotes()/numberUninominalDistricts);
				int val2 = val1*newConcentration;
				
				if (newConcentration < 0) {
					JOptionPane.showMessageDialog(null,language.getString("labels","negativeConcentration"));
					concentrationField.setValue(concentration);
				} else if ((newConcentration > numberUninominalDistricts) || (sum > numberUninominalDistricts)) {
					Object[] arguments = new Object[] {new Integer(numberUninominalDistricts),new Integer(sum)};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","numberDistrictsConcentration"),arguments));
					concentrationField.setValue(concentration);
				} else if (val2>party.getVotes()) {
					Object[] arguments = new Object[] {new Integer(val1),new Integer(val2),new Integer(party.getVotes())};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","notEnoughVoters"),arguments));
					concentrationField.setValue(concentration);
				} else if ((newConcentration>0) && (party.getCoefficient()>1)) {
					if (val1>numberVotersPerDistrict) {
						Object[] arguments = new Object[] {new Double((double)numberVotersPerDistrict/party.getCoefficient())};
						JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","maximumShare"),arguments));
						shareField.setValue(share);
					}
				} else {
					concentrationField.setValue(newConcentration);
					party.setConcentration(newConcentration);
				}
			}
		});

		// for the coefficient
		coefficientField = new DecimalField(coefficient,4,proportionFormat);
		coefficientField.setEnabled(false);
		coefficientLabel = new unSelectedMenuLabel(language.getString("labels","coefficient"));
		coefficientLabel.setToolTipText(language.getString("labels","coefficientToolTip"));
		coefficientField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				double nuovoCoefficient = coefficientField.getValue();
				int val1 = (int)(nuovoCoefficient*party.getVotes()/numberUninominalDistricts);
				int val2 = val1*party.getConcentration();
				if (nuovoCoefficient < 0) {
					JOptionPane.showMessageDialog(null,language.getString("labels","negativeCoefficient"));
					coefficientField.setValue(coefficient);
				} else if ((party.getConcentration() == 0) && (nuovoCoefficient != 1)) {
					JOptionPane.showMessageDialog(null,language.getString("labels","coefficientNoConcentration"));
					coefficientField.setValue(coefficient);
				} else if ((party.getConcentration() > 0)&&(nuovoCoefficient == 1)) {
					JOptionPane.showMessageDialog(null,language.getString("labels","coefficientConcentration"));
					coefficientField.setValue(coefficient);
				} else if (nuovoCoefficient > (100/share)) {
					Object[] arguments = new Object[] {new Double((double)100/share)};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","maximumCoefficient"),arguments));
					coefficientField.setValue(coefficient);
				} else if (val1>numberVotersPerDistrict) {
					Object[] arguments = new Object[] {new Double((double)numberVotersPerDistrict/party.getCoefficient())};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","maximumShare"),arguments));
					shareField.setValue(share);
				} else if (val2>party.getVotes()) {
					Object[] arguments = new Object[] {new Integer(val1),new Integer(val2),new Integer(party.getVotes())};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","notEnoughVoters"),arguments));
					coefficientField.setValue(coefficient);
				} else {
					coefficientField.setValue(nuovoCoefficient);
					party.setCoefficient(nuovoCoefficient);
				}
			}
		});



		
		// create a panel with the data;
		JPanel part = new JPanel();
        part.setLayout(new GridLayout(0, 2));
		part.add(namePartyLabel);
		part.add(namePartyField);
		part.add(shareLabel);
		part.add(shareField);
		part.add(distanceLabel);
		part.add(distanceField);
		part.add(majorLabel);
		part.add(majorField);
		part.add(detLabel);
		part.add(detField);
		part.add(concentrationLabel);
		part.add(concentrationField);
		part.add(coefficientLabel);
		part.add(coefficientField);
		// separate that panel from others and display in the current object which is a panel
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(party.getNameParty()),
			BorderFactory.createEmptyBorder(5,5,5,5)));
		setLayout(new BorderLayout());
		add(part,BorderLayout.CENTER);
	}

	public DecimalField getShareField()
		{
		return shareField;
		}

}