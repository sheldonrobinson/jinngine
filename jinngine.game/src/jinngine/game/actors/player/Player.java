package jinngine.game.actors.player;

import java.util.*;
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
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.SceneCopier;
import com.ardor3d.util.geom.SharedCopyLogic;

import jinngine.game.Game;
import jinngine.game.actors.Actor;
import jinngine.game.actors.PhysicalActor;
import jinngine.geometry.contact.ContactGenerator;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.constraint.contact.ContactConstraint;
import jinngine.physics.constraint.contact.ContactConstraintCreator;
import jinngine.physics.constraint.contact.FrictionalContactConstraint;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.force.Force;
import jinngine.physics.force.GravityForce;
import jinngine.physics.force.ImpulseForce;

public class Player implements PhysicalActor {

	private Body playerbody;
	private final List<FrictionalContactConstraint> constraints = new ArrayList<FrictionalContactConstraint>();
	
	private boolean keywstate = false;
	private double movespeed = 1.5;
	private double tangentforce = 5;
	
	@Override
	public void act(Game game) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(final Game game) {
		PhysicsScene physics = game.getPhysics();
		
		// Graphics		
        // body
//		final ColladaImporter colladaImporter = new ColladaImporter();
//        final ColladaStorage ballstorage = colladaImporter.readColladaFile("ball.dae");
//        final Node body = ballstorage.getScene();
//
//        // textures for ball
//        TextureState ts = new TextureState();
//        ts.setEnabled(true);
//        ts.setTexture(TextureManager.load("balllowtex.tga", Texture.MinificationFilter.Trilinear,
//                Format.GuessNoCompression, true),0);
//
//        ts.setTexture(TextureManager.load("balllownormalmap.tga", Texture.MinificationFilter.BilinearNoMipMaps,
//                Format.GuessNoCompression, true),1);
//        body.setRenderState(ts);
//        body.setScale(0.5);
               
		final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.readColladaFile("bearface.dae");
        final Node body = storage.getScene();
        body.setScale(0.75);
        TextureState headts = new TextureState();
        headts.setEnabled(true);
        headts.setTexture(TextureManager.load("bearlowtex.tga", 
        		Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true),0);
        headts.setTexture(TextureManager.load("bearlowpolynormalmap.tga", 
        		Texture.MinificationFilter.BilinearNoMipMaps,
                Format.GuessNoCompression, true),1);
        body.setRenderState(headts);
        
        
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
        
        final GLSLShaderObjectsState shader = new GLSLShaderObjectsState();
        //load shaders
        try {
          shader.setVertexShader(WaterNode.class.getClassLoader()
                  .getResourceAsStream("jinngine/game/resources/bumbshader.vert"));
          shader.setFragmentShader(WaterNode.class.getClassLoader()
        		  .getResourceAsStream("jinngine/game/resources/bumbshader.frag"));
      } catch (final IOException e) {
//  	  logger.log(Level.WARNING, "Error loading shader", e);
    	  e.printStackTrace();
          return;
      }
      shader.setUniform("texture0", 0);
      shader.setUniform("normalmap", 1);      
      body.setRenderState(shader);
        
        game.getRendering().getRootNode().attachChild(body);
        
        // Physics      
        jinngine.geometry.Box box = new jinngine.geometry.Box(1,1,1);
        box.setRestitution(0.20);
        box.setFrictionCoefficient(999);
        playerbody = new Body(box);
        playerbody.setPosition(new jinngine.math.Vector3(2.5,2-25,2));
        physics.addBody(playerbody);
        physics.addForce(new GravityForce(playerbody));
        
        final ImpulseForce impulse = new ImpulseForce(playerbody, new Vector3(), new Vector3(0,1,0), 0);
        game.getPhysics().addForce(impulse);
        
        
        // create a contact constraint creator, to book-keep the contact constraints
        // generated by the base body. 
        ContactConstraintCreator creator = new ContactConstraintCreator() {
        	@Override
        	public ContactConstraint createContactConstraint(Body b1, Body b2,
        			ContactGenerator g) {
        		
        		if ( b1 == playerbody || b2 == playerbody) {
        			
            		System.out.println("Player: add");

        			FrictionalContactConstraint constraint = new FrictionalContactConstraint(b1,b2,g,this);
        			constraints.add(constraint);
        			
        			if (keywstate) {
        				constraint.setTangentialVelocityX(movespeed);
        				constraint.setCouplingEnabled(false);
        				constraint.setFixedFrictionBoundsMagnitude(tangentforce);
        				
        			}
        			
        			return constraint;
        		} else {
        			return null;
        		}
        		
        	}
        	@Override
        	public void removeContactConstraint(ContactConstraint constraint) {
        		System.out.println("Player: remove");
        		constraints.remove(constraint);
        	}
        };
        
        // install creator
        game.getPhysics().addContactConstraintCreator(creator);
       
        //install some key controlls
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.SPACE), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("space pressed");
        		impulse.setMagnitude(4520);
        		
        	}
        }));

        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.W), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("w pressed");
        		
        		for (FrictionalContactConstraint f: constraints) {
    				f.setTangentialVelocityX(movespeed);
    				f.setCouplingEnabled(false);
    				f.setFixedFrictionBoundsMagnitude(tangentforce);
        		}
        		
        		keywstate = true;
        	}
        }));
        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyReleasedCondition(Key.W), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("w released");
        		
        		for (FrictionalContactConstraint f: constraints) {
    				f.setTangentialVelocityX(0);
    				f.setCouplingEnabled(true);
    				f.setFixedFrictionBoundsMagnitude(Double.POSITIVE_INFINITY);
        		}
        		
        		keywstate = false;
        		
        	}
        }));

        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.S), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("s pressed");
        		
        		for (FrictionalContactConstraint f: constraints) {
    				f.setTangentialVelocityX(-movespeed);
    				f.setCouplingEnabled(false);
    				f.setFixedFrictionBoundsMagnitude(tangentforce);
        		}
        		
        		keywstate = true;
        	}
        }));
        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyReleasedCondition(Key.S), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("s released");
        		
        		for (FrictionalContactConstraint f: constraints) {
    				f.setTangentialVelocityX(0);
    				f.setCouplingEnabled(true);
    				f.setFixedFrictionBoundsMagnitude(Double.POSITIVE_INFINITY);
        		}
        		
        		keywstate = false;
        		
        	}
        }));

        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.D), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("d pressed");
        		
        		for (FrictionalContactConstraint f: constraints) {
    				f.setTangentialVelocityY(-movespeed);
    				f.setCouplingEnabled(false);
    				f.setFixedFrictionBoundsMagnitude(tangentforce);
        		}
        		
        		keywstate = true;
        	}
        }));
        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyReleasedCondition(Key.D), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("d released");
        		
        		for (FrictionalContactConstraint f: constraints) {
    				f.setTangentialVelocityY(0);
    				f.setCouplingEnabled(true);
    				f.setFixedFrictionBoundsMagnitude(Double.POSITIVE_INFINITY);
        		}
        		
        		keywstate = false;
        		
        	}
        }));

        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyPressedCondition(Key.A), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("d pressed");
        		
        		for (FrictionalContactConstraint f: constraints) {
    				f.setTangentialVelocityY(movespeed);
    				f.setCouplingEnabled(false);
    				f.setFixedFrictionBoundsMagnitude(tangentforce);
        		}
        		
        		keywstate = true;
        	}
        }));
        
        game.getRendering().getLogicalLayer()
        .registerTrigger( new InputTrigger( new KeyReleasedCondition(Key.A), new TriggerAction() {
        	@Override
        	public void perform(Canvas source, TwoInputStates inputState, double tpf) {
        		System.out.println("d released");
        		
        		for (FrictionalContactConstraint f: constraints) {
    				f.setTangentialVelocityY(0);
    				f.setCouplingEnabled(true);
    				f.setFixedFrictionBoundsMagnitude(Double.POSITIVE_INFINITY);
        		}
        		
        		keywstate = false;
        		
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
