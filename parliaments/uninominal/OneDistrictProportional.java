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
package parliaments.uninominal;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Toolkit;
import java.text.*;
import java.lang.reflect.*;

import classesWrittenByOthers.*;
import votingObjects.*;
import parliaments.*;

public class OneDistrictProportional extends Parliament {


	// constructor

	public OneDistrictProportional(SimulationRepository sr) {
		super(sr);

		Set roundingKeys = roundingMethods.keySet();
		rounding = setMethodDialog(roundingKeys,"multiDistrictPlurinominal");
		String[] vals = {"OneDistrictProportional",rounding};
//		String key = makeKey(vals);	
//
//
//		String[] vals = {"OneDistrictProportional"};
		parliamentKey = makeKey(vals);	
System.out.println("parliamentKey: "+parliamentKey);		
//		if (simulationRepository.containsParliament(parliamentKey)) {
//System.out.println("one district proportional exists in repository - loaded");
//			allocationOfSeats = simulationRepository.loadParliament(parliamentKey);
//		} else {
//System.out.println("one district proportional does not exist in repository - created");
			allocationOfSeats = findAllocationOfSeats();
			simulationRepository.saveParliament(parliamentKey,this);
			setNoteToGraph(language.getString("messages","multiDistrictPlurinominal_title")+" "+rounding);
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
//		}
System.out.println(language.getString("uninominal","OneDistrictProportional")+" created");
	}

	// abstract method that must be overridden by all parliaments
	public HashMap <Integer,Integer> findAllocationOfSeats() {
//		return getProportionalAllocationOfSeats();
		// get the name and parameter of method from the map roundingMethods.
		String[] methodChar = (String[])roundingMethods.get(rounding);
		String nameOfMethod = methodChar[0];
System.out.println("nameOfMethod "+nameOfMethod);
		Integer paramOfMethod = new Integer(methodChar[1]);
System.out.println("paramOfMethod "+paramOfMethod+" is of type "+(paramOfMethod.getClass()).getName());
		// prepare the method to be invoked
		HashMap <Integer,Integer> mapOfSeats=new HashMap<Integer,Integer>();
		try {
			Class thisClass = this.getClass();
			Class[] typeOfParameters = new Class[] {LinkedList.class,ArrayList.class,Integer.class,Double.class,Integer.class,String.class};
			Method method = (this.getClass()).getMethod(nameOfMethod,typeOfParameters);

			// list of parameters to invoke the method to find the seats in the district
			Object[] paraList = new Object[]{listOfVoters,arrayOfParties,paramOfMethod,new Double(0),new Integer(sizeOfParliament),"nation"};
			mapOfSeats = (HashMap<Integer,Integer>)method.invoke(this,paraList);
			// complete mapOfSeats with missing parties (those who got no seats at all)
			mapOfSeats = completeMap(mapOfSeats);
		} catch (NoSuchMethodException er) {
System.out.println("---- no such method ----");
er.printStackTrace();
System.out.println("--------");
					returnCode = "interrupted";
			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","noSuchMethod")+" "+nameOfMethod);
		} catch (InvocationTargetException er) {
System.out.println("--- invocation -----");
er.printStackTrace();
System.out.println("--------");
					returnCode = "interrupted";
			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","invocationTarget")+" "+nameOfMethod+". "+er.getCause());
		} catch (IllegalAccessException er) {
System.out.println("---- illegal access ----");
er.printStackTrace();
System.out.println("--------");
					returnCode = "interrupted";
			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","illegalAccess")+" "+nameOfMethod);
//		} catch (ClassNotFoundException er) {
//			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","classNotFound")+" "+nameOfMethod);
//				} catch (InterruptedException e) {
//					returnCode = "interrupted";
//	System.out.println("interrupted");
		} 
		votesPerParty = votesPerPartyRef;
		return mapOfSeats;
	}

	public String getParliamentName() {
		return (language.getString("uninominal","OneDistrictProportional") + " - " + rounding+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}

	
}// end class definition