package jinngine.unused;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;


public class Cylinder extends Body {
	private final double h;
	private final double r1;
	private final double r2;
	private final Vector3 CM;
	

	public Cylinder( double h, double r1, double r2) {
		super();
		this.h = h;
		this.r1 = r1;
		this.r2 = r2;
		System.out.println(" lal " + h +","+r2+","+ r1 );
		CM = new Vector3(0,0,(1.0/4.0)*h*  (3*r1*r1+2*r1*r2+r2*r2)/(r1*r2+r1*r1+r2*r2) );		
		 //= new Vector3(0,0,(1.0/4.0)*2 );		
        CM.print();

        //compute the tensor
		updateMomentOfInertia();
	}
	
//	@Override
	public void updateMomentOfInertia() {
		// TODO Auto-generated method stub
		//the values is interchanged in the inertia tensor
		double r1 = this.r2;
		double r2 = this.r1;
		
		
		
		double Ixx = ((3.0/80.0)*(4*r1*r1*r1*r1*r1*r1+
				8*r1*r1*r1*r1*r1*r2+12*r2*r2*r1*r1*r1*r1+
				h*h*r1*r1*r1*r1+
				12*r2*r2*r2*r1*r1*r1+
				4*h*h*r1*r1*r1*r2+
				10*h*h*r2*r2*r1*r1+
				12*r2*r2*r2*r2*r1*r1+
				8*r2*r2*r2*r2*r2*r1+
				4*h*h*r2*r2*r2*r1+
				4*r2*r2*r2*r2*r2*r2+
				h*h*r2*r2*r2*r2)*state.M) / 
				(r1*r1*r1*r1+
						2*r1*r1*r1*r2+
						3*r2*r2*r1*r1+
						2*r2*r2*r2*r1+
						r2*r2*r2*r2);
		
		double Iyy = Ixx;
		
		double Izz =  (3.0/10.0)*state.M*((r2*r2*r2*r2+r1*r1*r1*r1+r1*r1*r1*r2+r2*r2*r1*r1+r2*r2*r2*r1)/(r1*r2+r1*r1+r2*r2));




		//Inertia tensor for the cylinder. 
		Matrix3.set( state.I,
				Ixx, 0.0f, 0.0f,
				0.0f, Iyy, 0.0f,
				0.0f, 0.0f, Izz );


		//I inverse
		Matrix3.inverse( this.state.I, this.state.Iinverse );
		
		Matrix3.print(state.I);

	}

//	@Override
	public void setupShape() {
		// TODO Auto-generated method stub
		   List<Vector3> list = new ArrayList<Vector3>();
//		    Random generator = new Random();

		    //create som points on the convex hull of the cylinder 
		    //the r2 end
		    for (float theta=0.01f; theta<2*Math.PI; theta+=Math.PI/16) {
		    	Vector3 p = new Vector3( r1*((double)(Math.sin(theta))), 
		    			r1*(double)(Math.cos(theta)), 
		    			0).minus(CM);          
		    	list.add( p );
		    }
		    
		    //the r1 end
		    for (float theta=0.01f; theta<2*Math.PI; theta+=Math.PI/16) {
		    	Vector3 p = new Vector3( r2*((double)(Math.sin(theta))), 
		    			r2*(double)(Math.cos(theta)), 
		    			h).add((new Vector3(0,0,0)).minus(CM));          
		    	list.add( p );
		    }

		    //fire off the convex hull
//		    ConvexHullAlgorithm algorithm = new RandomizedConvexHull();
//		    algorithm.run(list,shape);

	}

}
