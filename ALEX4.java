/*   Copyright: Marie-Edith Bissey, Guido Ortona - 2007
*    Dipartimento di Politiche Pubbliche e Scelte Collettive
*    Universita' del Piemonte Orientale (Italia)
*    Contact: bissey@sp.unipmn.it
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program; if not, write to the Free Software
*    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

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

import parliaments.*;
import globals.*;
import classesWrittenByOthers.*;
import gui.*;
import actions.*;
import votingObjects.*;

public class ALEX4 {


	public static void main (String[] args)	{
		Language language = Language.getInstance();

		 /*
		  * set look and feel
		  */
		String laf=UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(laf);
		} catch (UnsupportedLookAndFeelException exc) {
		} catch (Exception exc) {
		}

		/*
		 * check that the version of the current java is recent enough to see the
		 * menuParlamenti (this menu reads the contents of a directory, needs at least
		 * java 2 for it to work). The program was developped with java 1.2.2, this is
		 * the mininum version required for it to work.
		 */
		String version=System.getProperty("java.version");
		String minVersion="1.5.0";
		if (version.compareTo(minVersion)<0) {
			Object[] arguments = {minVersion};
			String message = MessageFormat.format(language.getString("messages","oldVersion"),arguments);
			JOptionPane.showMessageDialog(null,message,null,JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		} else {
			/*
			 * main frame with scrollable desktop pane, centered on screen with 100-pixel inset
			 * and showing a welcome dialog + the menu bar (created from class menus)
			 */
			MainFrame mainFrame = MainFrame.getInstance();
			mainFrame.setup();
		}
	}

}
