/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */
package jinngine.test.unit;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;

public class QuaterntionTest {
	@Test
	public void testEuler01() {
		// create a new quaternion, rotating PI/2 about the Y axis
		Quaternion q1 = Quaternion.rotation(Math.PI/2, new Vector3(0,1,0));
		Vector3 euler = new Vector3();
		
		// convert to the euler angles
		Quaternion.quaternionToEuler(q1, euler);
				
		// construct rotation matrix from the XYZ euler angles
		Matrix3 Rx = Quaternion.rotation(euler.x, new Vector3(1,0,0)).toRotationMatrix3();
		Matrix3 Ry = Quaternion.rotation(euler.y, new Vector3(0,1,0)).toRotationMatrix3();
		Matrix3 Rz = Quaternion.rotation(euler.z, new Vector3(0,0,1)).toRotationMatrix3();
						
		// final rotation matrix is
		Matrix3 Reuler = Rz.multiply(Ry).multiply(Rx);
		
		// rotation matrix from original quaternion
		Matrix3 Rquat = q1.toRotationMatrix3();

		// assert matrix equivalence 
		assertTrue( Reuler.subtract(Rquat).fnorm() < 1e-13 );		
	}
	
	@Test
	public void testEuler02() {
		// create a new quaternion, rotating PI/2 about the Y axis
		Quaternion q1 = Quaternion.rotation(Math.PI/2, new Vector3(0,1,0));
		// another quaternion rotating by PI around the Z axis
		Quaternion q2 = Quaternion.rotation(Math.PI/4, new Vector3(1,0,0));
		
		Quaternion q3 = q2.multiply(q1);
		
		Vector3 euler = new Vector3();
		
		// convert to the euler angles
		Quaternion.quaternionToEuler(q3, euler);
				
		// construct rotation matrix from the XYZ euler angles
		Matrix3 Rx = Quaternion.rotation(euler.x, new Vector3(1,0,0)).toRotationMatrix3();
		Matrix3 Ry = Quaternion.rotation(euler.y, new Vector3(0,1,0)).toRotationMatrix3();
		Matrix3 Rz = Quaternion.rotation(euler.z, new Vector3(0,0,1)).toRotationMatrix3();
						
		// final rotation matrix is
		Matrix3 Reuler = Rz.multiply(Ry).multiply(Rx);
		
		// rotation matrix from original quaternion
		Matrix3 Rquat = q3.toRotationMatrix3();

		// assert matrix equivalence 
		assertTrue( Reuler.subtract(Rquat).fnorm() < 1e-13 );		
	}

}
