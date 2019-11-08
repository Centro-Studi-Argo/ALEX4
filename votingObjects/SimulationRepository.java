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
import com.tomtessier.scrollabledesktop.*;
import parliaments.*;

public class SimulationRepository {
	private HashMap<String,Object> generalParameters = new HashMap<String,Object>();
	private ArrayList <Party> arrayOfParties = new ArrayList <Party> ();
	private LinkedList<Candidate> listOfCandidates = new LinkedList<Candidate>();
	private LinkedList<Voter> listOfVoters = new LinkedList<Voter>();
	private LinkedList<DistrictUninominal> listOfUninominalDistricts = new LinkedList<DistrictUninominal>();
	private LinkedList<DistrictPlurinominal> listOfPlurinominalDistricts = new LinkedList<DistrictPlurinominal>();
//	private LinkedList listOfCoalitions = new LinkedList();
	private HashMap <Integer,LinkedList<Coalition>> mapOfGroupsOfCoalitions = new HashMap <Integer,LinkedList<Coalition>> ();
	private String name;
	private String description;
	Language language = Language.getInstance();
	LinkedList listOfSimulations = ListOfSimulations.getInstance();
	MainFrame mainFrame = MainFrame.getInstance();
	private boolean repositoryComplete = true;
	private boolean simulationPossible = false;
	private LinkedList stuffToSimulate = new LinkedList();
	private HashMap <String,Parliament> parliaments = new HashMap<String,Parliament>();
	private int indxCoalitionToUse = 0;
	
	// constructor
	
	public SimulationRepository(HashMap<String,Object> gp,ArrayList <Party> ap,LinkedList<Candidate> lc,LinkedList<Voter> lv,LinkedList<DistrictUninominal> lu,
		LinkedList<DistrictPlurinominal> lp,String n,String d) {


		// objects
		generalParameters = gp;
		arrayOfParties = ap;
		listOfCandidates = lc;
		listOfVoters = lv;
		listOfUninominalDistricts = lu;
		listOfPlurinominalDistricts = lp;
		// if the name passed to the simulation is null, give a default
		name = (n==null)? language.getString("labels","defaultStartOfName")+(listOfSimulations.size()+1) : n;
		description = (d==null)? "" : d;
	}

	// constructor when loaded from file
	public SimulationRepository() {
	}

	// method to set the contents of the repository
	public void setName(String n, String d) {
		name = n;
		description = d;
	}

	public void setGeneralParameters(HashMap<String,Object> gp) {
		generalParameters = gp;
	}

	public void setParties(ArrayList <Party> ap) {
		arrayOfParties = ap;
System.out.println("parties"+arrayOfParties.size());
	}

	public void setVoters(LinkedList<Voter> lv) {
		listOfVoters = lv;
System.out.println("voters"+listOfVoters.size());
	}

	public void setUninominalDistricts(LinkedList<DistrictUninominal> lu) {
		listOfUninominalDistricts = lu;
System.out.println("uninominalDistricts "+listOfUninominalDistricts.size());
	}

	public void setCandidates(LinkedList<Candidate> lc) {
		listOfCandidates = lc;
	}

	public void setPlurinominalDistricts(LinkedList<DistrictPlurinominal> lp) {
System.out.println("plurinominal districts are: "+listOfPlurinominalDistricts.size());
		listOfPlurinominalDistricts = lp;
	}
	
	/*
	 * show the list of parliaments available with this repository
	 */
	public void showSimulationObjects() {
		ListOfParliaments listOfParliaments = new ListOfParliaments(this);

		listOfParliaments.showListOfParliaments();
	
	}

	// functions dealing with the saving of parliaments when allocation of seats is created, or their loading from the map

	public HashMap<Integer,Integer> loadParliament(String key) {
		Parliament parl = (Parliament)parliaments.get(key);
		return (parl.getAllocationOfSeats());
	}

	public boolean containsParliament(String key) {
System.out.println("in repository: size of list of parliaments "+parliaments.size()+"\ncontains parliament "+key+": "+parliaments.containsKey(key));
		return parliaments.containsKey(key);
	}

