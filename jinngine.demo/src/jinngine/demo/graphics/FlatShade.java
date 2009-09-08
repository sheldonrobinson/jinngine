package jinngine.demo.graphics;

import java.util.Iterator;
import javax.media.opengl.GL;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;

public class FlatShade implements ShapeRender {

//	private final GLU glu = new GLU();
	 
	@Override
	public void preRenderShape(Render render, Shape shape, Entity entity, GL gl) {
		
		if (entity.getAlarmed()) {
			// Create light components
			float ambientLight[] = { 2.0f, 1.5f, 1.5f, 1.0f };
			//		float diffuseLight[] = { 0.8f, 0.0f, 0.8f, 1.0f };
			//		float specularLight[] = { 0.5f, 0.5f, 0.5f, 1.0f };
			//		float position[] = { -1.5f, 1.0f, -4.0f, 1.0f };

			// Assign created components to GL_LIGHT0
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambientLight,0);
			//		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, diffuseLight,0);
			//		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, specularLight,0);
			//		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position,0);
		}
	}



	@Override
	public void init(Render render, GL gl) {
		// Somewhere in the initialization part of your program…
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		
		//gl.glShadeModel(GL.GL_FLAT);


		// Create light components
		float ambientLight[] = { 2.0f, 2.0f, 2.0f, 1.0f };
		float diffuseLight[] = { 0.2f, 0.2f, 0.2f, 1.0f };
		float specularLight[] = { 0.5f, 0.5f, 0.5f, 1.0f };
		float position[] = { -1.5f, 25.0f, -4.0f, 1.0f };

		// Assign created components to GL_LIGHT0
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambientLight,0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, diffuseLight,0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, specularLight,0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position,0);
		

	}
	
	

	@Override
	public void renderShape(Render render, Shape shape, GL gl) {



		//gl.glPushMatrix();
		gl.glMultMatrixd(Matrix4.pack(shape.getLocalTransform()),0);
		
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
		Iterator<Vector3[]> i = shape.getFaces(); 
		while (i.hasNext()) {
			gl.glBegin(GL.GL_POLYGON);
			Vector3[] face = i.next();
			//compute normal
			Vector3 n =face[1].minus(face[0]).cross(face[2].minus(face[1])).normalize();
			
			for ( Vector3 v: face) {
				gl.glNormal3d(n.x, n.y, n.z);
				//gl.glTexCoord2f(1.0f, 1.0f);
				//gl.glColor3d(v.a1, v.a2, v.a3);
				gl.glVertex3d(v.x, v.y, v.z);
				gl.glTexCoord2f(0.0f, 1.0f);
			}
			gl.glEnd();
		}
		
		
		gl.glPolygonMode( GL.GL_FRONT, GL.GL_LINE );
		gl.glLineWidth(1.7f);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glScaled(1.01, 1.01, 1.01);
		i = shape.getFaces(); 
		while (i.hasNext()) {
			gl.glBegin(GL.GL_POLYGON);
			Vector3[] face = i.next();
			//compute normal
			Vector3 n =face[1].minus(face[0]).cross(face[2].minus(face[1])).normalize();
			
			for ( Vector3 v: face) {
				gl.glNormal3d(n.x, n.y, n.z);
				//gl.glTexCoord2f(1.0f, 1.0f);
				gl.glColor3d(0.2,0.2, 0.2);
				gl.glVertex3d(v.x, v.y, v.z);
				gl.glTexCoord2f(0.0f, 1.0f);
			}
			gl.glEnd();
		}

	
		gl.glEnable(GL.GL_LIGHTING);

		
	}		
	
	/**
	 * This is where the "magic" is done:
	 *
	 * Multiply the current ModelView-Matrix with a shadow-projetion
	 * matrix.
	 *
	 * l is the position of the light source
	 * e is a point on within the plane on which the shadow is to be
	 *   projected.  
	 * n is the normal vector of the plane.
	 *
	 * Everything that is drawn after this call is "squashed" down
	 * to the plane. Hint: Gray or black color and no lighting 
	 * looks good for shadows *g*
	 */
	@SuppressWarnings("unused")
	private double[] shadowProjectionMatrix(Vector3 l, Vector3 e, Vector3  n)
	{
	  double d, c;
	  double[] mat = new double[16];

	  // These are c and d (corresponding to the tutorial)
	  
	  d = n.x*l.x + n.y*l.y + n.z*l.z;
	  c = e.x*n.x + e.y*n.y + e.z*n.z - d;

	  // Create the matrix. OpenGL uses column by column
	  // ordering

	  mat[0]  = l.x*n.x+c; 
	  mat[4]  = n.y*l.x; 
	  mat[8]  = n.z*l.x; 
	  mat[12] = -l.x*c-l.x*d;
	  
	  mat[1]  = n.x*l.y;        
	  mat[5]  = l.y*n.y+c;
	  mat[9]  = n.z*l.y; 
	  mat[13] = -l.y*c-l.y*d;
	  
	  mat[2]  = n.x*l.z;        
	  mat[6]  = n.y*l.z; 
	  mat[10] = l.z*n.z+c; 
	  mat[14] = -l.z*c-l.z*d;
	  
	  mat[3]  = n.x;        
	  mat[7]  = n.y; 
	  mat[11] = n.z; 
	  mat[15] = -d;

	  return mat;
	}
		

}
