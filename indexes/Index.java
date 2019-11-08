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
package indexes;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.Toolkit;
import java.text.*;
import java.io.*;
import java.beans.*;

import classesWrittenByOthers.*;
import globals.*;
import parliaments.*;
import graphs.*;
import votingObjects.*;

public abstract class Index {// extends Observable implements Observer {
	// global variables for the class

	Parliament parliament;
	protected double majorityLevel;
	Object indexValue="";
	public Language language = Language.getInstance();
	public HashMap <Integer,Double> powerMap = new HashMap <Integer,Double> ();
	HashMap <Integer,Integer> mapForIndex	= new HashMap <Integer,Integer> ();
	// array of parties
	ArrayList <Party>arrayOfParties = new ArrayList<Party>();
	public MainFrame mainFrame = MainFrame.getInstance();
	
	// constructor
	public Index() {
	}
	public Index(Parliament parl) {

		parliament = parl;
		majorityLevel = parl.getMajorityLevel();
System.out.println("\tBEG INDEX: majority level: "+majorityLevel);

		indexValue = computeIndex();
	}

	public JPanel parameters() {
		return new JPanel();
	}
	
	public void getIndex() {
		indexValue = computeIndex();
	
	}
	
	public Object getIndexValue() {	// returns the string value of index
		return indexValue;
	}

	public Object recallParameters() {
		return null;
	}

	public void setMapForIndex(HashMap<Integer,Integer>map) {
		mapForIndex = map;
		
	}
//	public void update (Observable parl,Object obj) {
//System.out.println("updating index");
//		indexValue = computeIndex();
//System.out.println("index is "+indexValue.toString());
//System.out.println("notify observers...");
//		setChanged();
//		notifyObservers();
//	}

	public void setPowerMap(HashMap <Integer,Double> map ) {
		powerMap = map;
	}
	
	public HashMap <Integer,Double> getPowerMap () {
		return powerMap;
	}

//	public SemiCircle graph (HashMap <Integer,Double>map) {
//		// change the shapleyvalue map to hold integers instead of doubles: * by 100
//		Set <Integer> k = map.keySet();
//		Iterator  <Integer> i = k.iterator();
//		HashMap <Integer,Integer>forGraph = new HashMap<Integer,Integer>();
//		while (i.hasNext()) {
//			Integer inti = i.next();
//			double val = (map.get(inti)).doubleValue();
//			int vali = (int)(val*100);
//			forGraph.put(inti,vali);
//		}
//		Dimension dimDesk = mainFrame.getScrollableDesktop().getSize();
//		int height = (int)(dimDesk.height * 0.25);
//		int width = (int)(dimDesk.width * 0.25);
//		return (new SemiCircle(forGraph,new Dimension(width,height),arrayOfParties) );
//	}


	protected abstract Object computeIndex();
}// end of class