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

public class DistrictPlurinominal {// extends DistrictUninominal {
	private int name;
	private String romanName;
	private LinkedList<DistrictUninominal> listOfDistricts = new LinkedList<DistrictUninominal>();
	private HashMap<Integer,Integer> seatAllocationInDistrict = new HashMap<Integer,Integer>();
	private LinkedList<Voter> listOfVoters = new LinkedList<Voter>();
	Language language = Language.getInstance();
	
	// constructor
	public DistrictPlurinominal(int name,LinkedList<DistrictUninominal> listOfDistricts) {
		this.name=name;
System.out.println("name of district P "+getNameOfDistrictP());
//		this.listOfDistricts=(LinkedList<DistrictUninominal>)listOfDistricts.clone();
		Iterator <DistrictUninominal> i = listOfDistricts.iterator();
		while (i.hasNext()) {
			DistrictUninominal d = i.next();
			this.listOfDistricts.add(d);
		}
System.out.println("final dimensions of listOfDistricts "+listOfDistricts.size());
		// get the voters from the uninominal colleges
		listOfVoters=getVotersFromDistricts(listOfDistricts);
System.out.println("final dimensions of listOfVoters "+listOfVoters.size());
	}

	// function to get the voters from the list of colleges: get the list of voters for
	// each college, and append it to the listOfVoters;
	LinkedList <Voter> getVotersFromDistricts(LinkedList<DistrictUninominal> listOfDistricts) {
		LinkedList<Voter> tempList=new LinkedList<Voter>();
		int n=listOfDistricts.size();
		for (int i=0;i<n;++i) {
			DistrictUninominal aDistrict=listOfDistricts.get(i);
			LinkedList <Voter> voters=aDistrict.getVoters();
//System.out.println("dimension of voters "+voters.size());
			tempList.addAll(voters);
//System.out.println("dimension of temporary list "+tempList.size());
		}
//System.out.println("final dimensions of temporary list "+tempList.size());
		return tempList;
	}

	public String getNameOfDistrictP() {
		Roman roman=new Roman();
		String romanName=roman.makeRoman(name);
		return romanName;
	}

	String getNameInt() {
		return(new Integer(name).toString());
	}

	public LinkedList <Voter> getListOfVoters() {
		if (listOfVoters.size() == 0) {
System.out.println("getVotersFromDistricts - "+getNameOfDistrictP());
			listOfVoters = getVotersFromDistricts(listOfDistricts);
System.out.println("done");
		}
		return listOfVoters;
	}

	public void setAllocationOfSeats(HashMap <Integer,Integer> seatsInDistrict) {
		seatAllocationInDistrict=seatsInDistrict;
	}

	public HashMap<Integer,Integer> getSeatAllocationInDistrict() {
		return seatAllocationInDistrict;
	}

	public LinkedList<DistrictUninominal> getListOfDistricts() {
		return listOfDistricts;
	}

	public int getNumberOfDistricts() {
		return listOfDistricts.size();
	}

	public void addToListOfDistricts(LinkedList<DistrictUninominal> newDistricts) {
		listOfDistricts.addAll(newDistricts);
	}
	

	public String toString() {
		String out = "";
		out += "districtPlurinominal_"+name+"_name : " + name + " :// "+ language.getString("labels","nameOfDistrictP") + "(roman= " + getNameOfDistrictP() + ")\n";
		// need the list of the colleges names
		LinkedList <Integer> list=new LinkedList<Integer>();
		for (int i=0;i<listOfDistricts.size();++i) {
			DistrictUninominal aDistrict=listOfDistricts.get(i);
			list.add(new Integer(aDistrict.getNameOfDistrictU()));
		}
		out += "districtPlurinominal_"+name+"_listOfDistricts : "+list.toString() + "\n";
		return(out);
	}
	
}// end class definition










