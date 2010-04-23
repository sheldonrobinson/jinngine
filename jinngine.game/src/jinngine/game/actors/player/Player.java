package jinngine.game.actors.player;

import java.util.*;
import java.io.File;
import java.io.IOException;

import com.ardor3d.extension.effect.water.WaterNode;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.framework.Canvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.input.Key;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyQuaternion;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Renderable;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.export.xml.XMLExporter;
import com.ardor3d.util.geom.SceneCopier;
import com.ardor3d.util.geom.SharedCopyLogic;

import jinngine.game.Game;
import jinngine.game.Toolbox;
import jinngine.game.actors.Actor;
import jinngine.game.actors.PhysicalActor;
import jinngine.geometry.contact.ContactGenerator;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.contact.ContactConstraint;
import jinngine.physics.constraint.contact.ContactConstraintCreator;
import jinngine.physics.constraint.contact.FrictionalContactConstraint;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.force.Force;
import jinngine.physics.force.GravityForce;
import jinngine.physics.force.ImpulseForce;
import jinngine.physics.solver.Solver.constraint;
import jinngine.util.Pair;

public class Player extends Node implements PhysicalActor {

	private Body playerbody;
	private final List<FrictionalContactConstraint> constraints = new ArrayList<FrictionalContactConstraint>();
	private boolean keywstate = false;
	private double movespeed = 1.5;
	private double tangentforce = 5.2;
	
	private Constraint controlconstraint;
	
	@Override
	public void write(Ardor3DExporter e) throws IOException {
		// TODO Auto-generated method stub
		super.write(e);
		e.getCapsule(this);				
	}
	
	
	@Override
	public void create(Game game) {
		this.setName("Actor:Player");

		final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.readColladaFile("girl.dae");
        final Node body = storage.getScene();
        body.setScale(1);
        body.setName("playermainbody");
        TextureState headts = new TextureState();
        headts.setEnabled(true);
        headts.setTexture(TextureManager.load("girltexlow.tga", 
        		Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true),0);
        headts.setTexture(TextureManager.load("girlnormallow.tga", 
        		Texture.MinificationFilter.BilinearNoMipMaps,
                Format.GuessNoCompression, true),1);
        body.setRenderState(headts);
        

        final GLSLShaderObjectsState shader = new GLSLShaderObjectsState();
        //load shaders
        try {
          shader.setVertexShader(WaterNode.class.getClassLoader().getResourceAsStream("jinngine/game/resources/bumbshader.vert"));
          shader.setFragmentShader(WaterNode.class.getClassLoader().getResourceAsStream("jinngine/game/resources/bumbshader.frag"));
      } catch (final IOException e) {
//  	  logger.log(Level.WARNING, "Error loading shader", e);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void start(final Game game) {
		Scene physics = game.getPhysics();
		
        Node body = (Node)getChild("playermainbody");
        
        body.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	Body body = playerbody;
            	caller.setTranslation(body.state.position.x, body.state.position.y, body.state.position.z);
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
        jinngine.geometry.Box box = new jinngine.geometry.Box(1,1,1);
        box.setRestitution(0.20);
        box.setFrictionCoefficient(1);
        playerbody = new Body("default", box);
        Toolbox.setTransformFromNode(body, playerbody);
        physics.addBody(playerbody);
        physics.addForce(new GravityForce(playerbody));
        
        final ImpulseForce impulse = new ImpulseForce(playerbody, new Vector3(), new Vector3(0,1,0), 0);
        game.getPhysics().addForce(impulse);
        
        final Body dummy = new Body("default");
        dummy.setFixed(true);
        
        final double walkvelocity = 2;
        final Vector3 velocity = new Vector3();
        final Vector3 force = new Vector3();
                
        // create control constraint
        controlconstraint = new Constraint() {       	
        	final constraint c = new constraint();       	
        	final constraint c2 = new constraint();       	
        	
        	
        	@Override
        	public void applyConstraints(ListIterator<constraint> iterator,
        			double dt) {
        		// -(unf-uni)  -correction 
        		c.assign(playerbody, dummy,
        				new Vector3(1,0,0), new Vector3(), new Vector3(), new Vector3(),new Vector3(1,0,0), new Vector3(), new Vector3(), new Vector3(), Math.min(force.x,0), Math.max(0,force.x), null, playerbody.state.velocity.x-velocity.x, 0  );
        		iterator.add(c);

        		c2.assign(playerbody, dummy,
        				new Vector3(0,0,1), new Vector3(), new Vector3(), new Vector3(),new Vector3(0,0,1), new Vector3(), new Vector3(), new Vector3(),Math.min(force.z,0), Math.max(0,force.z), null, playerbody.state.velocity.z-velocity.z, 0  );
        		iterator.add(c2);

        		
        	}
        	@Override
        	public Pair<Body> getBodies() {
        		return new Pair<Body>(playerbody,dummy);
        	}
        };
        
        game.getPhysics().addConstraint(controlconstraint);
        game.getPhysics().liveconstraints.add(controlconstraint);
        
        
        // tangential movement vectors
        final Vector3 movementx = new Vector3(0,0,1);
        final Vector3 movementy = new Vector3(-1,0,0);

        //debug mesh
        Toolbox.addJinngineDebugMesh("playerdebugmesh", body, playerbody);

        
        // create a contact constraint creator, to book-keep the contact constraints
        // generated by the base body. 
//        ContactConstraintCreator creator = new ContactConstraintCreator() {
//        	@Override
//        	public ContactConstraint createContactConstraint(Body b1, Body b2,
//        			ContactGenerator g) {
//        		
//        		if ( b1 == playerbody ) {
//            		System.out.println("Player: add");
//        			FrictionalContactConstraint constraint = new FrictionalContactConstraint(b1,b2,g,this);
//        			constraints.add(constraint);
//        			return constraint;
//        		} else if ( b2 == playerbody)  {
//        			
//        			FrictionalContactConstraint constraint = new FrictionalContactConstraint(b1,b2,g,this);
//        			constraint.setTangentialVelocityMultiplier(-1);
//        			constraints.add(constraint);
//        			return constraint;
//        			
//        			
//        		} else {
//        			return null;
//        		}
//        		
//        	}
//        	@Override
//        	public void removeContactConstraint(ContactConstraint constraint) {
//        		System.out.println("Player: remove");
//        		constraints.remove(constraint);
//        	}
//        };
//        
//        // install creator
//        game.getPhysics().addContactConstraintCreator(creator);
       
        //install some key controls
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.SPACE), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
//        		System.out.println("space pressed");
        		impulse.setMagnitude(6.520);
        		
        	}
        }));

        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.W), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
