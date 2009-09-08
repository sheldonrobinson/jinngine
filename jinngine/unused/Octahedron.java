package jinngine.unused;
import java.util.*;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class Octahedron extends Body {
	private final double sides;
	
	public Octahedron( double sides ) {
		super();
		this.sides = sides;
	      updateMomentOfInertia();

//	      this.setSupportMap(new HillClimbSupportMap(shape));
	}
	
//	@Override
	public void setupShape() {
		// TODO Auto-generated method stub
	    Vector3 p111 = new Vector3( sides, sides, 0 );
	    Vector3 p001 = new Vector3( -sides, sides, 0 );
	    Vector3 p010 = new Vector3( sides, -sides, 0 );
	    Vector3 p100 = new Vector3( -sides, -sides, 0 );
	    Vector3 p000 = new Vector3( 0, 0, -sides );	    
	    Vector3 p2 = new Vector3( 0, 0, sides );	    

	    
	    List<Vector3> list = new ArrayList<Vector3>();
	    list.add(p111);
	    list.add(p001);
	    list.add(p010);
	    list.add(p100);
	    list.add(p000);
	    list.add(p2);
	    
	    
	    //convex hull
//	    ConvexHullAlgorithm algorithm = new RandomizedConvexHull();
//	    algorithm.run(list,shape);
	}

	public void updateMomentOfInertia() {
		// TODO Auto-generated method stub
		//real edge length
	    double a = (double)Math.sqrt(8)*sides; 
	    
	    //I
	    Matrix3.set( state.I,
	                 (1/20.0f)*state.M*a*a, 0.0f, 0.0f,
	                 0.0f, (1/20.0f)*state.M*a*a, 0.0f,
	                 0.0f, 0.0f, (1/20.0f)*state.M*a*a );
	    
	    
	    //I inverse
	    Matrix3.inverse( this.state.I, this.state.Iinverse );
	}

}