//				// if concentrated is true, iterate through all keys of the map, and find out the percentage of
//				// collegi uninominali in which the party is concentrated. Determine the corresponding number
//				// of collegi plurinominali in which the party has to be concentrated, round up to the next integer
//				// so it is at least 1
//				// put the concentrated colleges with the party in the plurinominal college(s) where they are concentrated
//				// if the plurinominal college is not complete, fill up with non-major colleges taken at random.
//				int name=0;
//				if (concentrated==true)	{//IF THERE ARE CONCENTRATED PARTIES
//					Set keys=partiesConcentrated.keySet();
//					Iterator k=keys.iterator();
//					while (k.hasNext())	{ //FOR EACH PARTY IN THE MAP PARTITICONCENTRATI
//				
//						Thread.sleep(0);
//				
//						// get the name of the party (key of map) and the number of uninominal colleges in which it is concentrated
//						Integer key=(Integer)k.next();
//						int partyName=key.intValue();
//System.out.println("CONSIDERANDO PARTITO CONCENTRATO "+partyName);
//						int numberConcentrated=((Integer)partiesConcentrated.get(key)).intValue();
//System.out.println("numberUninominalDistricts "+numberUninominalDistricts+" numCollegiPlurinominali "+numberPlurinominalDistricts);
//System.out.println("numberConcentrated "+numberConcentrated);
//						// find out the percentage of the uninominal colleges this represents
//						double perc=(double)numberConcentrated/numberUninominalDistricts;
//System.out.println("perc "+perc);
//						// use perc to find out in how many collegi pluriniominali the party must be concentrated
//						// rounding up to the next integer
//						int numPluriConcentrated=(int)(perc*numberPlurinominalDistricts);
//						numPluriConcentrated=(numPluriConcentrated<1)?1:numPluriConcentrated;
//System.out.println("numPluriConcentrated "+numPluriConcentrated);
//				
//						// find out in which colleges the party is concentrated, put them in a list and remove them
//						// from remainingDistricts
//						LinkedList thisConcentratedParty=new LinkedList();
//						int j=0;
//						while(thisConcentratedParty.size()<numberConcentrated) {
//System.out.println("size of thisConcentratedParty "+thisConcentratedParty.size());
//							DistrictUninominal aDistrict=(DistrictUninominal)majorDistricts.get(j);
//System.out.println("il collegio "+aDistrict.getNameOfDistrictU()+" contiene parties concentrated "+aDistrict.getConcentratedMajorParty());
//							int nameOfDistrict=(aDistrict.getConcentratedMajorParty()==true)?aDistrict.getNameMajorParty():0;
//System.out.println("nome del partito concentrato, nameOfDistrict "+nameOfDistrict+" partyName "+partyName);
//							if (nameOfDistrict==partyName) {
//System.out.println("they are equal");
//								thisConcentratedParty.add(aDistrict);
//								majorDistricts.remove(aDistrict);
//System.out.println("size of thisConcentratedParty "+thisConcentratedParty.size()+" size of collegi rimanenti "+majorDistricts.size());
//							} else {
//								++j;
//							}
//						}// end of while thisConcentratedParty
//				
//						// iterate over thisConcentratedParty, and distribute the colleges in the collegi plurinominali
//						// look at numPluriConcentrated, and see how to distribute the parties equally between this number
//						// of colleges. if the numPluriConcentrated colleges are full and some uninominal colleges remain
//						// "unemployed", they are added back in remainingDistricts
//						int distributePerDistrict=thisConcentratedParty.size()/numPluriConcentrated;
//						// if distributePerDistrict>numberCandidates, set it to numberCandidates.
//						if (distributePerDistrict>numberCandidates) {
//							distributePerDistrict=numberCandidates;
//						}
//System.out.println("distributePerDistrict "+distributePerDistrict);
//						LinkedList chosenDistricts=new LinkedList();
//						// create the list of collegi uninominali with the required number of major colleges
//						// take from thisConcentratedParty
//						while(chosenDistricts.size()<distributePerDistrict) {
//				
//							Thread.sleep(0);
//				
//							DistrictUninominal aDistrictU=(DistrictUninominal)thisConcentratedParty.getFirst();
//							chosenDistricts.add(aDistrictU);
//System.out.println("collegio maggiore chosen "+aDistrictU.getNameOfDistrictU());
//							thisConcentratedParty.remove(aDistrictU);
//						} // end of while chosenDistricts<distributePerDistrict
//						// if necessary, complete with uninominal colleges where no party is concentrated
//						// take from remainingDistricts, at random
//						while(chosenDistricts.size()<numberCandidates) {
//				
//							Thread.sleep(0);
//				
//							DistrictUninominal aDistrictU=(DistrictUninominal)remainingDistricts.get(random.nextInt(remainingDistricts.size()));
//							chosenDistricts.add(aDistrictU);
//System.out.println("collegio chosen "+aDistrictU.getNameOfDistrictU());
//							remainingDistricts.remove(aDistrictU);
//						}// end of while chosenDistricts<numberCandidates
//						++name;
//						DistrictPlurinominal aDistrictPlurinominal=new DistrictPlurinominal(name,chosenDistricts);
//						listOfPlurinominalDistricts.add(aDistrictPlurinominal);
//System.out.println("for name "+name+" i collegi (con parties concentrated) scelti sono:");
//for (int m=0;m<chosenDistricts.size();++m)
//{
//DistrictUninominal aDistrict=(DistrictUninominal)chosenDistricts.get(m);
//System.out.println(aDistrict.getNameOfDistrictU());
//}
//						// if some colleges remain in thisConcentratedParty, put them back into remainingDistricts
//System.out.println("size of thisConcentratedParty "+thisConcentratedParty.size());
//						if(thisConcentratedParty.size()>0) {
//							remainingDistricts.addAll(thisConcentratedParty);
//						}
//						updateProgressBar(listOfPlurinominalDistricts.size(),language.getString("labels","creationPlurinominalDistricts")+nf.format(progressBar.getPercentComplete()));
//					}// end of while k.hasNext()
//				} // end of if concentrated
//				// if concentrated is false, or all the major colleges have been dealt with, create the remaining
//				// plurinominal colleges taking at random "numeroCandidati" uninominal colleges.
//System.out.println("NOW FOR THE COLLEGES NOT CONTAINING CONCENTRATED PARTIES");
//				while (listOfPlurinominalDistricts.size()<numberPlurinominalDistricts) {
//				
//					Thread.sleep(0);
//				
//					// select numeroCandidati colleges at random from remainingDistricts
//					LinkedList chosenDistricts=new LinkedList();
//					while (chosenDistricts.size()<numberCandidates) {
//						DistrictUninominal aDistrict=(DistrictUninominal)remainingDistricts.get(random.nextInt(remainingDistricts.size()));
//						chosenDistricts.add(aDistrict);
//						remainingDistricts.remove(aDistrict);
//					}
//					// create a new plurinominal college, non maggiore
//					++name;
////System.out.println("for name "+name+" i collegi scelti sono:");
////for (int k=0;k<chosenDistricts.size();++k)
////	{
////	DistrictUninominal aDistrict=(DistrictUninominal)chosenDistricts.get(k);
////	System.out.println(aDistrict.getCollegioName());
////	}