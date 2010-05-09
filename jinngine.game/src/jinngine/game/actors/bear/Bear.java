package jinngine.game.actors.bear;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ardor3d.extension.effect.water.WaterNode;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.SceneCopier;
import com.ardor3d.util.geom.SharedCopyLogic;

import jinngine.game.*;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.CommandableActor;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.SelectableActor;
import jinngine.game.actors.interaction.BodyPlacement;
import jinngine.math.*;
import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.force.GravityForce;
import jinngine.physics.force.SpringForce;

public class Bear implements CommandableActor, SelectableActor {

	private final Map<Node,Body> nodebodymap = new LinkedHashMap<Node, Body>(); 
	private final jinngine.math.Vector3 targetpos = new jinngine.math.Vector3();
	private Node head;
	public  Body bodyhead;
	
	private final Body gotocontroller = new Body("default");
	
	
	@Override
	public void create(Game game) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void act( Game game ) {
		gotocontroller.setPosition(targetpos);
	}

	@Override
	public void start( Game game) {
		final Scene physics = game.getPhysics();
		final Node rootnode = game.getRendering().getScene();
		final GLSLShaderObjectsState shader = new GLSLShaderObjectsState();

		// head
		final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.load("bearface.dae");
        head = storage.getScene();
        head.setScale(0.75);

        // textures for bear head
        TextureState headts = new TextureState();
        headts.setEnabled(true);
        headts.setTexture(TextureManager.load("bearlowtex.tga", 
        		Texture.MinificationFilter.Trilinear,
        		TextureStoreFormat.GuessCompressedFormat, true),0);
        headts.setTexture(TextureManager.load("bearlowpolynormalmap.tga", 
        		Texture.MinificationFilter.BilinearNoMipMaps,
        		TextureStoreFormat.GuessCompressedFormat, true),1);
        head.setRenderState(headts);

        // create the physics for the head
        bodyhead = new Body("default", new jinngine.geometry.Box(1,1,1));
        physics.addBody(bodyhead);
        physics.addForce(new GravityForce(bodyhead));
        bodyhead.setPosition(new jinngine.math.Vector3(2,3.5-25,2));
        
        // connect position update for head
        head.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	caller.setTranslation(bodyhead.state.position.x, bodyhead.state.position.y, bodyhead.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(bodyhead.state.rotation.a11, bodyhead.state.rotation.a12, bodyhead.state.rotation.a13,
            			bodyhead.state.rotation.a21, bodyhead.state.rotation.a22, bodyhead.state.rotation.a23, 
            			bodyhead.state.rotation.a31, bodyhead.state.rotation.a32, bodyhead.state.rotation.a33);
            	
            	caller.setRotation(mat);
            }
        });
        
        // register Node - Body connection in map
        nodebodymap.put( head, bodyhead );
        
        // register this actor in the head node
        head.setUserData(this);

        // body
        final ColladaStorage ballstorage = colladaImporter.load("ball.dae");
        final Node body = ballstorage.getScene();
        body.setScale(0.35);
        
        final Body bodybody = new Body("default", new jinngine.geometry.Box(1,1,1));
        physics.addBody(bodybody);
        physics.addForce(new GravityForce(bodybody));
        bodybody.setPosition(new jinngine.math.Vector3(2,3-25,2));
               
        body.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	Body body = bodybody;
            	caller.setTranslation(body.state.position.x, body.state.position.y, body.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(body.state.rotation.a11, body.state.rotation.a12, body.state.rotation.a13,
            			body.state.rotation.a21, body.state.rotation.a22, body.state.rotation.a23, 
            			body.state.rotation.a31, body.state.rotation.a32, body.state.rotation.a33);
            	
            	caller.setRotation(mat);
            }
        });

        // textures for ball
        TextureState ts = new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load("balllowtex.tga", Texture.MinificationFilter.Trilinear,
        		TextureStoreFormat.GuessCompressedFormat, true),0);

        ts.setTexture(TextureManager.load("balllownormalmap.tga", Texture.MinificationFilter.BilinearNoMipMaps,
        		TextureStoreFormat.GuessCompressedFormat, true),1);
        body.setRenderState(ts);

        HingeJoint joint = new HingeJoint(bodyhead,bodybody, new jinngine.math.Vector3(2,3.25-25,2), new jinngine.math.Vector3(1,0,0));
        joint.getHingeControler().setLimits(-0.25, 0.25);
        physics.addConstraint(joint);

        Node righthand = (Node) SceneCopier.makeCopy(body, new SharedCopyLogic());
        righthand.setScale(0.15);
        final Body righthandbody = new Body("default", new jinngine.geometry.Box(0.15,0.15,0.15));
        righthandbody.setPosition(new jinngine.math.Vector3(2.5,3-25,2));
        physics.addBody(righthandbody);
        physics.addForce(new GravityForce(righthandbody));
               
        righthand.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	Body body = righthandbody;
            	caller.setTranslation(body.state.position.x, body.state.position.y, body.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(body.state.rotation.a11, body.state.rotation.a12, body.state.rotation.a13,
            			body.state.rotation.a21, body.state.rotation.a22, body.state.rotation.a23, 
            			body.state.rotation.a31, body.state.rotation.a32, body.state.rotation.a33);
            	
            	caller.setRotation(mat);
            }
        });

        HingeJoint rightarmtobodyjoint = new HingeJoint(righthandbody,bodybody, new jinngine.math.Vector3(2,3-25,2), new jinngine.math.Vector3(0,0,1));
        rightarmtobodyjoint.getHingeControler().setLimits(-0.25, 0.25);
        physics.addConstraint(rightarmtobodyjoint);
        
        
        
        
        
        
        
        
        
        
        Node lefthand  = (Node) SceneCopier.makeCopy(body, new SharedCopyLogic());

        
        
        
        //attach nodes 
        rootnode.attachChild(head);
        rootnode.attachChild(body);
        rootnode.attachChild(righthand);
//        rootnode.attachChild(leftfoot);
        

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
      
      head.setRenderState(shader);
      body.setRenderState(shader);
      righthand.setRenderState(shader);
      
      //add a primitive goto force
      gotocontroller.setFixed(true);
      game.getPhysics().addForce( new SpringForce(bodyhead, new jinngine.math.Vector3(), gotocontroller, new jinngine.math.Vector3(), 0));
        
	}

	@Override
	public void stop(Game game) {
		// TODO Auto-generated method stub

	}

	@Override
	public Body getBodyFromNode(Node node) {
	 return nodebodymap.get(node);
	}

	@Override
	public void moveToPosition(jinngine.math.Vector3 pos) {
		targetpos.assign(pos);
	}

	@Override
	public ActionActor provideActionActor(ActorOwner owner, Actor target, Node node,
			jinngine.math.Vector3 pickpoint, Vector2 screenpos ) {
		
		System.out.println("Bear: got an actor "+ target);
		
		// spawn a BodyPlacement actor if possible
		if (target instanceof PhysicalActor) {
			PhysicalActor physactor = (PhysicalActor)target;
			Body body = physactor.getBodyFromNode(node);
		
			if (body != null)
				return new BodyPlacement(owner, body,pickpoint,screenpos);
		}
		
		
		return null;
	}

	@Override
	public void setSelected(Game game, boolean selected) {
		if ( selected) 
			head.setScale(0.85);
		else
			head.setScale(0.75);

		
	}

	@Override
	public boolean canBeSelected() {
		return true;
	}

}
