package jinngine.demo;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jinngine.demo.graphics.Entity;
import jinngine.demo.graphics.FlatShade;
import jinngine.demo.graphics.Hull;
import jinngine.geometry.Box;
import jinngine.math.Matrix4;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Engine;
import jinngine.physics.Model;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.physics.solver.SubspaceMinimization;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RunTest {
	private Model model = new Engine();
		
	public RunTest() {
		//load configuration
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new File("configuration.xml"), new DefaultHandler() {
				Body currentBody = null;
				@Override
				public void startElement(String uri, String localName,
						String name, Attributes attributes) throws SAXException {
					
					System.out.println("startElement event");
					
					if (name.equals("box") ) {
						double h = Double.parseDouble(attributes.getValue("height"));
						double w = Double.parseDouble(attributes.getValue("width"));
						double d = Double.parseDouble(attributes.getValue("depth"));						
						//double mass = Double.parseDouble(attributes.getValue("mass"));
						boolean fixed = Boolean.parseBoolean(attributes.getValue("fixed"));
						
						Box box = new Box(h,w,d);
						//box.setMass(mass);
						currentBody = new Body(box);
						
						if (fixed) {
							currentBody.setFixed(true);
						}
						
						//Tell the model about our new box and attach a gravity force to it
						model.addBody(currentBody);

						if (!fixed) {
							model.addForce(new GravityForce(currentBody));
						}
					}
					
					if (name.equals("position")) {
						double x = Double.parseDouble(attributes.getValue("x"));
						double y = Double.parseDouble(attributes.getValue("y"));
						double z = Double.parseDouble(attributes.getValue("z"));						
						currentBody.setPosition(new Vector3(x,y,z));
						currentBody.updateTransformations();
					}
					
					if (name.equals("orientation")) {
						double s = Double.parseDouble(attributes.getValue("s"));						
						double x = Double.parseDouble(attributes.getValue("x"));
						double y = Double.parseDouble(attributes.getValue("y"));
						double z = Double.parseDouble(attributes.getValue("z"));						
						currentBody.state.q.assign(new Quaternion(s, new Vector3(x,y,z)));						
						currentBody.updateTransformations();
					}					


					if (name.equals("velocity")) {
						double x = Double.parseDouble(attributes.getValue("x"));
						double y = Double.parseDouble(attributes.getValue("y"));
						double z = Double.parseDouble(attributes.getValue("z"));						
						currentBody.setVelocity(new Vector3(x,y,z));
					}

					if (name.equals("angularvelocity")) {
						double x = Double.parseDouble(attributes.getValue("x"));
						double y = Double.parseDouble(attributes.getValue("y"));
						double z = Double.parseDouble(attributes.getValue("z"));						
						currentBody.setAngularVelocity(new Vector3(x,y,z));
					}

				}
			});
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

	
		//run test
		model.setDt(0.08);
		model.setSolver(new SubspaceMinimization(true));
		model.tick();
		model.setSolver(new ProjectedGaussSeidel(300, true));
		model.tick();
		
	}
	
	public static void main(String[] args) {
		new RunTest();
	}
	
}
