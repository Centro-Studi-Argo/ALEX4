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
package votingObjects;

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

import globals.*;
import classesWrittenByOthers.*;
import gui.*;
import actions.*;

public class DistrictUninominal {
	private int numberVoters;
	private boolean concentratedMajorParty;
	private int nameMajorParty = -1; // default value (if this, not concentrated)
	LinkedList<Voter> listOfVoters=new LinkedList<Voter>();
	private int nameOfDistrict;
	private HashMap<String,Object> generalParameters;
	Language language = Language.getInstance();
	MainFrame mainFrame = MainFrame.getInstance();

	// constructors
	public DistrictUninominal(HashMap<String,Object> generalParameters,int name,boolean major,int majorPartyName) {
	nameOfDistrict=name;
	concentratedMajorParty=major;
	nameMajorParty=majorPartyName;
	this.generalParameters=generalParameters;
	numberVoters=((Integer)generalParameters.get("numberVoters")).intValue();
	}
		
//	public DistrictUninominal(int name,LinkedList listOfVoters) {
//		this.listOfVoters=(LinkedList)listOfVoters.clone();
//		nameOfDistrict=name;
//	}
//		
//	public DistrictUninominal(int numberVoters,boolean major,int name,int nameOfDistrict) {
//		this.numberVoters=numberVoters;
//		concentratedMajorParty=major;
//		nameMajorParty=name;
//		this.nameOfDistrict=nameOfDistrict;
//	}
//		
//	public DistrictUninominal(int numberVoters,boolean major,int nameOfDistrict) {
//		this.numberVoters=numberVoters;
//		concentratedMajorParty=major;
//		this.nameOfDistrict=nameOfDistrict;
//	}

	
	public boolean addVoter(Voter aVoter) {
		if (listOfVoters.size()<numberVoters) {
			listOfVoters.addFirst(aVoter);
			return true;
		} else {// note: error message on loading: this is when I expect the number of voters to be wrong. In simulations, the problem would have been caught already at this point
System.out.println("size of list of voters "+ listOfVoters.size());
			Object[] args = {generalParameters.get("numberVoters")};
			JOptionPane.showMessageDialog(mainFrame,
				MessageFormat.format(language.getString("labels","tooManyVoters"),args) + "\n" + language.getString("messages","loadingInterrupted"),
				language.getString("messages","loadingInterrupted"),
				JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	public LinkedList<Voter> getVoters() {
		return(listOfVoters);
	}
	
	public Voter getVoter(int index) {
		return((Voter)listOfVoters.get(index));
	}

	public boolean getConcentratedMajorParty() {
		return concentratedMajorParty;
	}
		
	public int getNameMajorParty() {
		return nameMajorParty;
	}

	public int getNameOfDistrictU() {
		return nameOfDistrict;
	}

	public String toString() {
		String out = "";
		out += "districtUninominal_"+nameOfDistrict+"_nameOfDistrict : " + nameOfDistrict + " :// "+ language.getString("labels","nameOfDistrictU") + "\n";
		out += "districtUninominal_"+nameOfDistrict+"_concentratedMajorParty : " + concentratedMajorParty + " :// "+ language.getString("labels","concentratedMajorParty") + "\n";
		out += "districtUninominal_"+nameOfDistrict+"_nameMajorParty : " + nameMajorParty + " : //" + language.getString("labels","nameMajorParty") + "\n";
		out += "districtUninominal_"+nameOfDistrict+"_listOfVoters :"+listOfVoters.toString() + "\n";
		return(out);
	}

}// end class definition