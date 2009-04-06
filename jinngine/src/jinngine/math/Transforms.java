package jinngine.math;

/**
 * Various geometric transformations on matrix form 
 */
public final class Transforms {	
	/**
	 * Return a 4x4 translation matrix for homogeneus coordinates
	 * @param r Vector representing a translation 
	 * @return A 4x4 translation matrix
	 */
	public final static Matrix4 translate4(Vector3 r) {
		Matrix4 T = new Matrix4();
		Matrix4.identity(T);
		T.a14 = r.a1;
		T.a24 = r.a2;
		T.a34 = r.a3;
		return T;
	}
	/**
	 * Given homogeneous transformation matrix, return translation vector
	 * @param M transformation matrix
	 * @return translation vector
	 */
	public final static Vector3 translation(Matrix4 M) {
		return new Vector3(M.a14, M.a24, M.a34);
	}
	
	/**
	 * Create a 4x4 rotation matrix for homogeneous coordinates
	 * @param q Quaternion representing a rotation i R3 space
	 * @return A 4x4 rotation matrix
	 */
	  public final static Matrix4 rotate4(Quaternion q) {
		  Matrix4 M = new Matrix4();
		  Vector3 v = q.v;
		  double s = q.s;
		  M.a11 = 1-2*(v.a2*v.a2+v.a3*v.a3); M.a12 =  2*v.a1*v.a2-2*s*v.a3;      M.a13 = 2*s*v.a2+2*v.a1*v.a3;  
		  M.a21 = 2*v.a1*v.a2+2*s*v.a3;      M.a22 =  1-2*(v.a1*v.a1+v.a3*v.a3); M.a23 = -2*s*v.a1+2*v.a2*v.a3;
		  M.a31 = -2*s*v.a2+2*v.a1*v.a3;     M.a32 =  2*s*v.a1+2*v.a2*v.a3;      M.a33 =  1-2*(v.a1*v.a1+v.a2*v.a2);	  
		  M.a44 = 1;	  
		  return M;
	  }
	  
	  // 1  0  0  x    r  r  r  0      r  r  r  x
	  // 0  1  0  y    r  r  r  0  =   r  r  r  y
	  // 0  0  1  z    r  r  r  0      r  r  r  z
	  // 0  0  0  1    0  0  0  1      0  0  0  1

	  /**
	   * Create a combined rotation and translation matrix, in described order, T(r)R(q)
	   * @param q Quaternion representing a rotation
	   * @param r Vector representing translation
	   * @return Combined rotation and translation matrix 
	   */
	  public final static Matrix4 rotateAndTranslate4(Quaternion q, Vector3 r) {
		  Matrix4 M = new Matrix4();
		  Vector3 v = q.v;
		  double s = q.s;
		  M.a11 = 1-2*(v.a2*v.a2+v.a3*v.a3); M.a12 =  2*v.a1*v.a2-2*s*v.a3;      M.a13 = 2*s*v.a2+2*v.a1*v.a3;       M.a14 = r.a1;
		  M.a21 = 2*v.a1*v.a2+2*s*v.a3;      M.a22 =  1-2*(v.a1*v.a1+v.a3*v.a3); M.a23 = -2*s*v.a1+2*v.a2*v.a3;      M.a24 = r.a2;
		  M.a31 = -2*s*v.a2+2*v.a1*v.a3;     M.a32 =  2*s*v.a1+2*v.a2*v.a3;      M.a33 =  1-2*(v.a1*v.a1+v.a2*v.a2); M.a34 = r.a3;	  
		  M.a44 = 1;	  
		  return M;
		  
	  }
	  
	  /**
	   * Create a combined transform and translation matrix, in described order, T(r)B
	   * @param B Matrix representing a transform
	   * @param r Vector representing translation
	   * @return Combined transform and translation matrix 
	   */
	  public final static Matrix4 transformAndTranslate4(Matrix3 B, Vector3 r) {
		  Matrix4 M = new Matrix4();
		  M.a11 = B.a11; M.a12 = B.a12; M.a13 = B.a13; M.a14 = r.a1;
		  M.a21 = B.a21; M.a22 = B.a22; M.a23 = B.a23; M.a24 = r.a2;
		  M.a31 = B.a31; M.a32 = B.a32; M.a33 = B.a33; M.a34 = r.a3;	  
		                                               M.a44 = 1;	  
		  return M;
	  }
	  
	  /**
	   * Create a scaling transform scaled in the entries of vector s 
	   * @param s
	   * @return
	   */
	  public final static Matrix4 scale(Vector3 s) {
		  Matrix4 M = new Matrix4();
		  M.a11 = s.a1; M.a12 = 0; M.a13 = 0; M.a14 = 0;
		  M.a21 = 0; M.a22 = s.a2; M.a23 = 0; M.a24 = 0;
		  M.a31 = 0; M.a32 = 0; M.a33 = s.a3; M.a34 = 0;	  
		                                               M.a44 = 1;	  
		  return M;
		  
	  }

}
