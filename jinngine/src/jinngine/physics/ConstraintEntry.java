package jinngine.physics;

import jinngine.geometry.ContactGenerator;
import jinngine.math.Vector3;
import jinngine.physics.constraint.ContactConstraint;

/**
 * A linear constraint definition involving two bodies. 
 * 
 * consider a position constraint c(b1,b2) = 0 = r1-r2 
 * 
 * this is a function of 6 variables, the position of b1 and b2
 * Since this is a R6 -> R , the jacobian will be 6x1 
 * J^T = [ v1  -v2 ]
 * 
 * J^T M^-1 J = [ v1 -v2] [ 1/m1 0 \\ 0 1/m2] [v1 -v2]^T =
 * [v1(1/m1)  -v2(1/m2)] [v1 - v2]^T  = v1
 * 
 * @author mo
 *
 */
public class ConstraintEntry  {

	public Constraint owner;
	public Body body1,body2;
	public final Vector3 b1 = new Vector3(),b2= new Vector3(),b3= new Vector3(),b4= new Vector3();
	public final Vector3 j1= new Vector3(),j2= new Vector3(),j3= new Vector3(),j4= new Vector3();
	public double lambda = 0;
	public double lambdaMin = 0;
	public double lambdaMax = 0;
	public double diagonal = 0;
	public double b = 0;
	public boolean enabled = true;
	public ConstraintEntry coupledMax;
	public ContactGenerator.ContactPoint aux;
	public int index;
	public double phixk;
	
	public ConstraintEntry() {}
	
	public ConstraintEntry(Constraint owner, final Body body1, final Body body2, final Vector3 b1, final Vector3 b2, final Vector3 b3, final Vector3 b4, final Vector3 j1, final Vector3 j2, final Vector3 j3, final Vector3 j4, double lambdaMin, double lambdaMax, ConstraintEntry coupledMax, double b) {
		super();
		this.owner = owner;
		this.body1 = body1;
		this.body2 = body2;
		this.b1.assign(b1);
		this.b2.assign(b2);
		this.b3.assign(b3);
		this.b4.assign(b4);
		this.j1.assign(j1);
		this.j2.assign(j2);
		this.j3.assign(j3);
		this.j4.assign(j4);
		this.lambdaMin = lambdaMin;
		this.lambdaMax = lambdaMax;
		this.coupledMax = coupledMax;
		this.b = b;
		this.diagonal = j1.dot(b1) + j2.dot(b2) +  j3.dot(b3) + j4.dot(b4);
	}
	
	public final ConstraintEntry assign(ConstraintEntry constraint) {
		owner = constraint.owner;
		body1 = constraint.body1;
		body2 = constraint.body2;
		j1.assign(constraint.j1);
		j2.assign(constraint.j2);
		j3.assign(constraint.j3);
		j4.assign(constraint.j4);
		b1.assign(constraint.b1);
		b2.assign(constraint.b2);
		b3.assign(constraint.b3);
		b4.assign(constraint.b4);
		
		lambdaMin = constraint.lambdaMin;
		lambdaMax = constraint.lambdaMax;
		coupledMax = constraint.coupledMax;
		b = constraint.b;
		lambda = constraint.lambda;
		diagonal = constraint.diagonal;
		enabled = true;
		return this;
	}
	
	public final ConstraintEntry assign( Constraint owner, Body body1, Body body2, Vector3 b1, Vector3 b2, Vector3 b3, 
			Vector3 b4, Vector3 j1, Vector3 j2, Vector3 j3,
			Vector3 j4, double lambdaMin, double lambdaMax, ConstraintEntry coupledMax, double b) {
		
		this.owner = owner;
		this.body1 = body1;
		this.body2 = body2;
		this.b1.assign(b1);
		this.b2.assign(b2);
		this.b3.assign(b3);
		this.b4.assign(b4);
		this.j1.assign(j1);
		this.j2.assign(j2);
		this.j3.assign(j3);
		this.j4.assign(j4);
		this.lambda = 0;
		this.lambdaMin = lambdaMin;
		this.lambdaMax = lambdaMax;
		this.coupledMax = coupledMax;
		this.b = b;
		this.diagonal = j1.dot(b1) + j2.dot(b2) +  j3.dot(b3) + j4.dot(b4);
		
		return this;
	}

	public final Vector3 getB1() { 
		return b1.copy();
	}
	
	public final Vector3 getB2() {
		return b2.copy();	
	}

	public final Vector3 getB3() {
		return b3.copy();
	}

	public final Vector3 getB4() {
		return b4.copy();
	}

	public final Body getBody1() {
		return body1;
	}

	public final Body getBody2() {
		return body2;
	}

	public final double getDiagonal() {
		return diagonal;
	}

	public final Vector3 getJ1() {
		return j1.copy();
	}

	public final Vector3 getJ2() {
		return j2.copy();
	}

	public final Vector3 getJ3() {
		return j3.copy();
	}

	public final Vector3 getJ4() {
		return j4.copy();
	}

	public final double getLambda() {
		return lambda;
	}

	public final double getLambdaMax() {
		return lambdaMax;
	}

	public final double getLambdaMin() {
		return lambdaMin;
	}

	public final double getbValue() {
		return b;
	}

	public final void setDiagonal(double diagonal) {
		this.diagonal = diagonal;
	}

	public final void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public final boolean isEnabled() {
		return enabled;
	}

	public final void setEnabled(boolean enabled) {
		this.enabled = enabled;		
	}

}