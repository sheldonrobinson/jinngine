package jinngine.game.actors.player;

import java.util.*;
import java.io.File;
import java.io.IOException;

import com.ardor3d.extension.effect.water.WaterNode;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.framework.Canvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.OutputCapsule;
import com.google.common.base.Predicate;


import jinngine.game.Game;
import jinngine.game.Toolbox;
import jinngine.game.actors.PhysicalActor;
import jinngine.geometry.contact.ContactGenerator;
import jinngine.geometry.contact.ContactGenerator.ContactPoint;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.ContactTrigger;
import jinngine.physics.Scene;
import jinngine.physics.Trigger;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.contact.ContactConstraint;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.Solver.NCPConstraint;
import jinngine.util.Pair;

public class Player extends Node implements PhysicalActor {

	private Body playerbody;
	private Constraint controlconstraint;
	private double jumpable = 0;
	private boolean jumped = false;
	private int contacts = 0;
	private boolean jumpedinframe = false;
	private boolean focuscamera = false;
	private double focusxcoord = 0;
	
	private double tick = 0;
	
	private KeyboardState previouskbstate;
	
	private jinngine.geometry.Box box;
	
    final double walkvelocity = 2;
    final double walkimpulse = 0.5*2;
    final Vector3 velocity = new Vector3();
    final Vector3 force = new Vector3();

    private final Vector3 jumpdirection = new Vector3(0,1,0);	
    private final Vector3 heading = new Vector3(1,0,0);
    private final Vector3 playerdisplace = new Vector3(0,0,0);
    
    
    private final List<ContactConstraint> contactconstraints = new ArrayList<ContactConstraint>();
	
	
	@Override
	public void write(final OutputCapsule oc) throws IOException {
		super.write(oc);
	}
		
	@Override
	public void create(Game game) {
		this.setName("Actor:Player");

		final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.load("girl.dae");
        final Node body = storage.getScene();
        body.setScale(1);
        body.setName("playermainbody");
        TextureState headts = new TextureState();
        headts.setEnabled(true);
        headts.setTexture(TextureManager.load("girltexlow.tga", 
        		Texture.MinificationFilter.Trilinear,
        		TextureStoreFormat.GuessCompressedFormat, true),0);
        headts.setTexture(TextureManager.load("girlnormallow.tga", 
        		Texture.MinificationFilter.BilinearNoMipMaps,
        		TextureStoreFormat.GuessCompressedFormat, true),1);
        body.setRenderState(headts);
        

        final GLSLShaderObjectsState shader = new GLSLShaderObjectsState();
        //load shaders
        try {
          shader.setVertexShader(WaterNode.class.getClassLoader().getResourceAsStream("jinngine/game/resources/bumbshader.vert"));
          shader.setFragmentShader(WaterNode.class.getClassLoader().getResourceAsStream("jinngine/game/resources/bumbshader.frag"));
      } catch (final IOException e) {
    	  e.printStackTrace();
          return;
      }
      shader.setUniform("texture0", 0);
      shader.setUniform("normalmap", 1);      
      body.setRenderState(shader);
      
      // attach the node
      attachChild(body);
      
      body.setTranslation(body.getTranslation().add(0,-20,0,null));
      
      // attach to root
      game.getRendering().getRootNode().attachChild(this);
	}

	@Override
	public void act(Game game) {
		
		playerdisplace.y = Math.sin(tick)*0.25;
		
		
		if (jumpable > 0)
			jumpable--;
		
		jumpedinframe = false;
		
  		//reset jump velocity and force
		velocity.y = 0;
		force.y = 0;

		// get camera
		ReadOnlyVector3 location = game.getRendering().getCamera().getLocation();
		double camlocation = location.getX();
		
		if (focuscamera) {
			
			game.getRendering().getCamera().setLocation( (camlocation*0.95+focusxcoord*0.05), -25+7, -20);
			game.getRendering().getCamera().lookAt((camlocation*0.95+focusxcoord*0.05), -25, 0, com.ardor3d.math.Vector3.UNIT_Y);
			
			if (Math.abs(camlocation-focusxcoord) < 0.01) {
				focuscamera = false;
			}
		} else {
			
			if (Math.abs(camlocation-playerbody.state.position.x) > 6.7) {
				focusxcoord = playerbody.state.position.x;
				focuscamera = true;
			}
		}

	}

