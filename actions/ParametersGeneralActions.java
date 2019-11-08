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

package actions;

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
import gui.*;
import votingObjects.*;
import com.tomtessier.scrollabledesktop.*;


public class ParametersGeneralActions implements ActionListener {
	private PanelPopulation pop;
	private PanelParties part;
	private PanelProbabilities prob;
	private int numberParties,totalNumberVoters,numberUninominalDistricts,numberVoters,numberCandidates;
	private double probFirst,probSecond,probPreferred;
	private HashMap <String,Object> generalParameters=new HashMap<String,Object>();
	MainFrame mainFrame;
	Language language = Language.getInstance();

	public ParametersGeneralActions(PanelPopulation popPanel,PanelParties partPanel,PanelProbabilities probPanel) {
		super();
		 
		pop = popPanel;
		part = partPanel;
		prob = probPanel;
		mainFrame=MainFrame.getInstance();
System.out.println("current locale "+language.getCurrentLocale());
	}

	
    public void actionPerformed(ActionEvent event) {
		// Get current values of the parameters
		probPreferred = prob.getProbPreferred();
		generalParameters.put("probPreferred",new Double(probPreferred));
		probSecond = prob.getProbSecond();
		generalParameters.put("probSecond",new Double(probSecond));
		probFirst = prob.getProbFirst();
		generalParameters.put("probFirst",new Double(probFirst));
		numberParties = part.getNumberParties();
		generalParameters.put("numberParties",new Integer(numberParties));
		numberUninominalDistricts = pop.getNumberUninominalDistricts();
		generalParameters.put("numberUninominalDistricts",new Integer(numberUninominalDistricts));
		numberVoters = pop.getNumberVoters();
		generalParameters.put("numberVoters",new Integer(numberVoters));
		totalNumberVoters = pop.getTotalNumberVoters();
		generalParameters.put("totalNumberVoters",new Integer(totalNumberVoters));
		numberCandidates = pop.getNumberCandidates();
		generalParameters.put("numberCandidates",new Integer(numberCandidates));
		generalParameters.put("numberPlurinominalDistricts",new Integer(numberUninominalDistricts/numberCandidates));
//System.out.println("[param actions] totalNumberVoters "+totalNumberVoters);
//System.out.println("numberUninominalDistricts/numberParties "+(numberUninominalDistricts/numberParties)+" totalNumberVoters/numberUninominalDistricts "+(totalNumberVoters/numberUninominalDistricts));
		// give a warning if both parties and colleges are too numerous
		if (((numberUninominalDistricts/numberParties)<2)&&(totalNumberVoters<10)) {
//System.out.println("all too big");
			JOptionPane.showMessageDialog(mainFrame,language.getString("labels","warningParties")+"\n\n"+language.getString("labels","warningDistricts"));
		}

		// give a warning if the number of colleges is more than 1/10 of the number of voters
		else if (((numberUninominalDistricts/numberParties)>=2)&&(totalNumberVoters<10)) {
			JOptionPane.showMessageDialog(mainFrame,language.getString("labels","warningDistricts"));
		}

		// give a warning if the number of parties is more than 1/2 of the number of collegi
		else if (((numberUninominalDistricts/numberParties)<2)&&(totalNumberVoters>=10)) {
			JOptionPane.showMessageDialog(mainFrame,language.getString("labels","warningParties"));
		}

		// create a confirm dialog with all the values of the parameters
		String[] showOrder={"numberVoters","numberUninominalDistricts","numberCandidates","numberParties","probFirst","probSecond","probPreferred"};
		String testoConferma = new String(language.getString("labels","headingConfirmGeneralParameters"));
		for (int k=0;k<showOrder.length;++k) {
			String key = showOrder[k];
			String value = ((Object) generalParameters.get(key)).toString();
			Object[] argConfirm;
			if (key.substring(0,4).compareTo("prob")==0) {
				argConfirm = new Object[] {language.getString("labels",key)};
				argConfirm = new Object[] {MessageFormat.format(language.getString("labels","probField"),argConfirm),value};
			} else {
				argConfirm = new Object[] {language.getString("labels",key),value};
			}
			// create label: case of prob, need to add something before name
			testoConferma += MessageFormat.format(language.getString("labels","bodyConfirmGeneralParameters"),argConfirm);
		}
		if (numberCandidates==1) {
			testoConferma += language.getString("labels","warningPlurinominal");;
		}
		int n=JOptionPane.showConfirmDialog(mainFrame,
			testoConferma,
			language.getString("labels","titleConfirmGeneralParameters"),
			JOptionPane.YES_NO_OPTION);
		// if yes has been selected, n=0
		if (n==0) {
			// value is confermed: set numberPartiti to the value, and create a pop-up
			// window to set up the characteristics of the parties
			JScrollableDesktopPane desktop=mainFrame.getScrollableDesktop();//(JScrollableDesktopPane)mainFrame.getContentPane();
			JInternalFrame intFrame=desktop.getSelectedFrame();
			intFrame.dispose();

			// now, create a new window with the details of the parties
			ParametersParties partyParam = new ParametersParties();
			JPanel partyPanel = partyParam.setupPartyParameters(generalParameters);
			desktop.add(language.getString("labels","partyParameters"),partyPanel);
		}
			// if no has been selected, n=1

	}


}