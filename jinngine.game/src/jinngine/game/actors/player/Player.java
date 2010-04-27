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
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.export.xml.XMLExporter;
import com.ardor3d.util.geom.SceneCopier;
import com.ardor3d.util.geom.SharedCopyLogic;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.google.common.base.Predicate;


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
	private Constraint controlconstraint;
	private double jumpable = 0;
	
	
	
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
		if (jumpable > 0)
			jumpable--;
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
        box.setRestitution(0.00);
        //box.setFrictionCoefficient(1);
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
                
        // create control velocity constraint
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
			@Override
			public void getNcpConstraints(ListIterator<constraint> constraints) {
				constraints.add(c);
				constraints.add(c2);
			}
        };
        
        game.getPhysics().addConstraint(controlconstraint);
        game.getPhysics().addLiveConstraint(controlconstraint);
        
        
        // tangential movement vectors
//        final Vector3 movementx = new Vector3(0,0,1);
//        final Vector3 movementy = new Vector3(-1,0,0);

        //debug mesh
//        Toolbox.addJinngineDebugMesh("playerdebugmesh", body, playerbody);

        
        
        // WASD control ( more or less the same as in the ardor3d examples)
        final Predicate<TwoInputStates> keysHeld = new Predicate<TwoInputStates>() {
            Key[] keys = new Key[] { Key.W, Key.A, Key.S, Key.D, Key.SPACE };

            public boolean apply(final TwoInputStates states) {
                for (final Key k : keys) {
                    if (states.getCurrent() != null && states.getCurrent().getKeyboardState().isDown(k)) {
                        return true;
                    }
                }
                return true;
            }
        };

        final TriggerAction moveAction = new TriggerAction() {
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                 KeyboardState kb = inputStates.getCurrent().getKeyboardState();
                 
//                 System.out.println("Key action");
                 //handle keys
                 // MOVEMENT
                 
                 velocity.assignZero();
                 force.assignZero();
                 
                 if (kb.isDown(Key.W)) {
             		velocity.z = walkvelocity;
            		force.z = 1;
                 }
                 if (kb.isDown(Key.S)) {
             		velocity.z = -walkvelocity;
            		force.z = -1;
                 }
                 if (kb.isDown(Key.A)) {
             		velocity.x = walkvelocity;
            		force.x = 1;
                 }
                 if (kb.isDown(Key.D)) {
             		velocity.x = -walkvelocity;
            		force.x = -1;
                 }  
                 
                 if (kb.isDown(Key.SPACE)) {
                	 if (jumpable == 0) {
                		 jumpable = jumpable + 100;
                		 impulse.setMagnitude(7);
                	 }
                 }
            }
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
