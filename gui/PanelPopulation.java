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
import com.tomtessier.scrollabledesktop.*;
import actions.*;

public class PanelPopulation extends JPanel {
	private int numberVoters;
	private WholeNumberField numberVotersField;
	private int totalNumberVoters;// total number of electors
	private int numberUninominalDistricts;
	private WholeNumberField numberDistrictsField;
	private int numberCandidates;// for plurinominali
	private WholeNumberField numberCandidatesField;

	Language language = Language.getInstance();
	
	public PanelPopulation (HashMap <String,Object> generalParameters) {
		super();
		
		if (generalParameters.size()<=0) {
			numberVoters = 100;
			numberUninominalDistricts = 10;
			totalNumberVoters = numberVoters*numberUninominalDistricts;
			numberCandidates = 1; // default is 1, collegi uninominali
		} else {
			numberVoters = ((Integer)generalParameters.get("numberVoters")).intValue();
			numberUninominalDistricts = ((Integer)generalParameters.get("numberUninominalDistricts")).intValue();
			numberCandidates = ((Integer)generalParameters.get("numberCandidates")).intValue();
			totalNumberVoters = ((Integer)generalParameters.get("totalNumberVoters")).intValue();
		}
	}
		
	JPanel addComponents() {
		// add text box for the number of electors
		numberVotersField=new WholeNumberField(numberVoters,1);
		JLabel numberVotersLabel=new SubMenuLabel(language.getString("labels","numberVoters"));
		numberVotersLabel.setToolTipText(language.getString("labels","toolTipVoters"));
		numberVotersLabel.setLabelFor(numberVotersField);
		numberVotersField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int newNumberVoters=numberVotersField.getValue();
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				if (newNumberVoters<=0) {
					JOptionPane.showMessageDialog(null,language.getString("labels","negativeVoters"));
					numberVotersField.setValue(numberVoters);
				} else {
					numberVoters=newNumberVoters;
					totalNumberVoters=numberVoters*numberUninominalDistricts;
				}
			}
		});
		
		// add text box for the number of colleges
		numberDistrictsField=new WholeNumberField(numberUninominalDistricts,1);
		JLabel numberDistrictsLabel=new SubMenuLabel(language.getString("labels","numberUninominalDistricts"));
		numberDistrictsLabel.setLabelFor(numberDistrictsField);
		numberDistrictsLabel.setToolTipText(language.getString("labels","toolTipDistricts"));
		numberDistrictsField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				int newNumberDistricts=numberDistrictsField.getValue();
				if (newNumberDistricts<=0) {
					JOptionPane.showMessageDialog(null,language.getString("labels","negativeDistricts"));
					numberDistrictsField.setValue(numberUninominalDistricts);
				} else {
					numberUninominalDistricts=newNumberDistricts;
					totalNumberVoters=numberVoters*numberUninominalDistricts;
				}
			}
		});
			
		// add text box for the number of candidates per plurinominal college
		numberCandidatesField=new WholeNumberField(numberCandidates,1);
		JLabel numeroCandidatiLabel=new SubMenuLabel(language.getString("labels","numberCandidates"));
		numeroCandidatiLabel.setLabelFor(numberCandidatesField);
		numeroCandidatiLabel.setToolTipText(language.getString("labels","toolTipCandidates"));
		numberCandidatesField.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent event)
				{
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				int newNumberCandidates=numberCandidatesField.getValue();
				int divNumberCandidates=0;
				int rest=0;
				if (newNumberCandidates>=1)
					{
					divNumberCandidates=numberUninominalDistricts/newNumberCandidates;
					rest=numberUninominalDistricts-(divNumberCandidates*newNumberCandidates);
					}
				if (newNumberCandidates<1)
					{
					JOptionPane.showMessageDialog(null,language.getString("labels","negativeCandidates"));
					numberCandidatesField.setValue(numberCandidates);
					}
				else if (rest!=0)
					{
					JOptionPane.showMessageDialog(null,language.getString("labels","messageNumberCandidates"));
					numberCandidatesField.setValue(numberCandidates);
					}
				else
					{
					numberCandidates=newNumberCandidates;
					}
				}
			});


			
		 // panels for electors and colleges
        JPanel pop = new JPanel();
        pop.setLayout(new GridLayout(0, 2));
		pop.add(new TitleLabel(language.getString("labels","titlePopulationPanel")));
		pop.add(new JLabel(""));
		pop.add(numberVotersLabel);
		pop.add(numberVotersField);
		pop.add(numberDistrictsLabel);
		pop.add(numberDistrictsField);
		pop.add(numeroCandidatiLabel);
		pop.add(numberCandidatesField);
		
		// container for general population variables
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		setLayout(new BorderLayout());
		add(pop,BorderLayout.CENTER);
		return pop;
	}

	// methods to pass the variables to the button
	public int getTotalNumberVoters() {
//System.out.println("[pop panel] totalNumberVoters "+totalNumberVoters);
		return totalNumberVoters;
	}

	public int getNumberVoters() {
		return numberVoters;
	}

	public int getNumberUninominalDistricts() {
		return numberUninominalDistricts;
	}

	public int getNumberCandidates() {
		return numberCandidates;
	}
}