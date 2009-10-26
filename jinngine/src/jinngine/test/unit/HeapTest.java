package jinngine.test.unit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import jinngine.util.Heap;
import junit.framework.TestCase;

public class HeapTest extends TestCase {
	
	/**
	 * A simple test inserting 3 elements, and expecting to get them out 
	 * of the min-heap in increasing order
	 */
	public void testHeap1() {
		//Create a min-heap with a comparator for doubles
		Heap<Double> heap = new Heap<Double>( new Comparator<Double>() { 
			@Override
			public int compare(Double o1, Double o2) {
				return (o1>o2?1:-1);
			}
		});

		//insert elements
		heap.insert(3.0);
		heap.insert(1.0);
		heap.insert(2.0);
		
		//draw out elements and check value
		Double d = heap.pop();
		assertTrue(d==1.0);
		
		d = heap.pop();
		assertTrue(d==2.0);

		d = heap.pop();
		assertTrue(d==3.0);
	}
	
	/**
	 * min-heap test mixing insert and pops
	 */
	public void testHeap2() {
		//Create a min-heap with a comparator for doubles
		Heap<Double> heap = new Heap<Double>( new Comparator<Double>() { 
			@Override
			public int compare(Double o1, Double o2) {
				return (o1>o2?1:-1);
			}
		});

		//insert elements
		heap.insert(3.0);
		heap.insert(1.0);
		heap.insert(2.0);
		
		//expect 1.0
		Double d = heap.pop();
		assertTrue(d==1.0);
		
		//insert elements
		heap.insert(1.0);
		
		//expect 1.0
		d = heap.pop();
		assertTrue(d==1.0);
		
		//insert elements
		heap.insert(4.0);
		
		//expect 2.0
		d = heap.pop();
		assertTrue(d==2.0);

		//expect 3.0
		d = heap.pop();
		assertTrue(d==3.0);
		
		//expect 4.0
		d = heap.pop();
		assertTrue(d==4.0);
	}

	/**
	 * min-heap test doing heap-sort. A list of random numbers is 
	 * created and sorted. The sorted list is compared to the result of 
	 * sorting using Collections.sort() method.
	 */
	public void testHeapSort() {
		//dummy comparator for Doubles
		Comparator<Double> c = new Comparator<Double>() { 
			@Override
			public int compare(Double o1, Double o2) {
				return (o1>o2?1:-1);
			}
		};

		//Create a min-heap with a comparator for doubles
		Heap<Double> heap = new Heap<Double>(c);

		//create some random numbers and insert them into the heap and
		//the list
		int n = 2048;
		Random r = new Random(6); //note fixed seed
		List<Double> l = new ArrayList<Double>();
		for (int i=0;i<n; i++) {
			Double number = r.nextDouble()-0.5;

			heap.insert(number);
			l.add(number);	
		}

		//sort list using Collections, same comparator
		Collections.sort(l, c);
		
		//compare each element
		for (int i=0;i<n; i++) {
			assertTrue( l.get(i) == heap.pop() );
		}		
	}


}
