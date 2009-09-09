package jinngine.geometry.contact;

import java.util.Iterator;

import jinngine.math.Vector3;

/**
 * A contact generator is used by the {@link ContactConstraint} to generate its contact information. ContactGenerator 
 * encapsulates all geometry and algorithm specific details. Therefore, it is the responsibility 
 * of the simulator (Model) to instantiate correct implementations of ContactGenerators, when various types of geometries 
 * in proximity are encountered during simulation. A ContactConstraint can handle one or more ContactGenerators. This allows for bodies in the simulation, which have more 
 * than one geometry instance attached.
 * 
 * @author silcowitz
 */
public interface ContactGenerator {

	/**
	 * A simple structure class representing a contact point with a normal and a penetration depth
	 * @author Moo
	 */
	public class ContactPoint {
		/**
		 * Point on body A in world space
		 */
		public final Vector3 paw = new Vector3();

		/**
		 * Point on body A in A space
		 */
		public final Vector3 pa = new Vector3();
		
		/**
		 * Point on body B in world space
		 */
		public final Vector3 pbw = new Vector3();
		
		/**
		 * Point on body B in B space
		 */
		public final Vector3 pb = new Vector3();

		/**
		 * Interaction point in world space
		 */
		public final Vector3 midpoint = new Vector3();

		/**
		 * Contact normal in world space
		 */
		public final Vector3 normal = new Vector3();

		/**
		 * Penetration depth
		 */
		public double depth;
		
		
		public double distance;
		public double restitution;
		public double friction;
	}
	
	/**
	 * Run/update contact generation. This method will be invoked by the ContactConstraint,
	 * to update the contact information, to reflect the present configuration. An implementation
	 * should run all necessary computations during this call, and generate all contact points, as
	 * to be returned by getContacts().
	 * @param dt TODO
	 */
	public boolean run(double dt);
	
	/**
	 * Get all contact points generated by this contact generator
	 * @return An iterator of ContactPoints
	 */
	public Iterator<ContactPoint> getContacts();		
}
