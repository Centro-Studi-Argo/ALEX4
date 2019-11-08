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

public class PanelProbabilities extends JPanel {

	private double probFirst;
	private DecimalField probFirstField;
	private double probSecond;
	private DecimalField probSecondField;
	private double probPreferred;
	private DecimalField probPreferredField;
	Object[] argField;
	Language language = Language.getInstance();
	
	public PanelProbabilities(HashMap <String,Object> generalParameters) {
	
		super();

		if (generalParameters.size()<=0) {
			probFirst=0.80;
			probSecond=0.10;
			probPreferred=0.90;
		} else {
			probFirst=((Double)generalParameters.get("probFirst")).doubleValue();
			probSecond=((Double)generalParameters.get("probSecond")).doubleValue();
			probPreferred=((Double)generalParameters.get("probPreferred")).doubleValue();
		}
		
		// add text box for the probability of choosing the first adjacent party
		argField = new Object[] {language.getString("labels","probFirst")};
		String message=MessageFormat.format(language.getString("labels","probField"),argField);
		JLabel probFirstLabel = new SubMenuLabel(message);
		NumberFormat proportionFormat = NumberFormat.getNumberInstance();
		proportionFormat.setMinimumFractionDigits(0);
		proportionFormat.setMaximumFractionDigits(2);
		probFirstField = new DecimalField(probFirst,4,proportionFormat);
		probFirstField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				double newProbFirst = probFirstField.getValue();
				double probSecond = probSecondField.getValue();
				if ((newProbFirst<0)||(newProbFirst>1)) {
					argField = new Object[] {language.getString("labels","probFirst")};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","rangeProbabilities"),argField));
					probFirstField.setValue(probFirst);
				} else if (newProbFirst+probSecond>1) {
					argField = new Object[] {language.getString("labels","probAll")};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","sumProbabilities"),argField));
					probFirstField.setValue(probFirst);
				} else if (newProbFirst<probSecond) {
					argField = new Object[] {language.getString("labels","probSecond"),
								language.getString("labels","smallerThan"),
								language.getString("labels","probFirst")};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","orderProbabilities"),argField));
					probFirstField.setValue(probFirst);
				} else {
					probFirst = newProbFirst;
				}
			}
		});

		// add text box for the probability of choosing the second adjacent party
		argField = new Object[] {language.getString("labels","probSecond")};
		JLabel probSecondLabel = new SubMenuLabel(MessageFormat.format(language.getString("labels","probField"),argField));
		probSecondField = new DecimalField(probSecond,4,proportionFormat);
		probSecondField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				double newProbSecond = probSecondField.getValue();
				double probFirst = probFirstField.getValue();
				if ((newProbSecond<0)||(newProbSecond>1)) {
					argField = new Object[] {language.getString("labels","probSecond")};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","rangeProbabilities"),argField));
					probSecondField.setValue(probSecond);
				} else if (newProbSecond>probFirst) {
					argField = new Object[] {language.getString("labels","probSedond"),
								language.getString("labels","smallerThan"),
								language.getString("labels","probFirst")};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","orderProbabilities"),argField));
					probSecondField.setValue(probSecond);
				} else if (newProbSecond+probFirst>1) {
					argField = new Object[] {language.getString("labels","probAll")};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","sumProbabilities"),argField));
					probSecondField.setValue(probSecond);
				} else {
					probSecond = newProbSecond;
				}
			}
		});

		// add text box for the probability of choosing the favourite candidate (plurinominali)
		JLabel pluriLabel = new SubMenuLabel(language.getString("labels","messageProbabilities"));
		JLabel pluriField = new JLabel("");
		argField = new Object[] {language.getString("labels","probPreferred")};
		JLabel probPreferredLabel = new SubMenuLabel(MessageFormat.format(language.getString("labels","probField"),argField));
		probPreferredField = new DecimalField(probPreferred,4,proportionFormat);
		probPreferredField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// look at the values entered in the field and check that they are
				// acceptable. If not, put the last known correct value in the field
				double newProbPreferred=probPreferredField.getValue();
				if ((newProbPreferred<0)||(newProbPreferred>1)) {
					argField = new Object[] {language.getString("labels","probPreferred")};
					JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","rangeProbabilities"),argField));
					probPreferredField.setValue(probPreferred);
				} else {
					probPreferred=newProbPreferred;
				}
			}
		});

			
		 // panels for probabilities
        JPanel prob = new JPanel();
        prob.setLayout(new GridLayout(0, 2));
		prob.add(new TitleLabel(language.getString("labels","titleProbabilitiesPanel")));
		prob.add(new JLabel(""));
		prob.add(probFirstLabel);
		prob.add(probFirstField);
		prob.add(probSecondLabel);
		prob.add(probSecondField);
		prob.add(pluriLabel);
		prob.add(pluriField);
		prob.add(probPreferredLabel);
		prob.add(probPreferredField);
		
		// container for probability variables
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		setLayout(new BorderLayout());
		add(prob,BorderLayout.CENTER);
		}

	// methods to pass the variables to the button
	public double getProbFirst() {
		return probFirst;
	}

	public double getProbSecond() {
		return probSecond;
	}

	public double getProbPreferred() {
		return probPreferred;
	}
}