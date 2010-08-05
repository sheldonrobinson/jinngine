package jinngine.physics;

public class DisabledDeactivationPolicy implements DeactivationPolicy {

	@Override
	public void activate(Body b) {
		b.deactivated = false;
	}

	@Override
	public void deactivate(Body b) {
		b.deactivated = true;
	}

	@Override
	public void forceActivate(Body b) {
		// ignore
	}

	@Override
	public boolean shouldBeActivated(Body b) {
		// if b is not active, activate it
		if ( b.deactivated ) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean shouldBeDeactivated(Body b) {
		// never deactivate
		return false;
	}

}
