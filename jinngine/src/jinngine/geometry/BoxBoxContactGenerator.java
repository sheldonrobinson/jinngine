package jinngine.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.math.Transforms;
import jinngine.physics.Body;
import jinngine.util.Pair;

public class BoxBoxContactGenerator implements ContactGenerator {

	private final Box b1;
	private final Box b2;
	private final Body body1;
	private final Body body2;
	private final List<ContactPoint> contacts = new ArrayList<ContactPoint>();
	
	public BoxBoxContactGenerator( jinngine.geometry.Box a, jinngine.geometry.Box b) {
		b1 = a;
		b2 = b;
		body1 = b1.getBody();
		body2 = b2.getBody();	
	}
	
	@Override
	public Iterator<ContactPoint> getContacts() {
		return contacts.iterator();
	}

	@Override
	public boolean run(double dt) {
		Vector3[] p = new Vector3[16];
		for (int i=0;i<16;i++) p[i] = new Vector3();
 		Vector3 n = new Vector3();
		double[] dists = new double[16];

		//System.out.println("run boxbox:");

		
		int N = boxbox( body1.state.rCm.copy(),
				body1.state.q.rotationMatrix3(), 
				body1,
				b1.getExtends(),
				body2.state.rCm.copy(),
				body2.state.q.rotationMatrix3(),
				body2,
				b2.getExtends(),
				0.15, p, n, dists );
		
		
		contacts.clear();
		for ( int i=0;i<N; i++) {
			if ( p[i] != null) {
				ContactPoint cp = new ContactPoint();
				cp.depth = dists[i];
				cp.normal.assign(n.multiply(-1));				
				cp.midpoint.assign(p[i]);
				//p[i].print();
				//System.out.println("dist="+dists[i]);
				//n.print();
				contacts.add( cp);
			} else {
				break;
			}
		}

		//n.print();

		
		return true;
	}


