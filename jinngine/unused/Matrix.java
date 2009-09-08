package jinngine.unused;
import java.util.Random;

import jinngine.math.Vector3;

//very basic matrix mxn implementation
public class Matrix {
	private int m; //rows
	private int n; //coloumns
	
	//Note
	//
	// An m x n matrix is
	//               n
	//             columns (J changes -> )
	//      a(i,j) ________________
	//     m      |11 12 13 .  .  .
	//    rows    |21 22
	// (i changes)|31   33
	//            |.      .
	//            |.        .
	//            |.          .
	
	public final double[][] elements;
	
	public Matrix(int m,int n) {
		elements = new double[m][n];
		this.n = n; this.m = m;				
		Matrix.zero(this);
	}
	
	
	
	public static void gaussianElimination(Matrix A) {
		//algorithm from Wikipedia. So called single pivot algorithm
		//http://en.wikipedia.org/wiki/Gaussian_elimination
		
		//perform a gaussian elimination on the matrix
		int[] rowPermutation = null; // Permutation vector
		rowPermutation = new int[A.m]; //Permutation vector
		for(int i=0;i<A.m;i++) rowPermutation[i]=i; //set identity

		
		int i = 0; int j = 0;
		while (i<(A.m) && j<(A.n-1) ) {
			//find pivot element (the gratest value in the j'th column )
			int maxi = i;
			for (int k=i+1; k<A.m; k++) {
				if ( Math.abs(A.elements[rowPermutation[k]][j])  >  Math.abs(A.elements[rowPermutation[maxi]][j]) ) {
					maxi = k;
				}
			}
			
			if (A.elements[rowPermutation[maxi]][j] != 0) {				
				//swap the rows using the permutation vector
				int temp = rowPermutation[i];
				rowPermutation[i] = rowPermutation[maxi];
				rowPermutation[maxi] = temp;
				
				//normalize the row relating to A[i,j]
				double value = A.elements[rowPermutation[i]][j];
				for (int u=0; u<A.n; u++) {
					A.elements[rowPermutation[i]][u] /= value;
				}
	
				//Matrix.print(A);
				//System.out.println("------------------------------------");

				//TODO the u=0 is a hack, to avoid implementing back-substitution
				for (int u=0 /*i+1*/; u<A.m; u++) {
					//skip if we reach the i'th row
					if (u==i) continue;
					double multiplier = A.elements[rowPermutation[u]][j];
					
					//if (multiplier == 0) { System.out.println("multiplier zero??");}

					//todo
					for (int v=j; v<A.n; v++) {
						A.elements[rowPermutation[u]][v] -= A.elements[rowPermutation[i]][v] * multiplier; 	
					}
					
					
					//Matrix.print(A);
					//System.out.println("------------------------------------");
				}
				
				//increment i
				i++;
			}
			//incerement j
			j++;
		}
		
		double[][] permutated = new double[A.m][A.n];
		//now we must reorder the permutation of the matrix
		for (int c=0;c<A.m;c++) {
			for (int u=0;u<A.n;u++) {
				permutated[c][u] = A.elements[rowPermutation[c]][u];
			}
		}
		
		//set the new reordered ellements
		//TODO this isn't verry smart, since the reordering of the matrix is better done in-place
		//A.elements = permutated;
		//copy
		for (i=0;i<A.m;i++)
			for (j=0;j<A.n;j++)
				A.elements[i][j] = permutated[i][j];
			
		
	}

	//return identity of this matrix dimentions
	public Matrix identity() {
		Matrix A = new Matrix(m,n);
		Matrix.identity(A);				
		return A;
	}
	
	public Matrix concatenateVertical(Matrix B) {
		Matrix A = this;
		assert A.m == B.m;  //matrices have the same number of rows
		Matrix C = new Matrix(A.m,A.n+B.n);
		
		//System.out.println(""+C.n);
		
		//copy A into C
		Matrix.copy( A, C, 0, 0);
		
		//copy B into C
		Matrix.copy(B, C, 0, A.n );
	
		return C;
	}
	
	public Matrix concatenateHorizontal(Matrix B) {
		Matrix A = this;
		assert A.n == B.n;  //matrices have the same number of columns
		Matrix C = new Matrix(A.m + B.m,A.n);
		
		//System.out.println(""+C.n);
		
		//copy A into C
		Matrix.copy( A, C, 0, 0);
		
		//copy B into C
		Matrix.copy(B, C, A.m, 0 );
	
		return C;
	}
	
	public Matrix inverse() {
		if (this.n!=this.m) return null;
		Matrix system = this.concatenateVertical(this.identity());
		Matrix.gaussianElimination(system);
		//Matrix.print(system);
		//System.out.println("********");
		
		Matrix returnMatrix = new Matrix(this.n,this.n);
		Matrix.copy(system, 0, this.n, returnMatrix, 0, 0);
		//Matrix.print(system);
		
		//check for prefixed identity matrix (if
		//not, the given matrix was singular)
		for (int i=0;i<this.n;i++)
			if (system.elements[i][i] != 1.0f )
				return null;
		
		
		return returnMatrix;
	}

	public static void random(Matrix A) {
		Random generator = new Random();
		//set matrix to random values
		for (int i=0; i<A.m; i++)
			for(int j=0; j<A.n; j++)
				A.elements[i][j]=generator.nextFloat()*64;
		
	}
	
	public static void zero(Matrix A) {
		//Zero out this matrix
		for (int i=0; i<A.m; i++)
			for(int j=0; j<A.n; j++)
				A.elements[i][j]=0;
	}
	