	@Override
	public void start(final Game game) {
		Scene physics = game.getPhysics();
		
        Node body = (Node)getChild("playermainbody");
        
        body.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	Body body = playerbody;
            	Vector3 disp = body.state.position.copy();
            	Vector3.add( disp,  playerdisplace );
            	
            	caller.setTranslation(disp.x, disp.y, disp.z);
            	ReadOnlyMatrix3 mat = new Matrix3(body.state.rotation.a11, body.state.rotation.a12, body.state.rotation.a13,
            			body.state.rotation.a21, body.state.rotation.a22, body.state.rotation.a23, 
            			body.state.rotation.a31, body.state.rotation.a32, body.state.rotation.a33);
            	
            	caller.setRotation(mat);
            }
        });
                
        //connect actor to mesh
        body.setUserData(this);
                
        // setup shadows
        game.getRendering().getPssmPass().add(body);
        game.getRendering().getPssmPass().addOccluder(body);
        
        // Physics      
        box = new jinngine.geometry.Box(1,1,1);
//        jinngine.geometry.Sphere box = new Sphere(1);
        box.setRestitution(0.25);
        box.setFrictionCoefficient(0.15);
        box.setEnvelope(0.2);
        playerbody = new Body("default", box);
//        box.setLocalTransform(jinngine.math.Matrix3.identity(), playerdisplace );
        Toolbox.setTransformFromNode(body, playerbody);
        physics.addBody(playerbody);
        physics.addForce(new GravityForce(playerbody));
        
//        final ImpulseForce impulse = new ImpulseForce(playerbody, new Vector3(), new Vector3(0,1,0), 0);
//        game.getPhysics().addForce(impulse);
        
        final Body dummy = new Body("default");
        dummy.setFixed(true);
        
           
        // create control velocity constraint
        controlconstraint = new Constraint() {       	
        	final NCPConstraint c = new NCPConstraint();       	
        	final NCPConstraint c2 = new NCPConstraint();   
        	final NCPConstraint c3 = new NCPConstraint();   
        	final NCPConstraint c4 = new NCPConstraint();   
        	final NCPConstraint c5 = new NCPConstraint();   

        	@Override
        	public void applyConstraints(ListIterator<NCPConstraint> iterator,
        			double dt) {
        		
        		Vector3 u = playerbody.state.velocity;//.add(playerbody.state.omega.cross(ri));
        		
        		// -(unf-uni)  -correction 
        		c.assign(playerbody, dummy,
        				new Vector3(1,0,0), new Vector3(), new Vector3(), new Vector3(),
        				new Vector3(1,0,0), new Vector3(), new Vector3(), new Vector3(), 
        				Math.min(force.x,0), Math.max(0,force.x), 
        				null,
        				u.x-velocity.x, 0  );
        		iterator.add(c);

        		c2.assign(playerbody, dummy,
        				new Vector3(0,0,1), new Vector3(), new Vector3(), new Vector3(),
        				new Vector3(0,0,1), new Vector3(), new Vector3(), new Vector3(), 
        				Math.min(force.z,0), Math.max(0,force.z), 
        				null,
        				u.z-velocity.z, 0  );
        		iterator.add(c2);

        		
        		c3.assign(playerbody, dummy,
        				jumpdirection, new Vector3(), new Vector3(), new Vector3(),
        				jumpdirection, new Vector3(), new Vector3(), new Vector3(),
        				Math.min(force.y,0), Math.max(0,force.y), 
        				null, 
        				jumpdirection.dot(playerbody.state.velocity)-velocity.y, 0  );
        		iterator.add(c3);

        		
        		Vector3 axis = playerbody.state.rotation.multiply(new Vector3(1,0,0));
        		c4.assign(playerbody, dummy,
        				new Vector3(), new Vector3(0,1,0), new Vector3(), new Vector3(),
        				new Vector3(), new Vector3(0,1,0), new Vector3(), new Vector3(),
        				-1, 1, 
        				null, 
        				playerbody.state.omega.y-axis.cross(heading).y*10, 0  );
        		iterator.add(c4);
        		
        		c5.assign(playerbody, dummy,
        				new Vector3(), new Vector3(0,0,1), new Vector3(), new Vector3(),
        				new Vector3(), new Vector3(0,0,1), new Vector3(), new Vector3(),
        				-1, 1, 
        				null, 
        				playerbody.state.omega.z-axis.cross(heading).z*10, 0  );
        		iterator.add(c5);


        	}
        	
        	@Override
        	public Pair<Body> getBodies() {
        		return new Pair<Body>(playerbody,dummy);
        	}
			@Override
			public Iterator<NCPConstraint> getNcpConstraints() {
				throw new UnsupportedOperationException();
			}
        };
        
        // add constraint to jinngine
        game.getPhysics().addConstraint(controlconstraint);
        game.getPhysics().addLiveConstraint(controlconstraint);
        
        // create a trigger that registers players contact forces  
        Trigger trigger = new ContactTrigger(playerbody, 0.025, new ContactTrigger.Callback() {
        	@Override
        	public void contactAboveThreshold(Body interactingBody, ContactConstraint contact) {
        		
        		contacts++;    
        		
        		contactconstraints.add(contact);
        		        		
        	}
        	public void contactBelowThreshold(Body interactingBody, ContactConstraint contact ) { 
        		
        		contactconstraints.remove(contact);
        		contacts--; }
        });
        
        // add the trigger
        game.getPhysics().addTrigger(trigger);
        
        
        
        
        //debug mesh
