package jinngine.physics;
import java.util.Iterator;


public interface Constraint {
        /**
         * Insert the ConstraintEntries of this Constraint into the list modeled by iterator
         * @param iterator
         * @param dt
         */
        public void applyConstraints( Iterator<ConstraintEntry> iterator, double dt );

}
