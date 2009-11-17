package jinngine.demo;



import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import jinngine.demo.graphics.Graphics;
import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.math.Vector3;
import jinngine.math.Quaternion;
import jinngine.physics.Body;
import jinngine.physics.Model;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.physics.solver.QuadraticPrograming;
import jinngine.physics.solver.SubspaceMinimization;

public class Demo2 implements KeyListener {
	Graphics g = new Graphics();
	Model model = g.getModel();

	public Demo2() {
		
		Body floor = new Body(new Box(1500,10,1500));
		floor.setPosition(new Vector3(0,-25,0));
		//floor.state.q.assign(Quaternion.rotation(-0.27, Vector3.k));
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

		model.setDt(0.00001);
		//wee need some power for this
		//model.getSolver().setMaximumIterations(3224);		
		//model.setSolver(new ProjectedGaussSeidel(115));
		model.setSolver(new SubspaceMinimization(true));
		//model.setSolver(new QuadraticPrograming());
		
		
		//build a wall
		for (int i=0; i<2; i++) {
			for (int j=0; j<2; j++) {
				new Cube(g, new Vector3(3,2,2), new Vector3(-17+i*(3.1) +(j%2)*1.5,-18.8+j*2.1 ,-25), 10+j*0 );
//				new Cube(g, new Vector3(3,2,2), new Vector3(-17+i*3.1 +(j%2)*1.5*.2,-18.8+j*2.1 ,-25), 10 );

			}
		}
		


		
		

		//start animation
		g.addKeyListener(this);
		g.start();
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (model == null) {
			return;
		}
		
		switch (e.getKeyChar()) {
		case 'w':
			System.out.println("writing file");
			try {
				
				File f=new File("configuration.xml");
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f));
				
				writer.write("<data>\n");
				
				Iterator<Body> i = model.getBodies();
				while(i.hasNext()) {
					Body b = i.next();
					Geometry geo = b.getGeometries().next();
					if (geo instanceof Box) {
						Box box = (Box)geo;
						Vector3 size = box.getDimentions();
						Vector3 pos = b.state.rCm.copy();
						Quaternion q = b.state.q.copy();
						writer.write(("<box height=\""+size.x+"\" width=\""+size.y+"\" depth=\""+size.z+"\" mass=\""+b.state.M+"\" fixed=\""+b.isFixed()+"\">\n"));
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

	
	public static void main( String args[]) {
		new Demo2();
	}
}