	/**
	 * Improved Box Box Collision Test.
	 *
	 * @param p_a       Center of box A in WCS.
	 * @param p_b       Center of box B in WCS
	 * @param R_a       Box A's orientation in WCS
	 * @param R_b       Box B's orientation in WCS
	 * @param ext_a     Extents of box A, i.e. half edge sizes.
	 * @param ext_b     Extents of box B, i.e. half edge sizes.
	 * @param envelope  The size of the collision envelope. If cloest point are separted by more than this distance then there is no contact.
	 * @param p         Pointer to array of contact points, must have room for at least eight vectors.
	 * @param n         Upon return this argument holds the contact normal pointing from box A towards box B.
	 * @param distance  Pointer to array of separation (or penetration) distances. Must have room for at least eight values.
	 *
	 * @return          If contacts exist then the return value indicates the number of contacts, if no contacts exist the return valeu is zero.
	 */
	private int boxbox(Vector3  p_a,
			Matrix3  R_a,
			Body ba,
			Vector3  ext_a,
			Vector3  p_b,
			Matrix3  R_b,
			Body bb,
			Vector3  ext_b,
			double  envelope,
			Vector3[]  p,
			Vector3    n,
			double[]  distances) { 
		//		#include <OpenTissue/configuration.h>
		//
		//		#include <OpenTissue/core/math/math_coordsys.h>
//		#include <OpenTissue/core/math/math_constants.h>
//
//		#include <OpenTissue/core/math/math_precision.h>
//		#include <OpenTissue/core/geometry/geometry_compute_closest_points_line_line.h>
//		#include <OpenTissue/collision/intersect/intersect_rect_quad_edges.h>



//		      typedef          math::CoordSys<real_type>      coordsys_type;
//		      typedef typename coordsys_type::quaternion_type quaternion_type;

//		      assert(p);
//		      assert(distances);

		      
		      //--- Sign lookup table, could be precomputed!!!
		      Vector3[] sign = new Vector3[8];
//		      for (int i=0;i<8;i++) sign[i] = new Vector3(); //init array
//		      System.out.println("start");
//		      for(int mask=0;mask<8;++mask)
//		      {
//		        sign[mask].a1 = (mask&0x0001)==1?1:-1;
//		        sign[mask].a2 = ((mask>>1)&0x0001)==1?1:-1;
//		        sign[mask].a3 = ((mask>>2)&0x0001)==1?1:-1;
//		        sign[mask].print();
//		      }
//		      System.out.println("stop");
		      
		      sign[0] = new Vector3(-1,-1,-1);
		      sign[1] = new Vector3( 1,-1,-1);
		      sign[2] = new Vector3(-1, 1,-1);
		      sign[3] = new Vector3( 1, 1,-1);
		      sign[4] = new Vector3(-1,-1, 1);
		      sign[5] = new Vector3( 1,-1, 1);
		      sign[6] = new Vector3(-1, 1, 1);
		      sign[7] = new Vector3( 1, 1, 1);

		      
		      //--- extract axis of boxes in WCS
		      Vector3[] A = new Vector3[3];
		      for (int i=0;i<3;i++) A[i] = new Vector3(); //init array
		      
		      A[0].a1 = R_a.a11;   A[0].a2 = R_a.a21;   A[0].a3 = R_a.a31;
		      A[1].a1 = R_a.a12;   A[1].a2 = R_a.a22;   A[1].a3 = R_a.a32;
		      A[2].a1 = R_a.a13;   A[2].a2 = R_a.a23;   A[2].a3 = R_a.a33;

		      Vector3[] B =  new Vector3[3];
		      for (int i=0;i<3;i++) B[i] = new Vector3(); //init array
		      B[0].a1 = R_b.a11;   B[0].a2 = R_b.a21;   B[0].a3 = R_b.a31;
		      B[1].a1 = R_b.a12;   B[1].a2 = R_b.a22;   B[1].a3 = R_b.a32;
		      B[2].a1 = R_b.a13;   B[2].a2 = R_b.a23;   B[2].a3 = R_b.a33;

		      //--- To compat numerical round-offs, these tend to favor edge-edge
		      //--- cases, when one really rather wants a face-case. Truncating
		      //--- seems to let the algorithm pick face cases over edge-edge
		      //--- cases.
		      for(int i=0;i<3;++i)
		        for(int j=0;j<3;++j)
		        {
		          if( Math.abs( A[i].get(j) ) < 1e-7 )
		            A[i].set(j,0);
		          if( Math.abs( B[i].get(j) ) < 1e-7 )
		            B[i].set(j,0);
		        }

		        Vector3[] a = new Vector3[8];
		        Vector3[] b = new Vector3[8];
		        //--- corner points of boxes in WCS
		        for(int i=0;i<8;++i)
		        {
		          a[i] = A[2].multiply(sign[i].get(2)*ext_a.get(2)).add( A[1].multiply(sign[i].get(1)*ext_a.get(1)) ).add(  A[0].multiply(sign[i].get(0)*ext_a.get(0)) ).add(p_a);
		          b[i] = B[2].multiply(sign[i].get(2)*ext_b.get(2)).add( B[1].multiply(sign[i].get(1)*ext_b.get(1)) ).add(  B[0].multiply(sign[i].get(0)*ext_b.get(0)) ).add(p_b);
		        }

		        //--- Potential separating axes in WCS
		        Vector3[] axis = new Vector3[15];
			    for (int i=0;i<15;i++) axis[i] = new Vector3(); //init array

			    axis[0] = A[0].copy();
			    axis[1] = A[1].copy();
			    axis[2] = A[2].copy();
			    axis[3] = B[0].copy();
			    axis[4] = B[1].copy();
			    axis[5] = B[2].copy();
			    axis[6] = A[0].cross(B[0]); //what does this do
			    if(axis[6].get(0)==0 && axis[6].get(1)==0 && axis[6].get(2)==0)
			    	axis[6].assign(A[0]);
			    else
			    	axis[6].assign( axis[6].multiply(1/Math.sqrt(axis[6].dot(axis[6]))) );
			    axis[7].assign(A[0].cross(B[1]));
			    if(axis[7].get(0)==0 && axis[7].get(1)==0 && axis[7].get(2)==0)
			    	axis[7].assign(A[0]);
			    else
			    	axis[7].assign( axis[7].multiply(1/Math.sqrt(axis[7].dot(axis[7]))) );
			    axis[8].assign(A[0].cross(B[2]));
			    if(axis[8].get(0)==0 && axis[8].get(1)==0 && axis[8].get(2)==0)
			    	axis[8].assign(A[0]);
			    else
			    	axis[8].assign(axis[8].multiply(1/Math.sqrt(axis[8].dot(axis[8]))) );
			    axis[9].assign(A[1].cross(B[0]));
			    if(axis[9].get(0)==0 && axis[9].get(1)==0 && axis[9].get(2)==0)
			    	axis[9].assign(A[1]);
			    else
			    	axis[9].assign( axis[9].multiply(1/Math.sqrt(axis[9].dot(axis[9]))) );
			    axis[10].assign( A[1].cross(B[1]));
			    if(axis[10].get(0)==0 && axis[10].get(1)==0 && axis[10].get(2)==0)
			    	axis[10].assign(A[1]);
			    else
			    	axis[10].assign( axis[10].multiply(1/Math.sqrt(axis[10].dot(axis[10]))) );
			    axis[11].assign(A[1].cross(B[2]));
			    if(axis[11].get(0)==0 && axis[11].get(1)==0 && axis[11].get(2)==0)
			    	axis[11].assign(A[1]);
			    else
			    	axis[11].assign( axis[11].multiply(1/Math.sqrt(axis[11].dot(axis[11]))) );
			    axis[12].assign(A[2].cross(B[0]));
			    if(axis[12].get(0)==0 && axis[12].get(1)==0 && axis[12].get(2)==0)
			    	axis[12].assign(A[2]);
			    else
			    	axis[12].assign( axis[12].multiply(1/Math.sqrt(axis[12].dot(axis[12]))) );
			    axis[13].assign( A[2].cross(B[1]));
			    if(axis[13].get(0)==0 && axis[13].get(1)==0 && axis[13].get(2)==0)
			    	axis[13].assign(A[2]);
			    else
			    	axis[13].assign( axis[13].multiply(1/Math.sqrt(axis[13].dot(axis[13]))) );
			    axis[14].assign(A[2].cross(B[2]));
			    if(axis[14].get(0)==0 && axis[14].get(1)==0 && axis[14].get(2)==0)
			    	axis[14].assign(A[2]);
			    else
			    	axis[14].assign(axis[14].multiply(1/Math.sqrt(axis[14].dot(axis[14]))) );


		        //--- project vertices of boxes onto separating axis
		        double[] min_proj_a = new double[15];
		        double[] min_proj_b = new double[15];
		        double[] max_proj_a = new double[15];
		        double[] max_proj_b = new double[15];
		        for(int i=0;i<15;++i)
		        {
//		          min_proj_a[i] = min_proj_b[i] = math::detail::highest<real_type>();
//		          max_proj_a[i] = max_proj_b[i] = math::detail::lowest<real_type>();

		          min_proj_a[i] = min_proj_b[i] = Double.POSITIVE_INFINITY;
		          max_proj_a[i] = max_proj_b[i] = Double.NEGATIVE_INFINITY;

		        }
		        for(int i=0;i<15;++i)
		        {
		          for(int j=0;j<8;++j)
		          {
		            double proj_a =  a[j].dot(axis[i]);
		            double proj_b = b[j].dot(axis[i]);
		            min_proj_a[i] = Math.min(min_proj_a[i],proj_a);
		            max_proj_a[i] = Math.max(max_proj_a[i],proj_a);
		            min_proj_b[i] = Math.min(min_proj_b[i],proj_b);
		            max_proj_b[i] = Math.max(max_proj_b[i],proj_b);
		          }
		          //--- test for valid separation axis if so return
		          if (min_proj_a[i] > (max_proj_b[i]+envelope) ||   min_proj_b[i] > (max_proj_a[i]+envelope))
		            return 0;
		        }
		        //--- Compute box overlaps along all 15 separating axes, and determine
		        //--- minimum overlap
		        double[] overlap = new double[15];
		        double minimum_overlap = Double.NEGATIVE_INFINITY; //math::detail::lowest<real_type>();
		        int minimum_axis = 15;
		        boolean[] flip_axis = new boolean[15];
		        //--- Notice that edge-edge cases are testet last, so face cases
		        //--- are favored over edge-edge cases
		        for(int i=0;i<15;++i)
		        {
		          flip_axis[i] = false;
//		          overlap[i] = math::detail::highest<real_type>();
		          overlap[i] = Double.POSITIVE_INFINITY;

		          if(max_proj_a[i] <= min_proj_b[i])
		          {
		            overlap[i] = Math.min( overlap[i], min_proj_b[i] - max_proj_a[i] );
		            if(overlap[i]>minimum_overlap)
		            {
		              minimum_overlap = overlap[i];
		              minimum_axis = i;
		              flip_axis[i] = false;
		            }
		          }
		          if(max_proj_b[i] <= min_proj_a[i])
		          {
		            overlap[i] = Math.min( overlap[i], min_proj_a[i] - max_proj_b[i] );
		            if(overlap[i]>minimum_overlap)
		            {
		              minimum_overlap = overlap[i];
		              minimum_axis = i;
		              flip_axis[i] = true;
		            }
		          }
		          if(min_proj_a[i] <= min_proj_b[i] &&  min_proj_b[i] <= max_proj_a[i])
		          {
		            overlap[i] = Math.min( overlap[i], -(max_proj_a[i] - min_proj_b[i]) );
		            if(overlap[i]>minimum_overlap)
		            {
		              minimum_overlap = overlap[i];
		              minimum_axis = i;
		              flip_axis[i] = false;
		            }
		          }
		          if(min_proj_b[i] <= min_proj_a[i] &&  min_proj_a[i] <= max_proj_b[i])
		          {
		            overlap[i] = Math.min(overlap[i], -(max_proj_b[i] - min_proj_a[i]) );
		            if(overlap[i]>minimum_overlap)
		            {
		              minimum_overlap = overlap[i];
		              minimum_axis = i;
		              flip_axis[i] = true;
		            }
		          }
		        }
		        if(minimum_overlap>envelope)
		          return 0;
		        //--- Take care of normals, so they point in the correct direction.
		        for(int i=0;i<15;++i)
		        {
		          if(flip_axis[i])
		            axis[i].assign(axis[i].multiply(-1));
		        }
		        //--- At this point we know that a projection along axis[minimum_axis] with
		        //--- value minimum_overlap will lead to non-penetration of the two boxes. We
		        //--- just need to generate the contact points!!!
		        int corners_inside = 0;
		        int corners_B_in_A = 0;
		        int corners_A_in_B = 0;
		        boolean[] AinB = new boolean[8];
		        boolean[] BinA = new boolean[8];

//		        coordsys_type WCStoB(p_b,R_b);
//		        coordsys_type WCStoA(p_a,R_a);
//		        
//		        WCStoA = inverse(WCStoA);
//		        WCStoB = inverse(WCStoB);
		        
		        //Matrix4 WtoA = Matrix4.invert(new Matrix4(R_a));
		        //Matrix4 WtoB = Matrix4.invert(new Matrix4(R_b));
		        //Matrix3 WtoA = Matrix3.inverse(R_b, WtoA);
		        
		        Vector3 eps_a = ext_a.add(new Vector3(envelope,envelope,envelope));
		        Vector3 eps_b = ext_b.add(new Vector3(envelope,envelope,envelope));
		        for(int i=0;i<8;++i)
		        {
		          Vector3 a_in_B = a[i].copy();
		          a_in_B.assign(bb.toModel(a_in_B)); //WCStoB.xform_point(a_in_B);
		          Vector3 abs_a = a_in_B.abs(); //vector3_type abs_a = fabs(a_in_B);
		          if(abs_a.weaklyLessThan(eps_b))//if(abs_a <= eps_b)
		          {
		            ++corners_inside;
		            ++corners_A_in_B;
		            AinB[i] = true;
		          }
		          else
		            AinB[i] = false;
		          Vector3 b_in_A = b[i].copy();
		          b_in_A.assign(ba.toModel(b_in_A));//WCStoA.xform_point(b_in_A);
		          Vector3 abs_b = b_in_A.abs();//vector3_type abs_b = fabs(b_in_A);
		          if(abs_b.weaklyLessThan(eps_a)) // if(abs_b <= eps_a)
		          {
		            ++corners_inside;
		            ++corners_B_in_A;
		            BinA[i] = true;
		          }
		          else
		            BinA[i] = false;
		        }
		        //--- This may indicate an edge-edge case
		        if(minimum_axis >= 6 )
		        {
		          //--- However the edge-edge case may not be the best choice,
		          //--- so if we find a corner point of one box being inside
		          //--- the other, we fall back to use the face case with
		          //--- minimum overlap.
		          if(corners_inside != 0 )//if(corners_inside)//--- Actually we only need to test end-points of edge for inclusion (4 points instead of 16!!!).
		          {
			        	System.out.println("facecase forced ");

		            minimum_overlap = Double.NEGATIVE_INFINITY; //math::detail::lowest<real_type>();
		            minimum_axis = 15;
		            for(int i=0;i<6;++i)
		            {
		              if(overlap[i]>minimum_overlap)
		              {
		                minimum_overlap = overlap[i];
		                minimum_axis = i;
		              }
		            }
		          }
		        }

		        //System.out.println("" + -Double.MAX_VALUE);
		        //--- now we can safely pick the contact normal, since we
		        //--- know wheter we have a face-case or edge-edge case.
		        n.assign(axis[minimum_axis]);

		        //--- This is definitely an edge-edge case
		        if(minimum_axis>=6)
		        {
		        	System.out.println("edge edge case");
		        	//--- Find a point p_a on the edge from box A.
		        	for(int i=0;i<3;++i)
		        		if(n.dot(A[i]) > 0) //if(n*A[i] > 0)
		        			p_a.assign(p_a.add( A[i].multiply(ext_a.get(i)))); //p_a += ext_a(i)*A[i];
		        		else
		        			p_a.assign(p_a.minus( A[i].multiply(ext_a.get(i)))); //p_a -= ext_a(i)*A[i];
		        	//--- Find a point p_b on the edge from box B.
		        	for(int i=0;i<3;++i)
		        		if(n.dot(B[i]) < 0) //if(n*B[i] < 0)
		        			p_b.assign( p_b.add(B[i].multiply(ext_b.get(i)))); //p_b += ext_b(i)*B[i];
		        		else
		        			p_b.assign( p_b.minus(B[i].multiply(ext_b.get(i)))); //p_b -= ext_b(i)*B[i];
		        	//--- Determine the indices of two unit edge direction vectors (columns
		        	//--- of rotation matrices in WCS).
		        	int columnA = ((minimum_axis)-6)/3;
		        	int columnB = ((minimum_axis)-6)%3;
		        	double s,t;
		        	//--- Compute the edge-paramter values s and t corresponding to the closest
		          //--- points between the two infinite lines parallel to the two edges.

		          //TODO method missing
		          //OpenTissue::geometry::compute_closest_points_line_line(p_a, A[columnA], p_b, B[columnB], s, t);
		          Pair<Double> s_t_values = compute_closest_points_line_line(p_a.copy(), A[columnA].copy(), p_b.copy(), B[columnB].copy());
		          s = s_t_values.getFirst();
		          t = s_t_values.getSecond();
		          //System.out.println(s+","+t);
		          //--- Use the edge parameter values to compute the closest
		          //--- points between the two edges.
		          p_a.assign(p_a.add( A[columnA].multiply(s))); //p_a += A[columnA]*s;
		          p_b.assign(p_b.add( B[columnB].multiply(t))); //p_b += B[columnB]*t;
		          //--- Let the contact point be given by the mean of the closest points.
		          p[0] = (p_a.add(p_b).multiply(0.5)); //p[0] = (p_a + p_b)*.5;
		          distances[0] = overlap[minimum_axis];
		          return 1;
		        }
		        //--- This is a face-``something else'' case, we actually already have taken
		        //--- care of all corner points, but there might be some edge-edge crossings
		        //--- generating contact points



		        //--- Make sure that we work in the frame of the box that defines the contact
		        //--- normal. This coordinate frame is nice, because the contact-face is a axis
		        //--- aligned rectangle. We will refer to this frame as the reference frame, and
		        //--- use the letter 'r' or 'R' for it. The other box is named the incident box,
		        //--- its closest face towards the reference face is called the incidient face, and
		        //--- is denoted by the letter 'i' or 'I'.
		        Vector3[] R_r,R_i;  //--- Box direction vectors in WCS
		        Vector3 ext_r = new Vector3() ,ext_i =new Vector3();          //--- Box extents
		        Vector3 p_r = new Vector3(), p_i = new Vector3();              //--- Box centers in WCS
		        boolean[] incident_inside;    //--- corner inside state of incident box.
		        if (minimum_axis  < 3)
		        {
		          //--- This means that box A is defining the reference frame
		          R_r = A;
		          R_i = B;
		          p_r = p_a;
		          p_i = p_b;
		          ext_r = ext_a;
		          ext_i = ext_b;
		          incident_inside = BinA;
		        }
		        else
		        {
		          //--- This means that box B is defining the reference frame
		          R_r = B;
		          R_i = A;
		          p_r = p_b;
		          p_i = p_a;
		          ext_r = ext_b;
		          ext_i = ext_a;
		          incident_inside = AinB;
		        }

		        //--- 2007-01-08: Stefan Glimberg:
		        //---
		        //--- We've encountered a bug while using box_box_improved
		        //---
		        //--- The bug occurs if a small box, A, collides with a larger box B
		        //--- in a face-face collision, such that box A has 4 corners in box B,
		        //--- and box B has no corners in box A.
		        //---
		        //--- The "seperation plane" for the minimum overlap is chosen for box A,
		        //--- and A is also the reference box. Thus the penetration depth will be
		        //--- calculated as 0.
		        //---
		        //--- A quick fix for this, is to make a check whether A has 4 points in B
		        //--- and B has 0 points in A, and then force the "seperation plane" to be
		        //--- defined in accordance to B.
		        //bool A_preferred = corners_A_in_B==0 && corners_B_in_A>0;
		        //bool B_preferred = corners_B_in_A==0 && corners_A_in_B>0;
		        //if(A_preferred)
		        //{
		        //  minimum_overlap = overlap[0];
		        //  minimum_axis = 0;
		        //  for(unsigned int i=1;i<3;++i)
		        //  {
		        //    if(overlap[i]>minimum_overlap)
		        //    {
		        //      minimum_overlap = overlap[i];
		        //      minimum_axis = i;
		        //    }
		        //  }
		        //}
		        //if(B_preferred)
		        //{
		        //  minimum_overlap = overlap[3];
		        //  minimum_axis = 3;
		        //  for(unsigned int i=4;i<6;++i)
		        //  {
		        //    if(overlap[i]>minimum_overlap)
		        //    {
		        //      minimum_overlap = overlap[i];
		        //      minimum_axis = i;
		        //    }
		        //  }
		        //}
		        //n = axis[minimum_axis];


		        //--- Following vectors are used for computing the corner points of the incident
		        //--- face. At first they are used to determine the axis of the incidient box
		        //--- pointing towards the reference box.
		        //---
		        //--- n_r_wcs = normal pointing away from reference frame in WCS coordinates.
		        //--- n_r = normal vector of reference face dotted with axes of incident box.
		        //--- abs_n_r = absolute values of n_r.
		        Vector3 n_r_wcs = new Vector3() ,n_r = new Vector3() ,abs_n_r = new Vector3(); //vector3_type n_r_wcs,n_r,abs_n_r;
		        if (minimum_axis < 3)
		        {
		          n_r_wcs.assign(n);
		        }
		        else
		        {
		          n_r_wcs.assign(n.multiply(-1));
		        }

		        //--- Each of these is a measure for how much the axis' of the incident box
		        //--- points in the direction of n_r_wcs. The largest absolute value give
		        //--- us the axis along which we will find the closest face towards the reference
		        //--- box. The sign will tell us if we should take the positive or negative
		        //--- face to get the closest incident face.
		        n_r.a1 = R_i[0].dot(n_r_wcs);
		        n_r.a2 = R_i[1].dot(n_r_wcs);
		        n_r.a3 = R_i[2].dot(n_r_wcs);
		        abs_n_r.a1 = Math.abs(n_r.a1);
		        abs_n_r.a2 = Math.abs(n_r.a2);
		        abs_n_r.a3 = Math.abs(n_r.a2);
		        //--- Find the largest compontent of abs_n_r: This corresponds to the normal
		        //--- for the indident face. The axis number is stored in a3. the other
		        //--- axis numbers of the indicent face are stored in a1,a2.
		        int a1,a2,a3;
		        if (abs_n_r.get(1) > abs_n_r.get(0))
		        {
		          if (abs_n_r.get(1) > abs_n_r.get(2))
		          {
		            a1 = 2; a2 = 0; a3 = 1;
		          }
		          else
		          {
		            a1 = 0; a2 = 1; a3 = 2;
		          }
		        }
		        else
		        {
		          if (abs_n_r.get(0) > abs_n_r.get(2))
		          {
		            a1 = 1; a2 = 2; a3 = 0;
		          }
		          else
		          {
		            a1 = 0; a2 = 1; a3 = 2;
		          }
		        }
		        //--- Now we have information enough to determine the incidient face, that means we can
		        //--- compute the center point of incident face in WCS coordinates.

		        int plus_sign[] = new int[3];
		        Vector3 center_i_wcs = new Vector3();
		        if (n_r.get(a3) < 0)
		        {
		        	center_i_wcs.assign(p_i.add(R_i[a3].multiply(ext_i.get(a3))) );//center_i_wcs = p_i + ext_i(a3) * R_i[a3];
		        	plus_sign[a3] = 1;
		        }
		        else
		        {
		          center_i_wcs.assign(  p_i.minus(R_i[a3].multiply(ext_i.get(a3))) ); //center_i_wcs = p_i - ext_i(a3) * R_i[a3];
		          plus_sign[a3] = 0;
		        }
		        //--- Compute difference of center point of incident face with center of reference coordinates.
		        Vector3 center_ir = center_i_wcs.minus(p_r);
		        //--- Find the normal and non-normal axis numbers of the reference box
		        int code1,code2,code3;
		        if (minimum_axis < 3)
		          code3 = minimum_axis;  //012
		        else
		          code3 = minimum_axis-3;  //345
		        if (code3==0)
		        {
		          code1 = 1;
		          code2 = 2;
		        }
		        else if (code3==1)
		        {
		          code1 = 2;
		          code2 = 0;
		        }
		        else
		        {
		          code1 = 0;
		          code2 = 1;
		        }
		        //--- Find the four corners of the incident face, in reference-face coordinates
		        double quad[] = new double[8]; //--- 2D coordinate of incident face (stored as x,y pairs).
		        boolean inside[] = new boolean[4];     //--- inside state of the four coners of the quad
		        //--- Project center_ri onto reference-face coordinate system (has origo
		        //--- at the center of the reference face, and the two orthogonal unit vectors
		        //--- denoted by R_r[code1] and R_r[code2] spaning the face-plane).
		        double c1 = R_r[code1].dot(center_ir);
		        double c2 = R_r[code2].dot(center_ir);
		        //--- Compute the projections of the axis spanning the incidient
		        //--- face, onto the axis spanning the reference face.
		        //---
		        //--- This will allow us to determine the coordinates in the reference-face
		        //--- when we step along a direction of the incident face given by either
		        //--- a1 or a2.
		        double m11 = R_r[code1].dot(R_i[a1]);
		        double m12 = R_r[code1].dot(R_i[a2]);
		        double m21 = R_r[code2].dot(R_i[a1]);
		        double m22 = R_r[code2].dot(R_i[a2]);
		        {
		          double k1 = m11 * ext_i.get(a1);
		          double k2 = m21 * ext_i.get(a1);
		          double k3 = m12 * ext_i.get(a2);
		          double k4 = m22 * ext_i.get(a2);

		          plus_sign[a1] = 0;
		          plus_sign[a2] = 0;
		          int mask = ( (plus_sign[a1]<<a1) |  (plus_sign[a2]<<a2) |  (plus_sign[a3]<<a3));
		          inside[0] = incident_inside[ mask ];

		          quad[0] = c1 - k1 - k3;
		          quad[1] = c2 - k2 - k4;

		          plus_sign[a1] = 0;
		          plus_sign[a2] = 1;
		          mask = (plus_sign[a1]<<a1 |  plus_sign[a2]<<a2 |  plus_sign[a3]<<a3);
		          inside[1] = incident_inside[ mask ];

		          quad[2] = c1 - k1 + k3;
		          quad[3] = c2 - k2 + k4;

		          plus_sign[a1] = 1;
		          plus_sign[a2] = 1;
		          mask = (plus_sign[a1]<<a1 |  plus_sign[a2]<<a2 |  plus_sign[a3]<<a3);
		          inside[2] = incident_inside[ mask ];

		          quad[4] = c1 + k1 + k3;
		          quad[5] = c2 + k2 + k4;

		          plus_sign[a1] = 1;
		          plus_sign[a2] = 0;
		          mask = (plus_sign[a1]<<a1 |  plus_sign[a2]<<a2 |  plus_sign[a3]<<a3);
		          inside[3] = incident_inside[ mask ];

		          quad[6] = c1 + k1 - k3;
		          quad[7] = c2 + k2 - k4;
		        }
		        //--- find the size of the reference face
		        double rect[] = new double[2];
		        rect[0] = ext_r.get(code1);
		        rect[1] = ext_r.get(code2);

		        //--- Intersect the edges of the incident and the reference face
		        double crossings[] = new double[16];
		        //int edge_crossings = OpenTissue::intersect::rect_quad_edges(rect,quad,inside,crossings);
		        int edge_crossings = rect_quad_edges(rect, quad, inside, crossings);
		        //System.out.println("edgecrossings = " + edge_crossings );
		        assert(edge_crossings<=8);

		        if(!(corners_inside!=0) && !(edge_crossings!=0)) // if(!corners_inside && !edge_crossings)
		          return 0;

		        //--- Convert the intersection points into reference-face coordinates,
		        //--- and compute the contact position and depth for each point.
		        double det1 = 1./(m11*m22 - m12*m21);
		        m11 *= det1;
		        m12 *= det1;
		        m21 *= det1;
		        m22 *= det1;
		        int cnt = 0;
		        for (int j=0; j < edge_crossings; ++j)
		        {
		          //--- Get coordinates of edge-edge crossing point in reference face coordinate system.
		          double p0 = crossings[j*2] - c1;
		          double p1 = crossings[j*2+1] - c2;
		          //--- Compute intersection point in (almost) WCS. Actually we have
		          //--- displaced origin to center of reference frame box
		          double k1 =  m22*p0 - m12*p1;
		          double k2 = -m21*p0 + m11*p1;
		          Vector3 point =  center_ir.add( R_i[a1].multiply(k1) ).add( R_i[a2].multiply(k2) );//center_ir + k1*R_i[a1] + k2*R_i[a2];
		          //--- Depth of intersection point
		          double depth = n_r_wcs.dot(point) - ext_r.get(code3);
		          if(depth<envelope)
		          {
		            p[cnt].assign( point.add(p_r));//point + p_r;//--- Move origin from center of reference frame box to WCS
		            distances[cnt] = depth;
		            ++cnt;
		          }
		        }
		        //      assert((corners_inside + cnt)<=8);//--- If not we are in serious trouble!!!
		        //--- I think there is a special case, if corners_inside = 8 and
		        //--- corners_in_A = 4 and corners_in_B = 4, then there really
		        //--- can only be 4 contacts???

		        if(corners_inside!=0)
		        {
		          int start_corner_A = cnt;
		          int end_corner_A = cnt;

		          //--- Compute Displacement of contact plane from origin of WCS, the
		          //--- contact plane is equal to the face plane of the reference box
		          double w =  ext_r.get(code3) + (n_r_wcs.dot(p_r)); //ext_r(code3) +  n_r_wcs*p_r;

		          if(corners_A_in_B!=0)
		          {
		            for (int i=0; i < 8; ++i)
		            {
		              if(AinB[i])
		              {
		                Vector3 point = a[i].copy();
		                double depth = n_r_wcs.dot(point) - w;//n_r_wcs*point - w;
		                if(depth<envelope)
		                {
		                  p[cnt] = point;
		                  distances[cnt] = depth;
		                  ++cnt;
		                }
		              }
		            }
		            end_corner_A = cnt;
		          }
		          if(corners_B_in_A!=0)
		          {
		            for (int i=0; i < 8; ++i)
		            {
		              if(BinA[i])
		              {
		                Vector3 point = b[i].copy();
		                boolean redundant = false;
		                for(int j=start_corner_A;j<end_corner_A;++j)
		                {
		                  if( p[j].minus(point).abs().lessThan(new Vector3(envelope,envelope,envelope)) )//if( p[j].is_equal(point,envelope) )
		                  {
		                    redundant = true;
		                    break;
		                  }
		                }
		                if(redundant)
		                  continue;
		                double depth = n_r_wcs.dot(point) - w; //n_r_wcs*point - w;
		                if(depth<envelope)
		                {
		                  p[cnt] = point;
		                  distances[cnt] = depth;
		                  ++cnt;
		                }
		              }
		            }
		          }
		        }
		        //      assert(cnt<=8);//--- If not we are in serious trouble!!!
		        return cnt;
		    }

	
	   /**
	    * Compute Closest Points between Lines
	    *
	    *
	    * @param pA   Point on first line.
	    * @param uA   Direction of first line (unit vector).
	    * @param pB   Point on second line.
	    * @param uB   Direction of second line (unit vector).
	    * @param s    The line parameter of the closest point on the first line pA + uA*s
	    * @param t    The line parameter of the closest point on the second line pB + uB*t
	    *
	    */
	    private Pair<Double> compute_closest_points_line_line(
	      Vector3 pA , Vector3 uA,
	      Vector3 pB, Vector3 uB )
	    {

	      Vector3 r = pB.minus(pA);
	      double k = uA.dot(uB);
	      double q1 = uA.dot(r);
	      double q2 = uB.multiply(-1).dot(r);
	      double w = 1 - k*k;
	      double s = 0;  double t = 0;
	      double epsilon = 1e-13;//math::working_precision<real_type>();
	      if(Math.abs(w) > epsilon)
	      {
	        s = (q1 + k*q2)/w;
	        t = (q2 + k*q1)/w;
	      }
	    	//System.out.println("line-line " + s +" , "+ t);

	      
	      return new Pair<Double>(s,t);
	    }


