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
import globals.*;

public class Party {
	private TextField namePartyField;
	private JLabel namePartyLabel;
	private String nameParty;

	private DecimalField shareField;
	private JLabel shareLabel;
	private double share;

	private WholeNumberField concentrationField;
	private JLabel concentrationLabel;
	private int concentration=0;
	private JLabel detLabel;
	private JLabel detField;
	private DecimalField coefficientField;
	private JLabel coefficientLabel;
	private double coefficient=1;

	private DecimalField distanceField;
	private JLabel distanceLabel;
	private double distance = 0;

	private JCheckBox majorField;
	private JLabel majorLabel;
	private boolean major=false;

	private HashMap <String,Object> generalParameters;
	private int name;

	private boolean lastParty;
	private ParametersParties characteristicsOfParties;

	private float color = -1;

//	JPanel part = new JPanel();

	Language language = Language.getInstance();

	// for any party
	public Party(String nameParty,HashMap<String,Object> generalParameters,
		double initialShare,boolean lastParty,ParametersParties characteristicsOfParties)
		{
		super();
		this.nameParty=nameParty;
		this.generalParameters=generalParameters;
		share=initialShare;
		this.lastParty=lastParty;
		this.characteristicsOfParties=characteristicsOfParties;
		}
	// constructor for when read from file
	public Party(int name,String nameParty,HashMap<String,Object> generalParameters,double share,int concentration,double coefficient,boolean major,double distance)
		{
		super();
		this.name=name;
		this.nameParty=nameParty;
		this.generalParameters=generalParameters;
		this.share=share;
		this.concentration=concentration;
		this.coefficient=coefficient;
		this.major=major;
		this.distance=distance;
		}
	// constructor for when read from file (loading parliament)
	public Party(int name,String nameParty,double share,int concentration,double coefficient,boolean major,double distance)
		{
		super();
		this.name=name;
		this.nameParty=nameParty;
		this.share=share;
		this.concentration=concentration;
		this.coefficient=coefficient;
		this.major=major;
		this.distance=distance;
		}

	public void setDistance(double value) {
		this.distance = value;
	}
	public double getDistance() {
		return distance;
	}
	public boolean isLast()
		{
		return lastParty;
		}
		
	public void setName(int name)
		{
		this.name=name;
		}
	// return parameters for model
	public double getShare()
		{
		return share;
		}
	public int getVotes() {
		int totalVoters = ((Integer)generalParameters.get("totalNumberVoters")).intValue();
System.out.println("totalVoters = "+totalVoters);
System.out.println("total votes "+ (share*totalVoters)/100);
		return (int)((share*totalVoters)/100);
	}
	public boolean getMajor()
		{
		return major;
		}
	public double getCoefficient()
		{
		return coefficient;
		}
	public int getConcentration()
		{
		return concentration;
		}
	public int getName()
		{
		return name;
		}

	public String getNameParty()
		{
		return nameParty;
		}

	public void setNameParty (String value) {
		nameParty = value;
	}
	
	public void setShare(double value)
		{
		share=value;		
		}

	public void setMajor(boolean value) {
		major = value;
	}
		
	public void setConcentration(int value)
		{
		concentration=value;
		}

	public void setCoefficient(double value)
		{
		coefficient=value;
		}

	public void setColor(float value) {
		color=value;
	}
	public float getColor()	{
		return color;
	}
	
	public String toString()
		{
		String out=new String();


		out += "party_"+name+"_name:"+nameParty+" : "+language.getString("labels","nameParty")+"\n";


		out += "party_"+name+"_name:"+nameParty+" : "+language.getString("labels","nameParty")+"\n";
		DecimalFormat df = new DecimalFormat("##.##");
		out += "party_"+name+"_share:"+df.format(share)+" : //"+language.getString("labels","share")+"\n";
		out += "party_"+name+"_major:"+major+" :// "+language.getString("labels","major")+"\n";
		out += "party_"+name+"_concentration:"+concentration+": // "+language.getString("labels","concentration")+"\n";
		out += "party_"+name+"_coefficient:"+coefficient+" :// "+language.getString("labels","coefficient")+"\n";
		out += "party_"+name+"_distance:"+distance+" :// "+language.getString("labels","distance")+"\n";
		out += "\n";
		return(out);
		}

	}
