package jinngine.physics.constraint;
import java.util.Iterator;
import jinngine.physics.solver.*;


public interface Constraint {
        /**
         * Insert the ConstraintEntries of this Constraint into the list modeled by iterator
         * @param iterator
         * @param dt
         */
        public void applyConstraints( Iterator<ConstraintEntry> iterator, double dt );

}
