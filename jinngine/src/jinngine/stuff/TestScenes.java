package jinngine.stuff;
//import  java.awt.event.*;
//
//
public class TestScenes {
//	
//	public static void boxStack(Model model, Visualisation visualisation) {
//		model.setDt(0.015);
//
//		//BASE2
//		Box base2 = new Box(00120.0f,0.5,000230.0f);
//		base2.setMass( 9.999999e9f );
//		base2.setPosition( new Vector3( 0, -3, -30.0f));
//		base2.setVelocity( new Vector3( 0.000f, 0.000f, 0.0f ));
//		//base2.setCubeSides( 00120.0f,0.5,000230.0f );
//		base2.setFixed(true);
//		base2.setPermanent();
//		//base2.q = base2.q.rotation( (double)Math.PI*0.5f, new Vector3(0,1,0) );
//		base2.shape.getAABB().tightAABB( base2, base2.shape, 0.5f ); 
//		model.addBody(base2);
//		visualisation.drawBody(base2);
//
//
//		Box box;
//		for (int i=0; i<3; i++) 
//		for (int j=0; j<1; j++) {
//			box = new Box(3+(4-i),3,3+(4-i));
//			//box.setCubeSides(2);
//			box.setMass(15);
////			box.setPermanent();
////			box.setFixed(true);
//			box.setPosition( new Vector3( 4.5*j, -0.5 + i*(0.298+3), 5	 ));
//			//box.setVelocity(new Vector3(0,-0.002,0));
//			model.addBody(box);
//			visualisation.drawBody(box);
//			model.addForce(new GravityForce(box,0.05));
//		}
//		
//		
//	}
//	
//	public static void ballStack(Model model, Visualisation visualisation) {
//		
//		model.setDt(0.025);
//
//		//BASE2
//		Box base2 = new Box(00120.0f,0.5,000230.0f );
//		base2.setMass( 9.999999e9f );
//		base2.setPosition( new Vector3( 0, -3, -30.0f));
//		base2.setVelocity( new Vector3( 0.000f, 0.000f, 0.0f ));
//		//base2.setCubeSides( 00120.0f,0.5,000230.0f );
//		base2.setFixed(true);
//		base2.setPermanent();
//		//base2.q = base2.q.rotation( (double)Math.PI*0.5f, new Vector3(0,1,0) );
//		base2.shape.getAABB().tightAABB( base2, base2.shape, 1.5f ); 
//		model.addBody(base2);
//		visualisation.drawBody(base2);
//
//
//		Body wheel;
//		for (int i=0; i<5; i++) 
//		for (int j=0; j<1; j++) {
//			wheel = new Sphere(2);
//		    //wheel.q = wheel.q.rotation( (double)Math.PI*1.75, new Vector3(0,1,0) );
//			wheel.setMass(323 );
//			wheel.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//			wheel.setPosition( new Vector3( 4.5*j, 0 + i*4.5, 5	 ));
//			wheel.shape.getAABB().tightAABB( wheel, wheel.shape, 1.5f );
//			//wheel.setFixed(true);
//			model.addBody(wheel);
//			visualisation.drawBody(wheel);
//			model.addForce(new GravityForce(wheel,1.1));
//		}
//		
//	}
//	
//	public static void bowlAndBall( Model model, Visualisation visualisation ) {
//
//		for ( double theta = 0; theta<2*Math.PI; theta+=Math.PI/4.0) {
//			//BASE
//			Box base = new Box(00050.0f,4,00050.0f);
//			base.setMass( 9.999999e9f );
//			base.setPosition( new Vector3( 20*Math.sin(theta), -12.5f-3, 20*Math.cos(theta)));
//			//base.setCubeSides( 00050.0f,4,00050.0f );
//			base.setFixed(true);
//			base.setPermanent();
//			Quaternion slope = base.state.q.rotation( -(double)Math.PI*0.024f, new Vector3(1,0,0) );
//			Quaternion rotate = base.state.q.rotation(theta, new Vector3(0,1,0));
//			base.state.q = rotate.Multiply(slope);
//			
//			//base.shape.getAABB().tightAABB( base, base.shape, 1.5f ); 
//			model.addBody(base);
//			visualisation.drawBody(base);
//		}
//
//		final Body wheel = new Sphere(2);
//		//	c.q = c.q.rotation( (double)Math.PI*1.75, new Vector3(1,0,0) );
//		wheel.setMass(7 );
//		wheel.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel.setPosition( new Vector3( 6 , -2, 7	 ));
//		//wheel.setFixed(true);
//		model.addBody(wheel);
//		visualisation.drawBody(wheel);
//		model.addForce(new GravityForce(wheel,0.5));
//
//		
//
//		
//	}
//	
//	public static void GJKtest( Model model, Visualisation visualisation ) {
//		//BASE2
//		Box base2 = new Box(00120.0f,0.5,000230.0f);
//		base2.setMass( 9.999999e9f );
//		base2.setPosition( new Vector3( 0, -12.5f-3, -30.0f));
//		base2.setVelocity( new Vector3( 0.000f, 0.000f, 0.0f ));
//		//base2.setCubeSides( 00120.0f,0.5,000230.0f );
//		base2.setFixed(true);
//		base2.setPermanent();
//		//base2.q = base2.q.rotation( (double)Math.PI*0.5f, new Vector3(0,1,0) );
//		base2.shape.getAABB().tightAABB( base2, base2.shape, 1.5f ); 
//		model.addBody(base2);
//		visualisation.drawBody(base2);
//
//
//		final Body wheel = new Sphere(7);
//		//	c.q = c.q.rotation( (double)Math.PI*1.75, new Vector3(1,0,0) );
//		wheel.setMass(7 );
//		wheel.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel.setPosition( new Vector3( 0 , -3, 0	 ));
//		//wheel.setFixed(true);
//		model.addBody(wheel);
//		visualisation.drawBody(wheel);
//		model.addForce(new GravityForce(wheel,0.1));
//		
//	}
//
//	public static void dome( Model model, Visualisation visualisation ) {
//		//BASE2
//		
//		Box base2 = new Box(00120.0f,0.5,000230.0f);
//		base2.setMass( 9.999999e9f );
//		base2.setPosition( new Vector3( 0, -12.5f-3, -30.0f));
//		base2.setVelocity( new Vector3( 0.000f, 0.000f, 0.0f ));
//		//base2.setCubeSides( 00120.0f,0.5,000230.0f );
//		base2.setFixed(true);
//		base2.setPermanent();
//		//base2.q = base2.q.rotation( (double)Math.PI*0.5f, new Vector3(0,1,0) );
//		base2.shape.getAABB().tightAABB( base2, base2.shape, 1.5f ); 
//		model.addBody(base2);
//		visualisation.drawBody(base2);
//
//
//		final Body wheel = new Dome(7);
//			wheel.state.q = wheel.state.q.rotation( (double)Math.PI*6.75, new Vector3(1,1,1) );
//		wheel.setMass(27 );
//		wheel.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel.setPosition( new Vector3( 0 , -3, 0	 ));
//		//wheel.setFixed(true);
//		model.addBody(wheel);
//		visualisation.drawBody(wheel);
//		model.addForce(new GravityForce(wheel,0.1));
//		
//	}
//
//	
//	public static void chain(Model model, Visualisation visualisation) {
//		Body prev = null;
//		
//		model.setDt(0.025);
//		
//		for ( int n=0; n<7; n++) {
//			final Box box = new Box(2,4,2);
//			//box.setCubeSides(2,4,2);
//			box.setMass(20);
//			//box.q = box.q.rotation( (double)Math.PI*0.2, new Vector3(1,0,0) );
//			box.setPosition( new Vector3( -3.5 , -n*5, -3.5	 ));
//			box.setAngularVelocity(new Vector3(0,0,0));
//			//wheel2.setFixed(true);
//			
//			
//			
//			model.addBody(box);
//			visualisation.drawBody(box);
//			
//			
//			if (prev != null) {
//				model.addForce( new SpringForce(box,new Vector3(0,2,1), prev, new Vector3(0,-2,1) ) );
//				model.addForce( new SpringForce(box,new Vector3(0,2,-1), prev, new Vector3(0,-2,-1) ) );
//				model.addForce( new GravityForce(box,0.01));
//			} else {
//				box.setFixed(true);
//				box.setPermanent();
//				box.setMass(9e99);
//			}
//			
//			prev = box;
//		}
//
//	}
//	
//	public static void boogieCar( Model model, Visualisation visualisation, double yoff) {
//		//double yoff = 3.5;
//		model.setDt(0.028);
//		
//		//BASE
//		Box base = new Box(00030.0f,4,00030.0f );
//		base.setMass( 9.999999e9f );
//		base.setPosition( new Vector3( 0.0f, -12.5f-3, 0));
////		base.setCubeSides( 00030.0f,4,00030.0f );
//		base.setFixed(true);
//		base.setPermanent();
//		base.state.q = base.state.q.rotation( (double)Math.PI*0.034f, new Vector3(1,0,0) );
//		//base.shape.getAABB().tightAABB( base, base.shape, 1.5f ); 
//		//model.addBody(base);
//		//visualisation.drawBody(base);
//		
//		//BASE
//		Box base3 = new Box(00030.0f,4,00030.0f);
//		base3.setMass( 9.999999e9f );
//		base3.setPosition( new Vector3( 0.0f, -12.5f-3, 40));
//		//base3.setCubeSides( 00030.0f,4,00030.0f );
//		base3.setFixed(true);
//		base3.setPermanent();
//		base3.state.q = base3.state.q.rotation( -(double)Math.PI*0.034f, new Vector3(1,0,0) );
//		//base3.shape.getAABB().tightAABB( base3, base3.shape, 1.5f ); 
//		//model.addBody(base3);
//		//visualisation.drawBody(base3);
//		
//		//BASE2
//		Box base2 = new Box(00120.0f,0.5,000230.0f);
//		base2.setMass( 9.999999e9f );
//		base2.setPosition( new Vector3( 0, -12.5f-3, -30.0f));
//		base2.setVelocity( new Vector3( 0.000f, 0.000f, 0.0f ));
//		//base2.setCubeSides( 00120.0f,0.5,000230.0f );
//		base2.setFixed(true);
//		base2.setPermanent();
//		//base2.q = base2.q.rotation( (double)Math.PI*0.5f, new Vector3(0,1,0) );
//		base2.shape.getAABB().tightAABB( base2, base2.shape, 1.5f ); 
//		//model.addBody(base2);
//		//visualisation.drawBody(base2);
//
//
//		final Body wheel = new Sphere(2);
//		//	c.q = c.q.rotation( (double)Math.PI*1.75, new Vector3(1,0,0) );
//		wheel.setMass(4);
//		wheel.sleepKinetic = 0;
//		wheel.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel.setPosition( new Vector3( 0 , yoff-3, 0	 ));
//		//wheel.setFixed(true);
//		model.addBody(wheel);
//		
//		final Body wheel2 = new Sphere(2);
//		//	c.q = c.q.rotation( (double)Math.PI*1.75, new Vector3(1,0,0) );
//		wheel2.setMass(4 );
//		wheel2.sleepKinetic = 0;
//		wheel2.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel2.setPosition( new Vector3( -7 , yoff-3, 0	 ));
//
//		//wheel.setFixed(true);
//		model.addBody(wheel2);
//
//		final Body wheel3 = new Sphere(2);
//		//	c.q = c.q.rotation( (double)Math.PI*1.75, new Vector3(1,0,0) );
//		wheel3.setMass(4 );
//		wheel3.sleepKinetic = 0;
//
//		wheel3.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel3.setPosition( new Vector3( 0 , yoff-3, -7	 ));
//
//		//wheel.setFixed(true);
//		model.addBody(wheel3);
//
//		final Body wheel4 = new Sphere(2);
//		//wheel4.q = wheel4.q.rotation( (double)Math.PI*0.5, new Vector3(0,1,0) );
//		wheel4.setMass(4 );
//		wheel4.sleepKinetic = 0;
//		wheel4.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel4.setPosition( new Vector3( -7 , yoff-3, -7	 ));
//
//		//wheel.setFixed(true);
//		model.addBody(wheel4);
//
//		
//		
//		final Body box = new Box(5,5,5);//new Cylinder(model,4,3,2);
//		//box.setCubeSides(4,4,4);
//		box.setMass(15);
//		box.sleepKinetic = 0;
//		box.state.q = box.state.q.rotation( (double)-Math.PI*0.5, new Vector3(1,0,0) );
//		box.setPosition( new Vector3( -3.5 , yoff+0.1, -3.5	 ));
//		box.setAngularVelocity(new Vector3(0,0,0));
//		//wheel2.setFixed(true);
//		model.addBody(box);
//		
//		model.muteBodyPair(new Pair<Body>(box,wheel));
//		model.muteBodyPair(new Pair<Body>(box,wheel2));
//		model.muteBodyPair(new Pair<Body>(box,wheel3));
//		model.muteBodyPair(new Pair<Body>(box,wheel4));
//		
//		
//		model.addForce(new JointForce( wheel, box, new Vector3(0,0,0) ));		
//		model.addForce(new JointForce(wheel2, box, new Vector3(0,0,0) ));
//		model.addForce(new JointForce(wheel3, box, new Vector3(0,0,0) ));
//		model.addForce(new JointForce(wheel4, box, new Vector3(0,0,0) ));
//
//		final MotorForce left1 = new MotorForce(wheel,new Vector3(0,0,40), new Vector3(0,1,0));
//		final MotorForce left2 = new MotorForce(wheel3,new Vector3(0,0,40), new Vector3(0,1,0));
//		final MotorForce right1 = new MotorForce(wheel2,new Vector3(0,0,40), new Vector3(0,1,0));
//		final MotorForce right2 = new MotorForce(wheel4,new Vector3(0,0,40), new Vector3(0,1,0));
//
//		model.addForce(left1);
//		model.addForce(left2);
//		model.addForce(right1);
//		model.addForce(right2);
//
//		visualisation.addControler(
//				new Controler() {
//					public void keyPressed(KeyEvent key) {
//						if ( key.getKeyChar() == 'j') {
//							//jump
//							wheel.setVelocity(wheel.getVelocity().add(new Vector3(0,12,0)));
//							wheel2.setVelocity(wheel2.getVelocity().add(new Vector3(0,12,0)));
//							wheel3.setVelocity(wheel3.getVelocity().add(new Vector3(0,12,0)));
//							wheel4.setVelocity(wheel4.getVelocity().add(new Vector3(0,12,0)));
//
//						}
//						if ( key.getKeyChar() == 'a') {
//							left1.setFactor(1);
//							left2.setFactor(1);
//						}
//						if ( key.getKeyChar() == 'z') {
//							left1.setFactor(-1);
//							left2.setFactor(-1);
//						}
//						if ( key.getKeyChar() == 's') {
//							right1.setFactor(1);
//							right2.setFactor(1);
//						}
//						if ( key.getKeyChar() == 'x') {							
//							right1.setFactor(-1);
//							right2.setFactor(-1);
//						}
//					}
//					public void keyReleased(KeyEvent key) {
//						if ( key.getKeyChar() == 'a') {
//							left1.setFactor(0);
//							left2.setFactor(0);
//						}
//						if ( key.getKeyChar() == 'z') {
//							left1.setFactor(0);
//							left2.setFactor(0);							
//						}
//						if ( key.getKeyChar() == 's') {
//							right1.setFactor(0);
//							right2.setFactor(0);							
//						}
//						if ( key.getKeyChar() == 'x') {
//							right1.setFactor(0);
//							right2.setFactor(0);														
//						}
//					}
//				} );
//
////		model.addForce(new MotorForce(box, new Vector3(1,0,0), new Vector3(5,0,0)));
//
//		
//		
//		model.addForce(new GravityForce(wheel,0.3));
//		model.addForce(new GravityForce(box,0.3));
//		model.addForce(new GravityForce(wheel2,0.3));
//		model.addForce(new GravityForce(wheel3,0.3));
//		model.addForce(new GravityForce(wheel4,0.3));
//	
//		
//		visualisation.drawBody(wheel);
//		visualisation.drawBody(wheel2);
//		visualisation.drawBody(wheel3);
//		visualisation.drawBody(wheel4);
//		visualisation.drawBody(box);
//		
//	}
//
//	public static void springForce( Model model, Visualisation visualisation ) {
//
//		double yoff = 2.5;
//		model.setDt(0.025);
//
//		//BASE
//		Box base = new Box(00030.0f,0.5,00030.0f );
//		base.setMass( 9.999999e9f );
//		base.setPosition( new Vector3( 0.0f, -12.5f, 0));
//		//base.setCubeSides( 00030.0f,0.5,00030.0f );
//		base.setFixed(true);
//		base.setPermanent();
//		base.shape.getAABB().tightAABB( base, base.shape, 1.5f ); 
//		base.state.q = base.state.q.rotation( (double)Math.PI*0.5f, new Vector3(0,1,0) );
//		model.addBody(base);
//		visualisation.drawBody(base);
//		
//		//BASE2
//		Box base2 = new Box(00120.0f,0.5,000130.0f);
//		base2.setMass( 9.999999e9f );
//		base2.setPosition( new Vector3( 0, -12.5f-0.7, -30.0f));
//		base2.setVelocity( new Vector3( 0.000f, 0.000f, 0.0f ));
//		//base2.setCubeSides( 00120.0f,0.5,000130.0f );
//		base2.setFixed(true);
//		base2.setPermanent();
//		base2.shape.getAABB().tightAABB( base2, base2.shape, 1.5f ); 
//		base2.state.q = base2.state.q.rotation( (double)Math.PI*0.5f, new Vector3(0,1,0) );
//		model.addBody(base2);
//		visualisation.drawBody(base2);
//
//
//		Body wheel = new Sphere(1);
//		//	c.q = c.q.rotation( (double)Math.PI*1.75, new Vector3(1,0,0) );
//		wheel.setMass(5.0 );
//		wheel.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel.setPosition( new Vector3( 0 , yoff+2, 1.75	 ));
//		//wheel.setFixed(true);
//
//		Body wheel2 = new Sphere(1);
//		//	c.q = c.q.rotation( (double)Math.PI*1.75, new Vector3(1,0,0) );
//		wheel2.setMass(99.0 );
//		wheel2.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel2.setPosition( new Vector3( 4 , yoff-2, 0	 ));
//		//wheel2.setFixed(true);
//
//		Body wheel3 = new Sphere(1);
//		//	c.q = c.q.rotation( (double)Math.PI*1.75, new Vector3(1,0,0) );
//		wheel3.setMass(99.0 );
//		wheel3.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		wheel3.setPosition( new Vector3( -4 , yoff-2, 0	 ));
//		//wheel3.setFixed(true);
//
//		
//		
//		Body rod=new Cylinder(3,1,1);
//		rod.setMass(3);
//		rod.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		rod.setPosition( new Vector3( 0 , yoff, 0	 ));
//		rod.setVelocity( new Vector3( 0.0000f, -0.0000f, -0.0000f ));
//
//		Body c2=new Sphere(1);
//		//c2.setCubeSides(1);
//		c2.setMass(99.0);
//		//c2.setFixed(true);
//		c2.setAngularVelocity( new Vector3( 0.00f, -0.00, -0.0000f) );
//		c2.setPosition( new Vector3( 0 , yoff-2, 4	 ));
//		c2.setVelocity( new Vector3( 0.0000f, -0.0000f, -0.0000f ));
//
//
//
//		//model.addBody(rod);
//		model.addBody(c2);
//		model.addBody(wheel);
//		model.addBody(wheel2);
//		model.addBody(wheel3);
//
//		
//		//visualisation.drawBody(rod);
//		visualisation.drawBody(c2);
//		visualisation.drawBody(wheel);
//		visualisation.drawBody(wheel2);
//		visualisation.drawBody(wheel3);
//
//		
////		model.addForce( new SpringForce(rod,new Vector3(7.5,0,1.5), c2, new Vector3(0,0,-1.5), 1.5 ));
////		model.addForce( new SpringForce(rod,new Vector3(-7.5,0,1.5), c2, new Vector3(0,0,-1.5), 1.5 ));
////
////		model.addForce( new SpringForce(rod,new Vector3(0,1,0), wheel2, new Vector3(0,1.5,0),1.5 ) );
////		model.addForce( new SpringForce(rod,new Vector3(1,0,0), wheel2, new Vector3(0,1.5,0),1.5 ) );
////		
////		model.addForce( new SpringForce(rod,new Vector3(0,-1,0), wheel3, new Vector3(0,-1.5,0),1.5 ) );
////		model.addForce( new SpringForce(rod,new Vector3(-1,0,0), wheel3, new Vector3(0,-1.5,0),1.5 ) );
//
//		model.addForce( new SpringForce(wheel2,new Vector3(0,0,0), wheel3, new Vector3(0,00,0) ) );
//		model.addForce( new SpringForce(wheel3,new Vector3(0,0,0), c2, new Vector3(0,0,0) ) );
//		model.addForce( new SpringForce(c2,new Vector3(0,0,0), wheel2, new Vector3(0,0,0) ) );
//
//		model.addForce( new SpringForce(wheel2,new Vector3(0,0,0), wheel, new Vector3(0,0,0) ) );
//		model.addForce( new SpringForce(wheel3,new Vector3(0,0,0), wheel, new Vector3(0,0,0) ) );
//		model.addForce( new SpringForce(c2,new Vector3(0,0,0), wheel, new Vector3(0,0,0) ) );
////
//		
////		model.addForce( new SpringForce(rod,new Vector3(0,-1,0), wheel3, new Vector3(0,0,0) ) );
////		model.addForce( new SpringForce(rod,new Vector3(-1,0,0), wheel3, new Vector3(0,0,0) ) );
////		model.addForce( new SpringForce(rod,new Vector3(7.5,0,1.5), c2, new Vector3(0,0,0) ));
////		model.addForce( new SpringForce(rod,new Vector3(-7.5,0,1.5), c2, new Vector3(0,0,0) ));
////		model.addForce( new SpringForce(rod,new Vector3(0,1,0), wheel2, new Vector3(0,0,0) ) );
////		model.addForce( new SpringForce(rod,new Vector3(1,0,0), wheel2, new Vector3(0,0,0) ) );
//
////		model.addForce( new SpringForce(rod,new Vector3(0,0,1.5), wheel, new Vector3(0,0,0) ) );
////		model.addForce( new SpringForce(rod,new Vector3(0,0,-1.5), wheel, new Vector3(0,0,0) ) );
//
////		model.addForce( new SpringForce(rod,new Vector3(0,0,1.5), wheel2, new Vector3(0,0,0) ) );
////		model.addForce( new SpringForce(rod,new Vector3(0,0,-1.5), wheel2, new Vector3(0,0,0) ) );
//
//		
//		//model.addForce(new GravityForce(rod));
//		model.addForce(new GravityForce(c2,0.1));
//		model.addForce(new GravityForce(wheel2,0.1));
//		model.addForce(new GravityForce(wheel3,0.1));
//		model.addForce(new GravityForce(wheel,0.1));
//
//		
//	}
//	
//	static public void oldScenes( Model model, Visualisation visualisation ) {
//
//		//Create a new 
//		Body c;
//
//		model.setDt(0.015);
//
//		int stack =1; double height = 0.92f; double diameter = 4.0f;
//		for (int i=0;i<(stack-0) ;i++) {
//			int j = 0; double delta_theta = ((double)(2*Math.PI)/12.0f);
//			for (double theta=delta_theta; theta<2*Math.PI; theta+=delta_theta ) {
//				j++;
//
//				for ( int k=0;k<1;k++) {
//					Body cu = new Box(1.2f, 0.92f, 0.72f );
//					
//					//Tetrahedron cu = new Tetrahedron(model,0.5);
//					//c.setOmegaCm( new Vector( 0.00, 1.202*(-(i%2)), 0.000) );
//					cu.setPosition( new Vector3( /*0.60*j+(0.3*(i%2))*/ diameter*(double)Math.sin(theta + (i%2)*0.5f*delta_theta ),  (height*1.05f)*i-7.4500f, -5.3f-0.65f*k+diameter*(double)Math.cos(theta + (i%2)*delta_theta*0.5f ) ));
//					cu.setVelocity( new Vector3( 0.000f, 0.000f, 0.0000f ));
//					//cu.setCubeSides( 1.2f, height, 0.62f );
//					//cu.actingForce.assign(new Vector3(0,-50.5f*1.0f,0));
//					model.addForce(new GravityForce(cu,0.1));
//					cu.setMass( 6.0f*1 );
//					cu.state.q = cu.state.q.rotation( theta+(i%2)*0.5f*delta_theta, new Vector3(0,1,0) );
//					//c.q = c.q.Rotation( Math.PI/(32.0f), new Vector(0,0,1) );
//					model.addBody(cu);
//					visualisation.drawBody(cu);
//
//				}
//			}
//		}
//
//
//		//BASE
//		for (int i=0; i<1; i++) {
//			Box cu = new Box(00130.0f+40.0f*i,9.0f,00130.0f+40.0f*i);
//			cu.setAngularVelocity( new Vector3( 0.00f, 0.0000f, 0.00000f) );
//			cu.setPosition( new Vector3( 0.0f, -12.5f -(4.5f*i), -4.7f*0));
//			cu.setVelocity( new Vector3( 0.000f, 0.000f, 0.0f ));
//			//cu.setCubeSides( 00130.0f+40.0f*i,9.0f,00130.0f+40.0f*i );
//			cu.setMass( 9.999999e9f );
//			cu.setFixed(true);
//			cu.setPermanent();
//			cu.shape.getAABB().tightAABB( cu, cu.shape, 5.5f ); 
//			cu.state.q = cu.state.q.rotation( (double)Math.PI*0.5f, new Vector3(0,1,0) );
//
//			model.addBody(cu);
//			visualisation.drawBody(cu);
//		}    		
//	}
//	
}
