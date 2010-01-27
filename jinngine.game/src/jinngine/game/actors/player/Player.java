package jinngine.game.actors.player;

import java.io.IOException;

import com.ardor3d.extension.effect.water.WaterNode;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
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
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.force.GravityForce;

public class Player implements PhysicalActor {

	private Body righthandbody;
	
	@Override
	public void act(Game game) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Game game) {
		PhysicsScene physics = game.getPhysics();
		
        // body
		final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage ballstorage = colladaImporter.readColladaFile("ball.dae");
        final Node body = ballstorage.getScene();

        // textures for ball
        TextureState ts = new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load("balllowtex.tga", Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true),0);

        ts.setTexture(TextureManager.load("balllownormalmap.tga", Texture.MinificationFilter.BilinearNoMipMaps,
                Format.GuessNoCompression, true),1);
        body.setRenderState(ts);
        body.setScale(0.25);

        righthandbody = new Body(new jinngine.geometry.Sphere(0.25));
        righthandbody.setPosition(new jinngine.math.Vector3(2.5,-5-25,2));
        physics.addBody(righthandbody);
        physics.addForce(new GravityForce(righthandbody));
               
        body.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	Body body = righthandbody;
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
	}

	@Override
	public void stop(Game game) {
		// TODO Auto-generated method stub

	}

	@Override
	public Body getBodyFromNode(Node node) {
		return righthandbody;
	}

}