	public static Matrix identity(Matrix A) {
		//make A all zeros
		Matrix.zero(A);

		//set diagonal of ones
		for (int i=0;i<A.n;i++)
			A.elements[i][i] = 1;
		
		return A;
	}
	
	//copy the source matrix into the destination, starting at the i,j index
	public static void copy(Matrix source, Matrix dest, int i, int j ) {
		for (int x=0;x<source.m; x++)
			for (int y=0;y<source.n; y++) 
				dest.elements[x+i][y+j] = source.elements[x][y];
	}
	
	//copy the source matrix into the destination, starting at the i,j index in dest, and 
	//at u,v in source
	public static void copy(Matrix source, int u, int v, Matrix dest, int i, int j ) {
		for (int x=0;x<source.m-u; x++)
			for (int y=0;y<source.n-v; y++) 
				dest.elements[x+i][y+j] = source.elements[x+u][y+v];
	}

	//insert the m'th row of the matrix into destRow
	public static void getRow( Matrix A, int m, double[] destRow ) {
		for (int j=0;j<A.n;j++)
			destRow[j] = A.elements[m][j];
	}
	
	//set the m'th row of the matrix to sourceRow
	public static void setRow( Matrix A, int m, double[] sourceRow) {
		for (int j=0;j<A.n;j++)
			A.elements[m][j] = sourceRow[j];
	}
	
	//swap two rows in the matrix
	public static void swapRow( Matrix A, int am, int bm  ) {
		for (int j=0;j<A.n;j++) {
			double t = A.elements[am][j];
			A.elements[am][j] = A.elements[bm][j];
			A.elements[bm][j] = t;
		}
			
	}
	
	//multiply A by scalar b and put result in C
	public static void multiply( Matrix A, double b, Matrix C) {
		for (int i=0; i<A.m; i++) 
			for (int j=0; j<A.n; j++) 
				C.elements[i][j] = A.elements[i][j] * b;
	}
	
	//Multiply A by B and place result in C
	public static void multiply( Matrix A, Matrix B, Matrix C) {
		//check dimentions
		//   B
		// A C
				
		// Basic matrix multiplication
		if ( A.n == B.m) {
			//for each row in A
			for (int i=0; i<A.m;i++) {
				//for each column in B
				for (int j=0;j<B.n;j++) {
					//set zero
					C.elements[i][j] = 0;
					
				    //again for the columns in A (and rows in B) 
					for (int k=0;k<A.n;k++)
						C.elements[i][j] += A.elements[i][k] * B.elements[k][j];
				}
			}
		} else {
			System.out.println("Matrices has wrong dimentions");
			System.exit(-1);
		}
	}
		
	//multiply row by scalar
	public static void multiplyRow(Matrix A, int m, double a) {
		for (int j=0;j<A.n;j++)
			A.elements[m][j] *= a;
	}
	
	public static void print(Matrix A) {
		for(int i=0;i<A.m;i++) {
			System.out.println();
			for(int j=0;j<A.n;j++) {
				System.out.print(" " + String.valueOf((double)((int)(A.elements[i][j]*10000))/10000)  + " ");
			}
		}			
	}
	
	//functional methods
	public Matrix multiply( Matrix M ) {
		Matrix R = new Matrix(this.m, M.n );
		Matrix.multiply(this, M, R);
		return R;
	}
	
	public Matrix multiply( double r ) {
		Matrix R = new Matrix(this.m, this.n );
		Matrix.multiply(this, r, R);
		return R;
	}

	
	public Matrix transpose() {
		Matrix T = new Matrix(n,m);		
		//copy elements
		for (int i=0; i<this.m; i++) 
			for (int j=0; j<this.n; j++)
				T.elements[j][i] = this.elements[i][j];

		return T;
	}

	//return a copy of this matrix
	public Matrix copy() {
		Matrix M = new Matrix(m,n);
		Matrix.copy(this, M, 0, 0);
		return M;
	}

	public double[] pack() {
		double[] array = new double[m*n]; int x = 0;
		for (int j=0;j<n; j++)
			for (int i=0;i<m; i++)
				array[x++] = elements[i][j];
		return array;
	}
	
	public Matrix subscript(int i, int j, int h, int w) {
		Matrix M = new Matrix(h,w);
		Matrix.copy(this, M, i, j);
		return M;
	}

	public Vector3 subscript3x1(int i, int j) {
		Vector3 v = new Vector3();
		v.a1 = this.elements[i][j];
		v.a2 = this.elements[i+1][j];
		v.a3 = this.elements[i+2][j];
		return v;
	}


	public static void gaussSeidel( Matrix A, double b[], double x[] ) {
		double[][] M = A.elements;
		
		for (int iterations=0; iterations<156; iterations++) {
			for (int i=0; i<x.length; i++) {
				double sum=0;
				for (int j=0; j<x.length; j++) 
					sum += M[i][j] * x[j];
				
				x[i] += ( b[i] - sum  ) / M[i][i];

			}
		}
	}
	
	
	public static void projectedGaussSeidel( Matrix A, double b[], double c[][], double x0[] ) {
		double[][] M = A.elements;
		
		double[] x = x0;

		for (int iterations=0; iterations<32; iterations++) {

			for (int i=0; i<x.length; i++) {
				double sum=0;

				for (int j=0; j<x.length; j++) 
					sum += M[i][j] * x[j];

				double delta_xi = ( b[i] - sum  ) / M[i][i];				
				x[i] += delta_xi;
				x[i] = Math.max(c[i][0], Math.min(x[i],c[i][1] ));
			}
		}
	}
	

	
} //class
