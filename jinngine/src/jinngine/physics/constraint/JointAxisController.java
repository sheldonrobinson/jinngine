package jinngine.physics.constraint;

public interface JointAxisController {
	//	public void setLinearLimits( double dMin, double dMax);
	//	public double getLinearPosition();
	//	public void setLinearFrictionForce( double magnitude );
	//	public void setLinearMotorForce( double magnitude );

	public void setAngularLimits( double thetaMin, double thetaMax );
	public double getAngularPosition();
	public double getAngularVelocity();
	//	public void setAngularFrictionForce( double magnitude );
	public void setAngularMotorForce( double magnitude );
	public void setDesiredAngularVelocity(double velocity, double forceMagnitude );
}
