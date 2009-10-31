package jinngine.physics.constraint;
import java.util.ListIterator;
import jinngine.physics.solver.*;
import jinngine.physics.solver.Solver.constraint;


public interface Constraint {
        /**
         * Insert the ConstraintEntries of this Constraint into the list modeled by iterator
         * @param iterator
         * @param dt
         */
        public void applyConstraints( ListIterator<constraint> iterator, double dt );

}
