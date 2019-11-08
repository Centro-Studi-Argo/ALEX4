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
package actions;

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

import javax.swing.border.*;
import javax.swing.table.*;
import java.lang.reflect.*;
import java.beans.*;


import classesWrittenByOthers.*;
import gui.*;
import votingObjects.*;
import com.tomtessier.scrollabledesktop.*;
import globals.*;

public class CreatorVotingObjects {

	Language language = Language.getInstance();
	
	private HashMap <String,Object> generalParameters;
	private ArrayList <Party> arrayOfParties;
	private LinkedList <Candidate> listOfCandidates = new LinkedList<Candidate>();
	private LinkedList<Voter> listOfVoters = new LinkedList<Voter>();
	private LinkedList<DistrictUninominal> listOfUninominalDistricts = new LinkedList<DistrictUninominal>();
	private LinkedList<DistrictPlurinominal> listOfPlurinominalDistricts = new LinkedList<DistrictPlurinominal>();

//	SimulationRepository sr = new SimulationRepository();;

	private LinkedList what = new LinkedList();
	
	private int totalNumberVoters;
	private int numberVoters;
	private int numberParties;
	private int numberCandidates;
	private int numberUninominalDistricts; 
	private int numberPlurinominalDistricts;
	private int[] thresholdVector;
	private double probFirst,probSecond,probPreferred,probThird,probRem;
	final JProgressBar progressBar;
	String returnCode = "done";
	MainFrame mainFrame = MainFrame.getInstance();

//	public CreatorVotingObjects (HashMap generalParameters,ArrayList arrayOfParties,JProgressBar progressBar) {
//		this.generalParameters=generalParameters;
//		this.arrayOfParties=arrayOfParties;
//
//		totalNumberVoters=((Integer)generalParameters.get("totalNumberVoters")).intValue();
//		numberVoters = ((Integer)generalParameters.get("numberVoters")).intValue();
//		numberParties=((Integer)generalParameters.get("numberParties")).intValue();
//		numberCandidates=((Integer)generalParameters.get("numberCandidates")).intValue();
//		numberUninominalDistricts = ((Integer)generalParameters.get("numberUninominalDistricts")).intValue();
//
//		numberPlurinominalDistricts = numberUninominalDistricts/numberCandidates;
//		
//		probFirst=((Double)generalParameters.get("probFirst")).doubleValue();
//		probSecond=((Double)generalParameters.get("probSecond")).doubleValue();
//		probPreferred=((Double)generalParameters.get("probPreferred")).doubleValue();
//		probThird=1-probFirst-probSecond;
//		probRem=1-(probPreferred/100);
//		
//		this.progressBar=progressBar;
//	}
	public CreatorVotingObjects (HashMap <String,Object> generalParameters,ArrayList <Party> arrayOfParties,JProgressBar progressBar,SimulationRepository sr) {
		this.generalParameters=generalParameters;
		this.arrayOfParties=arrayOfParties;

		totalNumberVoters=((Integer)generalParameters.get("totalNumberVoters")).intValue();
		numberVoters = ((Integer)generalParameters.get("numberVoters")).intValue();
		numberParties=((Integer)generalParameters.get("numberParties")).intValue();
		numberCandidates=((Integer)generalParameters.get("numberCandidates")).intValue();
		numberUninominalDistricts = ((Integer)generalParameters.get("numberUninominalDistricts")).intValue();

		numberPlurinominalDistricts = numberUninominalDistricts/numberCandidates;
		
		probFirst=((Double)generalParameters.get("probFirst")).doubleValue();
		probSecond=((Double)generalParameters.get("probSecond")).doubleValue();
		probPreferred=((Double)generalParameters.get("probPreferred")).doubleValue();
		probThird=1-probFirst-probSecond;
		probRem=1-(probPreferred/100);
//		if (progressBar!=null) {
			this.progressBar=progressBar;
//		}
		if (sr != null) {
//			this.sr = sr;
			listOfVoters = sr.getListOfVoters();
			listOfCandidates = sr.getListOfCandidates();
			listOfUninominalDistricts = sr.getListOfUninominalDistricts();
			listOfPlurinominalDistricts = sr.getListOfPlurinominalDistricts();
		}
	}
	public String createVotingObjects(LinkedList what) {

		// so that the % in progress bar are displayed correctly according to Locale, and with only 2 decimal digits
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);

