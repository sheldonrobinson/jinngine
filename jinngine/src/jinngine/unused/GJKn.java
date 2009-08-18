package jinngine.unused;



public class GJKn {
	private final int          max         = 32;
	private final int          N           = 5;
	private final Vector4[][]  simplices   = new Vector4[N][4];
	private final double[]     lambda      = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	private final int[]        permutation = new int[] {0,1,2,3,4,5,6,7,8,9}; //...
	private int                simplex     = 0;
	private final double       epsilon     = 1e-12;
	private final double[][]   D           = new double[1<<N][N];
	
	public void run(SupportMap4 Sa, SupportMap4 Sb, Vector4 pa, Vector4 pb ) {
		int iterations = 0;
		simplex = 0;

		Vector4 v = new Vector4();
		Vector4 w = new Vector4();
		Vector4 sa = new Vector4();
		Vector4 sb = new Vector4();
		
		//initialy choose arbitry v 
		sa.assign( Sa.supportPoint(new Vector4(1,1,1,1).multiply(-1)));
		sb.assign( Sb.supportPoint(new Vector4(1,1,1,1).multiply(1) ));	    							
		v.assign( sa.minus(sb) );
		
		while (true) {
			iterations++;
			System.out.println("(*) Iteration " +v.norm()); //			v.print();
			
//			for (int i=0;i<N;i++) {
//				System.out.print(""+permutation[i]+",");
//			} System.out.println();

			//iteration limit
			if (iterations > max) {
				System.out.println("GJK: Iteration limit");
				break;
			}
			
			//penetration test
			if (v.norm() < epsilon) {
				System.out.println("GJK: Intersection");
				break;
			}
			
			//store points of convex objects A and B, and A-B 
			sa.assign( Sa.supportPoint(v.multiply(-1)));
			sb.assign( Sb.supportPoint(v.multiply(1) ));	    							
			w.assign( sa.minus(sb) );
			//w.print();
			
			//Seperating axis
//			if ( v.dot(w) > 0 ) {
//				System.out.println("GJK: Seperation");
//				break;
//			} 
			
			//Termination condition
			if ( Math.abs( v.dot(v)-v.dot(w) ) < epsilon  ) {
				System.out.println("GJK: Termination:" + (v.dot(v)-v.dot(w)) );
				break;
			}

			//add w to the simplices
			Vector4[] row = simplices[permutation[simplex]];
			row[0] = w.copy();
			row[1] = sa.copy();
			row[2] = sb.copy();
			row[3] = v.copy();
			simplex++;
			
			//find the smalest supporting set
			//n specifies how many elements are in Ix 
			determinants(simplex);
			
			//Calculate the vector v
			v.assign(new Vector4());
			for (int i=0; i<simplex;i++) {
				v.assign(v.add(simplices[permutation[i]][0].multiply( lambda[permutation[i]])) );
                //System.out.println("Lambda="+lambda[permutation[i]]);				
			}
		} //while true		
		
		System.out.println("GJK: Distance computed " + v.norm() + " in " + iterations + " iterations" );
		
		//Calculate the vector points
		pb.assign(pa.assign(new Vector4()));
		for (int i=0; i<simplex;i++) {
			pa.assign(pa.add(simplices[permutation[i]][1].multiply( lambda[permutation[i]])) );
			pb.assign(pb.add(simplices[permutation[i]][2].multiply( lambda[permutation[i]])) );
			//System.out.println("Lambda="+lambda[permutation[i]]);				
		}
		v.print();
		pa.print();
		pb.print();
	}

	public static void printArray(int[] a,int k, double d) {
		for ( int i : a) System.out.print(i);
		System.out.println(":"+ k + " determinant = "+ d);
	}
	
