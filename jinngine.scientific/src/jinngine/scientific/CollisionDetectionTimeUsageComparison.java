package jinngine.scientific;

import java.io.PrintStream;
import java.util.*;
import jinngine.collision.AllPairsTest;
import jinngine.collision.BroadfaseCollisionDetection;
import jinngine.geometry.Geometry;
import jinngine.geometry.SupportMap3;
import jinngine.geometry.contact.ContactGenerator;
import jinngine.geometry.contact.ContactGeneratorClassifier;
import jinngine.geometry.contact.FeatureSupportMapContactGenerator;
import jinngine.geometry.contact.SamplingSupportMapContacts;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.force.Force;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.physics.solver.experimental.SubspaceMinimization;
import jinngine.util.Pair;

/**
 * Compares two contact generation methods. Produces a histogram where 
 * methods are compared at each test configuration.
 * @author mo
 *
 */
public class CollisionDetectionTimeUsageComparison implements Test {

	private Map<String,String> results = new HashMap<String, String>();
	private String resultnumbers;
	private String resultlabels;
	private String method1abv = "GJK/Feature";  //name of method 1
	private String method2abv = "SAMPLE";       //name of method 2
	
	public void run(String testname, String[] configurations, PrintStream latexOut, PrintStream matlabOut ) {
		resultnumbers = "[ ";
		resultlabels  = "{ ";
		
		//for each configuration
		for (String config: configurations) {
			//run each contact generator
			double result1 = runTest(config, new ContactGeneratorClassifier() {
				@Override
				public ContactGenerator createGeneratorFromGeometries(Geometry a,
						Geometry b) {
					// *** insert method 1 here ***
					return new FeatureSupportMapContactGenerator((SupportMap3) a, a,(SupportMap3)b, b );
				}
			});
			
			
			//run each contact generator
			double result2 = runTest(config, new ContactGeneratorClassifier() {
				@Override
				public ContactGenerator createGeneratorFromGeometries(Geometry a,
						Geometry b) {
					// *** insert method 2 here ***
					return new SamplingSupportMapContacts(a.getBody(), b.getBody(), (SupportMap3) a, (SupportMap3)b );
					
				}
			});

			
			//read meta data from the configuration file
			ConfigurationMetaInfo meta = new ConfigurationMetaInfo(config);
			
			// store the result
			results.put( config, result1 + " " + result2);
			resultnumbers = resultnumbers + result1 + " " + result2 + "; ";
			resultlabels  = resultlabels +"'"+ meta.getAbbreviation() + "', ";

		}
		
		resultnumbers = resultnumbers + "] ";
		resultlabels  = resultlabels + " }";
		
		
		//matlab code
		matlabOut.println("figure;"); 
		matlabOut.println("bar("+resultnumbers+");");
		matlabOut.println("set(gca, 'FontSize', 15);");
		matlabOut.println("grid on;");
		//matlabOut.println("set(gca,'xlim',[1 "+configurations.length+"]);");
		matlabOut.println("title(\'Average Contact Point Generation Time\');");
		//matlabOut.println("set(gca,'XTickLabel',"+resultlabels+");");
		matlabOut.println("xticklabel_rotate([1:"+configurations.length+"],45,"+resultlabels+",'interpreter','none');");
		//matlabOut.println("xlabel('Configurations');");
		matlabOut.println("ylabel('Milli seconds');");
		matlabOut.println("legend('"+method1abv+"', '"+method2abv+"');");
		matlabOut.println("brighten(0.99);");
		//matlabOut.println("colormap cmap;");
		matlabOut.println("print -dps2c -loose " + testname +".ps;");		
		matlabOut.println("print -dpdf " + testname +".pdf;");
		matlabOut.println("");
		
		//do latex include
		latexOut.println("\\begin{figure}");
		latexOut.println("  \\centering");
		latexOut.println("  \\includegraphics[width=0.5\\columnwidth]{"+testname+".pdf}");
		latexOut.println("  \\caption{ insert description here }");
		latexOut.println("  \\label{fig:"+testname+"}");
		latexOut.println("\\end{figure}\n");
		latexOut.println("");
	}

	private double runTest(String configuration, ContactGeneratorClassifier classifier ) { 
		//create a broadphase handler
		final BroadfaseCollisionDetection bf = new AllPairsTest();

		//create a handler for the scene
		PhysicsScene scene = new PhysicsScene() {
			@Override
			public void addBody(Body b) {
				b.updateTransformations();
				Iterator<Geometry> gi = b.getGeometries();
				while(gi.hasNext()) {
					bf.add(gi.next());
				}
			}

			public void addConstraint(Constraint c) {}
			public void addForce(Force f) {}
			public Iterator<Body> getBodies() {return null;}
			public Iterator<Constraint> getConstraints() {return null;}
			public void removeBody(Body b) {}
			public void removeConstraint(Constraint c) {}
			public void removeForce(Force f) {}
			
		};

		//load a scene 
		Scene.loadScene(configuration, scene);
		
		//run broad phase to find pairs
		bf.run();
		
		//get the pairs
		Iterator<Pair<Geometry>> pairs = bf.getOverlappingPairs().iterator();
		
		//make a list of generators
		List<ContactGenerator> contacts = new ArrayList<ContactGenerator>();
		
		//generate constact constraints for all pairs
		while(pairs.hasNext()) {
			Pair<Geometry> gp = pairs.next();
			
			//all pairs that we are testing
			if (gp.getFirst() instanceof SupportMap3 && gp.getSecond() instanceof SupportMap3) {
				contacts.add( classifier.createGeneratorFromGeometries(gp.getFirst(), gp.getSecond()));
			}
		}

		// get the time
		long time = System.currentTimeMillis();
		
		double trials =1;
		
		//Run all the contact generators
		for (int i=0;i<trials;i++)
			for (ContactGenerator g: contacts) {
				g.run(1);

				//write out contacts
				//			Iterator<ContactPoint> cps = g.getContacts();
				//			while(cps.hasNext())
				//				cps.next().midpoint.print();
			}
		
		// see how long time was used
		long elapsed = System.currentTimeMillis() - time;
		
		System.out.println("Method: elapsed millis "+ elapsed/trials);

		// return average milliseconds to generate contact points
		return elapsed/trials;
	}
	

	
//	public static void main(String[] args) {
//		List<String> l = new ArrayList<String>();
//		l.add("structuredfrictiondependent.xml");
//		l.add("largerstack.xml");
//		l.add("smallgearstack.xml");
//		l.add("inclined.xml");
//		
//		CollisionDetectionTimeUsageComparison x = new CollisionDetectionTimeUsageComparison();
//		x.run(l);
//		System.out.println(x.getMatlabPlotCode(""));		
//		
//	}




}
