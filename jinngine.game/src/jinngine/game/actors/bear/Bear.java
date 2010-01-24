package jinngine.game.actors.bear;

import java.io.IOException;

import com.ardor3d.extension.effect.water.WaterNode;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.math.Matrix3;
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
import jinngine.game.Actor;
import jinngine.math.*;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.force.GravityForce;

public class Bear implements Actor {

	public  Body bodyhead;
	
	@Override
	public void act( Game game ) {
		// TODO Auto-generated method stub
	}

	@Override
	public void start( Game game) {
		final PhysicsScene physics = game.getPhysics();
		final Node rootnode = game.getRendering().getRootNode();

		final GLSLShaderObjectsState shader = new GLSLShaderObjectsState();

		// head
		final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.readColladaFile("bearface.dae");
        final Node head = storage.getScene();
        head.setScale(0.75);

        // textures for bear head
        TextureState headts = new TextureState();
        headts.setEnabled(true);
        headts.setTexture(TextureManager.load("bearlowtex.tga", 
        		Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true),0);
        headts.setTexture(TextureManager.load("bearlowpolynormalmap.tga", 
        		Texture.MinificationFilter.BilinearNoMipMaps,
                Format.GuessNoCompression, true),1);
        head.setRenderState(headts);


        bodyhead = new Body(new jinngine.geometry.Box(1,1,1));
        physics.addBody(bodyhead);
        physics.addForce(new GravityForce(bodyhead));
        bodyhead.setPosition(new jinngine.math.Vector3(2,3.5-25,2));
        
        head.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	caller.setTranslation(bodyhead.state.position.x, bodyhead.state.position.y, bodyhead.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(bodyhead.state.rotation.a11, bodyhead.state.rotation.a12, bodyhead.state.rotation.a13,
            			bodyhead.state.rotation.a21, bodyhead.state.rotation.a22, bodyhead.state.rotation.a23, 
            			bodyhead.state.rotation.a31, bodyhead.state.rotation.a32, bodyhead.state.rotation.a33);
            	
            	caller.setRotation(mat);
            }
        });

        // body
        final ColladaStorage ballstorage = colladaImporter.readColladaFile("ball.dae");
        final Node body = ballstorage.getScene();
        body.setScale(0.35);
        
        final Body bodybody = new Body(new jinngine.geometry.Box(1,1,1));
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
                Format.GuessNoCompression, true),0);

        ts.setTexture(TextureManager.load("balllownormalmap.tga", Texture.MinificationFilter.BilinearNoMipMaps,
                Format.GuessNoCompression, true),1);
        body.setRenderState(ts);

        HingeJoint joint = new HingeJoint(bodyhead,bodybody, new jinngine.math.Vector3(2,3.25-25,2), new jinngine.math.Vector3(1,0,0));
        joint.getHingeControler().setLimits(-0.25, 0.25);
        physics.addConstraint(joint);

        Node righthand = (Node) SceneCopier.makeCopy(body, new SharedCopyLogic());
        righthand.setScale(0.15);
        final Body righthandbody = new Body(new jinngine.geometry.Box(0.15,0.15,0.15));
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
        
	}

	@Override
	public void stop(Game game) {
		// TODO Auto-generated method stub

	}

}
