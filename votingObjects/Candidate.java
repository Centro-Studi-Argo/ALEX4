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

import classesWrittenByOthers.*;
import gui.*;
import actions.*;

public class Candidate {
	private int party;
	private int positionInList;

	// constructor
	public Candidate(int party,int position) {
		this.party=party;
		this.positionInList=position;
	}

	public Candidate(Integer party,Integer position) {
		this.party=party.intValue();
		this.positionInList=position.intValue();
	}

		
	public int getPartyName() {
		return party;
	}

	public int getPositionInList() {
		return positionInList;
	}

	public String toStringFull() {
		String out="";
//		out += "candidate_"+party+"-"+positionInList+"_party:"+party+"\n";
//		out += "candidate_"+party+"-"+positionInList+"_positionInList:"+positionInList+"\n";
		out += "candidate"+party+"_"+positionInList+":"+party+"_"+positionInList;
		return out;
	}
	public String toString() {
		return (party+"_"+positionInList);
	}
}// end class definition