package jinngine.scientific;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//import jinngine.demo.graphics.Entity;
//import jinngine.demo.graphics.FlatShade;
//import jinngine.demo.graphics.Graphics;
//import jinngine.demo.graphics.Hull;
import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.HingeJoint;
import jinngine.physics.force.Force;
import jinngine.physics.force.GravityForce;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Scene {
	public static void storeScene(String name, final PhysicsScene model) {
		
		System.out.println("writing file");
		try {
			
			File f=new File(name);
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f));
			
			writer.write("<data>\n");
			
			Iterator<Body> i = model.getBodies();
			while(i.hasNext()) {
				Body b = i.next();
				
				if (b.identifier.equals("gear")) {
					Vector3 pos = b.state.rCm.copy();
					Quaternion q = b.state.q.copy();
					
					//set some default identifier
					if (b.identifier.equals("none")) {
						b.identifier = b.toString();
					}

					writer.write(("<gear id=\""+b.identifier+"\">\n"));
					writer.write(("  <position x=\""+pos.x+"\" y=\""+pos.y+"\" z=\""+pos.z+"\"/>\n"));
					writer.write(("  <orientation s=\""+q.s+"\" x=\""+q.v.x+"\" y=\""+q.v.y+"\" z=\""+q.v.z+"\"/>\n"));
					writer.write(("  <velocity x=\""+b.state.vCm.x+"\" y=\""+b.state.vCm.y+"\" z=\""+b.state.vCm.z+"\"/>\n"));
					writer.write(("  <angularvalocity x=\""+b.state.omegaCm.x+"\" y=\""+b.state.omegaCm.y+"\" z=\""+b.state.omegaCm.z+"\"/>\n"));
					writer.write("</gear>\n");
					
				} else { //hack
					Geometry geo = b.getGeometries().next();
					if (geo instanceof Box) {
						Box box = (Box)geo;
						Vector3 size = box.getDimentions();
						Vector3 pos = b.state.rCm.copy();
						Quaternion q = b.state.q.copy();
						
						//set some default identifier
						if (b.identifier.equals("none")) {
							b.identifier = b.toString();
						}
						
						writer.write(("<box id=\""+b.identifier+"\""+" height=\""+size.x+"\" width=\""+size.y+"\" depth=\""+size.z+"\" mass=\""+b.state.M+"\" fixed=\""+b.isFixed()+"\" hidden=\""+b.hidden+"\">\n"));
						writer.write(("  <position x=\""+pos.x+"\" y=\""+pos.y+"\" z=\""+pos.z+"\"/>\n"));
						writer.write(("  <orientation s=\""+q.s+"\" x=\""+q.v.x+"\" y=\""+q.v.y+"\" z=\""+q.v.z+"\"/>\n"));
						writer.write(("  <velocity x=\""+b.state.vCm.x+"\" y=\""+b.state.vCm.y+"\" z=\""+b.state.vCm.z+"\"/>\n"));
						writer.write(("  <angularvalocity x=\""+b.state.omegaCm.x+"\" y=\""+b.state.omegaCm.y+"\" z=\""+b.state.omegaCm.z+"\"/>\n"));
						writer.write("</box>\n");
					}
				}
			}
			
			//store permanent constraints
			Iterator<Constraint> ci = model.getConstraints();
			while(ci.hasNext()) {
				Constraint c = ci.next();
//			for ( Constraint c: ((Engine)model).getConstraints()) {
				if ( c instanceof HingeJoint) {
					HingeJoint hj = (HingeJoint)c;
					hj.b1.updateTransformations();

					writer.write("<hinge id1=\""+hj.b1.identifier+"\" id2=\""+hj.b2.identifier+"\""
							+" pix=\""+hj.pi.x+"\""
							+" piy=\""+hj.pi.y+"\""
							+" piz=\""+hj.pi.z+"\""
							
							+" nix=\""+hj.ni.x+"\""
							+" niy=\""+hj.ni.y+"\""
							+" niz=\""+hj.ni.z+"\""							

							+" pjx=\""+hj.pj.x+"\""
							+" pjy=\""+hj.pj.y+"\""
							+" pjz=\""+hj.pj.z+"\""
							
							+" njx=\""+hj.nj.x+"\""
							+" njy=\""+hj.nj.y+"\""
							+" njz=\""+hj.nj.z+"\""							

							+" t2ix=\""+hj.t2i.x+"\""
							+" t2iy=\""+hj.t2i.y+"\""
							+" t2iz=\""+hj.t2i.z+"\""							

							+" t3ix=\""+hj.t3i.x+"\""
							+" t3iy=\""+hj.t3i.y+"\""
							+" t3iz=\""+hj.t3i.z+"\""							

							+" t2jx=\""+hj.t2j.x+"\""
							+" t2jy=\""+hj.t2j.y+"\""
							+" t2jz=\""+hj.t2j.z+"\""							

							+" upper=\""+hj.upperLimit+"\""
							+" lower=\""+hj.lowerLimit+"\""							
							+" hinge=\""+hj.hinge+"\""							

							
							+"/>\n");
				}
			}
			
			writer.write("</data>\n");
			
			writer.flush();
			writer.close();
			
			//g.getRender().start();

		} catch (FileNotFoundException e1) {
			
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void loadScene(String name, final PhysicsScene model ) {
		//map for body identifiers
		final Map<String, Body> bodies = new HashMap<String,Body>();
		
		//load configuration
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new File(name), new DefaultHandler() {
				Body currentBody = null;
				@Override
				public void startElement(String uri, String localName,
						String name, Attributes attributes) throws SAXException {
					
					//System.out.println("startElement event");
					
					if (name.equals("gear")) {
						currentBody = new Body();
						//currentBody.identifier = "gear";
						String id = attributes.getValue("id");
						bodies.put(id, currentBody);

						
						int k = 8; double r = 1.0; double s = 1.5;
						for ( int n=0; n<k; n++) {
							double theta = (n+1)*Math.PI*2/k;					
							//resize the drawing shape to the right dimensions
							Vector3 displacement = new Vector3(Math.cos(theta)*r*s, Math.sin(theta)*r*s,0);
							Matrix3 transform = Quaternion.toRotationMatrix3(Quaternion.rotation(-theta-Math.PI/4, Vector3.k), new Matrix3());

							Vector3 size = new Vector3(1*s,1*s,2*s);
							
							//Setup the physics
							Box box = new Box(size.x, size.y, size.z);
							box.setAuxiliary(this);			
							box.setLocalTransform(transform, displacement);
//							box.setMass(mass);
							currentBody.addGeometry(box);
						}
						

						currentBody.finalize();		
						//b.setPosition(position);
						//b.sleepKinetic = 0.1;
						Force force = new GravityForce(currentBody);
						model.addForce(force);
						model.addBody(currentBody);
						

						Iterator<Geometry> gi = currentBody.getGeometries();
						while(gi.hasNext()) {
							Geometry geo = gi.next();

							//a shape for drawing
							List<Vector3> points = new LinkedList<Vector3>();
							points.add( new Vector3( 1, 0.333, 1 ).multiply(0.5));
							points.add( new Vector3( -1.5, 1.5, 1 ).multiply(0.5));
							points.add( new Vector3( 1, -1, 1 ).multiply(0.5));
							points.add( new Vector3( -0.333, -1, 1 ).multiply(0.5));
							points.add( new Vector3( 1, 0.333, -1 ).multiply(0.5));
							points.add( new Vector3( -1.5, 1.5, -1 ).multiply(0.5));
							points.add( new Vector3( 1, -1, -1 ).multiply(0.5));
							points.add( new Vector3( -0.333, -1, -1 ).multiply(0.5));

							//transform
							Vector3 size = ((Box)geo).getDimentions();
							Matrix3 scale = Matrix3.diagonal(size);
							Matrix3 transform = new Matrix3();
							Vector3 displacement = new Vector3();
							geo.getLocalTransform(transform, displacement);
							
							for (Vector3 p: points) {
								p.assign( transform.multiply(scale.multiply(p)).add(displacement) );
							}
							
							//create drawing shape
//							Hull shape = new Hull(points.iterator());
							//tell the renderer about all this (not directly related to jinngine physics)
							//looks weird, but just a simple class to make the graphics work
//							Entity e = new Entity() {
//								final Body body = currentBody;
//								private boolean alarmed = false;
//								@Override
//								public boolean getAlarmed() { return alarmed; }
//								@Override
//								public Vector3 getPosition() { return body.state.rCm.copy();}
//								@Override
//								public Body getPrimaryBody() {return body;}
//								@Override
//								public void setAlarmed(boolean alarmed) {this.alarmed = alarmed;}
//								@Override
//								public void setPosition(Vector3 p) {body.setPosition(p);}
//								@Override
//								public void setSelected(boolean selected) {}
//							};
							
//							geo.setAuxiliary(e);							
							
//							if (graphics!=null) 
//								graphics.getRender().addShape( new FlatShade(), shape, currentBody.state.transform, e);			
						}
					}

					if (name.equals("box") ) {

						
						double h = Double.parseDouble(attributes.getValue("height"));
						double w = Double.parseDouble(attributes.getValue("width"));
						double d = Double.parseDouble(attributes.getValue("depth"));						
						double mass = Double.parseDouble(attributes.getValue("mass"));
						boolean fixed = Boolean.parseBoolean(attributes.getValue("fixed"));
						boolean hidden = Boolean.parseBoolean(attributes.getValue("hidden"));
						
						Box box = new Box(h,w,d);
						box.setMass(mass);
						currentBody = new Body(box);
						currentBody.hidden = hidden;

						//register the box
						String id = attributes.getValue("id");
						bodies.put(id, currentBody);

						
						if (fixed) {
							currentBody.setFixed(true);
						}
						
						currentBody.identifier=id;
						
						//drawing
//						if (graphics != null && !hidden) {
//							//Setup a shape (a box) for drawing
//							Vector3 size = new Vector3(h,w,d);
//							List<Vector3> points = new LinkedList<Vector3>();
//							points.add( new Vector3( 1, 1, 1 ).multiply(0.5));
//							points.add( new Vector3( -1, 1, 1 ).multiply(0.5));
//							points.add( new Vector3( 1, -1, 1 ).multiply(0.5));
//							points.add( new Vector3( -1, -1, 1 ).multiply(0.5));
//							points.add( new Vector3( 1, 1, -1 ).multiply(0.5));
//							points.add( new Vector3( -1, 1, -1 ).multiply(0.5));
//							points.add( new Vector3( 1, -1, -1 ).multiply(0.5));
//							points.add( new Vector3( -1, -1, -1 ).multiply(0.5));
//
//							//resize the drawing shape to the right dimensions
//							Matrix4 transform = jinngine.math.Transforms.scale(size);
//							for (Vector3 p: points)
//								p.assign( transform.multiply(p));
//
//							//create drawing shape
//							Hull shape = new Hull(points.iterator());
//							
//							//tell the renderer about all this (not directly related to jinngine physics)
//							//looks weird, but just a simple class to make the graphics work
//							Entity e = new Entity() {
//								final Body body = currentBody;
//								private boolean alarmed = false;
//								@Override
//								public boolean getAlarmed() { return alarmed; }
//								@Override
//								public Vector3 getPosition() { return body.state.rCm.copy();}
//								@Override
//								public Body getPrimaryBody() {return body;}
//								@Override
//								public void setAlarmed(boolean alarmed) {this.alarmed = alarmed;}
//								@Override
//								public void setPosition(Vector3 p) {body.setPosition(p);}
//								@Override
//								public void setSelected(boolean selected) {}
//							};
//
//							//bind the box geometry to the entity
//							box.setAuxiliary(e); 
//							
//							//finally, ask render to draw this shape
//							graphics.getRender().addShape( new FlatShade(), shape, currentBody.state.transform, e);
//						}
						
						//Tell the model about our new box and attach a gravity force to it
						model.addBody(currentBody);

						if (!fixed) {
							Force f = new GravityForce(currentBody);
							model.addForce(f);
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

					
					if (name.equals("hinge")) {
						double pix = Double.parseDouble(attributes.getValue("pix"));
						double piy = Double.parseDouble(attributes.getValue("piy"));
						double piz = Double.parseDouble(attributes.getValue("piz"));					
						
						double nix = Double.parseDouble(attributes.getValue("nix"));
						double niy = Double.parseDouble(attributes.getValue("niy"));
						double niz = Double.parseDouble(attributes.getValue("niz"));
						
						double pjx = Double.parseDouble(attributes.getValue("pjx"));
						double pjy = Double.parseDouble(attributes.getValue("pjy"));
						double pjz = Double.parseDouble(attributes.getValue("pjz"));					
						
						double njx = Double.parseDouble(attributes.getValue("njx"));
						double njy = Double.parseDouble(attributes.getValue("njy"));
						double njz = Double.parseDouble(attributes.getValue("njz"));						

						double t2ix = Double.parseDouble(attributes.getValue("t2ix"));
						double t2iy = Double.parseDouble(attributes.getValue("t2iy"));
						double t2iz = Double.parseDouble(attributes.getValue("t2iz"));

						double t3ix = Double.parseDouble(attributes.getValue("t3ix"));
						double t3iy = Double.parseDouble(attributes.getValue("t3iy"));
						double t3iz = Double.parseDouble(attributes.getValue("t3iz"));

						double t2jx = Double.parseDouble(attributes.getValue("t2jx"));
						double t2jy = Double.parseDouble(attributes.getValue("t2jy"));
						double t2jz = Double.parseDouble(attributes.getValue("t2jz"));
						
						double upper = Double.parseDouble(attributes.getValue("upper"));
						double lower = Double.parseDouble(attributes.getValue("lower"));

						boolean hinge = Boolean.parseBoolean(attributes.getValue("hinge"));

						
						Body id1 = bodies.get(attributes.getValue("id1"));						
						Body id2 = bodies.get(attributes.getValue("id2"));						

						HingeJoint j = new HingeJoint(id1, id2, new Vector3(pix,piy,piz), new Vector3(nix,niy,niz), 
								new Vector3(t2ix,t2iy,t2iz), new Vector3(t3ix,t3iy,t3iz), new Vector3(pjx,pjy,pjz), new Vector3(njx,njy,njz), 
								new Vector3(t2jx,t2jy,t2jz), lower, upper);
						
						j.hinge = hinge;

						
						model.addConstraint(j);
//						loader.addConstraint(j);
						
					}


				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
		}
		
	}

	
}
