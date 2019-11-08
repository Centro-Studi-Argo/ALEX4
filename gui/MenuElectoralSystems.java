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
import actions.*;
import classesWrittenByOthers.*;
import votingObjects.*;
import com.tomtessier.scrollabledesktop.*;

public class MenuElectoralSystems extends JMenuBar {// implements LocaleChangeEventListener {

	Language language = Language.getInstance();
	JFrame mainFrame;
	JMenu file;
	JMenu edit;
	JMenu simulations;
	JMenu help;
	/*
	 * Constructor
	 */
	public MenuElectoralSystems () {

		super();

		mainFrame=MainFrame.getInstance();
//		language.addLocaleChangeEventListener(this);
	}

//	public void LocaleHasChanged(LocaleChangeEvent evt) {
//System.out.println("\n----------------------\nin menus locale has changed");
//	JMenuBar menuBar setMenus();
//	validate();
//	}
	
	public JMenuBar setMenus () {

		JMenuItem menuItem;

		/*
		 * Create the file menu
		 */
		 
		file = new JMenu(language.getString("labels","fileMenu"));
		file.add(createMenuItem(language.getString("labels","openPreferences"),"open","preferences"));
		file.add(createMenuItem(language.getString("labels","simulatePreferences"),"simulate","preferences"));
		file.add(createMenuItem(language.getString("labels","savePreferences"),"save","preferences"));
		file.add(createMenuItem(language.getString("labels","closePreferences"),"close","preferences"));

		file.addSeparator();

		file.add(createMenuItem(language.getString("labels","openParliament"),"open","parliament"));
//		file.add(createMenuItem(language.getString("labels","simulateParliament"),"simulate","parliament"));
		file.add(createMenuItem(language.getString("labels","saveParliament"),"save","parliament"));
//
		file.addSeparator();

		file.add(createMenuItem(language.getString("labels","exitProgram"),"exit",""));
		
		/*
		 * Create the edit menu
		 */
		 
		createMenuToChangeLanguage();

		/*
		 * Create the simulations menu
		 */
		simulations = new JMenu(language.getString("labels","simulationsMenu"));

		// show the new simulation buttons (at the bottom of other menus)
		JMenu newS = new JMenu(language.getString("labels","new"));
		newS.add(createMenuItem(language.getString("labels","simulatePreferences"),"simulate","preferences"));
		newS.add(createMenuItem(language.getString("labels","openPreferences"),"open","preferences"));
		newS.add(createMenuItem(language.getString("labels","closePreferences"),"close","preferences"));
		simulations.add(newS);
		simulations.addSeparator();

		/*
		 * Create the help menu
		 */
		 
		help =new JMenu(language.getString("labels","helpMenu"));

		/*
		 * Create the menu bar and add all the menus to it
		 */
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(simulations);
		menuBar.add(help);
		
		return menuBar;
	}

	/*
	 * create menu item and relative action listener
	 */
	 
	 JMenuItem createMenuItem(String title,String action,String what) {
		JMenuItem menuItem=new JMenuItem(title);
		menuItem.setActionCommand(action+"-"+what);
		menuItem.addActionListener(new MenuElectoralSystemsActions());
		return menuItem;
	}

	public void createMenuToChangeLanguage() {
System.out.println("\tentered createMenuToChangeLanguage");
		JMenu changeLanguage = new JMenu(language.getString("labels","changeLanguage"));
		// what languages are available? for each of these (excluding the default and the value returned by Language for the current locale:
		File dir = new File("languages");
		FilenameFilter filter = new FilenameFilter() {
      	  public boolean accept(File dir, String name) {
        	    return name.startsWith("labels");
        	}
	    };
		String[] allFiles = dir.list(filter);
		for (int i =0; i<allFiles.length; ++i) {
			String fileName = allFiles[i];
			int start = fileName.indexOf("_");
			int end = fileName.indexOf(".");
			if (start != -1) {
				String extension = fileName.substring(start+1,end);
				if (extension.compareTo(language.getCurrentLanguage())!=0) {
					Locale locale = new Locale(extension);
System.out.println("\textension "+extension+" corresponds to "+locale.getDisplayLanguage(language.getCurrentLocale()));
					changeLanguage.add(createMenuItem(locale.getDisplayLanguage(language.getCurrentLocale()),"changeLanguage",extension));
				}
			}
		}
		edit = new JMenu(language.getString("labels","editMenu"));
		edit.add(changeLanguage);
		edit.addSeparator();
		edit.add(createMenuItem(language.getString("labels","setDefaultLanguage"),"changeLanguage","setDefault"));
		
	}

}
