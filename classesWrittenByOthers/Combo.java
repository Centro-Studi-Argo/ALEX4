package classesWrittenByOthers;

//This is a simple implementation of combinadics in Java

/**

* 

* This is a simple implementation of combinadics in Java
* Call the getMaxNumberOfCombinations ( slots, tokens ) method to essentially get C ( slots, tokens )
* Call the pascalsTriangle ( rows ) to get a Pascal triangle up to 'rows' row
* Call the printCombination ( slots, tokens, index ) method to print out the index'th combination for C ( slots, tokens )
* Call the printAllCombinations ( slots, tokens ) method to print out all the combinations for C ( slots, tokenss )
* 
* @author Michael J. Hudson - lasloo at gmail
*
*/

/*
* Bissey 2008 = changed the print functions to output strings
*/

public class Combo {
    /**
    * Main method currently being used to test this Combo object
    * 
    * @param args
    */
//     public static void main ( String [] args) 
//    {
//        Combo combo = new Combo ();
//        combo.printAllCombinations ( 6, 5 );
//    }


	public Combo () {
	}

    /**

    * 
    * Returns the maximum number of combinations for C(slots,tokens)
    * This is using a recursive algorithm so as to guarantee that no number in the calculation of the answer will go over MAXLONG
    * as long as the answer itself also does not go over MAXLONG
    * Normally, one would calculate full or partial factorials to get this answer per the equation: ( slots!/((slots-tokens)!*tokens!) )
    * However, intermediate steps in that calculation may involve numbers bigger than MAXLONG even if the answer itself is not bigger than MAXLONG
    * 
    * @param slots - the possible values (from 1) in a combination
    * @param tokens - the number of values in a combination
    * 
    * @return the maximum number of combinations for C(slots,tokens)
    */

    public long getMaxNumberOfCombinations(long slots, long tokens) {
        if ((tokens == 0) || (tokens == slots)) { 
             return 1;
       	}
        return getMaxNumberOfCombinations ( slots - 1, tokens - 1 ) + getMaxNumberOfCombinations ( slots - 1, tokens );
    }
 

    /**
    * Print out Pascal's triangle up to 'rows' row
    * 
    * @param rows - number of rows to print out
    */

     public void pascalsTriangle(long rows) {
     	for ( int slots = 0; slots < rows; slots++ ) {
	        for (int tokens = 0; tokens <= slots; tokens++) {
    	        System.out.print(getMaxNumberOfCombinations(slots, tokens) + " " );
           }
           System.out.println ( "" );
        }
    }

    /**
    * 
    * Prints all the combinations for C ( slots, tokens )
    * 
    * @param slots - the possible values (from 1) in a combination
    * @param tokens - the number of values in a combination
    */

      public void printAllCombinations ( long slots, long tokens ) {
      	long maxCombos = getMaxNumberOfCombinations ( slots, tokens );
        	for ( int index = 0; index < maxCombos; index++ ) {
            	printCombination ( slots, tokens, index );
          	}
      }
 
      public String outputAllCombinations ( long slots, long tokens ) {
//System.out.println("in outputAllCombinations");
      	long maxCombos = getMaxNumberOfCombinations ( slots, tokens );
		String st = new String();
		for ( int index = 0; index < maxCombos; index++ ) {
       		st+=outputCombination ( slots, tokens, index );
      	}
		st=st.substring(0,(st.length()-1));
		return st;
      }

 	  
    /**
    * 
    * Prints the index'th combination for C(slots,tokens)
    * 
    * @param slots - the possible values (from 1) in a combination
    * @param tokens - the number of values in a combination
    * @param index - unique identifier declaring which combination to print out. Should be between 0 and C(slots, tokens)
    */

	public void printCombination ( long slots, long tokens, long index ) {
		for ( long slotValue = 1; slotValue <= slots; slotValue++ ) {
			if ( tokens == 0 ) break;
			long threshold = getMaxNumberOfCombinations ( slots - slotValue, tokens - 1 );
			if ( index < threshold ) {
			    System.out.print ( slotValue + "\t" );
			    tokens = tokens - 1;
			} else if ( index >= threshold ) {
			    index = index - threshold;
			}
		}
		System.out.println ();
	}

	public String outputCombination ( long slots, long tokens, long index ) {
//System.out.println("in outputCombination:" + slots+" , "+tokens+" , "+index);
		String st = new String();
		for ( long slotValue = 1; slotValue <= slots; slotValue++ ) {
			String s = new String();
			if ( tokens == 0 ) break;
			long threshold = getMaxNumberOfCombinations ( slots - slotValue, tokens - 1 );
			if ( index < threshold ) {
			    s+= slotValue + "," ;
			    tokens = tokens - 1;
			} else if ( index >= threshold ) {
			    index = index - threshold;
			}
			st+=s;
		}
			st=st.substring(0,(st.length()-1));
			st+=";";
		return(st);
	}


	
}
/**

The result for printAllCombinations ( 8, 5 );

1 2 3 4 5 

1 2 3 4 6 

1 2 3 4 7 

1 2 3 4 8 

1 2 3 5 6 

1 2 3 5 7 

1 2 3 5 8 

1 2 3 6 7 

1 2 3 6 8 

1 2 3 7 8 

1 2 4 5 6 

1 2 4 5 7 

1 2 4 5 8 

1 2 4 6 7 

1 2 4 6 8 

1 2 4 7 8 

1 2 5 6 7 

1 2 5 6 8 

1 2 5 7 8 

1 2 6 7 8 

1 3 4 5 6 

1 3 4 5 7 

1 3 4 5 8 

1 3 4 6 7 

1 3 4 6 8 

1 3 4 7 8 

1 3 5 6 7 

1 3 5 6 8 

1 3 5 7 8 

1 3 6 7 8 

1 4 5 6 7 

1 4 5 6 8 

1 4 5 7 8 

1 4 6 7 8 

1 5 6 7 8 

2 3 4 5 6 

2 3 4 5 7 

2 3 4 5 8 

2 3 4 6 7 

2 3 4 6 8 

2 3 4 7 8 

2 3 5 6 7 

2 3 5 6 8 

2 3 5 7 8 

2 3 6 7 8 

2 4 5 6 7 

2 4 5 6 8 

2 4 5 7 8 

2 4 6 7 8 

2 5 6 7 8 

3 4 5 6 7 

3 4 5 6 8 

3 4 5 7 8 

3 4 6 7 8 

3 5 6 7 8 

4 5 6 7 8

**/
 