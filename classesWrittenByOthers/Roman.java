package classesWrittenByOthers;

public class Roman
	{
/*
 *
 *        ROMAN.Java  -  Converts integers to Roman numerals
 *
 *             Written by:  Jim Walsh
 *             Converted to Java by:  Basil Worrall
 * 			   Converted to a class and function by: Marie-Edith Bissey
 *
 */

	// constructor
	public Roman()
		{
		}

	public String makeRoman(int value)
		{
		String roman="";
	    while( value >= 1000 )
	    	{
	        roman += "M";
	        value -= 1000;
		    }
	    if( value >= 900 )
	    	{
	        roman += "CM";
	        value -= 900;
		    }
	    while( value >= 500 )
	    	{
	        roman += "D";
	        value -= 500;
		    }
	    if( value >= 400 )
	    	{
	        roman += "CD";
	        value -= 400;
		    }
	    while( value >= 100 )
		    {
	        roman += "C";
	        value -= 100;
		    }
	    if( value >= 90 )
	    	{
	        roman += "XC";
	        value -= 90;
		    }
	    while( value >= 50 )
	    	{
	        roman += "L";
	        value -= 50;
		    }
	    if( value >= 40 )
	    	{
	        roman += "XL";
	        value -= 40;
		    }
	    while( value >= 10 )
	    	{
	        roman += "X";
	        value -= 10;
		    }
	    if( value >= 9 )
	    	{
	        roman += "IX";
	        value -= 9;
		    }
	    while( value >= 5 )
	    	{
	        roman += "V";
	        value -= 5;
		    }
	    if( value >= 4 )
	    	{
	        roman += "IV";
	        value -= 4;
		    }
	    while( value > 0 )
	    	{
	        roman += "I";
	        value--;
		    }
		return (roman);
		}
	}