	    /**
	     * Rectangle Quadrilateral Edges Intersection Testing.
	     *
	     * Intersection test between a rectanlge, given by the two half-edge extents
	     * rect[0] and rect[1], i.e. the rectangle is given by all points, p, lying in side
	     *
	     *     -rect[0] <=  p(0)  <= rect[0] and   -rect[1] <=  p(1)  <= rect[1]
	     *
	     * The quadrangle (orientated rectangle) are given by the 4 corner points:
	     *
	     *    q1(0) = quad[0] and q1(1) = quad[1]
	     *    q2(0) = quad[2] and q2(1) = quad[3]
	     *    q3(0) = quad[4] and q3(1) = quad[5]
	     *    q4(0) = quad[6] and q4(1) = quad[7]
	     *
	     * This method is only concerned about edge-edge crossings, interior points are completely ignored.
	     *
	     *
	     * @param rect       A pointer to an array (2 reals) that defines the rectangle.
	     * @param quad       A pointer to an array (8 reals) that defines the quadrilateral.
	     * @param inside     A pointer to an array (4 booleans) holding the inside status of  quadrilateral corners.
	     * @param ret        Upon return holds the coordinates of the intersection points.
	     *
	     * @return           The number of intersection points.
	     */
	    int rect_quad_edges(double[] rect, double[] quad, boolean[] inside, double[]  ret)
	    {
	    	int cnt = 0;
	    	double[] r = ret; int returnindex = 0; int quadindex = 0; //real_type * r = ret;
	    	{
	    		//--- Test the four edges of the quad for crossing the edges of the rect.
	    		double qx0,qy0,qx1,qy1,tst;
	    		/*double[] q = quad;*/ int qindex = 0;
	    		for(int i=0;i<4;++i)
	    		{
	    			int index = i*2;
	    			qx0 = quad[index]; //qx0 = *q;     ++q;
	    			qy0 = quad[index+1];  //qy0 = *q;     ++q;
	    			/*double[] nextq = (i==3)?quad:q;*/ //int nextqindex = (i==3)?0:qindex;//real_type * nextq = (i==3)?quad:q;

	    			if ( i==3) {
	    				qx1 = quad[0];                 //qx1 = *nextq; ++nextq;
	    				qy1 = quad[1];                 //qy1 = *nextq;

	    			} else {
	    				qx1 = quad[index+2];            //qx1 = *nextq; ++nextq;
	    				qy1 = quad[index+3];            //qy1 = *nextq;
	    			}

	    			boolean inside0 = inside[i];
	    			boolean inside1 = inside[(i+1)%4];
	    			if(inside0 && inside1)
	    				continue;
	    			double dx = (qx1-qx0);
	    			double dy = (qy1-qy0);
	    			if(dx!=0)
	    			{
	    				double alpha = dy/dx;
	    				tst = - rect[0];     //--- left side
	    				if( ((qx0 < tst) && (qx1 > tst)) || ((qx0 > tst) && (qx1 < tst)) )
	    				{
	    					double qxt = -rect[0];
	    					double qyt = qy0 + (qxt-qx0)*alpha;
	    					if( (-rect[1] < qyt) &&   (qyt < rect[1]))
	    					{
	    						r[returnindex] = qxt; returnindex++;//*r = qxt; ++r;
	    						r[returnindex] = qyt; returnindex++;//*r = qyt; ++r;
	    						++cnt;
	    					}
	    				}
	    				tst = rect[0];
	    				if( ((qx0 < tst) && (qx1 > tst)) || ((qx0 > tst) && (qx1 < tst)) )
	    				{
	    					double qxt = rect[0];
	    					double qyt = qy0 + (qxt-qx0)*alpha;
	    					if( (-rect[1] < qyt) &&   (qyt < rect[1]))
	    					{
	    						r[returnindex] = qxt; returnindex++;//*r = qxt; ++r;
	    						r[returnindex] = qyt; returnindex++;//*r = qyt; ++r;
	    						++cnt;
	    					}
	    				}
	    			}
	    			if(dy!=0)
	    			{
	    				double inv_alpha = dx/dy;
	    				tst = - rect[1];     //--- bottom side
	    				if( ((qy0 < tst) && (qy1 > tst)) || ((qy0 > tst) && (qy1 < tst)) )
	    				{
	    					double qyt = -rect[1];
	    					double qxt = qx0 + (qyt-qy0)*inv_alpha;
	    					if( (-rect[0] < qxt)&&(qxt < rect[0]))
	    					{
	    						//*r = qxt;  ++r;
	    						//*r = qyt;  ++r;
	    						r[returnindex] = qxt; returnindex++;//*r = qxt; ++r;
	    						r[returnindex] = qyt; returnindex++;//*r = qyt; ++r;

	    						++cnt;
	    					}
	    				}
	    				tst =  rect[1];     //--- top side
	    				if( ((qy0 < tst) && (qy1 > tst)) || ((qy0 > tst) && (qy1 < tst)) )
	    				{
	    					double qyt = rect[1];
	    					double qxt = qx0 + (qyt-qy0)*inv_alpha;
	    					if( (-rect[0] < qxt)&&(qxt < rect[0]))
	    					{
	    						//	                 *r = qxt;  ++r;
	    						//	                 *r = qyt;  ++r;
	    						r[returnindex] = qxt; returnindex++;//*r = qxt; ++r;
	    						r[returnindex] = qyt; returnindex++;//*r = qyt; ++r;

	    						++cnt;
	    					}
	    				}
	    			}
	    		}
	    	}
	    	System.out.println("quad:" + cnt );
	    	return cnt;
	    }




}