	public void determinants(int k) {
		int X = 0;
		//int k = 5;
		
		while (true) {
			//poll next permutation
			X = next(X,k);
			
			if (X==0) {
				//no more permutations left
				break;
			}
			//int c = compl(X,k);
			int l=0,m=0;
			int[] inX = new int[k];
			int[] notInX = new int[k];
			
			//d(XuYj) <= 0 for all j not in X		
			for (int i=0; i<k; i++)   
				if ( (X&(1<<(k-i-1))) != 0)  
					inX[m++] = i; else notInX[l++] = i; // k -notInX -1 = i

			boolean conditionOne = true;
			double deltaXi   = 0; boolean conditionTwo = true;
			double deltaX    = 0;

			//check first condition
			for (int i=0;i<m;i++) {
				if (m==1)  D[X][inX[i]] = 1;
				deltaX     = deltaX + D[X][inX[i]];
				//System.out.println("Old determinant " + (X) + "," + inX[i] + " = " + D[X][inX[i]]);
				if (!(D[X][inX[i]] > 0)) 
					conditionOne = false;
			}
			
			//check second condition
			for (int j=0;j<l;j++) {
				double deltaXuYj = 0;
				for (int i=0;i<m;i++) {
					Vector4 yk = simplices[permutation[inX[0]]][0];
					Vector4 yi = simplices[permutation[inX[i]]][0];
					Vector4 yj = simplices[permutation[notInX[j]]][0];
					deltaXi    = D[X][inX[i]]; 
					deltaXuYj += deltaXi * yi.dot(yk.minus(yj));
				}
				
				deltaXuYj = Math.abs(deltaXuYj)<epsilon?0:deltaXuYj;
				
				if (!(deltaXuYj <= 0)) 
					conditionTwo = false;
				
				//store the new determinant in D
				D[X|(1<<(k-notInX[j]-1))][notInX[j]] = deltaXuYj;
				//System.out.println("New determinant " + (X|(1<<(k-notInX[j]-1))) + "," + notInX[j] + " = " + deltaXuYj);
			}
			
			//found X for which conditions hold!
			if ( conditionOne && conditionTwo) {
				//System.out.println("FOUND X=" + X + " where k="+k);
				//printBits(X);
				
				//calculate lambda values for subset X
				for (int i=0; i<m; i++) {
					lambda[permutation[inX[i]]] = D[X][inX[i]]/deltaX;
					//System.out.println("lambda " + lambda[permutation[inX[i]]] ); sum += lambda[permutation[inX[i]]];
				}
				
				//permute and set simplex size!   1 2 3  1100 110
                int newPermutation[] = new int[N];
				for (int i=0; i<m; i++) 
					newPermutation[i] = permutation[inX[i]];
				for (int j=0;j<l;j++)
					newPermutation[j+m] = permutation[notInX[j]];
				
				for (int i=0;i<k;i++)
					permutation[i] = newPermutation[i];
				
				simplex = m;
				return;
			}
		}
		
		System.out.println("GJK: no subset found");
	}

//	public static int compl(int n, int k) {
//		return ~n&~(0xFFFFFFFF<<k);
//	}

//	given a legal permutation
	public static int next(int n, int k) {
		int remainder = 0;
		int remainderBits = 0;
		int pos = 0;
		int previous = 1;

		while (pos<k) {
			if ((n&(1<<pos))!= 0) {
				//bit found, swap or add to remainder
				if (previous == 0) {
					//swap and put remainders in front
					return ((n^(1<<pos))^(1<<(pos-1)))|remainderBits<<(pos-1-remainder);
				} else {
					remainderBits |= 1<<(remainder++); //add to remainder
					n = (n&~(1<<(pos)));               //clear the bit			
					//no more permutations?
					if ( remainder == k) return 0;
				}
				previous = 1;
			} else { previous = 0;}

			//move on to next bit
			pos++;
		}

		//insert a new bit a the k'th bit, and put remainders in front
		return (1<<(k-1))|(remainderBits<<(k-1-remainder));
	}

	
	
	
	
	


public static void printBits(int n) {
	for (int i=31;i>(-1);i--)
		System.out.print("" + (n>>i&1) );
	System.out.println();
}

}

//  -,1
//  -,2
//  -,3
//  -,4
//  -,5
//  12,2 = 1,1(...)
//  13,3 = 1,1(...)
//  14,4 = 1,1(...)
//  15,5 = 1,1(...)
//  123,3 = 12,1(...) + 12,2(...)
//  124,4 = 12,1(...) + 12,2