//        Toolbox.addJinngineDebugMesh("playerdebugmesh", body, playerbody);
        
        // WASD control ( more or less the same as in the ardor3d examples)
        final Predicate<TwoInputStates> keysHeld = new Predicate<TwoInputStates>() {
            Key[] keys = new Key[] { Key.W, Key.A, Key.S, Key.D, Key.SPACE };

            public boolean apply(final TwoInputStates states) {
//                for (final Key k : keys) {
//                    if (states.getCurrent() != null && states.getCurrent().getKeyboardState().isDown(k)) {
//                        return true;
//                    }
//                }
                return true;
            }
        };

        final TriggerAction moveAction = new TriggerAction() {
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                 KeyboardState kb = inputStates.getCurrent().getKeyboardState();
                 

                 if (previouskbstate != null) {
                	 // get held keys
                	 EnumSet<Key> keys = kb.getKeysPressedSince(previouskbstate);

                	 velocity.assignZero();
                	 force.assignZero();
                	 box.setFrictionCoefficient(2);
                	 

                	 if (kb.isDown(Key.W)) {

                		 if (contacts>0)
                			 tick = tick + 0.5;

                		 heading.assign(0,0,1);
                		 velocity.z = walkvelocity;
                		 force.z = walkimpulse;
                		 box.setFrictionCoefficient(0.25);
                	 }
                	 if (kb.isDown(Key.S)) {
                		 tick = tick + 0.5;

                		 heading.assign(0,0,-1);
                		 velocity.z = -walkvelocity;
                		 force.z = -walkimpulse;
                		 box.setFrictionCoefficient(0.25);

                	 }
                	 if (kb.isDown(Key.A)) {
                		 tick = tick + 0.5;

                		 heading.assign(1,0,0);
                		 velocity.x = walkvelocity;
                		 force.x = walkimpulse;
                		 box.setFrictionCoefficient(0.25);

                	 }
                	 if (kb.isDown(Key.D)) {
                		 tick = tick + 0.5;

                		 heading.assign(-1,0,0);
                		 velocity.x = -walkvelocity;
                		 force.x = -walkimpulse;
                		 box.setFrictionCoefficient(0.25);

                	 }  


            		 if (contacts<1)
            			 tick = tick*0.5;


                	 if (tick > 2*Math.PI) tick = tick -2*Math.PI;

                	 //                 
                	 //                 if (kb.isDown(Key.W) && kb.isDown(Key.D)) {
                	 ////                	 System.out.println("W and D");
                	 //                	 movedirection.assign(-0.707106,0,0.707106);
                	 //                	 velocity.x = walkvelocity;
                	 //                	 force.x = walkimpulse;                 	 
                	 //                 } else if (kb.isDown(Key.D) && kb.isDown(Key.S)) {
                	 //                	 movedirection.assign(-0.707106,0,-0.707106);
                	 //                	 velocity.x = walkvelocity;
                	 //                	 force.x = walkimpulse;
                	 //                 } else if (kb.isDown(Key.S) && kb.isDown(Key.A)) {
                	 //                	 movedirection.assign(0.707106,0,-0.707106);
                	 //                	 velocity.x = walkvelocity;
                	 //                	 force.x = walkimpulse;
                	 //                 } else if (kb.isDown(Key.A) && kb.isDown(Key.W)) {
                	 //                	 movedirection.assign(0.707106,0,0.707106);
                	 //                	 velocity.x = walkvelocity;
                	 //                	 force.x = walkimpulse;
                	 //                 }
                	 //                 } else if (kb.isDown(Key.W)) {
                	 ////                	 System.out.println("W");
                	 //                	 movedirection.assign(0,0,1);
                	 //                	 velocity.x = walkvelocity;
                	 //                	 force.x = walkimpulse;
                	 //                 } else if (kb.isDown(Key.D)) {
                	 //                	 movedirection.assign(-1,0,0);
                	 //                	 velocity.x = walkvelocity;
                	 //                	 force.x = walkimpulse;
                	 //                 } else if (kb.isDown(Key.S)) {
                	 //                	 movedirection.assign(0,0,-1);
                	 //                	 velocity.x = walkvelocity;
                	 //                	 force.x = walkimpulse;
                	 //                 } else if (kb.isDown(Key.A)) {
                	 //                	 movedirection.assign(1,0,0);
                	 //                	 velocity.x = walkvelocity;
                	 //                	 force.x = walkimpulse;
                	 //                 }  

                	 if (kb.isDown(Key.SPACE)) {
                		 if (!jumped && contacts > 0 && !jumpedinframe ) {
                			 jumped = true;
                			 jumpedinframe = true;
                			 jumpdirection.assign(0,1,0);

                			 boolean canjump = false;
                			 // check for a "jumpable" contact normal
                			 for ( ContactConstraint contact: contactconstraints) {
                				 // get contact generators (geometry pairs)
                				 Iterator<ContactGenerator> cgs = contact.getGenerators();
                				 while (cgs.hasNext()) {
                					 // get contact points
                					 Iterator<ContactPoint> cps = cgs.next().getContacts();
                					 if (cps.hasNext()) {	 
                						 // take the normal from the first contact point (because we expect all normals to be the same)
                						 Vector3 normal = new Vector3(cps.next().normal);

                						 // turn normal the right way around
                						 if ( contact.getBodies().getSecond() == playerbody )
                							 Vector3.multiply(normal,-1);

                						 if (jumpdirection.dot(normal) > 0.75) {
                							 canjump = true;
                						 }
                					 }
                				 }
                			 } // for contactconstraints

                			 if ( canjump) {
                				 // impulse.setMagnitude(7);
                				 velocity.y=5;
                				 force.y = 20;
                			 }
                		 }
                	 } else { // not pressed space
                		 jumped = false;
                	 }
                 }
                // store this kb state
             	previouskbstate = kb;
            } // void perform
        };
        
        // install trigger
        InputTrigger mytrigger = new InputTrigger(keysHeld, moveAction);
        game.getRendering().getLogicalLayer().registerTrigger(mytrigger);
        
	}

	@Override
	public void stop(Game game) {

	}

	@Override
	public Body getBodyFromNode(Node node) {
		return playerbody;
	}
}
