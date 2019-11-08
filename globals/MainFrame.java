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

import com.tomtessier.scrollabledesktop.*;

import classesWrittenByOthers.*;
import gui.*;
import actions.*;
import votingObjects.*;

public class MainFrame extends JFrame implements LocaleChangeEventListener {
	JDesktopPane deskPane;
	JScrollableDesktopPane desktop;
	Language language = Language.getInstance();
	private long memoryLimit = 10000;
	
	private MainFrame() {
		super();
		language.addLocaleChangeEventListener(this);
	}

	static private MainFrame _instance = null;

	synchronized static public MainFrame getInstance() {
		if (_instance == null) {
			synchronized(MainFrame.class) {
				if (_instance == null) {
					_instance = new MainFrame();
				}
			}
		}
		return _instance;
	}

	
	public void LocaleHasChanged(LocaleChangeEvent evt) {
System.out.println("in mainframe locale has changed");
	MenuElectoralSystems menuElett = new MenuElectoralSystems();
	JMenuBar menuBar = menuElett.setMenus();
	this.setJMenuBar(menuBar);
	language.updateAllComponents(this);
	}

//	public void getAllComponents(Component c, LinkedList collection) {
//System.out.println("name of component "+c.getClass().getName());
//  collection.add(c);
//  if (c instanceof Container) {
//    Component[] kids = ((Container)c).getComponents();
//    for(int i=0; i<kids.length; i++)
//      getAllComponents(kids[i], collection);
//  }
//}
	
	public void setup() {
		String title = language.getString("labels","mainFrameTitle"); 
		this.setTitle(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		/*
		** prepare menuBar
		*/
		MenuElectoralSystems menuElett = new MenuElectoralSystems();
		JMenuBar menuBar = menuElett.setMenus();
		this.setJMenuBar(menuBar);

		/*
		** scrollable desktop with the menubar
		*/
		desktop=new JScrollableDesktopPane(menuBar);
		this.getContentPane().add(desktop);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int inset=100;
		this.setBounds(inset,inset,
			screenSize.width - inset*2,
			screenSize.height - inset*2);
				
		this.setVisible(true);
                ImageIcon icon = new ImageIcon("logoAlex.png",
                                 "logo alex");
		JOptionPane.showMessageDialog(this,
				language.getString("labels","welcomeMessage")+language.getString("labels","disclaimer"),
				language.getString("labels","welcomeScreen"),
				JOptionPane.INFORMATION_MESSAGE,icon);
	}

	public JScrollableDesktopPane getScrollableDesktop() {
		return desktop;
	}

	public JDesktopPane getDesktopPane() {
		JScrollPane scrollPane=null;
		JViewport viewport=null;
		JDesktopPane desktopPane=null;
		// component at 1st position in desktop is a container (scrollpane)
		Component[] components = desktop.getComponents();
		scrollPane= (JScrollPane)findComponentOfClass("com.tomtessier.scrollabledesktop.DesktopScrollPane",components);
		if (scrollPane!=null) {
			components = scrollPane.getComponents();
			viewport = (JViewport)findComponentOfClass("javax.swing.JViewport",components);
			if (viewport!=null) {
				components = viewport.getComponents();
				desktopPane	= (JDesktopPane)findComponentOfClass("com.tomtessier.scrollabledesktop.BaseDesktopPane",components);
				if (desktopPane!=null) {
					return desktopPane;
				} else {
					JOptionPane.showMessageDialog(this,language.getString("messages","errorPane"));
					return null;
				}
			} else {
				JOptionPane.showMessageDialog(this,language.getString("messages","errorView"));
				return null;
			}
		} else {
			JOptionPane.showMessageDialog(this,language.getString("messages","errorPane"));
			return null;
		}
	}


	private Component findComponentOfClass(String className,Component[] components) {
		Component component=null;
		for (int i=0;i<components.length;++i) {
			component = components[i];
			if (component.getClass().getName().compareTo(className)==0) {
				return component;
			}
		}
		return null;
	}
	public long getMemoryLimit() {
		return memoryLimit;
	}

	public String checkMemory() {
//System.out.print("check memory ");
		long freeMemory = Runtime.getRuntime().freeMemory();
//System.out.println("free: "+freeMemory+" limit: "+memoryLimit);
		if (freeMemory<memoryLimit) {
			JOptionPane.showMessageDialog(this,language.getString("messages","outOfMemory"));
			return "not enough";
		} else {
			return null;
		}
	}	
}
