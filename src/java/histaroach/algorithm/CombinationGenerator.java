package histaroach.algorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * CombinationGenerator accepts a set of elements and a number of elements to 
 * choose from the set, and then generates combinations C(n, r), where n is 
 * the total number of elements in the set, and r is the number of elements 
 * to choose from the set.
 * 
 * CombinationGenerator ignores the following edge cases: when a set is empty, 
 * and when r = 0.
 */
public class CombinationGenerator<T> implements Iterable<Set<T>>, Iterator<Set<T>> {
	
	private final int numAll;
	private final int numChoose;
	
	private final List<T> elements;
	private final int[] indexArray;
	
	private final BigInteger numTotalCombinations;
	private BigInteger numCombinationsGenerated;
	
	public CombinationGenerator(Set<T> collection, int numChoose) {
		if (numChoose > collection.size() || numChoose < 1 || 
				collection.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		numAll = collection.size();
		this.numChoose = numChoose;
		
		elements = new ArrayList<T>(collection);
		indexArray = new int[numChoose];
		
		for (int i = 0; i < indexArray.length; i++) {
			indexArray[i] = i;
		}
		
		BigInteger numAllFact = getFactorial(numAll);
		BigInteger numChooseFact = getFactorial(numChoose);
		BigInteger numNotChooseFact = getFactorial(numAll - numChoose);
		
		// C(n, r) = n!/(r!(n - r)!)
		numTotalCombinations = numAllFact.divide(
				numChooseFact.multiply(numNotChooseFact));
		numCombinationsGenerated = BigInteger.ZERO;
	}
	
	/**
	 * @return a factorial of num.
	 */
	private static BigInteger getFactorial(int num) {
		BigInteger fact = BigInteger.ONE;
		
		for (int i = num; i > 1; i--) {
			BigInteger bigI = new BigInteger(Integer.toString(i));
			fact = fact.multiply(bigI);
		}
		
		return fact;
	}
	
	@Override
	public Iterator<Set<T>> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return numCombinationsGenerated.compareTo(numTotalCombinations) == -1;
	}

	@Override
	public Set<T> next() {
		if (numCombinationsGenerated.compareTo(BigInteger.ZERO) == 1) {
			int i = numChoose - 1;
			
			while (indexArray[i] == numAll - numChoose + i) {
				i--;
			}
			
			indexArray[i]++;
			
			for (int j = i + 1; j < numChoose; j++) {
				indexArray[j] = indexArray[i] + j - i;
			}
		}
		
		numCombinationsGenerated = numCombinationsGenerated.add(BigInteger.ONE);
		return getCombination();
	}
	
	private Set<T> getCombination() {
		Set<T> combination = new HashSet<T>();
		
		for (int index : indexArray) {
			T element = elements.get(index);
			combination.add(element);
		}
		
		return combination;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
