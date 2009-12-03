package jinngine.scientific;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.geometry.Sphere;
import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Engine;
import jinngine.physics.Model;

public class Collada {
	
	public static void load(String file, Model model) throws SAXException, IOException {
		DOMParser parser = new DOMParser();
		parser.parse(file);		  
		Document doc = parser.getDocument();

		//read all rigid body declarations 
		Map<String,Element> rigid_bodies = new HashMap<String, Element>();
		NodeList nl = doc.getElementsByTagName("rigid_body");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				//get instantiation element
				Element el = (Element)nl.item(i);				
				String name = el.getAttribute("name");

				//store this element under name
				rigid_bodies.put(name, el);
			}
		}// rigid_body tags

		//read all nodes
		Map<String,Element> nodes = new HashMap<String, Element>();
		nl = doc.getElementsByTagName("node");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				//get instantiation element
				Element el = (Element)nl.item(i);				
				String name = el.getAttribute("name");

				//store this element under name
				nodes.put(name, el);
			}
		} //node tags

		//Get all rigid body instantiations from the COLLADA file
		nl = doc.getElementsByTagName("instance_rigid_body");
		if(nl != null && nl.getLength() > 0) 
			for(int i = 0 ; i < nl.getLength();i++) {				
				// get instantiation element
				Element el = (Element)nl.item(i);				
				NamedNodeMap m = el.getAttributes();
				
				String body = el.getAttribute("body");
				String target = el.getAttribute("target");
								
				// create a new body
				Body jinnginebody = new Body();
				
				// get the rigid body propperties
				Element element = rigid_bodies.get(body);
				
				// get the technique_common node (fails if not pressent)
				Element technique = (Element)element.getElementsByTagName("technique_common").item(0);
				
				// go through nodes of technique_common
				// determine fixed or not (if something is dynamic, it is not fixed)
				Element dynamic = (Element) technique.getElementsByTagName("dynamic").item(0);
				boolean fixed = !Boolean.parseBoolean(dynamic.getFirstChild().getNodeValue());
				jinnginebody.setFixed(fixed);

				// set mass (sometimes set to zero, not good, could cause trouble)
				Element mass = (Element) technique.getElementsByTagName("mass").item(0);
				double massvalue = Double.parseDouble(mass.getFirstChild().getNodeValue());
				jinnginebody.setMass(massvalue);

				// get physics shape (one)
				Element shape = (Element)technique.getElementsByTagName("shape").item(0);
				
				// "parse" the shapes into geometries
				jinnginebody.addGeometry(createGeometryFromShape(shape));
								
				// compute inertia etc.
				jinnginebody.finalize();
					
				// look at node to find global placement of body.
				
				
				//add to model
				model.addBody(jinnginebody);
				
			} // for "instance_rigid_body"
		
	}
	
	/**
	 * Parse numbers from an element
	 */
	private static double[] parseNumbersFromElement( Element element) {
		String[] values = element.getFirstChild().getNodeValue().split(" ");		
		double[] v = new double[values.length];
		
		int k=0;
		for (String d: values)
			v[k++] = Double.parseDouble(d);
		
		return v;
	}

	/**
	 * Parse a translate field and return the translation vector
	 * @param translate
	 * @return
	 */
	private static Vector3 parseTranslate( Element translate ) {
		double[] values = parseNumbersFromElement(translate);		
		return new Vector3(values[0],values[1],values[2]);		
	}
	
	/**
	 * Parse a rotation tag and return a rotation matrix
	 */
	private static Matrix3 parseRotate( Element rotate ) {
		double[] values = parseNumbersFromElement(rotate);
		return Quaternion.toRotationMatrix3(Quaternion.rotation(values[3], new Vector3(values[0],values[1],values[2])), new Matrix3());
	}
	/**
	 * Create geometry instance from a shape tag element
	 */
	private static Geometry createGeometryFromShape( Element shape ) {
		Geometry g = null;
		
		// go thru all nodes to get the geometry specification
		NodeList shapes = shape.getChildNodes();
		for (int node = 0; node<shapes.getLength(); node++ ) {
			Node geometrynode = shapes.item(node);
			
			//get the name of the tag
			String type = geometrynode.getNodeName();
			
			// box
			if (type.equals("box")) {
				// get the box dimensions from the half_extents tag
				Element half_extents = (Element)((Element)geometrynode).getElementsByTagName("half_extents").item(0);
				double[] values = parseNumbersFromElement(half_extents);

				// create the box geometry from the parsed dimentions
				Box box = new Box(values[0], values[1], values[2]);
				
				g = box;
				break;
			}

			// sphere
			if (type.equals("sphere")) {
				// get the radius tag
				Element radius = (Element)((Element)geometrynode).getElementsByTagName("radius").item(0);
				double[] values = parseNumbersFromElement(radius);

				// create the box geometry from the parsed dimentions
				Sphere sphere = new Sphere(values[0]);
				
				g = sphere;
				break;
			}
		}
		
		// if geometry is still null, then return null
		if (g==null)
			return null;

		
		Vector3 translation = new Vector3();
		Matrix3 rotation = Matrix3.identity();
		
		// go thru all nodes again to get the local transformations
		for (int node = 0; node<shapes.getLength(); node++ ) {
			Node geometrynode = shapes.item(node);
			
			//get the name of the tag
			String type = geometrynode.getNodeName();
			
			// translation
			if (type.equals("translate")) {
				translation.assign( parseTranslate((Element)geometrynode));
			}
			
			// rotation
			if (type.equals("rotate")) {
				rotation.assign( rotation.multiply( parseRotate((Element)geometrynode)));
			}
		}
		
		//apply local transform to g
		g.setLocalTransform(rotation, translation);
		
		return g;
	}
	

	/**
	 * Write a physics model configuration into the COLLADA 1.4 format
	 * @param file
	 * @param model
	 * @throws IOException 
	 */
	public static void write(String file, Model model) throws IOException {
		PrintStream ps = new PrintStream(new File(file));
		Date date = new Date();
		
		//write the header
		ps.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" 
				+"<COLLADA version=\"1.4.0\" xmlns=\"http://www.collada.org/2005/11/COLLADASchema\">\n"
			    +"  <asset>\n"
				+"    <contributor>\n"
				+"      <author>Jinngine physics engine, http://jinngine.sourceforge.net/</author>\n"
				+"	    <authoring_tool>Jinngine physics engine, http://jinngine.sourceforge.net/</authoring_tool>\n"
				+"	    <comments></comments>\n"
				+"	    <copyright></copyright>\n"
				+"	    <source_data>file://</source_data>\n"
				+"    </contributor>\n"
				+"    <created>"+date+"</created>\n"
				+"    <modified>"+date+"</modified>\n"
				+"    <unit meter=\"0.01\" name=\"centimeter\"/>\n"
				+"    <up_axis>Y_UP</up_axis>\n"
			    +"  </asset>\n");
		
	}
	
	
	
	
	
	
	
	
	public static void main(String[] args) throws IOException, SAXException {
		Model model = new Engine();
		
		Collada.load("untitled.dae", model);
		
		Collada.write("new.dae.xml", null);
	}

	
}
