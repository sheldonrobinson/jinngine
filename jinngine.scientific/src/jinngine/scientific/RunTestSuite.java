package jinngine.scientific;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import jinngine.geometry.Geometry;
import jinngine.geometry.SupportMap3;
import jinngine.geometry.contact.ContactGenerator;
import jinngine.geometry.contact.ContactGeneratorClassifier;
import jinngine.geometry.contact.FeatureSupportMapContactGenerator;
import jinngine.geometry.contact.SamplingSupportMapContacts;

public class RunTestSuite {

	public RunTestSuite() throws FileNotFoundException {
		PrintStream latex = new PrintStream("latex.tex");
		PrintStream matlab = new PrintStream("matlab.m");
		
		String[] suite = new String[] {"configurations/inclined.xml", 
				"configurations/structuredfrictiondependent.xml",
				"configurations/largerstack.xml",
				"configurations/smallgearstack.xml",
				"configurations/mediumgearstack.xml",
				"configurations/smallboxstack.xml"
		};
		
		Test test1 = new CollisionDetectionTimeUsageComparison();
		test1.run("collisiontimetest1", suite, latex, matlab);
		
		ContactGeneratorClassifier classifier = new ContactGeneratorClassifier() {
			public ContactGenerator createGeneratorFromGeometries(Geometry a,Geometry b) {
				return new FeatureSupportMapContactGenerator((SupportMap3) a, a,(SupportMap3)b, b );
			}
		};
		
		Test test2 = new EnergyOverFramesComparison(classifier);
		test2.run("energyoverframes1", suite, latex, matlab );

//		classifier = new ContactGeneratorClassifier() {
//			public ContactGenerator createGeneratorFromGeometries(Geometry a,Geometry b) {
//				return new SamplingSupportMapContacts(a.getBody(), b.getBody(), (SupportMap3)a, (SupportMap3)b);
//			}
//		};
//
//		Test test3 = new EnergyOverFramesComparison(classifier);
//		test3.run("energyoverframes2", suite, latex, matlab );

		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		new RunTestSuite();
	}
}
