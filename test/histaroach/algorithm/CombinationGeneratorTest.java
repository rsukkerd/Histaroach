package histaroach.algorithm;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;


public class CombinationGeneratorTest extends TestCase {
	
	private static final String WORD_1 = "abc";
	private static final String WORD_2 = "def";
	private static final String WORD_3 = "ghi";
	
	private static final Set<String> COLLECTION_1 = new HashSet<String>();
	static {
		COLLECTION_1.add(WORD_1);
	}
	
	private static final Set<String> COLLECTION_2 = new HashSet<String>();
	static {
		COLLECTION_2.add(WORD_1);
		COLLECTION_2.add(WORD_2);
	}
	
	private static final Set<String> COLLECTION_3 = new HashSet<String>();
	static {
		COLLECTION_3.add(WORD_1);
		COLLECTION_3.add(WORD_2);
		COLLECTION_3.add(WORD_3);
	}
	
	private static final Set<String> COMBINATION_1 = new HashSet<String>();
	static {
		COMBINATION_1.add(WORD_1);
	}
	
	private static final Set<String> COMBINATION_2 = new HashSet<String>();
	static {
		COMBINATION_2.add(WORD_2);
	}
	
	private static final Set<String> COMBINATION_3 = new HashSet<String>();
	static {
		COMBINATION_3.add(WORD_3);
	}
	
	private static final Set<String> COMBINATION_4 = new HashSet<String>();
	static {
		COMBINATION_4.add(WORD_1);
		COMBINATION_4.add(WORD_2);
	}
	
	private static final Set<String> COMBINATION_5 = new HashSet<String>();
	static {
		COMBINATION_5.add(WORD_1);
		COMBINATION_5.add(WORD_3);
	}
	
	private static final Set<String> COMBINATION_6 = new HashSet<String>();
	static {
		COMBINATION_6.add(WORD_2);
		COMBINATION_6.add(WORD_3);
	}
	
	private static final Set<String> COMBINATION_7 = new HashSet<String>();
	static {
		COMBINATION_7.add(WORD_1);
		COMBINATION_7.add(WORD_2);
		COMBINATION_7.add(WORD_3);
	}
	
	private static final Set<Set<String>> COMBINATIONS_1_1 = new HashSet<Set<String>>();
	static {
		COMBINATIONS_1_1.add(COMBINATION_1);
	}
	
	private static final Set<Set<String>> COMBINATIONS_2_1 = new HashSet<Set<String>>();
	static {
		COMBINATIONS_2_1.add(COMBINATION_1);
		COMBINATIONS_2_1.add(COMBINATION_2);
	}
	
	private static final Set<Set<String>> COMBINATIONS_2_2 = new HashSet<Set<String>>();
	static {
		COMBINATIONS_2_2.add(COMBINATION_4);
	}
	
	private static final Set<Set<String>> COMBINATIONS_3_1 = new HashSet<Set<String>>();
	static {
		COMBINATIONS_3_1.add(COMBINATION_1);
		COMBINATIONS_3_1.add(COMBINATION_2);
		COMBINATIONS_3_1.add(COMBINATION_3);
	}
	
	private static final Set<Set<String>> COMBINATIONS_3_2 = new HashSet<Set<String>>();
	static {
		COMBINATIONS_3_2.add(COMBINATION_4);
		COMBINATIONS_3_2.add(COMBINATION_5);
		COMBINATIONS_3_2.add(COMBINATION_6);
	}
	
	private static final Set<Set<String>> COMBINATIONS_3_3 = new HashSet<Set<String>>();
	static {
		COMBINATIONS_3_3.add(COMBINATION_7);
	}
	
	@Test
	public void OneChooseOneTest() {
		checkCombinations(COLLECTION_1, 1, COMBINATIONS_1_1);
	}
	
	@Test
	public void TwoChooseOneTest() {
		checkCombinations(COLLECTION_2, 1, COMBINATIONS_2_1);
	}
	
	@Test
	public void TwoChooseTwoTest() {
		checkCombinations(COLLECTION_2, 2, COMBINATIONS_2_2);
	}
	
	@Test
	public void ThreeChooseOneTest() {
		checkCombinations(COLLECTION_3, 1, COMBINATIONS_3_1);
	}
	
	@Test
	public void ThreeChooseTwoTest() {
		checkCombinations(COLLECTION_3, 2, COMBINATIONS_3_2);
	}
	
	@Test
	public void ThreeChooseThreeTest() {
		checkCombinations(COLLECTION_3, 3, COMBINATIONS_3_3);
	}
	
	private void checkCombinations(Set<String> collection, int numChoose, 
			Set<Set<String>> expectedCombinations) {
		CombinationGenerator<String> combinations = new CombinationGenerator<String>(
				collection, numChoose);
		
		int count = 0;
		for (Set<String> combination : combinations) {
			count++;
			assertTrue(expectedCombinations.contains(combination));
		}
		
		assertTrue(expectedCombinations.size() == count);
	}
}
