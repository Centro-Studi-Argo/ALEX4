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
package globals;

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

import votingObjects.*;

public class ListOfSimulations extends LinkedList <SimulationRepository> {

	Language language = Language.getInstance();

	private ListOfSimulations() {
		super();
	}

	static private ListOfSimulations _instance = null;

	synchronized static public ListOfSimulations getInstance() {
		if (_instance == null) {
			synchronized(ListOfSimulations.class) {
				if (_instance == null) {
					_instance = new ListOfSimulations();
				}
			}
		}
		return _instance;
	}

	// add function calls add for linkedList and update the menu
	public boolean add (SimulationRepository object) {
		boolean returnVal = super.add(object);
System.out.println("simulation added, menu updated if it exists");
		updateMenu();
		return returnVal;
	}

	// remove function calls remove for linkedList and update the menu
	public boolean remove (Object object) {
		boolean returnVal = super.remove(object);
		updateMenu();
		return returnVal;
	}


	private void updateMenu() {
		// obtain the menu corresponding to the simulationsMenu key in labels
		MainFrame mainFrame = MainFrame.getInstance();
		JMenuBar menuBar = mainFrame.getJMenuBar();
		JMenu menuToUpdate = null;
		if (menuBar!=null) {
			int n = menuBar.getMenuCount();
			for (int i=0; i<n; ++i) {
				JMenu menu = menuBar.getMenu(i);
				String menuName = menu.getText();
				if (menuName.compareTo(language.getString("labels","simulationsMenu"))==0) {
				menuToUpdate = menu;
				break;
				}
			}
			// call the updateMenu(menu) function
			if (menuToUpdate!=null) {
				updateMenu(menuToUpdate);
			} else {
				JOptionPane.showMessageDialog(mainFrame,language.getString("messages","impossUpdateSimulationsMenu"));
			}
		}
	}

	public void updateMenu(JMenu menu) {
		// remove existing item in menu (except the first one, link to new or open)
		while (menu.getItemCount()>2) {
			menu.remove(menu.getItemCount()-1);
		}
		Iterator i = this.iterator();
		while (i.hasNext()) {
			final SimulationRepository rep = (SimulationRepository)i.next();
			String name = rep.getRepositoryName();
			String description = rep.getRepositoryDescription();

			Action showAction = new AbstractAction(name) {
				public void actionPerformed (ActionEvent e) {
					rep.showSimulationObjects();
				}
			};
			JMenuItem menuItem = new JMenuItem(showAction);
			if (description.length()>0) {
				menuItem.setToolTipText(description);
			}
			
			menu.add(menuItem);
		}
	}

}
