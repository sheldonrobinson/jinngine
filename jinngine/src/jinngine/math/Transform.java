package jinngine.math;

public final class Transform {
	
	// quaternion data
	private double qx,qy,qz,qw;
	
	// translation data
	private double tx,ty,tz; 
	

	public final void getTranslation(final Vector3 translationOut) {
		translationOut.assign(tx, ty, tz);
	}
	
	public final void assignTranslation( final Vector3 translationIn ) {
		tx = translationIn.x; ty = translationIn.y; tz = translationIn.z;
	}
	
	public final void assignTranslation( final double x, final double y, final double z) {
		tx = x; ty = y; tz = z;
	}
	
	public final void getQuaternion( final Quaternion rotationOut ) {
		rotationOut.x = qx;
		rotationOut.y = qy;
		rotationOut.z = qz;
		rotationOut.w = qw;
	}

	public final void assignQuaternion( final Quaternion rotationIn ) {
		qx = rotationIn.x;
		qy = rotationIn.y;
		qz = rotationIn.z;
		qw = rotationIn.w;
	}
	
	//...
	
	public final void assignProduct( final Transform t1, final Transform t2 ) { 
		// compute R1(R2x+t2)+t1 = R1R2x + (R1t2+t1)
	}
	
	public final void apply( final Vector3 in, final Vector3 out) {
		// return q(in)+t in out ...
	}

}