	// done from parliament, so only allocation of seats is known
	public void saveParliament(String key, Parliament parliament) {
System.out.println("\n---------SAVING PARLIAMENT  "+key+"\n-------------\n");
		if (!parliaments.containsKey(key)) {
			parliaments.put(key,parliament);
		}
	}

	public HashMap<String,Parliament> getParliaments() {
		return parliaments;
	}

	public LinkedList<Coalition> getListOfCoalitions(int indexOfGroupOfCoalitions) {
		return mapOfGroupsOfCoalitions.get(new Integer(indexOfGroupOfCoalitions));
	}

	public LinkedList<Coalition> getCoalitionToUse() {
		return mapOfGroupsOfCoalitions.get(new Integer(indxCoalitionToUse));
	}
	
	public HashMap<Integer,LinkedList<Coalition>> getMapOfGroupsOfCoalitions() {
		return mapOfGroupsOfCoalitions;
	}

	public String getNameOfGroupOfCoalitions(int indexOfGroupOfCoalitions) {
		LinkedList <Coalition>lc = mapOfGroupsOfCoalitions.get(new Integer(indexOfGroupOfCoalitions));
		Coalition coa = lc.getFirst();
		return coa.getNameOfGroupOfCoalitions();
	}
	
	// add the coalition to the list corresponding to the name of the group of coalitions
	public void addCoalition (Coalition coa) {
		int nameg = coa.getIndexOfGroupOfCoalitions(); 
		LinkedList <Coalition> listOfCoalitions = new LinkedList <Coalition> ();
		if (mapOfGroupsOfCoalitions.containsKey(new Integer(nameg))) {// a coalition exist, remove the entry from map (it will be put back again later)
			listOfCoalitions = mapOfGroupsOfCoalitions.get(new Integer(nameg));
			mapOfGroupsOfCoalitions.remove(new Integer(nameg));
		} 
		listOfCoalitions.add(coa);
		mapOfGroupsOfCoalitions.put(new Integer(nameg),listOfCoalitions);
	}

	
	public void saveCoalitions (LinkedList<Coalition> listcoa,int nameg) {
System.out.println("ENTERED SAVECOALITIONS");
		LinkedList <Coalition> listOfCoalitions = new LinkedList <Coalition> ();
		// remove the coalitions where listofParties is empty
		for (int i=0;i<listcoa.size();++i) {
			Coalition coa = (Coalition)listcoa.get(i);
System.out.println("name for group "+nameg);
			int nbparties = (coa.getListOfParties()).size();
System.out.println("there are "+nbparties+" in the coalition "+coa.getNameOfCoalition());
			if (nbparties>0) {
System.out.println("coalition is saved");
				listOfCoalitions.add(coa);
			}
		}
System.out.println("finished creating the list of coalitions, add to map");
		if (mapOfGroupsOfCoalitions.containsKey(new Integer(nameg))) {
System.out.println("map already contains a list with this name");		
			mapOfGroupsOfCoalitions.remove(new Integer(nameg));
		}
System.out.println("put in map");
		mapOfGroupsOfCoalitions.put(new Integer(nameg),listOfCoalitions);
		indxCoalitionToUse=nameg;
	}
	
