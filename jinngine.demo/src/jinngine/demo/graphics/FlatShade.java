package jinngine.demo.graphics;

import java.util.Iterator;
import javax.media.opengl.GL;
import javax.media.opengl.glu.*;

import jinngine.math.Matrix4;
import jinngine.math.Vector3;

public class FlatShade implements ShapeRender {

	private final GLU glu = new GLU();
	
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
				gl.glNormal3d(n.a1, n.a2, n.a3);
				//gl.glTexCoord2f(1.0f, 1.0f);
				//gl.glColor3d(v.a1, v.a2, v.a3);
				gl.glVertex3d(v.a1, v.a2, v.a3);
				gl.glTexCoord2f(0.0f, 1.0f);
			}
			gl.glEnd();
		}
		
		
		gl.glPolygonMode( GL.GL_FRONT, GL.GL_LINE );
		gl.glLineWidth(2.7f);
		gl.glDisable(GL.GL_LIGHTING);
		i = shape.getFaces(); 
		while (i.hasNext()) {
			gl.glBegin(GL.GL_POLYGON);
			Vector3[] face = i.next();
			//compute normal
			Vector3 n =face[1].minus(face[0]).cross(face[2].minus(face[1])).normalize();
			
			for ( Vector3 v: face) {
				gl.glNormal3d(n.a1, n.a2, n.a3);
				//gl.glTexCoord2f(1.0f, 1.0f);
				gl.glColor3d(0.2,0.2, 0.2);
				gl.glVertex3d(v.a1, v.a2, v.a3);
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
	private double[] shadowProjectionMatrix(Vector3 l, Vector3 e, Vector3  n)
	{
	  double d, c;
	  double[] mat = new double[16];

	  // These are c and d (corresponding to the tutorial)
	  
	  d = n.a1*l.a1 + n.a2*l.a2 + n.a3*l.a3;
	  c = e.a1*n.a1 + e.a2*n.a2 + e.a3*n.a3 - d;

	  // Create the matrix. OpenGL uses column by column
	  // ordering

	  mat[0]  = l.a1*n.a1+c; 
	  mat[4]  = n.a2*l.a1; 
	  mat[8]  = n.a3*l.a1; 
	  mat[12] = -l.a1*c-l.a1*d;
	  
	  mat[1]  = n.a1*l.a2;        
	  mat[5]  = l.a2*n.a2+c;
	  mat[9]  = n.a3*l.a2; 
	  mat[13] = -l.a2*c-l.a2*d;
	  
	  mat[2]  = n.a1*l.a3;        
	  mat[6]  = n.a2*l.a3; 
	  mat[10] = l.a3*n.a3+c; 
	  mat[14] = -l.a3*c-l.a3*d;
	  
	  mat[3]  = n.a1;        
	  mat[7]  = n.a2; 
	  mat[11] = n.a3; 
	  mat[15] = -d;

	  return mat;
	}
		

}