		try {
			
			/*
			** create candidates
			*/
			int numberCandidates = ((Integer)generalParameters.get("numberCandidates")).intValue();
			if ((numberCandidates>1) && ( (what.contains("candidates")) || (what.contains("all")) ) ){
//System.out.println("create candidates");
				progressBar.setMinimum(1);
				progressBar.setMaximum(numberCandidates*numberParties);
				progressBar.setValue(1);
				for (int i=1;i<=numberParties;++i) {
					// create the list of candidates for the party
					for (int j=1;j<=numberCandidates;++j) {

						Thread.sleep(1);
						if (mainFrame.checkMemory()!= null){
							returnCode = "interrupted";
							return returnCode;
						}

						Candidate candidate=new Candidate(i,j);
						listOfCandidates.addLast(candidate);
						updateProgressBar(listOfCandidates.size(),language.getString("labels","creationCandidates")+" "+nf.format(progressBar.getPercentComplete()));
					}
				}
//System.out.println("done candidates: \n"+listOfCandidates);
				updateProgressBar(progressBar.getMaximum(),language.getString("labels","creationCandidates")+" "+nf.format(1));
			} // end of if candidates>1
	
			/*
			** create voters
			*/
			if ( (what.contains("votersParties")) || (what.contains("all")) ) {
//System.out.println("create voters");
				thresholdVector=new int[numberParties];
//System.out.println("create thresholdVector");
				int sum=0;
				double sumOfDistances = 0;
				for (int i=0;i<numberParties;++i) {
					Party aParty=arrayOfParties.get(i);
					// for all parties but the last one
					if (i<numberParties-1) {
						double share=aParty.getShare();
						int val=(int)Math.round((totalNumberVoters*share)/100);
						thresholdVector[i]=val;
						sum+=val;
//System.out.println("sum "+sum);
					} else {// for the last party, ensure that the sum of thresholds corresponds to the number of electors
						thresholdVector[i]=totalNumberVoters-sum;
					}
					double dist = aParty.getDistance();
					sumOfDistances += dist;
				}
//System.out.println(thresholdVector);

				progressBar.setMinimum(1);
				progressBar.setMaximum(totalNumberVoters);
				progressBar.setValue(1);

				for (int i=1;i<=totalNumberVoters;++i) {

					Thread.sleep(1);
					if (mainFrame.checkMemory()!= null){
						returnCode = "interrupted";
						return returnCode;
					}

//System.out.println("Voter "+i+" su "+totalNumberVoters);
					int index=i;
					// check with the share to see which is the first party preferred by the player
					// if the index of the elector is smaller than share for first party, then
					// first party is the preferred one; if the index of the elector is between
					// share for first party and share for second party, the second party is the
					// preferred one, etc;
					int sumt=0;
					int preferredParty=-1;
					int j;
					for (j=0;j<numberParties;++j) {
						sumt+=thresholdVector[j];
//System.out.println("sum "+sum);
						if (index<=sumt) {
							preferredParty=j+1;
							break;
						}
					}
//System.out.println("preferredParty "+preferredParty);
					LinkedList <Integer> prefsParties = new LinkedList<Integer>();
					if ((what.contains("votersParties")) || (what.contains("all"))) {
						// do parties have distance?
						// if so, use the function with distance,
						// otherwise, use the default
						if (sumOfDistances>0) {
							prefsParties = getVotersPreferencesForPartiesWithDistance(preferredParty,new Random(index));
						} else {
							prefsParties=getVotersPreferencesForParties(preferredParty,new Random(index));
						}
//System.out.println(prefsParties);
					}
					LinkedList <Candidate> prefsCandidates=new LinkedList<Candidate>();
					if ((numberCandidates>1) && (what.contains("votersCandidates"))) {
						prefsCandidates=getVotersPreferencesForCandidates(prefsParties,new Random(index));
					}
	
					Voter aVoter=new Voter(index,prefsParties,prefsCandidates,generalParameters);
					listOfVoters.addFirst(aVoter);
					updateProgressBar(index,language.getString("labels","creationVoters")+" "+nf.format(progressBar.getPercentComplete()));
				}// end for loop on voters
				// make sure it shows 100% at the end (tends to stop at 99,99%
				updateProgressBar(progressBar.getMaximum(),language.getString("labels","creationVoters")+" "+nf.format(1));
//System.out.println("done elettori");
			}

			/*
			** create voters
			*/
			if (what.contains("votersCandidates"))  {
//System.out.println("create voters' preferences for candidates");
//				thresholdVector=new int[numberParties];
//System.out.println("create thresholdVector");
//				int sum=0;
//				for (int i=0;i<numberParties;++i) {
//					Party aParty=(Party)arrayOfParties.get(i);
//					// for all parties but the last one
//					if (i<numberParties-1) {
//						double share=aParty.getShare();
//						int val=(int)Math.round((totalNumberVoters*share)/100);
//						thresholdVector[i]=val;
//						sum+=val;
//System.out.println("sum "+sum);
//					} else {// for the last party, ensure that the sum of thresholds corresponds to the number of electors
//						thresholdVector[i]=totalNumberVoters-sum;
//					}
//				}
//System.out.println(thresholdVector);

				progressBar.setMinimum(1);
				progressBar.setMaximum(totalNumberVoters);
				progressBar.setValue(1);

				for (int i=0;i<totalNumberVoters;++i) {

					Thread.sleep(1);
					if (mainFrame.checkMemory()!= null){
						returnCode = "interrupted";
						return returnCode;
					}

//System.out.println("Voter "+(i+1)+" su "+totalNumberVoters);
//					// check with the share to see which is the first party preferred by the player
//					// if the index of the elector is smaller than share for first party, then
//					// first party is the preferred one; if the index of the elector is between
//					// share for first party and share for second party, the second party is the
//					// preferred one, etc;
//					int sumt=0;
//					int preferredParty=-1;
//					int j;
//					for (j=0;j<numberParties;++j) {
//						sumt+=thresholdVector[j];
////System.out.println("sum "+sum);
//						if (index<=sumt) {
//							preferredParty=j+1;
//							break;
//						}
//					}
//System.out.println("preferredParty "+preferredParty);
//					LinkedList prefsParties = new LinkedList();
//					if ((what.contains("votersParties")) || (what.contains("all"))) {
//						prefsParties=getVotersPreferencesForParties(preferredParty,new Random(index));
//System.out.println(prefsParties);
//					}
					Voter aVoter=(Voter)listOfVoters.get(i);
					LinkedList <Integer> prefsParties = aVoter.getPartyPreferences();
//					LinkedList prefsCandidates=new LinkedList();
					LinkedList <Candidate> prefsCandidates = aVoter.getCandidatePreferences();
					if (prefsCandidates.size()==0) {
						prefsCandidates=getVotersPreferencesForCandidates(prefsParties,new Random(i));
						aVoter.setCandidatePreferences(prefsCandidates);
						updateProgressBar(i,language.getString("labels","creationCandidatePreferences")+" "+nf.format(progressBar.getPercentComplete()));
					} else {
						updateProgressBar(i,language.getString("labels","checkingCandidatePreferences")+" "+nf.format(progressBar.getPercentComplete()));
					}
	
				}// end for loop on voters
				// make sure it shows 100% at the end (tends to stop at 99,99%
				updateProgressBar(progressBar.getMaximum(),language.getString("labels","creationCandidatePreferences")+" "+nf.format(1));
//System.out.println("done elettori");
			}
			
			/*
			** create uninominal districts
			*/
			if ( (what.contains("uninominalDistricts")) || (what.contains("all")) ) {
//System.out.println("create uninominal districts");
				progressBar.setMinimum(1);
				progressBar.setMaximum(numberUninominalDistricts);
				progressBar.setValue(1);
	
				int nameUninominalDistrict=1;
				LinkedList <Party> majorParties=new LinkedList<Party>();
				LinkedList <Party>normalParties=new LinkedList<Party>();
//				LinkedList remainingVoters=(LinkedList)listOfVoters.clone();
				LinkedList <Voter> remainingVoters = new LinkedList <Voter> ();
				Iterator <Voter> iv = listOfVoters.iterator();
				while (iv.hasNext()) {
					Voter v = iv.next();
					remainingVoters.add(v);
				}
	
//System.out.println("check whether there are major parties");
				// check whether there are major parties: go through the list of parties
				// and keep track of how many and which have the major variable set to true
				// keep also their value for concentration and coefficient.
				for (int i=0;i<numberParties;++i) {
					Party aParty=arrayOfParties.get(i);
					boolean major=aParty.getMajor();
					if (major==true) {
						majorParties.addFirst(aParty);
					} else {
						normalParties.addFirst(aParty);
					}
				}
//System.out.println("done: there are "+majorParties.size()+" major parties and "+normalParties.size()+" normal parties");	
				// for each major party, select electors and put the required number in the colleges
				// that is, in the K colleges in which the party is concentrated, choose
				// T*numberVoters/numberUninominalDistricts where T is the coefficient of concentration.
				// Distribute the remaining electors with the
				// party preferred equally between the remaining C-K colleges. Delete all electors with this
				// first preference from the list of Electors.
				for (int i=0;i<majorParties.size();++i) {
	
					Thread.sleep(1);
					if (mainFrame.checkMemory()!= null){
						returnCode = "interrupted";
						return returnCode;
					}
	
					Party aParty=majorParties.get(i);
					int name=aParty.getName();
//System.out.println("trattamento del partito major "+name);
					LinkedList <Voter> listVotersForParty=new LinkedList<Voter>();
					// put all electors who have this party as a first preference in a list
					// and remove them from the list of remaining electors.
					int j;
					for (j=0;j<listOfVoters.size();++j) {
						Voter aVoter=listOfVoters.get(j);
						if (aVoter.getFirstPartyPreference()==name) {
							listVotersForParty.addFirst(aVoter);
							remainingVoters.remove(aVoter);
						}
					}
					// find out in how many colleges the party is concentrated
					int concentration=aParty.getConcentration();
					// find out the coefficient of concentration
					double coefficient=aParty.getCoefficient();
					// find out share of the party
					double share=aParty.getShare();
					// number of electors with this party as first preference
					int total=listVotersForParty.size();
					// find out how many electors are put in each of the main colleges
					int numberVotersToInsert=(int)(coefficient*total/numberUninominalDistricts);
System.out.println("concentration "+concentration);
System.out.println("coefficient "+coefficient);
System.out.println("share "+share);
System.out.println("total "+total);
System.out.println("number to insert "+numberVotersToInsert);
					// put the required number in the concentrated colleges
					for (j=1;j<=concentration;++j) {
						boolean major=aParty.getMajor();
						DistrictUninominal aDistrictU=new DistrictUninominal(generalParameters,nameUninominalDistrict,major,name);
						int sizeVotersU=(aDistrictU.getVoters()).size();
						while (sizeVotersU<numberVotersToInsert) {
							Voter aVoter=(Voter)listVotersForParty.removeFirst();
							aDistrictU.addVoter(aVoter);
							++sizeVotersU;
						}
System.out.println("Collegio magiore "+nameUninominalDistrict+" su "+concentration+": setting elettori per partito major");
						listOfUninominalDistricts.add(aDistrictU);
						++nameUninominalDistrict;
					}
					// are there non attribuited electors?
					int unusedVoters=listVotersForParty.size();
					// if so, add those electors back to the list of remaining electors
					if (unusedVoters>0) {
						remainingVoters.addAll(listVotersForParty);
					}
				}
		
				// complete the colleges already created with electors chosen at random among remainingVoters
//System.out.println("complete the colleges created with the parties maggiori");
				if (majorParties.size()>0) {
					for (int i=0;i<listOfUninominalDistricts.size();++i) {
	
						Thread.sleep(1);
						if (mainFrame.checkMemory()!= null){
							returnCode = "interrupted";
							return returnCode;
						}
					
						DistrictUninominal aDistrictU=(DistrictUninominal)listOfUninominalDistricts.get(i);
//System.out.println("Collegio magiore "+aDistrictU.getCollegioName()+" su "+listOfUninominalDistricts.size()+": filling up");
						// while there are less electors in the college than the required number
						int sizeVotersU=(aDistrictU.getVoters()).size();
					
						while (sizeVotersU<numberVoters) {
							// get a random elector from remainingVoters 
							Random random=new Random((aDistrictU.getVoters()).size());
							Voter aVoter=remainingVoters.get(random.nextInt(remainingVoters.size()));
							aDistrictU.addVoter(aVoter);
							remainingVoters.remove(aVoter);
							++sizeVotersU;
						}
						updateProgressBar(i,language.getString("labels","creationUninominalDistricts")+nf.format(progressBar.getPercentComplete()));
					}
				}
				
				// fill up the remaining colleges with electors chosen at random among the
				// electors not yet attributed, and complete the college
				// while there are less than the total number of colleges
//System.out.println("creating districts");
				while (listOfUninominalDistricts.size()<numberUninominalDistricts) {
//System.out.println("size of remainingVoters "+remainingVoters.size());	
					Thread.sleep(1);
					if (mainFrame.checkMemory()!= null){
						returnCode = "interrupted";
						return returnCode;
					}
	
					// create a new college which does not contain a partito major
					DistrictUninominal aDistrictU=new DistrictUninominal(generalParameters,nameUninominalDistrict,false,0);
					Random random=new Random(listOfUninominalDistricts.size());
					int sizeVotersU=(aDistrictU.getVoters()).size();
					while (sizeVotersU<numberVoters) {
						Voter aVoter=remainingVoters.get(random.nextInt(remainingVoters.size()));
						aDistrictU.addVoter(aVoter);
						remainingVoters.remove(aVoter);
						++sizeVotersU;
					}
					listOfUninominalDistricts.add(aDistrictU);
					++nameUninominalDistrict;
					updateProgressBar(listOfUninominalDistricts.size(),language.getString("labels","creationUninominalDistricts")+nf.format(progressBar.getPercentComplete()));
				}
//for (int du=0;du<listOfUninominalDistricts.size();++du) {
//DistrictUninominal adistrict=(DistrictUninominal)listOfUninominalDistricts.get(du);
//System.out.println(du+"\n"+adistrict+"\n");
//}
				updateProgressBar(progressBar.getMaximum(),language.getString("labels","creationUninominalDistricts")+nf.format(1));
//System.out.println("done uninominal districts");
			}

			/*
			** create plurinominal districts
			*/
			if ((numberCandidates>1) && ( (what.contains("candidates")) || (what.contains("all")) ) ){
//System.out.println("create plurinominal districts");
				progressBar.setMinimum(1);
				progressBar.setMaximum(numberPlurinominalDistricts);
				progressBar.setValue(1);

				// look in listOfUninominalDistricts to see whether some are concentrated
				// counter that finds out how many colleges are concentrated for each party
				// creates a map with name of college and number of concentrated party.
				// creates also a boolean concentrated which takes the value true if at least one concentrated
				// college is found
				// when a college with a concentrated party is found, put it into the list majorDistricts, and remove
				// it from the list remainingDistricts
//				LinkedList listOfPlurinominalDistricts=new LinkedList();
				for (int i=1;i<=numberPlurinominalDistricts;++i) {
					listOfPlurinominalDistricts.add(new DistrictPlurinominal(i,new LinkedList<DistrictUninominal>()));
				}
				// create the list of remaining colleges (clone of existing list)
//				LinkedList remainingDistricts=(LinkedList)listOfUninominalDistricts.clone();
				LinkedList <DistrictUninominal> remainingDistricts = new LinkedList <DistrictUninominal> ();
				Iterator <DistrictUninominal> idu = listOfUninominalDistricts.iterator();
				while (idu.hasNext()) {
					remainingDistricts.add(idu.next());
				}
				boolean concentrated=false;
				Random random=new Random();
				HashMap <Integer,Integer> partiesConcentrated=new HashMap<Integer,Integer>();
				LinkedList <DistrictUninominal> majorDistricts=new LinkedList<DistrictUninominal>();
				
				for (int i=0;i<numberUninominalDistricts;++i) {
				
					Thread.sleep(1);
					if (mainFrame.checkMemory()!= null){
						returnCode = "interrupted";
						return returnCode;
					}
			
					DistrictUninominal aDistrict=listOfUninominalDistricts.get(i);
					concentrated=aDistrict.getConcentratedMajorParty();
//System.out.println("collegio "+aDistrict.getCollegioName()+" contains concentrated parties "+concentrated);
					// if concentrated is true, get the name of the major party, see if it is already in
					// the partiesConcentrated map. if so, add 1 to the value, otherwise, add a new key with value of 1
					if (concentrated==true) {
						Integer nameOfParty=new Integer(aDistrict.getNameMajorParty());
//System.out.println("nameOfParty "+nameOfParty.intValue());
						Set <Integer> keys=partiesConcentrated.keySet();
						if (keys.contains(nameOfParty)) {
//System.out.println("keys contains nameOfParty");
							int value=((Integer)partiesConcentrated.get(nameOfParty)).intValue();
//System.out.println("current value is: "+value);
							++value;
//System.out.println("new value is "+value);
							partiesConcentrated.remove(nameOfParty);
							partiesConcentrated.put(nameOfParty,new Integer(value));
						} else {
//System.out.println("keys does not contain nameOfParty, put 1 as value");
							partiesConcentrated.put(nameOfParty,new Integer(1));
						}
//System.out.println("size of map of parties concentrated: "+partiesConcentrated.size());
//System.out.println("the map of parties concentrated is now:");
						Iterator <Integer> j=keys.iterator();
//System.out.println("found iterator");
						while(j.hasNext()) {
							Integer key=j.next();
//System.out.println("found key "+key);
							Integer val=partiesConcentrated.get(key);
//System.out.println("found value "+val);
							System.out.println("name "+key.intValue()+" nb collegi "+val.intValue());
						}
						// put the college in the list majorDistricts, and remove it from remainingDistricts
						majorDistricts.add(aDistrict);
						remainingDistricts.remove(aDistrict);
					} // end of if concentrated
//System.out.println("the value of concentrated is "+concentrated);
				}// end of for i
				


				while (majorDistricts.size()>0) { // there are concentrati, hence maggiori
					// which party is concentrated in the most colleges
					Set <Integer> keys = partiesConcentrated.keySet();
					Iterator <Integer> k = keys.iterator();
					int maxDistricts = 0;
					Integer party = new Integer(0);
					while (k.hasNext()) {
						Integer key = k.next();
						int numberConcentrated = (partiesConcentrated.get(key)).intValue();
						if (numberConcentrated>maxDistricts) {
							maxDistricts = numberConcentrated;
							party = key;
						}
					}// end while k.hasNext()
//System.out.println("maxDistricts = "+maxDistricts+" party = "+party);			
					// remove party from partityConcentrati
					partiesConcentrated.remove(party);
		
					// get the districts in which the party is concentrated from majorDistricts and remove them from majorDistricts
					LinkedList <DistrictUninominal> thisConcentratedParty = new LinkedList<DistrictUninominal>();
					Iterator <DistrictUninominal>j = majorDistricts.iterator();
					while (j.hasNext()) {
						DistrictUninominal aDistrict = j.next();
						int nameOfDistrict = (aDistrict.getConcentratedMajorParty()==true)? aDistrict.getNameMajorParty() : 0;
						if (nameOfDistrict==party.intValue()){
							thisConcentratedParty.add(aDistrict);
							j.remove();
						}
					}
//System.out.println("found thisConcentratedParty which contains all the colleges in which this party is concentrated");
					// fill up collegi plurinominali
					// get a random college, and complete with the parties until  thisConcentratedParty is empty
					j = thisConcentratedParty.iterator();
					while (j.hasNext()) {
//System.out.println("j has next: still colleges in thisConcentratedParty");
						int nbDistrictsAlreadyInserted = numberCandidates+1;
						DistrictPlurinominal aDistrictP=listOfPlurinominalDistricts.get(0); // because it needs to be initialised
						while (nbDistrictsAlreadyInserted>=numberCandidates) {
//System.out.println("nbDistrictsAlreadyInserted = "+nbDistrictsAlreadyInserted+" numberCandidates "+numberCandidates);
							aDistrictP = listOfPlurinominalDistricts.get(random.nextInt(listOfPlurinominalDistricts.size()));
//System.out.println("chosen aDistrictP = "+aDistrictP.getNameCollegio());
							nbDistrictsAlreadyInserted = aDistrictP.getNumberOfDistricts();
						}
						int nbToInsert = numberCandidates - nbDistrictsAlreadyInserted;
//System.out.println("nbToInsert "+nbToInsert);
						LinkedList <DistrictUninominal> toInsert = new LinkedList<DistrictUninominal>();
						for (int num=0;num<nbToInsert;++num) {
							if (thisConcentratedParty.size()>0) {
//System.out.println("num "+num+" , there are "+thisConcentratedParty.size()+" colleges in thisConcentratedParty");
								DistrictUninominal col = j.next();
								toInsert.add(col);
								j.remove();
							}
						}
						aDistrictP.addToListOfDistricts(toInsert);
					}
				}//end of while majorDistricts.size()>0
		
				// now fill up districts with remaining districts
//System.out.println("complete with remaining districts");
				while (remainingDistricts.size()>0) {
					int nbDistrictsAlreadyInserted = numberCandidates+1;
					DistrictPlurinominal aDistrictP=listOfPlurinominalDistricts.get(0); // because it needs to be initialised
					while (nbDistrictsAlreadyInserted>numberCandidates) {
						aDistrictP = listOfPlurinominalDistricts.get(random.nextInt(listOfPlurinominalDistricts.size()));
						nbDistrictsAlreadyInserted = aDistrictP.getNumberOfDistricts();
					}
					int nbToInsert = numberCandidates - nbDistrictsAlreadyInserted;
					LinkedList <DistrictUninominal>toInsert = new LinkedList<DistrictUninominal>();
					for (int num=0;num<nbToInsert;++num) {
						int index = random.nextInt(remainingDistricts.size());
						DistrictUninominal col = remainingDistricts.get(index);
						remainingDistricts.remove(index);
						toInsert.add(col);
					}
					aDistrictP.addToListOfDistricts(toInsert);
				}
//System.out.println("show the contents of the list");
//Iterator l = listOfPlurinominalDistricts.iterator();
//while (l.hasNext()) {
//	DistrictPlurinominal collegiop = (DistrictPlurinominal)l.next();
//System.out.println("Collegiop: "+collegiop.getNameOfDistrictP());
//	LinkedList colleges = collegiop.getListOfDistricts();
//	Iterator c = colleges.iterator();
//System.out.print("- ");
//	while (c.hasNext()) {
//		DistrictUninominal collegio = (DistrictUninominal)c.next();
//System.out.print(collegio.getNameOfDistrictU()+" - ");
//	}
//System.out.print("\n");
//}
		
					updateProgressBar(listOfPlurinominalDistricts.size(),language.getString("labels","creationPlurinominalDistricts")+nf.format(progressBar.getPercentComplete()));
//				}// end of while listaCollegiPlurinominale<numberPlurinominalDistricts
				
				updateProgressBar(progressBar.getMaximum(),language.getString("labels","creationPlurinominalDistricts")+nf.format(1));
//System.out.println("done plurinominal districts");
				}// end if candidates>1
			} catch (InterruptedException e) {
//System.out.println("interrupted");
				returnCode = "interrupted";
				return returnCode;
			}
//System.out.println("returning from creating voting objects");
		returnCode = "done";
		return returnCode;
		}	

	void updateProgressBar(final int index,final String what) {
		Runnable setValueAndString = new Runnable () {
			public void run() {
				progressBar.setValue(index);
				progressBar.setString(what);
			}
		};
		SwingUtilities.invokeLater(setValueAndString);
	}
	
	public ArrayList <Party> getArrayOfParties() {
		return arrayOfParties;
	}

	public LinkedList <Candidate> getListOfCandidates() {
		return listOfCandidates;
	}

	public LinkedList <Voter> getListOfVoters() {
		return listOfVoters;
	}

	public LinkedList <DistrictUninominal> getListOfUninominalDistricts () {
		return listOfUninominalDistricts;
	}

	public LinkedList <DistrictPlurinominal> getListOfPlurinominalDistricts() {
//System.out.println("in creatorVotingObjects: size of plurinominal districts "+listOfPlurinominalDistricts.size());
		return listOfPlurinominalDistricts;
	}

	// compute the preferences of each elector when distance between parties is present and put in list
	// get the distance value of the preferred party (automatically in first position of the preference ordering)
	// get the absolute difference between the distances of othe parties and that of the preferred parties
	// order the parties according to the difference in distances
	// then while the size of the ordered vector is greater than 2, take a random number between 1 and 100 and
	// according to its value get the first, second or another party at random, put in partyPreferences vector and remove
	// from the list of ordered parties.
	// if the size is equal to 2, get the random number in the probFirst100+probSecond100 interval
	// for the last party put directly in list
	LinkedList <Integer> getVotersPreferencesForPartiesWithDistance(int preferredParty, Random random) {
		int probFirst100 = (int)(probFirst * 100);
		int probSecond100 = (int)(probSecond * 100);
		int probThird100 = (int)(probThird * 100);

		LinkedList <Integer> partyPreferences=new LinkedList<Integer>();
//System.out.println("get party preferences");
		// set up the currentParty to be the preferred one
//System.out.println("preferred party "+preferredParty);
		// add the preferred party as first in preferences
		partyPreferences.addFirst(new Integer(preferredParty));
		// create a vector containing the parties, to be "destroyed" as preferences are found
		ArrayList <Integer> vectorOfParties=new ArrayList<Integer>(numberParties);
		int i;
		for(i=1;i<=numberParties;++i) {
				if (i != preferredParty) {
				vectorOfParties.add(new Integer(i));
				}
		}
//System.out.println("vector of parties "+vectorOfParties);
		// create vector of distances for the parties with respect to the preferred one
		Party refParty = arrayOfParties.get(preferredParty-1);
		double refDistance= refParty.getDistance();
//System.out.println("current party "+preferredParty+" corresponds to party "+refParty.getName());
		HashMap <Integer,Double> mapOfDistances = new HashMap<Integer,Double>();
		for (int pd=0;pd<arrayOfParties.size();++pd) {
			Party pdParty = arrayOfParties.get(pd);
//System.out.println("pd party corresponds to party "+pdParty.getName()+" reference party is party "+refParty.getName());
			if (pdParty.getName() != preferredParty) {
//System.out.println("not the reference party");
				mapOfDistances.put(new Integer(pdParty.getName()),new Double(Math.abs(pdParty.getDistance()-refDistance)));
			}
		}
//System.out.println("map of distances\n"+mapOfDistances);
		// order the vector of parties according to distances
		ArrayList <Integer> orderedVectorOfParties = new ArrayList<Integer>();
		while (mapOfDistances.size()>0) {
			double minDistance = 99999;
			Set <Integer> keys = mapOfDistances.keySet();
			Iterator <Integer> it = keys.iterator();
			Integer potentialKey = new Integer(-1);
			while (it.hasNext()) {
				Integer key = it.next();
				double possval = (mapOfDistances.get(key)).doubleValue();
//System.out.println("\tpossval "+possval+" mindistance "+minDistance);
				if (possval<=minDistance) {
//System.out.println("\tcurrent smallest distance");
					potentialKey = key;
					minDistance = possval;
				}
			}
//System.out.println("add "+potentialKey+" to ordered parties");
			orderedVectorOfParties.add(potentialKey);
			mapOfDistances.remove(potentialKey);
		}
//System.out.println("ordered vector of parties"+orderedVectorOfParties);
		Integer chosenParty = new Integer(-1);
		while (orderedVectorOfParties.size()>0) {
//System.out.println("ordered vector has size "+orderedVectorOfParties.size());
			// while size>2 , get prob choosing next, prob choosing second next, prob choosing other, and choose
			if (orderedVectorOfParties.size()>2) {
//System.out.println("size > 2");
				int val=(random.nextInt(100))+1;
				if (val<=probFirst100) {
					chosenParty = orderedVectorOfParties.get(0);
					orderedVectorOfParties.remove(0);
				} else if (val<=(probFirst100+probSecond100)) {
					chosenParty = orderedVectorOfParties.get(1);
					orderedVectorOfParties.remove(1);
				} else {
					int valp = (random.nextInt(orderedVectorOfParties.size()-2)) + 2;
					chosenParty = orderedVectorOfParties.get(valp);
					orderedVectorOfParties.remove(valp);
				}
				partyPreferences.add(chosenParty);
			}
			// remove chosen party from vector
			// if size == 2, same with only prob choosing next and rest
			if (orderedVectorOfParties.size()==2) {
//System.out.println("size = 2");
				int val = (random.nextInt(100-probThird100))+1;
				if (val<=probFirst100) {
					chosenParty = orderedVectorOfParties.get(0);
					orderedVectorOfParties.remove(0);
				} else {
					chosenParty = orderedVectorOfParties.get(1);
					orderedVectorOfParties.remove(1);
				}
				partyPreferences.add(chosenParty);	
			}
			if (orderedVectorOfParties.size()==1) {
//System.out.println("size==1");
				// if size == 1 put the party in preferences.
				partyPreferences.add(orderedVectorOfParties.get(0));
				orderedVectorOfParties.remove(0);
			}
		}
		return partyPreferences;
	}

	 double getMinValue(LinkedList <Double> collection) {
	 	double minval = (collection.getFirst()).doubleValue();
		Iterator <Double> it = collection.iterator();
		while (it.hasNext()) {
			double possmin = (it.next()).doubleValue();
			if (possmin<minval) {
				minval = possmin;
			}
		}
		return minval;
	 }
	// compute the preferences of each elector and put in list
	LinkedList <Integer> getVotersPreferencesForParties(int preferredParty, Random random) {

		double probFirst100 = probFirst * 100;
		double probSecond100 = probSecond * 100;
		double probThird100 = probThird * 100;

		LinkedList <Integer>partyPreferences=new LinkedList<Integer>();
//System.out.println("get party preferences");
		// set up the currentParty to be the preferred one
		int currentParty=preferredParty;
//System.out.println("current party "+currentParty);
		// add the preferred party as first in preferences
		partyPreferences.addFirst(new Integer(currentParty));
		// create a vector containing the parties, to be "destroyed" as preferences are found
		ArrayList <Integer>vectorOfParties=new ArrayList<Integer>(numberParties);
		int i;
		for(i=0;i<numberParties;++i) {
			vectorOfParties.add(i,new Integer(i+1));
		}

		// while there are more than one party in vectorOfParties
		while(vectorOfParties.size()>2) {
//System.out.println("\t size of partiti vector still greater than 2");
			// get a random number from 1 to 100
			// note that Random returns value between 0 (inclusive) and a maximum (exclusive), hence
			// I add +1 to the result to have them between 1 and 100 (both inclusive)
			int val=(random.nextInt(100))+1;
//System.out.println("\t random value is "+val);
			// find the position of the current party in the vector
			int pos=vectorOfParties.indexOf(new Integer(currentParty));
			// if the currentParty is the first in the vector (and vector size is greater or equal to 4)
			if ((pos==0)&&(vectorOfParties.size()>=4)) {
//System.out.println("\t if the currentParty is the first in the vector (and vector size is greater or equal to 4)");
				double sum1=probFirst100;
				double sum2=probFirst100+probSecond100;
				// if val is smaller than probFirst, choose first adjacent
				if(val<=sum1) {
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum1)&&(val<=sum2))	{ // if val is between probFirst and probSecond, choose second adjacent
					int newParty=(vectorOfParties.get(pos+2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // else choose one of the remaining parties at random
					int posNewParty=pos;
					while((posNewParty==pos)||(posNewParty==pos+1)||(posNewParty==pos+2)) {
						posNewParty=random.nextInt(vectorOfParties.size());
					}
					int newParty=(vectorOfParties.get(posNewParty)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}
			// if the position is 0 (and size of partiti vector is equal to 3)
			if ((pos==0)&&(vectorOfParties.size()==3)) {
//System.out.println("\t if the position is 0 (and size of partiti vector is equal to 3)");
				double sum1=probFirst100*(100/(probFirst100+probSecond100));
				double sum2=100;
//System.out.println("sum1 "+sum1+" sum2 "+sum2+" val "+val);
				// if val is smaller than probFirst, choose first adjacent
				if(val<=sum1) {
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // if val is between probFirst and probSecond, choose second adjacent
					int newParty=(vectorOfParties.get(pos+2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}
			// if the currentParty is the second and size of vectorOfParties is at least 5
			if ((pos==1)&&(vectorOfParties.size()>=5)) {
//System.out.println("\t if the currentParty is the second and size of vectorOfParties is at least 5");
				double sum1=probFirst100/2;
				double sum2=probFirst100;
				double sum3=probFirst100+probSecond100;
				// if val is smaller than sum1, choose first adjacent (left)
				if(val<=sum1) {
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum1)&&(val<=sum2))	{// if val is between sum1 and sum2, choose first adjacent (right)
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum2)&&(val<=sum3)) { // if val is between sum2 and sum3, choose second adjacent (right)
					int newParty=(vectorOfParties.get(pos+2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // else choose one of the remaining parties at random
					int posNewParty=pos;
					while((posNewParty==pos)||(posNewParty==pos-1)||
						(posNewParty==pos+1)||(posNewParty==pos+2)) {
						posNewParty=random.nextInt(vectorOfParties.size());
					}
					int newParty=(vectorOfParties.get(posNewParty)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}
			// if the currentParty is the second and size of vectorOfParties is equal to 4
			if ((pos==1)&&(vectorOfParties.size()==4)) {
//System.out.println("\t if the currentParty is the second and size of vectorOfParties is equal to 4");
				double coef=100/(probFirst100+probSecond100);
				double sum1=coef*(probFirst100/2);
				double sum2=coef*probFirst100;
				double sum3=100;
				// if val is smaller than sum1, choose first adjacent (left)
				if(val<=sum1) {
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum1)&&(val<=sum2)) { // if val is between sum1 and sum2, choose first adjacent (right)
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // if val is between sum2 and sum3, choose second adjacent (right)
					int newParty=(vectorOfParties.get(pos+2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}
			// if the currentParty is the second and size of vectorOfParties is equal to 3
			// 50% to get each of the next adjacent
			if ((pos==1)&&(vectorOfParties.size()==3)) {
//System.out.println("\t if the currentParty is the second and size of vectorOfParties is equal to 3. 50% to get each of the next adjacent");
				double sum1=50;
				double sum2=100;
				// if val is smaller than sum1, choose first adjacent (left)
				if(val<=sum1) {
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // if val is between sum1 and sum2, choose second adjacent (right)
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}
			// if the currentParty is the last but one and size is at least 5
			if ((pos==vectorOfParties.size()-2)&&(vectorOfParties.size()>=5)) {
//System.out.println("\t if the currentParty is the last but one and size is at least 5");
				double sum1=100-(probFirst100/2);
				double sum2=100-probFirst100;
				double sum3=100-(probFirst100+probSecond100);
				// if val is smaller than sum1, choose first adjacent (right)
				if(val>sum1) {
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val<=sum1)&&(val>sum2)) { // if val is between sum1 and sum2, choose first adjacent (left)
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val<=sum2)&&(val>sum3)) { // if val is between sum2 and sum3, choose second adjacent (left)
					int newParty=(vectorOfParties.get(pos-2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // else choose one of the remaining parties at random
					int posNewParty=pos;
					while((posNewParty==pos)||(posNewParty==pos+1)||(posNewParty==pos-1)||(posNewParty==pos-2)) {
						posNewParty=random.nextInt(vectorOfParties.size());
					}
					int newParty=(vectorOfParties.get(posNewParty)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}
			// if the currentParty is the last but one and size is equal to 4
			if ((pos==vectorOfParties.size()-2)&&(vectorOfParties.size()==4)) {
//System.out.println("\t if the currentParty is the last but one and size is equal to 4");
				double coef=100/(probFirst100+probSecond100);
				double sum1=100-coef*(probFirst100/2);
				double sum2=100-coef*probFirst100;
				// if val is smaller than sum1, choose first adjacent (right)
				if(val>sum1) {
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val<=sum1)&&(val>sum2)) { // if val is between sum1 and sum2, choose first adjacent (left)
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // else, choose second adjacent (left)
					int newParty=(vectorOfParties.get(pos-2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}
			// if the currentParty is the last but one and size is equal to 3
			// 50% chances to get one of the first adjacents
			if ((pos==vectorOfParties.size()-2)&&(vectorOfParties.size()==3)) {
//System.out.println("\t if the currentParty is the last but one and size is equal to 3. 50% chances to get one of the first adjacents");
				double sum1=50;
				// if val is smaller than sum1, choose first adjacent (right)
				if(val>sum1) {
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // else, choose first adjacent (left)
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}
			// if the currentParty is the last one and size is at least 4
			if ((pos==vectorOfParties.size()-1)&&(vectorOfParties.size()>=4)) {
//System.out.println("\t if the currentParty is the last one and size is at least 4");
				double sum1=100-probFirst100;
				double sum2=100-(probFirst100+probSecond100);
				// if val is smaller than probFirst, choose first adjacent(left)
				if(val>sum1) {
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val<=sum1)&&(val>sum2)) { // if val is between probFirst and probSecond, choose second adjacent(left)
					int newParty=(vectorOfParties.get(pos-2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // else choose one of the remainind parties at random
					int posNewParty=pos;
					while((posNewParty==pos)||(posNewParty==pos-1)||(posNewParty==pos-2)) {
						posNewParty=random.nextInt(vectorOfParties.size());
					}
					int newParty=(vectorOfParties.get(posNewParty)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}
			// if the currentParty is the last one and size is equal to 3
			if ((pos==vectorOfParties.size()-1)&&(vectorOfParties.size()==3)) {
//System.out.println("\t if the currentParty is the last one and size is equal to 3");
				double coef=100/(probFirst100+probSecond100);
				double sum1=100-coef*probFirst100;
				// if val is smaller than probFirst, choose first adjacent(left)
				if(val>sum1) {
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // else, choose second adjacent(left)
					int newParty=(vectorOfParties.get(pos-2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			} else if (vectorOfParties.size()>=7) {// else (the currentParty is somewhere in the middle, and size is at least 7)
//System.out.println("\t else (the currentParty is somewhere in the middle, and size is at least 7)");
				double sum1=probThird100/2;
				double sum2=sum1+(probSecond100/2);
				double sum3=sum2+(probFirst100/2);
				double sum4=sum2+probFirst100;
				double sum5=sum4+(probSecond100/2);
				// if val smaller than sum1, choose random between 0 and [pos-2]
				if (val<=sum1) {
					int posNewParty=pos;
					while((posNewParty==pos)||(posNewParty==pos-2)||(posNewParty==pos-1)||
						(posNewParty==pos+1)||(posNewParty==pos+2)) {
						posNewParty=random.nextInt(vectorOfParties.size());
					}
					int newParty=(vectorOfParties.get(posNewParty)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum1)&&(val<=sum2)) { // if val between sum1 and sum2, choose second adjacent (left)
					int newParty=(vectorOfParties.get(pos-2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum2)&&(val<=sum3)) { // if val between sum2 and sum3, choose first adjacent (left)
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum3)&&(val<=sum4)) { // if val between sum3 and sum4, choose first adjacent (right)
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum4)&&(val<=sum5)) { // if val between sum4 and sum5, choose second adjacent (right)
					int newParty=(vectorOfParties.get(pos+2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // else choose random between [pos+2] and size partiti
					int posNewParty=pos;
					while((posNewParty==pos)||(posNewParty==pos-2)||(posNewParty==pos-1)||
						(posNewParty==pos+1)||(posNewParty==pos+2)) {
						posNewParty=random.nextInt(vectorOfParties.size());
					}
					int newParty=(vectorOfParties.get(posNewParty)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			} else if (vectorOfParties.size()==6) {
//System.out.println("\t size of vector of parties  == 6");
				/*
				** else (the currentParty is somewhere in the middle, and size equal to 6)
				** there is only one size for remainders, so probThird is no longer divided by 2.
				** either the third is at the beginning or at the end (meaning pos is either
				** at 3rd or at 4th position [that is 2 or 3 with counting from 0])
				*/
				double sum1=0;
				if (pos==3) {
					sum1=probThird100;
				}
				double sum2=sum1+(probSecond100/2);
				double sum3=sum2+(probFirst100/2);
				double sum4=sum2+probFirst100;
				double sum5=sum4+(probSecond100/2);
				// if val smaller than sum1, choose random between 0 and [pos-2]
				if (val<=sum1) {
					int posNewParty=pos;
					while((posNewParty==pos)||(posNewParty==pos-2)||(posNewParty==pos-1)||
						(posNewParty==pos+1)||(posNewParty==pos+2)) {
						posNewParty=random.nextInt(vectorOfParties.size());
					}
					int newParty=(vectorOfParties.get(posNewParty)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum1)&&(val<=sum2)) { // if val between sum1 and sum2, choose second adjacent (left)
					int newParty=(vectorOfParties.get(pos-2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum2)&&(val<=sum3)) {// if val between sum2 and sum3, choose first adjacent (left)
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum3)&&(val<=sum4)) { // if val between sum3 and sum4, choose first adjacent (right)
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if ((val>sum4)&&(val<=sum5)) { // if val between sum4 and sum5, choose second adjacent (right)
					int newParty=(vectorOfParties.get(pos+2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // else choose random between [pos+2] and size partiti
					int posNewParty=pos;
					while((posNewParty==pos)||(posNewParty==pos-2)||(posNewParty==pos-1)||
						(posNewParty==pos+1)||(posNewParty==pos+2)) {
						posNewParty=random.nextInt(vectorOfParties.size());
					}
					int newParty=(vectorOfParties.get(posNewParty)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			} else if (vectorOfParties.size()==5) {
//System.out.println("\t size of vector of parties  == 5");
				/*
				** else (the currentParty is somewhere in the middle, and size equal to 5)
				** there are only first or second adjacents: modify the probabilities
				*/
				double coef=100/(probFirst100+probSecond100);
				double sum1=coef*probSecond100/2;
				double sum2=sum1+(coef*probFirst100/2);
				double sum3=sum1+coef*probFirst100;
				double sum4=100;
				// if val smaller than sum1, choose second adjacent (left)
				if (val<=sum1) {
					int newParty=(vectorOfParties.get(pos-2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if (val<=sum2) { // if val between sum1 and sum2, choose first adjacent (left)
					int newParty=(vectorOfParties.get(pos-1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else if (val<=sum3) { // if val between sum3 and sum4, choose first adjacent (right)
					int newParty=(vectorOfParties.get(pos+1)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				} else { // if val between sum3 and sum4, choose second adjacent (right)
					int newParty=(vectorOfParties.get(pos+2)).intValue();
					vectorOfParties.remove(pos);
					currentParty=newParty;
					partyPreferences.addLast(new Integer(newParty));
				}
				// exit the loop!
				continue;
			}// end if
		}// end while
//System.out.println("end of while");
		vectorOfParties.remove(vectorOfParties.indexOf(new Integer(currentParty)));
		partyPreferences.addLast(vectorOfParties.get(0));
//System.out.println("end of getPreferences");
//System.out.println(partyPreferences);
		return partyPreferences;
	}// end getPreferences
	

	// function to find the preferences of the elector for candidates in the plurinominal party
	// use the list of partyPreferences. On a scale of 100, give limits for each party as follows:
	// - for the preferred party, give an interval of numbers from 1 to n1=100*probPreferred
	// - for the second party in the list, give an interval of numbers from n1+1 to n2=n1+100*probRem*probFirst
	// - for the third party in the list, give an interval of numbers from n2+2 to n3=n2+100*probRem*probSecond
	// - for the rest of the parties in the list, give an interval of numbers form n3+1 to 100
	// NOTE: adapt those intervals if necessary so that there are at least numCandidatiCollegio numbers in the interval
	// as the method is to delete each number after its use (so we do not enter an infinite loop looking for random
	// numbers). The maximum number may therefore be greater than 100 (represented by maxCounter)
	// The results are put in a map containing the name of the party as key, and as values an array with the
	// lower and upper limit. The key to use for "rest of parties in the list" is 9999. Create also a list with
	// the names of the parties concerned by this key 9999.
	// To create the actual list of preferred candidates, put first a candidate from the preferred party.
	// Then for all the remaining entries in the list, get a random number from 1 to 100. Find in the map
	// the party to which it refers (falling between its lower and upper limit). If it is the 9999 key, get
	// a random number on the list of "other" parties, and choose the corresponding party.

	LinkedList <Candidate> getVotersPreferencesForCandidates(LinkedList <Integer> partyPreferences, Random random) {

		LinkedList <Candidate> candidatePreferences=new LinkedList<Candidate>();

//System.out.println("get candidates preferences");
//System.out.println("map of candidates");
//long tb=System.currentTimeMillis();
		// create the map containing the candidates for each party: key is the name of the party,
		// values in a list of candidates
		HashMap <Integer,LinkedList<Candidate>>mapOfCandidates=getMapOfCandidates();
//long ta=System.currentTimeMillis();
//System.out.println("\tlasted "+(ta-tb));
//System.out.println(mapOfCandidates);		
		// use the list of partyPreferences. On a scale of 100, give limits for each party as follows:
		HashMap <Integer,int[]>tempMap=new HashMap<Integer,int[]>();
//		LinkedList remainingParties=(LinkedList)partyPreferences.clone();
		LinkedList <Integer> remainingParties = new LinkedList<Integer>();
		Iterator <Integer> irp = partyPreferences.iterator();
		while (irp.hasNext()) {
			remainingParties.add(irp.next());
		}
//System.out.println("boundaries");
//tb=System.currentTimeMillis();
		int counter=1;
		// - for the preferred party, give an interval of numbers from 1 to n1=(probPreferred-1)
		Integer party=partyPreferences.get(0);
		remainingParties.remove(party);
		int[] boundaries1=new int[2];
		boundaries1[0]=counter;
		counter+=(probPreferred*100)-1;
		boundaries1[1]=counter;
		tempMap.put(party,boundaries1);
//System.out.println("party "+party+" low "+boundaries1[0]+" high "+boundaries1[1]);
		// check whether there are more parties.
		// - for the second party in the list, give an interval of numbers from n1+1 to n2=(n1+probRem*probFirst-1)*10
		if (numberParties>1) {
			party=partyPreferences.get(1);
			remainingParties.remove(party);
			++counter;
			int[] boundaries2=new int[2];
			boundaries2[0]=counter;
			counter+=(probRem*probFirst*100)-1;
			boundaries2[1]=counter;
			tempMap.put(party,boundaries2);
//System.out.println("1- party "+party+" low "+boundaries2[0]+" high "+boundaries2[1]);
		}
		// - for the third party in the list, give an interval of numbers from n2+2 to n3=(n2+probRem*probSecond-1)*10
		if (numberParties>2) {
			party=partyPreferences.get(2);
			remainingParties.remove(party);
			++counter;
			int[] boundaries3=new int[2];
			boundaries3[0]=counter;
			counter+=(probRem*probSecond*100)-1;
			boundaries3[1]=counter;
			tempMap.put(party,boundaries3);
//System.out.println("2- party "+party+" low "+boundaries3[0]+" high "+boundaries3[1]);
		}
		// - for the rest of the parties in the list, give an interval of numbers form n3+1 to 100 (or n3+1, whichever is the largest)
		if (numberParties>3) {
			++counter;
			int[] boundaries4=new int[4];
			boundaries4[0]=counter;
			counter=Math.max(100,counter+1);
			boundaries4[1]=counter;
			tempMap.put(new Integer(9999),boundaries4);
//System.out.println("3- party "+party+" low "+boundaries4[0]+" high "+boundaries4[1]);
		}
//ta=System.currentTimeMillis();
//System.out.println("\tlasted "+(ta-tb));

//System.out.println("tempMap");
//Set tempSet=tempMap.keySet();
//Iterator tmp=tempSet.iterator();
//while(tmp.hasNext()) {
//Integer tempKey=(Integer)tmp.next();
//int[] arr=(int[])tempMap.get(tempKey);
//System.out.println(tempKey+" = 0 => "+arr[0]+" , 1 => "+arr[1]);
//}
		// adapt tempMap so that each interval is at least numberCandidates wide for the first 3 preferred
		// parties, and numberCandidates*(numberParties-3) for the others: at this point, the interval will
		// be larger than 100, hence need to set up maxCounter correctly
//System.out.println("adaptIntervals");
//tb=System.currentTimeMillis();
		tempMap=adaptIntervals(tempMap,partyPreferences,numberCandidates);
//ta=System.currentTimeMillis();
//System.out.println("\tlasted "+(ta-tb));
		// set up maxCounter (last element for key=9999), used for random numbers and create the vector of integers from 0 to maxcounter
//System.out.println("interval");
//tb=System.currentTimeMillis();
		int minCounter=((int[])tempMap.get(partyPreferences.get(0)))[0];
		int maxParty=(numberParties>3)?9999:((Integer)partyPreferences.get(numberParties-1)).intValue();
		int maxCounter=((int[])tempMap.get(new Integer(maxParty)))[1];
		LinkedList <Integer> interval=new LinkedList<Integer>();
		for(int i=minCounter;i<=maxCounter;++i) {
			interval.addFirst(new Integer(i));
		}
//ta=System.currentTimeMillis();
//System.out.println("\tlasted "+(ta-tb));
		// Now, attribute the candidates to the list candidatePreferences (global variable
		// created at the beginning of the class)
		// clone the map of candidates, to get the remainingCandidates map
//		HashMap remainingCandidates=(HashMap)mapOfCandidates.clone();
		HashMap <Integer,LinkedList<Candidate>> remainingCandidates = new HashMap<Integer,LinkedList<Candidate>>();
		Set <Integer> krc = mapOfCandidates.keySet();
		Iterator <Integer> ikrc = krc.iterator();
		while (ikrc.hasNext()) {
			Integer kkrc = ikrc.next();
			LinkedList<Candidate> vrc = mapOfCandidates.get(kkrc);
			remainingCandidates.put(kkrc,vrc);
		}
		// the first preference is the first candidate of the preferred party.
		// remove this candidate from the list in remainingCandidates
		Integer preferredParty=partyPreferences.get(0);
		LinkedList <Candidate> candidates=remainingCandidates.get(preferredParty);
		Candidate preferredCandidate=candidates.getFirst();
		candidatePreferences.addLast(preferredCandidate);
		candidates.remove(preferredCandidate);
		remainingCandidates.remove(preferredParty);
		remainingCandidates.put(preferredParty,candidates);
		// then for the other candidates, while the size of candidatesPreferences is smaller
		// than (numberCandidates*numberParties):
		// random number from 0 to size of interval vector, find the corresponding party in tempMap
		// the preferred candidate is the first in the list for the corresponding party
		// in remainingCandidates.
		// Note, when a list is empty, remove the party from remainigCandidates
		// use containsKey to find whether the party corresponding to the random number exists,
		// and if not, draw a new number.
		// remove the used number from interval vector: this way, no risk of going in an infinite loop looking
		// for a number in the right interval. At most, we consider maxCounter numbers. The random number found is
		// the index of the number considered.
		while (candidatePreferences.size()<(numberParties*numberCandidates)) {
//System.out.println("each iteration for candidates");
//tb=System.currentTimeMillis();
//System.out.println("interval\n"+interval.size());
			int index=random.nextInt(interval.size());
			int number=(interval.get(index)).intValue();
			// iterate through tempMap until number falls between the boundaries
			Set <Integer> keys=tempMap.keySet();
			Iterator <Integer> j=keys.iterator();
			while (j.hasNext()) {
				Integer key=j.next();
				int[] bounds=(int[])tempMap.get(key);
				int lower=bounds[0];
				int upper=bounds[1];
				if ((number>=lower)&&(number<=upper)) {
//System.out.println("found interval in map");
					// does the map remainingCandidates still contain candidates for the current party?
					if (key.intValue()!=9999) {
						candidates=remainingCandidates.get(key);
						if (candidates.size()!=0) {
//System.out.println("there are candidates");
							preferredCandidate=candidates.getFirst();
							candidatePreferences.addLast(preferredCandidate);
							candidates.remove(preferredCandidate);
							remainingCandidates.remove(key);
							remainingCandidates.put(key,candidates);
							interval.remove(index);

//							candidates=null;
							
						} else {// no more candidates for this party,  remove all numbers from this party's range
//System.out.println("no more candidates for this party,  remove all numbers from this party's range");
//System.out.println("range: "+lower+"-"+upper+"interval before\n"+interval);
							Iterator <Integer> inti=interval.iterator();
							while (inti.hasNext()) {
								int element=(inti.next()).intValue();
								if (((element>=lower)&&(element<=upper))) {
									inti.remove();
								}
							}
//System.out.println("interval after\n"+interval);
							continue;
						}
				} else {// key is 9999: some other party, new random number
//System.out.println("key is 9999: some other party, new random number");
						if (remainingParties.size()>0) {
							int numb=random.nextInt(remainingParties.size());
							key=remainingParties.get(numb);
							candidates=remainingCandidates.get(key);
							if (candidates.size()!=0) {
//System.out.println("there are candidates");
								preferredCandidate=candidates.getFirst();
								candidatePreferences.addLast(preferredCandidate);
								candidates.remove(preferredCandidate);
								remainingCandidates.remove(key);
								remainingCandidates.put(key,candidates);
								if (candidates.size()==0) {
									remainingParties.remove(key);
								}
//								candidates=null;
							} else {// no more candidates for this party, remove it from remainingCandidates
//System.out.println("there are no candidates 1");
								continue;
							}
						} else {// no more candidates for this party, remove it from remainingCandidates
//System.out.println("there are no candidates 2");
							continue;
						}
					}
				}//end if number within bounds
			}// end while j.hasNext
//ta=System.currentTimeMillis();
//System.out.println("\tlasted "+(ta-tb));
		}// end while on candidatesPreferences

//			Runtime.getRuntime().gc();
//			System.out.println(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		// allow to clean weak references
		mapOfCandidates=null;
		tempMap=null;
		remainingParties=null;
		party=null;
		preferredParty=null;
		candidates=null;
		preferredCandidate=null;
		// return
//System.out.println(candidatePreferences);
		return candidatePreferences;
	}// end of getCandidatePreferences


	public HashMap <Integer,int[]> adaptIntervals(HashMap <Integer,int[]> tempMap,LinkedList <Integer> partyPreferences,int numberCandidates) {
//System.out.println("adapt intervals");
		// find the proportions for each key of tempMap.
		int numberParties=partyPreferences.size();
		int maxParty=(numberParties>3)?9999:((Integer)partyPreferences.get(numberParties-1)).intValue();
		int maxBound=(tempMap.get(new Integer(maxParty)))[1];
		int minBound=(tempMap.get(partyPreferences.get(0)))[0];
		HashMap <Integer,Float> proportions=new HashMap<Integer,Float>();
		HashMap <Integer,Integer>ranges=new HashMap<Integer,Integer>();
		float sumP=0;
		// proportions for the preferred parties (maximum 3)
		int nbPreferredParties=(numberParties<=3)?numberParties:3;
		Integer party=new Integer(0);
		for (int i=0;i<nbPreferredParties;++i) {
			party=partyPreferences.get(i);
			int[] bound=(int[])tempMap.get(party);
			int range=bound[1]-bound[0]+1; // need the +1 as the bounds are inclusive of both boundary values
			ranges.put(party,new Integer(range));
			float prop=(float)range/maxBound;
			sumP+=prop;
			proportions.put(party,new Float(prop));
		}
		// for all the others (key=9999 in tempMap)
		if (numberParties>3) {
			party=new Integer(9999);
			int[] bound=tempMap.get(party);
			int range=bound[1]-bound[0]+1;
			ranges.put(party,new Integer(range));
			proportions.put(party,new Float(1-sumP));
		}

		// change intervals
		// for the 3 preferred parties
		for (int i=0;i<nbPreferredParties;++i) {
			party=partyPreferences.get(i);
			int range=(ranges.get(party)).intValue();
			if (range<numberCandidates) {
				// expand the range for the party
				range=numberCandidates;
				ranges.remove(party);
				ranges.put(party,new Integer(range));
				// REWORK ALL THE OTHER INTERVALS
				tempMap=reworkIntervals(tempMap,party,range,proportions,partyPreferences);
			}// end of if range<numberCandidates
		} // end of for int i <nbPreferredParties
		// for all the others
		if (numberParties>3) {
			party=new Integer(9999);
			int minRange=numberCandidates*(numberParties-3);
			int range=(ranges.get(party)).intValue();
			if (range<minRange) {
				range=minRange;
				ranges.remove(party);
				ranges.put(party,new Integer(range));
				// rework all the other intervals
				tempMap=reworkIntervals(tempMap,party,range,proportions,partyPreferences);
			}
		}
		// clean up weak references
		proportions=null;
		ranges=null;
		party=null;
		// return
//Set keys = tempMap.keySet();
//Iterator k = keys.iterator();
//while (k.hasNext()) {
//	Integer key=(Integer)k.next();
//	int[] values=(int[])tempMap.get(key);
//	System.out.println("key: "+key+" low "+values[0]+" high "+values[1]);
//}
		return tempMap;
	}

	HashMap <Integer,int[]> reworkIntervals(HashMap <Integer,int[]> tempMap,Integer party,int range,HashMap <Integer,Float> proportion,LinkedList <Integer> partyPreferences) {
		// first get the proportion for this party
		float prop=(proportion.get(party)).floatValue();
		// find the new max boundaries by dividing the new range by the proportion
		int maxBound=Math.round(range/prop);
		// new boundaries for preferred party:
		Integer partito=partyPreferences.get(0);
		int numberParties=partyPreferences.size();
		int[] bounds1=new int[2];
		bounds1[0]=(tempMap.get(partito))[0];
		bounds1[1]=Math.round(maxBound*((Float)proportion.get(partito)).floatValue());
		tempMap.remove(partito);
		tempMap.put(partito,bounds1);
		if (numberParties>1) {
			// new boundaries for second party in preferences
			partito=partyPreferences.get(1);
			int[] bounds2=new int[2];
			bounds2[0]=bounds1[1]+1;
			bounds2[1]=bounds1[1]+Math.round(maxBound*(proportion.get(partito)).floatValue());
			tempMap.remove(partito);
			tempMap.put(partito,bounds2);
//System.out.println("bounds for second party "+bounds2[0]+" - "+bounds2[1]);
			if (numberParties>2) {
				// new boundaries for third party in preferences
				partito=partyPreferences.get(2);
				int[] bounds3=new int[2];
				bounds3[0]=bounds2[1]+1;
				bounds3[1]=bounds2[1]+Math.round(maxBound*(proportion.get(partito)).floatValue());
				tempMap.remove(partito);
				tempMap.put(partito,bounds3);
//System.out.println("bounds for third party "+bounds3[0]+" - "+bounds3[1]);
				if (numberParties>3) {
					// new boundaries for all the other parties
					int[] bounds4=new int[2];
					bounds4[0]=bounds3[1]+1;
					bounds4[1]=maxBound;
					tempMap.remove(new Integer(9999));
					tempMap.put(new Integer(9999),bounds4);
//System.out.println("bounds for preferred party "+bounds4[0]+" - "+bounds4[1]);
				}// end of if numPartiti>3
			}// end of if numPartiti>2
		}// end of if numPartiti>1
		partito=null;
		return tempMap;
	}

	public HashMap <Integer,LinkedList<Candidate>> getMapOfCandidates() {
		HashMap <Integer,LinkedList<Candidate>> map=new HashMap<Integer,LinkedList<Candidate>>();
		for (int i=1;i<=numberParties;++i) {
			// create the list of candidates for the party
			LinkedList <Candidate> list=new LinkedList<Candidate>();
			for (int j=1;j<=numberCandidates;++j) {
				Candidate candidate=new Candidate(i,j);
				list.addLast(candidate);
			}
			map.put(new Integer(i),list);
			list=null;
		}
	return map;
	}

//	public void makeListOfCoalitions (final int indexOfGroupOfCoalitions,final SimulationRepository sr) {
//		final JDialog dialog = new JDialog(mainFrame,"",true);
//		final String  nameOfGroup = new String();
//		final LinkedList coalitions = new LinkedList();
//		final LinkedList newCoalitions = new LinkedList();
//
//		Coalition left = new Coalition(language.getString("labels","left"));
//		Coalition right = new Coalition(language.getString("labels","right"));
//		// separate arrayOfParties into the first 50% and the second 50% creating two default coalitions "sinistra" and "destra"
//		int sumquote = 0;
//		for (int i=0;i<arrayOfParties.size();++i) {
//			Party aParty = (Party)arrayOfParties.get(i);
//			sumquote += aParty.getShare();
//			if (sumquote<=50) {
//				left.addParty(aParty);
//			} else {
//				right.addParty(aParty);
//			}
//		}
//		coalitions.add(left);
//		coalitions.add(right);
//		// add empty rows
//		int r=coalitions.size()+1;
//		while (coalitions.size()<arrayOfParties.size()) {
//			Coalition other = new Coalition("C"+r);
//			coalitions.add(other);
//			++r;
//		}
//
//
//		// show the table of the coalitions
//		JPanel panel = new JPanel();
//		GridBagLayout gridbag=new GridBagLayout();
//		GridBagConstraints c=new GridBagConstraints();
//		c.fill=GridBagConstraints.HORIZONTAL;
//		panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
//		panel.setLayout(gridbag);
//		c.gridx = 0;
//		c.gridy = 0;
//		panel.add(new TitleLabel(language.getString("labels","coalitionNameGroup")));
//		++c.gridy;
//		final JTextField nameg = new JTextField((new Integer(indexOfGroupOfCoalitions)).toString());
//		panel.add(nameg,c);
//		++c.gridy;
//		panel.add(new JLabel(language.getString("labels","coalitionDefinition")),c);
//		++c.gridy;
//		// show a table representing these coalitions with configurable names, and radio boxes, and a few blank lines
//		final JTable coalitionTable = new JTable(new TableData(arrayOfParties,coalitions));
//
//		JScrollPane coalitionScrollPane = new JScrollPane(coalitionTable);
//		coalitionTable.setPreferredScrollableViewportSize(new Dimension(300,100));
//		panel.add(coalitionScrollPane,c);
//		
//		Action saveCoalition = new AbstractAction(language.getString("labels","save")) {
//			public void actionPerformed(ActionEvent e) {
////System.out.println("cvo before");
////Iterator j=coalitions.iterator();
////while (j.hasNext()) {
////Coalition coal=(Coalition)j.next();
////System.out.println(coal);
////}
//				// check that there is a name for the group
//				String newnameg = nameg.getText();
////System.out.println("cvo newname g "+ newnameg+" trimmed "+newnameg.trim()+" has length "+ (newnameg.trim()).length());
//				if ((newnameg.trim()).length()>0) {
////System.out.println("cvo greater than 1, create coalitions");
//					// save the coalitions
////System.out.println("created empty list of coalitions");
////					LinkedList newCoalitions = new LinkedList();
////System.out.println("cvo create list of parties");
//					ArrayList partiesToUse = (ArrayList)arrayOfParties.clone();
////System.out.println("cvo number of parties "+partiesToUse.size());
//					// recreate coalitions from the rows of the table: double loop on rows and columns
//					for (int r=0;r<coalitionTable.getRowCount();++r) {
////System.out.println("cvo r: "+r+" size of list "+coalitions.size());
////System.out.println("cvo parties to use:");
////j=partiesToUse.iterator();
////while (j.hasNext()) {
////Party pa=(Party)j.next();
////System.out.println(pa);
////}
//						Coalition currentCoa;
//						if (coalitions.size()>0) {
//							currentCoa = (Coalition)coalitions.getFirst();
//							coalitions.removeFirst();
//							currentCoa.removeParties();
//						} else {
//							currentCoa = new Coalition();
//						}
//						currentCoa.setNameOfGroupOfCoalitions(newnameg);
//						currentCoa.setIndexOfGroupOfCoalitions(indexOfGroupOfCoalitions);
////	System.out.println("currentcoa :"+currentCoa);
//						String coaName = (String)coalitionTable.getValueAt(r,0);
//						currentCoa.setNameOfCoalition(coaName);
////	System.out.println("name of coalition "+coaName);
//						int partiesInCoalition = 0;
//						for (int c=1;c<coalitionTable.getColumnCount();++c) {
//							Boolean val = (Boolean)coalitionTable.getValueAt(r,c);
//							if (val.booleanValue()==true) {
//								String partyName = (String)coalitionTable.getColumnName(c);
////	System.out.println("name of party in coalition "+partyName);
//								for (int p=0;p<arrayOfParties.size();++p) {
////	System.out.println("considering the "+p+"th party");
//									Party party = (Party) arrayOfParties.get(p);
//									if ((party.getNameParty().compareTo(partyName))==0) {
////	System.out.println("this party is in the coalition");
//										currentCoa.addParty(party);
////	System.out.println("remove from partiesToUse");
//										partiesToUse.remove(party);
////	System.out.println("increment parties in coalition");
//										++partiesInCoalition;
////	System.out.println("break loop");
//										break;
//									}
//								}
//							}
//						}
//						if (partiesInCoalition>0) {
//							newCoalitions.add(currentCoa);
//						}
//					}
//					if (partiesToUse.size()>0) {
////	System.out.println("attention: some parties do not belong to any coalition");
//						Iterator p = partiesToUse.iterator();
//						while (p.hasNext()) {
//							Party party = (Party)p.next();
//							Coalition coa = new Coalition(party.getNameParty(),party);
//							newCoalitions.add(coa);
//						}
//					}
////System.out.println("after");
////j=newCoalitions.iterator();
////while (j.hasNext()) {
////Coalition coal=(Coalition)j.next();
////System.out.println(coal);
////}
//
//				// need to save the coalitions -> get the data from the table overwrite the current coalitions
//				sr.saveCoalitions(newCoalitions,indexOfGroupOfCoalitions);
//				dialog.setVisible(false);
//				}
// 			}
//		};
//			
//		++c.gridy;
//		JButton button = new JButton(language.getString("labels","save"));
//		button.addActionListener(saveCoalition);
//		panel.add(button,c);
//
//		dialog.getContentPane().add(panel);
//		dialog.setLocation(mainFrame.getX()+mainFrame.WIDTH/2,mainFrame.getY()+mainFrame.HEIGHT/2);
//		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//
//		dialog.pack();
//		dialog.setVisible(true);
//
//	}


	public void makeListOfCoalitions (final SimulationRepository sr) {
		final JDialog dialog = new JDialog(mainFrame,"",true);
		final HashMap <Integer,LinkedList<Coalition>> mapOfGroupsOfCoalitions = sr.getMapOfGroupsOfCoalitions();
		final String  nameOfGroup = new String();
		final LinkedList <Coalition> coalitions = new LinkedList<Coalition>();
		final LinkedList <Coalition> newCoalitions = new LinkedList<Coalition>();
		final int indexOfGroupOfCoalitions = mapOfGroupsOfCoalitions.size()+1;
		final ButtonGroup bg = new ButtonGroup();

		Coalition left = new Coalition(language.getString("labels","left"));
		Coalition right = new Coalition(language.getString("labels","right"));
		// separate arrayOfParties into the first 50% and the second 50% creating two default coalitions "sinistra" and "destra"
		int sumquote = 0;
		for (int i=0;i<arrayOfParties.size();++i) {
			Party aParty = arrayOfParties.get(i);
			sumquote += aParty.getShare();
			if (sumquote<=50) {
				left.addParty(aParty);
			} else {
				right.addParty(aParty);
			}
		}
		coalitions.add(left);
		coalitions.add(right);
		// add empty rows
		int r=coalitions.size()+1;
		while (coalitions.size()<arrayOfParties.size()) {
			Coalition other = new Coalition("C"+r);
			coalitions.add(other);
			++r;
		}


		// show the table of the coalitions
		JPanel panel = new JPanel();
		GridBagLayout gridbag=new GridBagLayout();
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		panel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		if (mapOfGroupsOfCoalitions.size()>0) {
			panel.add(new TitleLabel(language.getString("labels","chooseCoalition")),c);
			++c.gridy;
			Set <Integer> keys = mapOfGroupsOfCoalitions.keySet();
			Iterator <Integer> k = keys.iterator();
			while (k.hasNext()) {
				int indxcoa = (k.next()).intValue();
				LinkedList <Coalition> listCoa = sr.getListOfCoalitions(indxcoa);
				String namegcoa = sr.getNameOfGroupOfCoalitions(indxcoa);
				String out = "<html><b>"+language.getString("labels","coalitionNameGroup")+": "+namegcoa+"</b><br>";
				for (int ic=0;ic<listCoa.size();++ic) {
					Coalition coa = listCoa.get(ic);
					out+=coa.toStringDisplay();
				}
				JRadioButton rb = new JRadioButton(out);
				rb.setActionCommand((new Integer(indxcoa)).toString());
				bg.add(rb);
				panel.add(rb,c);
				++c.gridy;
			}


			Action chooseCoalition = new AbstractAction(language.getString("labels","choose")) {
				public void actionPerformed(ActionEvent e) {
				String command = bg.getSelection().getActionCommand();
				int idxc = (new Integer(command)).intValue();
				if ((idxc>0) && (idxc<=mapOfGroupsOfCoalitions.size())) {
					// need to save the coalitions -> get the data from the table overwrite the current coalitions
					sr.saveCoalitions(mapOfGroupsOfCoalitions.get(new Integer(idxc)),idxc);
					dialog.setVisible(false);
					}
	 			}
			};
				
			JButton button = new JButton(language.getString("labels","choose"));
			button.addActionListener(chooseCoalition);
			panel.add(button,c);
			++c.gridy ;

			
		}
		panel.add(new TitleLabel(language.getString("labels","createCoalition")),c);
		++c.gridy;
		panel.add(new JLabel(language.getString("labels","coalitionNameGroup")),c);
		++c.gridy;
		final JTextField nameg = new JTextField((new Integer(indexOfGroupOfCoalitions)).toString());
		panel.add(nameg,c);
		++c.gridy;
		panel.add(new JLabel(language.getString("labels","coalitionDefinition")),c);
		++c.gridy;
		// show a table representing these coalitions with configurable names, and radio boxes, and a few blank lines
		final JTable coalitionTable = new JTable(new TableData(arrayOfParties,coalitions));

		JScrollPane coalitionScrollPane = new JScrollPane(coalitionTable);
//		coalitionTable.setPreferredScrollableViewportSize(new Dimension(300,100));
		panel.add(coalitionScrollPane,c);
		
		Action saveCoalition = new AbstractAction(language.getString("labels","save")) {
			public void actionPerformed(ActionEvent e) {
//System.out.println("cvo before");
//Iterator j=coalitions.iterator();
//while (j.hasNext()) {
//Coalition coal=(Coalition)j.next();
//System.out.println(coal);
//}
				// check that there is a name for the group
				String newnameg = nameg.getText();
//System.out.println("cvo newname g "+ newnameg+" trimmed "+newnameg.trim()+" has length "+ (newnameg.trim()).length());
				if ((newnameg.trim()).length()>0) {
//System.out.println("cvo greater than 1, create coalitions");
					// save the coalitions
//System.out.println("created empty list of coalitions");
//					LinkedList newCoalitions = new LinkedList();
//System.out.println("cvo create list of parties");

//					ArrayList partiesToUse = (ArrayList)arrayOfParties.clone();
					ArrayList <Party> partiesToUse = new ArrayList <Party> ();
					for (int i = 0; i<arrayOfParties.size();++i) {
						Party party = arrayOfParties.get(i);
						partiesToUse.add(i,party);
					}
//System.out.println("cvo number of parties "+partiesToUse.size());
					// recreate coalitions from the rows of the table: double loop on rows and columns
					for (int r=0;r<coalitionTable.getRowCount();++r) {
//System.out.println("cvo r: "+r+" size of list "+coalitions.size());
//System.out.println("cvo parties to use:");
//j=partiesToUse.iterator();
//while (j.hasNext()) {
//Party pa=(Party)j.next();
//System.out.println(pa);
//}
						Coalition currentCoa;
						if (coalitions.size()>0) {
							currentCoa = coalitions.getFirst();
							coalitions.removeFirst();
							currentCoa.removeParties();
						} else {
							currentCoa = new Coalition();
						}
						currentCoa.setNameOfGroupOfCoalitions(newnameg);
						currentCoa.setIndexOfGroupOfCoalitions(indexOfGroupOfCoalitions);
//	System.out.println("currentcoa :"+currentCoa);
						String coaName = (String)coalitionTable.getValueAt(r,0);
						currentCoa.setNameOfCoalition(coaName);
//	System.out.println("name of coalition "+coaName);
						int partiesInCoalition = 0;
						for (int c=1;c<coalitionTable.getColumnCount();++c) {
							Boolean val = (Boolean)coalitionTable.getValueAt(r,c);
							if (val.booleanValue()==true) {
								String partyName = (String)coalitionTable.getColumnName(c);
//	System.out.println("name of party in coalition "+partyName);
								for (int p=0;p<arrayOfParties.size();++p) {
//	System.out.println("considering the "+p+"th party");
									Party party = (Party) arrayOfParties.get(p);
									if ((party.getNameParty().compareTo(partyName))==0) {
//	System.out.println("this party is in the coalition");
										currentCoa.addParty(party);
//	System.out.println("remove from partiesToUse");
										partiesToUse.remove(party);
//	System.out.println("increment parties in coalition");
										++partiesInCoalition;
//	System.out.println("break loop");
										break;
									}
								}
							}
						}
						if (partiesInCoalition>0) {
							newCoalitions.add(currentCoa);
						}
					}
					if (partiesToUse.size()>0) {
//	System.out.println("attention: some parties do not belong to any coalition");
						Iterator <Party> p = partiesToUse.iterator();
						while (p.hasNext()) {
							Party party = p.next();
							Coalition coa = new Coalition(party.getNameParty(),party);
							newCoalitions.add(coa);
						}
					}
//System.out.println("after");
//j=newCoalitions.iterator();
//while (j.hasNext()) {
//Coalition coal=(Coalition)j.next();
//System.out.println(coal);
//}

				// need to save the coalitions -> get the data from the table overwrite the current coalitions
				sr.saveCoalitions(newCoalitions,indexOfGroupOfCoalitions);
				dialog.setVisible(false);
				}
 			}
		};
			
		++c.gridy;
		JButton button = new JButton(language.getString("labels","save"));
		button.addActionListener(saveCoalition);
		panel.add(button,c);

		dialog.getContentPane().add(panel);
		dialog.setLocation(mainFrame.getX()+mainFrame.WIDTH/2,mainFrame.getY()+mainFrame.HEIGHT/2);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		dialog.pack();
		dialog.setVisible(true);

	}

	

	class TableData extends AbstractTableModel {
		String[] parties;
		Object[][] vector;
		public TableData(ArrayList <Party> arrayOfParties,LinkedList <Coalition> listOfCoalitions) {
			parties = new String[arrayOfParties.size()+1];
			vector = new Object[listOfCoalitions.size()][arrayOfParties.size()+1];
			// names of columns = names of all parties
			parties[0] = "coalitions";//language.getString("labels","coalitions");
			for (int p=0; p<arrayOfParties.size(); ++p) {
				Party party = arrayOfParties.get(p);
				parties[p+1] = party.getNameParty();
			}
			for (int i=0;i<listOfCoalitions.size();++i) {
				Coalition coa = listOfCoalitions.get(i);
				String coaName = coa.getNameOfCoalition();
				vector[i][0] = coaName;
				for (int p=0; p<arrayOfParties.size(); ++p) {
					Party party = arrayOfParties.get(p);
					vector[i][p+1] = new Boolean(false);
					if (coa.containsParty(party)) {
						vector[i][p+1] = new Boolean(true);
					}
				}
			}
		}	

		
		public int getColumnCount() {
			return parties.length;
		}
	
		public int getRowCount() {
			return vector.length;
		}
	
		public String getColumnName(int col) {
			return parties[col];
		}

		public boolean isCellEditable(int r,int c) {
			return true;
		}
		
		public Object getValueAt(int r,int c) {
			return vector[r][c];
		}

		public void setValueAt(Object val,int r,int c) {
			if (val instanceof Boolean) {
				boolean bval = ((Boolean)val).booleanValue();
				vector[r][c] = new Boolean(bval);
				fireTableCellUpdated(r,c);
				if (bval==true) {
					for (int i=0; i<getRowCount();++i) {
						if (i!=r) {
							vector[i][c] = new Boolean(false);
							fireTableCellUpdated(i,c);
						}
					}
				}
				
			} else {
				vector[r][c] = val;
				fireTableCellUpdated(r,c);
			}
		}
		
		public Class getColumnClass(int c) {
			return getValueAt(0,c).getClass();
		}

	
	}

	public void makeDistances(ArrayList <Party> arrayOfParties) {
		JLabel distanceLabel;
		NumberFormat proportionFormat=NumberFormat.getNumberInstance();
		proportionFormat.setMinimumFractionDigits(0);
		proportionFormat.setMaximumFractionDigits(2);

		final JDialog dialog = new JDialog(mainFrame,"",true);
		// create a panel with the data;
		JPanel part = new JPanel();
        part.setLayout(new GridLayout(0, 2));
		part.add(new JLabel(language.getString("labels","distance")));
		part.add(new JLabel(""));
		for (int i=0; i<arrayOfParties.size(); ++i) {
			final Party party = arrayOfParties.get(i);
			final double distance = party.getDistance();
			// for the distance
			final DecimalField distanceField = new DecimalField(distance,4,proportionFormat);
			distanceLabel = new SubMenuLabel(party.getNameParty());
			distanceLabel.setToolTipText(language.getString("labels","distanceToolTip"));
			distanceField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					// look at the values entered in the field and check that they are
					// acceptable. If not, put the last known correct value in the field
					double nuovaDistance = distanceField.getValue();
					if (nuovaDistance < 0) {
						JOptionPane.showMessageDialog(null,language.getString("labels","negativeDistance"));
						distanceField.setValue(distance);
					} else if (nuovaDistance > 100){
						JOptionPane.showMessageDialog(null,language.getString("labels","distanceTooLarge"));
						distanceField.setValue(distance);
					} else {
						distanceField.setValue(nuovaDistance);
						party.setDistance(nuovaDistance);
					}
				}
			});
			part.add(distanceLabel);
			part.add(distanceField);
		}// end loop on parties
		JButton button = new JButton(language.getString("labels","save"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dialog.setVisible(false);
			}
		});
		
		part.add(button);
		dialog.getContentPane().add(part);
		dialog.setLocation(mainFrame.getX()+mainFrame.WIDTH/2,mainFrame.getY()+mainFrame.HEIGHT/2);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		dialog.pack();
		dialog.setVisible(true);
	}

}