	/*
	 * methods to check whether a repository (loaded from file) is complete, and to call the corresponding simulation if necessary
	 */
	 // if the repository is complete, set repositoryComplete to true
	 // if it is not complete but the missing stuff can be simulated, set simulationPossible to true
	 // in this case, set also the list of stuff to simulate and call the correct functions
	public void checkRepository() {
		int checkRepositoryComplete = 0;
		// the minimum a repository can have is generalParameters, which must be complete (9 variables, they have been checked before)
		checkRepositoryComplete += (generalParameters.size() == 9) ? 1 : 0;
System.out.println("size of general parameters: "+generalParameters.size());
System.out.println("checkRepositoryComplete - gp: "+checkRepositoryComplete);
		// are there parties?
		checkRepositoryComplete += (arrayOfParties.size() > 0) ? 1 : 0;
System.out.println("checkRepositoryComplete - parties: "+checkRepositoryComplete);
		// are there voters? if voters but no parties, recreate parties from voters (get shares and names)
		// if no voters and no parties, set both to be simulated
		checkRepositoryComplete += (listOfVoters.size()>0)? 1 : 0;
System.out.println("checkRepositoryComplete - voters: "+checkRepositoryComplete);
		// are there uninominal districts?
		checkRepositoryComplete += (listOfUninominalDistricts.size() > 0) ? 1 : 0;
System.out.println("checkRepositoryComplete - districts u: "+checkRepositoryComplete);
		// if there are, but there were no voters simulated, ignore their list of voters and re-create it
		// should there be candidates and plurinominal districts?
		if (((Integer)generalParameters.get("numberCandidates")).intValue() > 1) {
			// are there candidates?
			checkRepositoryComplete += (listOfCandidates.size() > 0) ? 1 : 0;
System.out.println("checkRepositoryComplete - cand: "+checkRepositoryComplete);
			// are there plurinominal districts?
			checkRepositoryComplete += (listOfPlurinominalDistricts.size() > 0) ? 1 : 0;
System.out.println("checkRepositoryComplete - disctricts p: "+checkRepositoryComplete);
		}
System.out.println("checkRepositoryComplete: "+checkRepositoryComplete);
		repositoryComplete = ((((Integer)generalParameters.get("numberCandidates")).intValue() > 1)) ? ((checkRepositoryComplete == 6) ? true : false ) : ((checkRepositoryComplete == 4) ? true : false );
		// if repository is not complete, see whether it is possible to attempt simulation of missing values/objects
		if (repositoryComplete == false) {
			// if some general parameters are missing: simulation impossible
			if (generalParameters.size() != 9) {
				simulationPossible = false;
			} else {
				simulationPossible = true;
			}
		} else {
			simulationPossible = false;
		}
	}
	
	public boolean getRepositoryComplete() {
		return repositoryComplete;
	}
	
	public boolean getSimulationPossible() {
		return simulationPossible;
	}

	public String simulateMissing() {
		int nbParties = ((Integer)generalParameters.get("numberParties")).intValue();
		int nbVoters = ((Integer)generalParameters.get("totalNumberVoters")).intValue();
		// are there parties?
		if ((arrayOfParties.size() != nbParties) && (listOfVoters.size() != nbVoters)) {
			// now, create a new window with the details of the parties
			ParametersParties partyParam = new ParametersParties();
			JPanel partyPanel = partyParam.setupPartyParameters(generalParameters);
			mainFrame.getScrollableDesktop().add(language.getString("labels","partyParameters"),partyPanel);
		} else {

			if (arrayOfParties.size() != nbParties) {
				getPartiesFromFirstPreferences();
			}
			CreatorVotingObjects creatorVotingObjects = new CreatorVotingObjects(generalParameters,arrayOfParties,new JProgressBar(),this);
			LinkedList <String> what = new LinkedList <String>();
			// are there voters? if not, call the simulation
			if (listOfVoters.size() != ((Integer)generalParameters.get("totalNumberVoters")).intValue()) {
				// call simulateVoters
				what.add("votersParties");
				creatorVotingObjects.createVotingObjects(what);
				what = new LinkedList <String> ();
			}
			// are there uninominal districts?
			if (listOfUninominalDistricts.size() != ((Integer)generalParameters.get("numberUninominalDistricts")).intValue()) {
				// simulate districts
				what.add("uninominalDistricts");
				creatorVotingObjects.createVotingObjects(what);
				what = new LinkedList <String> ();
	//		} else { // check whether the districts have voters. if not do this part of the simulation
			}
			// are there candidates (if need be)?
			int nbCand = ((Integer)generalParameters.get("numberCandidates")).intValue();
			if (nbCand > 1) {
				if (listOfCandidates.size() != nbCand) {
					// create candidates from arrayOfParties
					what.add("candidates");
					creatorVotingObjects.createVotingObjects(what);
					what = new LinkedList <String> ();
				}
			}
			// are there plurinominal districts (if need be?)
			if (nbCand>1) {
				if (listOfPlurinominalDistricts.size() != ((Integer)generalParameters.get("numberPlurinominalDistricts")).intValue()) {
					// simulate plurinominal from uninominal districts.
					what.add("plurinominalDistricts");
					creatorVotingObjects.createVotingObjects(what);
					what = new LinkedList <String> ();
				}
			}
			return "OK";
		}
		return "";
	}

