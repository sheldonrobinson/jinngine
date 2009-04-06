package jinngine.stuff;

import jinngine.geometry.Shape;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class Tetrahedron extends Body { 
//  private final Shape tetrahedronShape;
//  private final SupportMap3 tetrahedronSupportMap;
  double sides = 1;
  
  public Tetrahedron( double sides ) {
    super();
    this.sides = sides;    
    Shape tetrahedronShape = new Shape();      
    setupShape(tetrahedronShape);
    this.setSupportMap(new HillClimbSupportMap( tetrahedronShape ));
  
  }
  
  public void updateMomentOfInertia() {
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

  public void setupShape() {
    return;
  }

  public void setupShape(Shape shape) {
  
    Vector3 p111 = new Vector3( sides, sides, sides );
    Vector3 p001 = new Vector3( -sides, -sides, sides );
    Vector3 p010 = new Vector3( -sides, sides, -sides );
    Vector3 p100 = new Vector3( sides, -sides, -sides );
    
    //(1,1,1) (-1,-1,1)

    //(2,2,0)
    shape.addVertex(p111);
    shape.addVertex(p001);
    shape.addVertex(p010);
    shape.addVertex(p100);
    
  }



    
}
