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

public class ParametersPartiesActions implements ActionListener
	{
	private int numberParties,numberVoters,totalNumberVoters,numberUninominalDistricts,numberCandidates;
	private double probFirst,probSecond,probPreferred;
	private HashMap <String,Object> generalParameters;
	private ArrayList <Party> arrayOfParties;
	private JProgressBar progressBar;
	private javax.swing.Timer timer;
	private JButton progressButton;
	private JPanel progressPanel;
	private DecimalFormat form=new DecimalFormat("###0.##");
	MainFrame mainFrame;
	Language language = Language.getInstance();

	public ParametersPartiesActions (HashMap <String,Object> parameters,ArrayList <Party> partiti) {
		super();
		mainFrame=MainFrame.getInstance();
		generalParameters=parameters;
		
		totalNumberVoters=((Integer)generalParameters.get("totalNumberVoters")).intValue();
		numberVoters=((Integer)generalParameters.get("numberVoters")).intValue();
		numberCandidates=((Integer)generalParameters.get("numberCandidates")).intValue();
		numberUninominalDistricts=((Integer)generalParameters.get("numberUninominalDistricts")).intValue();
		numberParties=((Integer)generalParameters.get("numberParties")).intValue();
		probFirst=((Double)generalParameters.get("probFirst")).doubleValue();
		probSecond=((Double)generalParameters.get("probSecond")).doubleValue();
		probPreferred=((Double)generalParameters.get("probPreferred")).doubleValue();

		arrayOfParties=partiti;

	}

    public void actionPerformed(ActionEvent event) {
		// values to put on the buttons
		Object[] names=new Object[3];
		names[0]=language.getString("labels","toCreationOfObjects");
		names[1]=language.getString("labels","backToStart");
		names[2]=language.getString("labels","backToParties");
	
	
		// create a confirm dialog with all the values of the parameters
		String[] showOrder={"numberVoters","numberUninominalDistricts","numberCandidates","numberParties","probFirst","probSecond","probPreferred"};
		JPanel panelConfirm = new JPanel();
		panelConfirm.setLayout(new GridLayout(0,1));
		panelConfirm.add(new JLabel(language.getString("labels","headingConfirmGeneralParameters")+"\n"));
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
			panelConfirm.add(new SubMenuLabel(MessageFormat.format(language.getString("labels","bodyConfirmGeneralParameters"),argConfirm)+"\n"));
		}
		if (numberCandidates==1) {
			panelConfirm.add(new JLabel(language.getString("labels","warningPlurinominal")+"\n\n\n"));
		}
		
		for (int i=0;i<numberParties;++i) {
			Party party=arrayOfParties.get(i);
			double share=party.getShare();
			boolean major=party.getMajor();
			int concentration=party.getConcentration();
			double coefficient=party.getCoefficient();
			panelConfirm.add(new JLabel(language.getString("labels","party")+" "+(i+1)+"\n"));
			panelConfirm.add(new JLabel(language.getString("labels","nameParty")+" = "+party.getNameParty()+"\n"));
			panelConfirm.add(new JLabel(language.getString("labels","distance")+" = "+party.getDistance()+"\n"));
			String majorString;
			if (major==true) {
				majorString = language.getString("labels","andIsMajor")+".";
			} else {
				majorString = language.getString("labels","andIsNotMajor")+".";
			}
			Object[] argConfirm = new Object[] {new Double(share),majorString,new Integer(concentration),new Double(coefficient)};
			panelConfirm.add(new SubMenuLabel(MessageFormat.format(language.getString("labels","confirmPartyParameters"),argConfirm)+"\n"));
		}
	
		JScrollPane scrollbar=new JScrollPane();
		scrollbar.setViewportView(panelConfirm);
		Dimension dim=mainFrame.getContentPane().getPreferredSize();
		scrollbar.setPreferredSize(new Dimension((int)((dim.getWidth())*0.5),(int)((dim.getHeight())*0.5)));
	
		final JScrollableDesktopPane desktop=mainFrame.getScrollableDesktop();//(JScrollableDesktopPane)mainFrame.getContentPane();
		final JInternalFrame intFrame=desktop.getSelectedFrame();
	
		int n=JOptionPane.showOptionDialog(intFrame,scrollbar,language.getString("labels","titleConfirmPartyParameters"),JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE,null,names,names[0]);
	
			
		// if Back to parties: do nothing
		// if back to beginning: show general parameters panel
		// of ok: close parties frame and go on 
		if (n==1) {	 // back to beginning
			intFrame.dispose();
			ParametersGeneral generalParam = new ParametersGeneral (mainFrame);
			JPanel generalParametersPanel = generalParam.setupGeneralParameters(generalParameters);
			desktop.add(language.getString("labels","generalParameters"),generalParametersPanel);
		} else if (n==0) { // all OK, simulate preferences, colleges, etc
			// display gui for creation of voting objects, in separate thread as may take time and we need to
			// see the panel before the end of the creation of objects (shows progress frame)
			intFrame.dispose();
			LinkedList <String> what = new LinkedList<String>();
			what.add("all");
			CreationVotingObjects creationVotingObjects= new CreationVotingObjects(generalParameters,arrayOfParties,what,null);
			creationVotingObjects.showProgressInCreation();
		}
	}
}