//        		System.out.println("w pressed");
        		
//        		for (FrictionalContactConstraint f: constraints) {
//    				f.setTangentialVelocityX(movementx.multiply(movespeed));
//    				f.setCouplingEnabled(false);
//    				f.setFixedFrictionBoundsMagnitude(tangentforce);
//        		}

        		velocity.z = walkvelocity;
        		force.z = 1;

        		
        		keywstate = true;
        	}
        }));
        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyReleasedCondition(Key.W), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
//        		System.out.println("w released");
        		
//        		for (FrictionalContactConstraint f: constraints) {
//    				f.setTangentialVelocityX(movementx.multiply(0));
//    				f.setCouplingEnabled(true);
//    				f.setFixedFrictionBoundsMagnitude(Double.POSITIVE_INFINITY);
//        		}
        		
        		velocity.z = 0;
        		force.z = 0;
        		
        		keywstate = false;
        		
        	}
        }));

        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.S), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
//        		System.out.println("s pressed");
        		
//        		for (FrictionalContactConstraint f: constraints) {
//    				f.setTangentialVelocityX(movementx.multiply(-movespeed));
//    				//f.setCouplingEnabled(false);
//    				f.setFixedFrictionBoundsMagnitude(tangentforce);
//        		}
        		
        		velocity.z = -walkvelocity;
        		force.z = -1;
        		keywstate = true;
        	}
        }));
        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyReleasedCondition(Key.S), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
//        		System.out.println("s released");
        		
//        		for (FrictionalContactConstraint f: constraints) {
//    				f.setTangentialVelocityX(movementx.multiply(0));
////    				f.setCouplingEnabled(true);
//    				f.setFixedFrictionBoundsMagnitude(Double.POSITIVE_INFINITY);
//        		}
        		 
        		velocity.z = 0;
        		force.z = 0;
        		keywstate = false;
        		
        	}
        }));

        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.D), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
//        		System.out.println("d pressed");
        		
//        		for (FrictionalContactConstraint f: constraints) {
//    				f.setTangentialVelocityY(movementy.multiply(movespeed));
////    				f.setCouplingEnabled(false);
//    				f.setFixedFrictionBoundsMagnitude(tangentforce);
//        		}
        		
        		velocity.x = -walkvelocity;
        		force.x = -1;
        		
        		keywstate = true;
        	}
        }));
        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyReleasedCondition(Key.D), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
//        		System.out.println("d released");
        		
//        		for (FrictionalContactConstraint f: constraints) {
//    				f.setTangentialVelocityY(movementy.multiply(0));
////    				f.setCouplingEnabled(true);
//    				f.setFixedFrictionBoundsMagnitude(Double.POSITIVE_INFINITY);
        		
//        		}
        		
        		velocity.x = 0;
        		force.x = 0;
        		
        		keywstate = false;
        		
        	}
        }));

        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.A), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
//        		System.out.println("d pressed");
        		
//        		for (FrictionalContactConstraint f: constraints) {
//    				f.setTangentialVelocityY(movementy.multiply(-movespeed));
////    				f.setCouplingEnabled(false);
//    				f.setFixedFrictionBoundsMagnitude(tangentforce);
//        		}
        		velocity.x = walkvelocity;
        		force.x = 1;
        		keywstate = true;
        	}
        }));
        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyReleasedCondition(Key.A), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
//        		System.out.println("d released");
        		
//        		for (FrictionalContactConstraint f: constraints) {
//    				f.setTangentialVelocityY(movementy.multiply(0));
////    				f.setCouplingEnabled(true);
//    				f.setFixedFrictionBoundsMagnitude(Double.POSITIVE_INFINITY);
//        		}
        		velocity.x = 0;
        		force.x = 0;
        		
        		keywstate = false;
        		
        	}
        }));
        
        
        
        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.RETURN), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("Writing scene graph");
        		
                try {
                	BinaryExporter.getInstance().save( game.getRendering().getRootNode(), new File("testgraph.xml"));
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        	
        	}
        }));

        
	}
	
	

	@Override
	public void stop(Game game) {
		// TODO Auto-generated method stub

	}

	@Override
	public Body getBodyFromNode(Node node) {
		return playerbody;
	}

}
