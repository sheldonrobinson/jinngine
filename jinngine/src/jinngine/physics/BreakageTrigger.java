
package jinngine.physics;

import jinngine.physics.constraint.Constraint;
import jinngine.physics.solver.Solver.NCPConstraint;

public class BreakageTrigger implements Trigger {

    private final Constraint constraint;
    private final double limit;

    public BreakageTrigger(final Constraint constraint, final double limit) {
        this.constraint = constraint;
        this.limit = limit;
    }

    @Override
    public void setup(final Scene s) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean update(final Scene s, final double dt) {
        double totalForce = 0;
        for (final NCPConstraint ci : constraint) {
            totalForce += Math.abs(ci.lambda) / dt;
        }

        // if total force exceeds limit, remove target constraint
        if (totalForce > limit) {
            s.removeConstraint(constraint);
            return false;
        }

        return true;
    }

    @Override
    public void cleanup(final Scene s) {
        // TODO Auto-generated method stub

    }

}
