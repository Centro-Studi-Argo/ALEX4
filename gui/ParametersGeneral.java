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
import java.lang.ref.*;

import globals.*;
import classesWrittenByOthers.*;
import actions.*;
import com.tomtessier.scrollabledesktop.*;

public class ParametersGeneral {

	PanelPopulation popPanel;
	PanelParties partPanel;
	PanelProbabilities probPanel;

	JFrame mainFrame;
	Language language = Language.getInstance();

	/*
	 * Constructor
	 */
	public ParametersGeneral (JFrame mainFrame) {

		super();

		mainFrame=MainFrame.getInstance();
	}

	/*
	 * ask for the preference parameters.
	 * if confirmed, ask for the party parameters
	 * if confirmed, simulate the preferences of voters for parties and candidates
	 * create the uninominal and plurinominal colleges
	 */
	public JPanel setupGeneralParameters(HashMap <String,Object> generalParameters) {
//System.out.println("size of general parameters "+generalParameters.size());
		if (generalParameters.size()>0) {
//System.out.println("elements in generalParameters");
//Set keys = generalParameters.keySet();
//Iterator i = keys.iterator();
//while (i.hasNext()) {
//	String key=(String)i.next();
//	System.out.println(key+" = "+generalParameters.get(key));
//}
			// the menu has been called from the partitiMenu, so
			// call the panels with the current values
			// create population panel
			popPanel=new PanelPopulation(generalParameters);
			popPanel.addComponents();
	
			// create partiti panel
			partPanel=new PanelParties(generalParameters);
	
			// create probability panel
			probPanel=new PanelProbabilities(generalParameters);
		} else {
			// create panel for the first display of the menu
			// create population panel
//System.out.println("create poppanel");
			popPanel=new PanelPopulation(new HashMap<String,Object>());
			popPanel.addComponents();
	
			// create partiti panel
//System.out.println("create partpanel");
			partPanel=new PanelParties(new HashMap<String,Object>());
	
			// create probability panel
//System.out.println("create probpanel");
			probPanel=new PanelProbabilities(new HashMap<String,Object>());
		}

		// create button
//System.out.println("create buttonpanel");
		JButton button = new JButton(language.getString("messages","ok"));
		button.addActionListener (new ParametersGeneralActions(popPanel,partPanel,probPanel));

		// initial warning
		JLabel warning=new JLabel(language.getString("messages","fieldsWarning"));
		warning.setForeground(Color.red);
		warning.setFont(new Font("SansSerif",Font.BOLD,12));
		JPanel warningPanel=new JPanel();
		warningPanel.setLayout(new GridLayout(0, 1));
		warningPanel.add(warning);

//System.out.println("create mainpanel");
		// grid bag layout and place objects in pane
		GridBagLayout gridbag=new GridBagLayout();
		GridBagConstraints c=new GridBagConstraints();
		JPanel panel=new JPanel();
		panel.setLayout(gridbag);
		c.fill=GridBagConstraints.HORIZONTAL;
		// warning panel
		c.weightx=0.5;
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=2;
//System.out.println("add warning");
		gridbag.setConstraints(warningPanel,c);
		panel.add(warning);
		// population panel
		c.gridwidth=1;
		c.gridx=0;
		c.gridy=1;
//System.out.println("add poppanel");
		gridbag.setConstraints(popPanel,c);
		panel.add(popPanel);
		// parties panel
		c.gridx=0;
		c.gridy=2;
//System.out.println("add partpanel");
		gridbag.setConstraints(partPanel,c);
		panel.add(partPanel);
		// probability panel
		c.gridx=0;
		c.gridy=3;
//System.out.println("add probpanel");
		gridbag.setConstraints(probPanel,c);
		panel.add(probPanel);
		// button
		c.gridx=0;
		c.gridy=4;
		c.gridwidth=2;
//System.out.println("add button");
		gridbag.setConstraints(button,c);
		panel.add(button);

		return panel;
	}
}
	


