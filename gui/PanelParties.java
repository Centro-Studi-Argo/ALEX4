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
import actions.*;
import com.tomtessier.scrollabledesktop.*;

public class PanelParties extends JPanel {
	private int numberParties;
	private WholeNumberField numberPartiesField;
	
	Language language = Language.getInstance();

	public PanelParties (HashMap <String,Object> generalParameters) {
		super();
		
		if (generalParameters.size()<=0) {
			numberParties=3;
		} else {
			numberParties=((Integer)generalParameters.get("numberParties")).intValue();
		}
		
		// add text box for size the number of political parties
		numberPartiesField=new WholeNumberField(numberParties,8);
		JLabel numberPartiesLabel=new SubMenuLabel(language.getString("labels","numberParties"));
		numberPartiesLabel.setLabelFor(numberPartiesField);
		numberPartiesField.addActionListener(new ActionListener() {
			// look at the values entered in the field and check that they are
			// acceptable. If not, put the last known correct value in the field
			public void actionPerformed(ActionEvent event) {
				int newNumberParties=numberPartiesField.getValue();
				if (newNumberParties<=0) {
					JOptionPane.showMessageDialog(null,language.getString("labels","negativeParties"));
					numberPartiesField.setValue(numberParties);
				} else {
					// set numberParties to its new value
					numberParties=newNumberParties;
				}
			}
		});

		 // panels for parties
        JPanel pop = new JPanel();
        pop.setLayout(new GridLayout(0, 2));
		pop.add(new TitleLabel(language.getString("labels","titlePartiesPanel")));
		pop.add(new JLabel(""));
		pop.add(numberPartiesLabel);
		pop.add(numberPartiesField);
		
		// container for party variables
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		setLayout(new BorderLayout());
		add(pop,BorderLayout.CENTER);
	}

	// methods to pass the variables to the button
	public int getNumberParties() {
		return numberParties;
	}
}