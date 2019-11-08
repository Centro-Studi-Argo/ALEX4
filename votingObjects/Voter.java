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
import java.lang.ref.*;
import java.io.*;

import classesWrittenByOthers.*;
import gui.*;
import actions.*;

public class Voter {

	private LinkedList<Integer> partyPreferences=new LinkedList<Integer>();
	private LinkedList<Candidate> candidatePreferences=new LinkedList<Candidate>();
//	private HashMap mapVotesCandidatesVST=new HashMap();
	private int name;
	private HashMap<String,Object> generalParameters;

	/*
	** constructor
	** a voter has a list of preferences for parties (always same size as nbParties)
	** and a list of preferences for candidates which is empty if numberCandidates is equal to 1,
	** and the size of numberCandidates*numberParties otherwise.
	*/
	public Voter (int name, LinkedList<Integer> prefsParties, LinkedList<Candidate> prefsCandidates, HashMap<String,Object> generalParameters) {
		this.name=name;
		this.generalParameters=generalParameters;
		partyPreferences=prefsParties;
		candidatePreferences=prefsCandidates;
	}
	// when preferences for candidates are not known immediately
	public Voter(int name, LinkedList<Integer> prefsParties, HashMap<String,Object> generalParameters) {
		this.name = name;
		this.generalParameters = generalParameters;
		partyPreferences = prefsParties;
	}


	public void addPreferencesForCandidates(LinkedList<Candidate> prefsCandidates) {
		candidatePreferences = prefsCandidates;
	}
	
	public int getFirstPartyPreference() {
		return (partyPreferences.get(0)).intValue();
	}

	public int getNameVoter() {
		return name;
	}

	// this function is used by condorcetWinner, to find the preferred party in a given list (which 
	// tipically is not be the complete preference order)
	public Integer getPreferredParty(LinkedList<Integer> listOfPartiesNames) {
		int min=partyPreferences.size();
		Integer preferredParty=new Integer(0);
		for (int p=0;p<listOfPartiesNames.size();++p) {
			Integer partyIndex=listOfPartiesNames.get(p);
			int index=partyPreferences.indexOf(partyIndex);
			if ((index<min)&&(index!=-1)) {
				min=index;
				preferredParty=partyIndex;
			} else if (index==-1) {
				System.out.println("the party is not in the preferences of the elector!");
				System.exit(0);
			}
		}
		return(preferredParty);
	}

	// get the position of the party in the preference vectors
	public int getPositionOfParty(Integer partyIndex) {
		int index=partyPreferences.indexOf(partyIndex);
//System.out.println("the partito "+partyIndex+" is at position "+index);
		return(index);
	}

	public LinkedList<Integer> getPartyPreferences() {
		return (partyPreferences);
	}

	public void setCandidatePreferences(LinkedList<Candidate> cp) {
		candidatePreferences = cp;
	}
	
	public LinkedList<Candidate> getCandidatePreferences() {
		return candidatePreferences;
	}

	// first candidate in assoluto
	public Candidate getFirstCandidatePreference() {
		return ((Candidate)candidatePreferences.getFirst());
	}

	// first candidate, after discarding those already elected
	public Candidate getFirstCandidatePreference(LinkedList<Candidate> candidatiEletti) {
		Candidate candidate=new Candidate(0,0);
		elect:
		for (int c=0;c<candidatePreferences.size();++c) {
			candidate=candidatePreferences.get(c);
//System.out.println("\tVoter "+name+": get first not elected, current is at the "+candidate.getPositionInList()+"th position for party "+candidate.getPartyName());
			int elected=0;
			for (int ce=0;ce<candidatiEletti.size();++ce) {
				Candidate candel=candidatiEletti.get(ce);
//				WeakReference wrCandel=new WeakReference(candel,queue);
				if ((candidate.getPositionInList()==candel.getPositionInList())&&(candidate.getPartyName()==candel.getPartyName())) {
					elected=1;
					// candidate already elected, get next
//System.out.println("\telected, consider next candidate");
					continue elect;
				}
				candel=null;
			}// end for ce
			if (elected==0) {
				break;
			}
		}// end for c
//System.out.println("\treturn");
		return candidate;
	}
		
//	public void createMapVotesCandidatesVST() {
//		// for each candidatesPreferences, create a key in map with party name, and value of 1
//		for (int i=0;i<candidatePreferences.size();++i) {
//			mapVotesCandidatesVST.put((Candidate)candidatePreferences.get(i),new Integer(1));
//		}
//	}
//		
//	public HashMap getMapVotesCandidatesVST() {
//		return mapVotesCandidatesVST;
//	}

	public String toString() {
		return(new Integer(name).toString()+" ");
	}

	public String toStringParties() {
		return ("voter_"+name+"_partyPreferences:"+partyPreferences.toString() + ":\n");
	}

	public String toStringCandidates() {
		if (candidatePreferences.size()>0) {
			return ("voter_"+name+"_candidatePreferences:"+candidatePreferences.toString() + ":\n");
		} else {
			return "";
		}
	}

	public String toStringPartiesSorted() {
		StringBuffer out = new StringBuffer();
		out.append("voter_"+name+"_partyPreferences:");
		Iterator i = partyPreferences.iterator();
		while (i.hasNext()) {
			Integer partyName = (Integer)i.next();
			out.append(partyName+":");
		}
		out.append("\n");
		return out.toString();
	}
		

}// end class definition