	private boolean getPartiesFromFirstPreferences() {
		// get first preferences of all voters (create a map with "name" of party and number voters with this party as first preference as value)
		// when the map is created, get the share dividing the number of votes by the total number of voters.
		// if some parties are missing, create them with a share of 0.
		// note that the parties created in this way are not major and will not be concentrated in districts
		HashMap <Integer,Integer> partiesAndVotes = new HashMap <Integer,Integer> ();
		Iterator<Voter> i = listOfVoters.iterator();
		while (i.hasNext()) {
			Voter v = (Voter)i.next();
			Integer firstPreference = new Integer(v.getFirstPartyPreference());
			if (partiesAndVotes.containsKey(firstPreference)) {
				int votes = (partiesAndVotes.get(firstPreference)).intValue() + 1;
				partiesAndVotes.remove(firstPreference);
				partiesAndVotes.put(firstPreference,new Integer(votes));
			} else {
				partiesAndVotes.put(firstPreference,new Integer(1));
			}
		}// end while on voters
		for (int j= 1; j <= ((Integer)generalParameters.get("numberParties")).intValue() ; ++j) {
			if (partiesAndVotes.containsKey(new Integer(j))) {
				double share = (double)100*((Integer)partiesAndVotes.get(new Integer(j))).intValue()/((Integer)generalParameters.get("totalNumberVoters")).intValue();
System.out.println("simulateMissing: share "+share);
				arrayOfParties.add(new Party(j,new String(language.getString("labels","party")+" "+new Integer(j)),generalParameters,share,0,1,false,0));
			} else {
				arrayOfParties.add(new Party(j,new String(language.getString("labels","party")+" "+new Integer(j)),generalParameters,0,0,1,false,0));
			}
		}
		return true;
	}
	
	// methods to return the votingObjects lists and the name and description of the simulation repository
	public HashMap <String,Object> getGeneralParameters() {
		return generalParameters;
	}

	public ArrayList<Party> getArrayOfParties() {
		return arrayOfParties;
	}

	public LinkedList<Candidate> getListOfCandidates() {
		return listOfCandidates;
	}

	public LinkedList<Voter> getListOfVoters() {
		return listOfVoters;
	}

	public LinkedList<DistrictUninominal> getListOfUninominalDistricts() {
		return listOfUninominalDistricts;
	}

	public LinkedList<DistrictPlurinominal> getListOfPlurinominalDistricts() {
		return listOfPlurinominalDistricts;
	}

	public String getRepositoryName() {
		return name;
	}

	public String getRepositoryDescription() {
		return description;
	}

	public String toStringSimulationParameters() {
		StringBuffer out = new StringBuffer();
		// general parameters
		Set<String> keys = generalParameters.keySet();
		Iterator<String> i = keys.iterator();
		while (i.hasNext()) {
			String key = i.next();
			Object value = generalParameters.get(key);
			out.append(key+" : " + value.toString() + " :// " + language.getString("labels",key) + "\n");
		}
		out.append("\n");
		// parties
		Iterator<Party> p = arrayOfParties.iterator();
		while (p.hasNext()) {
			Party party = p.next();
			out.append(party.toString());
		}
		return out.toString();		
	}
	
