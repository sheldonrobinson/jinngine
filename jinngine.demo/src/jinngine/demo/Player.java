package jinngine.demo;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jinngine.demo.graphics.Entity;
import jinngine.demo.graphics.FlatShade;
import jinngine.demo.graphics.Graphics;
import jinngine.demo.graphics.Hull;
import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.math.Matrix4;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Model;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.FischerNewtonConjugateGradients;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.physics.solver.SubspaceMinimization;

public class Player implements KeyListener {
	final Graphics g = new Graphics();
	final Model model = g.getModel();

	public Player() {
		
		model.setSolver(new SubspaceMinimization());
		//model.setSolver(new ProjectedGaussSeidel(645));
		
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
						double mass = Double.parseDouble(attributes.getValue("mass"));
						
						Box box = new Box(h,w,d);
						box.setMass(mass);
						currentBody = new Body(box);
						
						//Setup a shape (a box) for drawing
						Vector3 size = new Vector3(h,w,d);
						List<Vector3> points = new LinkedList<Vector3>();
						points.add( new Vector3( 1, 1, 1 ).multiply(0.5));
						points.add( new Vector3( -1, 1, 1 ).multiply(0.5));
						points.add( new Vector3( 1, -1, 1 ).multiply(0.5));
						points.add( new Vector3( -1, -1, 1 ).multiply(0.5));
						points.add( new Vector3( 1, 1, -1 ).multiply(0.5));
						points.add( new Vector3( -1, 1, -1 ).multiply(0.5));
						points.add( new Vector3( 1, -1, -1 ).multiply(0.5));
						points.add( new Vector3( -1, -1, -1 ).multiply(0.5));

						//resize the drawing shape to the right dimensions
						Matrix4 transform = jinngine.math.Transforms.scale(size);
						for (Vector3 p: points)
							p.assign( transform.multiply(p));
						
						//create drawing shape
						Hull shape = new Hull(points.iterator());
						
						currentBody.sleepKinetic = 0.0;

						//Tell the model about our new box and attach a gravity force to it
						model.addForce(new GravityForce(currentBody));
						model.addBody(currentBody);
						
						//tell the renderer about all this (not directly related to jinngine physics)
						//looks weird, but just a simple class to make the graphics work
						Entity e = new Entity() {
							final Body body = currentBody;
							private boolean alarmed = false;
							@Override
							public boolean getAlarmed() { return alarmed; }
							@Override
							public Vector3 getPosition() { return body.state.rCm.copy();}
							@Override
							public Body getPrimaryBody() {return body;}
							@Override
							public void setAlarmed(boolean alarmed) {this.alarmed = alarmed;}
							@Override
							public void setPosition(Vector3 p) {body.setPosition(p);}
							@Override
							public void setSelected(boolean selected) {}
						};

						//bind the box geometry to the entity
						box.setAuxiliary(e); 
						
						//finally, ask render to draw this shape
						g.getRender().addShape( new FlatShade(), shape, currentBody.state.transform, e);						
						
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
		
		
		//Setup a shape (a box) for drawing
		Vector3 size = new Vector3(3,2,2);
		List<Vector3> points = new LinkedList<Vector3>();
		points.add( new Vector3( 1, 1, 1 ).multiply(0.5));
		points.add( new Vector3( -1, 1, 1 ).multiply(0.5));
		points.add( new Vector3( 1, -1, 1 ).multiply(0.5));
		points.add( new Vector3( -1, -1, 1 ).multiply(0.5));
		points.add( new Vector3( 1, 1, -1 ).multiply(0.5));
		points.add( new Vector3( -1, 1, -1 ).multiply(0.5));
		points.add( new Vector3( 1, -1, -1 ).multiply(0.5));
		points.add( new Vector3( -1, -1, -1 ).multiply(0.5));

		//resize the drawing shape to the right dimensions
		Matrix4 transform = jinngine.math.Transforms.scale(size);
		for (Vector3 p: points)
			p.assign( transform.multiply(p));
		
		//create drawing shape
		Hull shape = new Hull(points.iterator());
					
		//create a world that contains walls and a floor (not drawn)
		Body floor = new Body(new Box(1500,10,1500));
		floor.setPosition(new Vector3(0,-25,0));
		//floor.state.q.assign(Quaternion.rotation(-0.1, Vector3.k));
		floor.setFixed(true);
		
		Body back = new Body( new Box(200,200,2));		
		back.setPosition(new Vector3(0,0,-45));
		back.setFixed(true);

		Body front = new Body( new Box(200,200,2));		
		front.setPosition(new Vector3(0,0,-15));
		front.setFixed(true);

		Body left = new Body( new Box(2,200,200));		
		left.setPosition(new Vector3(-25,0,0));
		left.setFixed(true);

		Body right = new Body( new Box(2,200,200));		
		right.setPosition(new Vector3(0,0,0));
		right.setFixed(true);

		model.addBody(left);
		model.addBody(right);
		model.addBody(front);
		model.addBody(floor);
		model.addBody(back);
		
		//start animation
		g.addKeyListener(this);
		g.start();
	}
	
	public static void main(String[] args) {
		new Player();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (model == null) {
			return;
		}
		
		
		switch (e.getKeyChar()) {
		case 'w':
			//g.getRender().stop();
			
			System.out.println("writing file");
			try {
				
				File f=new File("configuration.xml");
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f));
				
				writer.write("<data>\n");
				
				Iterator<Body> i = model.getBodies();
				while(i.hasNext()) {
					Body b = i.next();
					Geometry geo = b.getGeometries().next();
					if (geo instanceof Box && !b.isFixed()) {
						Box box = (Box)geo;
						Vector3 size = box.getDimentions();
						Vector3 pos = b.state.rCm.copy();
						Quaternion q = b.state.q.copy();
						writer.write(("<box height=\""+size.x+"\" width=\""+size.y+"\" depth=\""+size.z+"\" mass=\""+b.state.M+"\">\n"));
						writer.write(("  <position x=\""+pos.x+"\" y=\""+pos.y+"\" z=\""+pos.z+"\"/>\n"));
						writer.write(("  <orientation s=\""+q.s+"\" x=\""+q.v.x+"\" y=\""+q.v.y+"\" z=\""+q.v.z+"\"/>\n"));
						writer.write(("  <velocity x=\""+b.state.vCm.x+"\" y=\""+b.state.vCm.y+"\" z=\""+b.state.vCm.z+"\"/>\n"));
						writer.write(("  <angularvalocity x=\""+b.state.omegaCm.x+"\" y=\""+b.state.omegaCm.y+"\" z=\""+b.state.omegaCm.z+"\"/>\n"));
						writer.write("</box>\n");
					}
				}

				writer.write("</data>\n");
				
				writer.flush();
				writer.close();
				
				//g.getRender().start();

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			break;
		}
	}
	@Override public void keyReleased(KeyEvent e) {}
	@Override public void keyTyped(KeyEvent e) {}

}
