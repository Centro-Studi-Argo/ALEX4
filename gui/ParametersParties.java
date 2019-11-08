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
import java.io.*;

import globals.*;
import classesWrittenByOthers.*;
import votingObjects.*;
import actions.*;
import com.tomtessier.scrollabledesktop.*;

public class ParametersParties
	{
	private int numberParties;
	private WholeNumberField partyField;
	private HashMap <String,Object> generalParameters;
	private ArrayList <Party> parties;
	private double sumOfShares;
	private double oldSumOfShares;
	MainFrame mainFrame;
	PanelPartyDetails lastPanel;
	Language language = Language.getInstance();
	
	public ParametersParties () {
		super();
		mainFrame = MainFrame.getInstance();
	}

	public JPanel setupPartyParameters(HashMap <String,Object> generalParameters) {

		this.generalParameters=generalParameters;
		numberParties = ((Integer)generalParameters.get("numberParties")).intValue();
		parties = new ArrayList <Party> (numberParties);
		// general panel
		int nbcol=3;
		JPanel panel= new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		// warnings
		// initial warning
		JLabel warning=new JLabel(language.getString("messages","fieldsWarning"));
		warning.setForeground(Color.red);
		warning.setFont(new Font("SansSerif",Font.BOLD,12));
		JPanel warningPanel=new JPanel();
		warningPanel.setLayout(new GridLayout(0, 1));
		warningPanel.add(warning);
		c.gridy = 0;
		c.gridwidth=nbcol;
		gridbag.setConstraints(warningPanel,c);
		panel.add(warningPanel);
		c.gridwidth = 1;
		// for each political party, create a label, and the set of options
		// and add it to a general panel (gridx corresponds to i)
		int i;
		sumOfShares = 0;
		Party party;
		for (i=1;i<=numberParties;++i) {
			double initialShare;
			String nameParty=new String(language.getString("labels","party")+" "+i);
			if (i<numberParties) {
				initialShare=0;
				party=new Party(nameParty,generalParameters,initialShare,false,this);
				sumOfShares+=initialShare;
			} else {
				initialShare=100-sumOfShares;
				party=new Party(nameParty,generalParameters,initialShare,true,this);
			}
			// set also the "name" of the party to the int i
			party.setName(i);
			// for the beginning, set oldSumOfShares to sumOfShares
			// note that sumOfShares and oldSumOfShares do not contain the value of the
			// initialShare of the last party
			oldSumOfShares = sumOfShares;
			PanelPartyDetails partPanel = new PanelPartyDetails(this,party,party.isLast(),generalParameters);
			// save party in the list
			parties.add(i-1,party);
			lastPanel = (party.isLast()==true) ? partPanel : null;
			// add panel to general panel
			// use 3 columns, so find the correct values of gridy and gridx so that
			// the windows are in the correct order (smallest party index to largest,
			// from left to right and top to bottom).
			c.gridwidth=1;
			double remainder=((double)(i+2)/3);
			int intRem=(i+(2))/3;
			c.gridy=intRem;
			if ((remainder-intRem)<=0.1) {
				c.gridx=0;
			} else if ((remainder-intRem)<=0.4) {
				c.gridx=1;
			} else {
				c.gridx=2;
			}
			gridbag.setConstraints(partPanel,c);
			panel.add(partPanel);
		}
		// now add an unmodificable field showing the sum of the quotes (which should always be 100)
		// return a message if it is not, with the number of points to leave or to add.
		JLabel sumOfSharesLabel=new SubMenuLabel(language.getString("labels","commentSumOfShares"));
		JPanel sumPanel=new JPanel();
        sumPanel.setLayout(new GridLayout(0, 1));
		sumPanel.add(sumOfSharesLabel,BorderLayout.CENTER);
		c.gridy=numberParties+1;
		c.gridx=0;
		c.gridwidth=nbcol;
		gridbag.setConstraints(sumPanel,c);
		panel.add(sumPanel);

		// detail on parties
		Object[] argLabel = new Object[] {new Integer(numberParties)};
		JLabel detailsLabel=new JLabel(MessageFormat.format(language.getString("labels","commentParties"),argLabel));
		JPanel detailsPanel=new JPanel();
        detailsPanel.setLayout(new GridLayout(0, 1));
		detailsPanel.add(detailsLabel,BorderLayout.CENTER);
		c.gridy=numberParties+2;
		c.gridx=0;
		c.gridwidth=nbcol;
		gridbag.setConstraints(detailsPanel,c);
		panel.add(detailsPanel);
		
		// add button
		// create button
		JButton button = new JButton(language.getString("messages","ok"));
		button.addActionListener (new ParametersPartiesActions(generalParameters,parties));
		c.gridy=(numberParties+3);
		c.gridx=0;
		c.gridwidth=nbcol;
		gridbag.setConstraints(button,c);
		panel.add(button);

		return panel;
	}

	void updateSumOfShares(final double oldShare,double newShare) {
		oldSumOfShares=sumOfShares;
		sumOfShares+=(newShare-oldShare);
	}
	
	double getOldSumOfShares() {
		return oldSumOfShares;
	}
	
	double getNewSumOfShares() {
		return sumOfShares;
	}

	void setSumOfShares(double currentQuote,double currentOldQuote) {
		sumOfShares=currentQuote;
		oldSumOfShares=currentOldQuote;
	}
	
	public Party getLastParty() {
		return (Party)parties.get(numberParties-1);
	}

	public ArrayList <Party> getParties() {
		return parties;
	}
	
	public PanelPartyDetails getLastPanel() {
	return lastPanel;
	}

}