	public String toString() {
		StringBuffer out = new StringBuffer();
		// name and description
		out.append("nameRepository : " + name + "\n");
		out.append("descriptionRepository : " + description + "\n");
		// simulation parameters
		out.append(toStringSimulationParameters());
		out.append("\n");
		// candidates
		Iterator<Candidate> ic = listOfCandidates.iterator();
		while (ic.hasNext()) {
			Candidate candidate = ic.next();
			out.append(candidate.toStringFull()+"\n");
		}
		out.append("\n");
		// voters: preferences for parties
		Iterator<Voter>iv = listOfVoters.iterator();
		while (iv.hasNext()) {
			Voter voter = iv.next();
			out.append(voter.toStringParties());
		}
		out.append("\n");
		// voters: preferences for candidates
		iv = listOfVoters.iterator();
		while (iv.hasNext()) {
			Voter voter = iv.next();
			out.append(voter.toStringCandidates());
		}
		out.append("\n");
		// uninominal districts
		Iterator<DistrictUninominal>iu = listOfUninominalDistricts.iterator();
		while (iu.hasNext()) {
			DistrictUninominal districtU= iu.next();
			out.append(districtU.toString());
		}
		// plurinominal districts
System.out.println("saving the plurinominal districts, there are "+listOfPlurinominalDistricts.size()+" of them");
		Iterator<DistrictPlurinominal>ip = listOfPlurinominalDistricts.iterator();
		while (ip.hasNext()) {
			DistrictPlurinominal districtP = ip.next();
			out.append(districtP.toString());
		}
		out.append("\n");
		// coalitions
System.out.println("saving the coalitions");
		Set <Integer> keysc = mapOfGroupsOfCoalitions.keySet();
		Iterator <Integer>io = keysc.iterator();
		while (io.hasNext()) {
			LinkedList<Coalition> listOfCoalitions = mapOfGroupsOfCoalitions.get(io.next());
			Iterator <Coalition>j = listOfCoalitions.iterator();
			while (j.hasNext()) {
				Coalition coa = j.next();
				out.append(coa.toStringFull());
			}
		}
		// parliaments
		Set<String> keys = parliaments.keySet();
		Iterator<String> k = keys.iterator();
		while (k.hasNext()) {
			String key =  k.next();
			Parliament parl = parliaments.get(key);
			out.append("parliament_"+key+":"+(parl.getAllocationOfSeats()).toString()+"\n");
		}		
		return out.toString();
	}

	public String toStringSorted() {
		StringBuffer out = new StringBuffer();
		// name and description
		out.append("nameRepository : " + name + "\n");
		out.append("descriptionRepository : " + description + "\n");
		// simulation parameters
		out.append(toStringSimulationParameters());
		out.append("\n");

		// for each plurinominal district , save the
		// composition in terms of uninominal districts
		// and voters
		if (listOfPlurinominalDistricts.size()>0) {
			// plurinominal districts
			Iterator<DistrictPlurinominal> i = listOfPlurinominalDistricts.iterator();
			while (i.hasNext()) {
				DistrictPlurinominal districtP= i.next();
				out.append(districtP.toString());
	
				// for each uninominal district in the list, save its composition in terms of voters
				LinkedList <DistrictUninominal>districtsUinP = districtP.getListOfDistricts();
				Iterator <DistrictUninominal>j = districtsUinP.iterator();
				while (j.hasNext()) {
					DistrictUninominal districtU = j.next();
					out.append("\tDistrict"+districtU.getNameOfDistrictU()+" -- ");
					if (districtU.getConcentratedMajorParty() == true) {
						out.append("contains concentrated major party "+districtU.getNameMajorParty());
					}
					out.append("\n");
					LinkedList <Voter>votersInDistrict = districtU.getVoters();
					Iterator <Voter>v = votersInDistrict.iterator();
					while (v.hasNext()) {
						Voter voter = v.next();
						out.append("\t\t"+voter.toStringPartiesSorted());
					}
				}
				
			}
		} else {
			// for each uninominal district in the list, save its composition in terms of voters
			Iterator <DistrictUninominal>j = listOfUninominalDistricts.iterator();
			while (j.hasNext()) {
				DistrictUninominal districtU = j.next();
				out.append("\tDistrict"+districtU.getNameOfDistrictU()+" -- ");
				if (districtU.getConcentratedMajorParty() == true) {
					out.append("contains concentrated major party "+districtU.getNameMajorParty());
				}
				out.append("\n");
				LinkedList <Voter>votersInDistrict = districtU.getVoters();
				Iterator <Voter>v = votersInDistrict.iterator();
				while (v.hasNext()) {
					Voter voter = v.next();
					out.append("\t\t"+voter.toStringParties());
				}
			}
		}
		out.append("\n");
		return out.toString();
	}
	

}// end class definition