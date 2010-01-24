package jinngine.game.actors.platform1;

import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

import jinngine.game.Actor;
import jinngine.game.Game;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.force.GravityForce;

public class Platform1 implements Actor {

	public Body platformbox1body;
	private static jinngine.math.Vector3 pos = new jinngine.math.Vector3();
	
	public Platform1(jinngine.math.Vector3 pos) {
		this.pos.assign(pos);
	}
	
	@Override
	public void act( Game game ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start( Game game ) {
		PhysicsScene physics = game.getPhysics();
		Node rootnode = game.getRendering().getRootNode();

		
		final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.readColladaFile("platformbox1.dae");
        final Node platformbox1 = storage.getScene();
        platformbox1.setScale(0.75);
        platformbox1.setTranslation(new Vector3(0,-25,0));
        platformbox1.setScale(0.5);
        rootnode.attachChild(platformbox1);
        
        // define some light
        final DirectionalLight light = new DirectionalLight();
        light.setDirection(0.5, 1, 0);
        light.setDiffuse(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setAmbient(new ColorRGBA(1.9f, 1.9f, 1.9f, 1.0f));
        light.setSpecular(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f));
        light.setAttenuate(false);
        light.setEnabled(true);
        
        final LightState ls = new LightState();
        ls.attach(light);
        ls.setEnabled(true);
        platformbox1.setRenderState(ls);
        
        platformbox1body = new Body(new jinngine.geometry.Box(0.5,0.5,0.5));
        physics.addBody(platformbox1body);
        physics.addForce(new GravityForce(platformbox1body));
        platformbox1body.setPosition(pos);
        platformbox1body.setAngularVelocity(new jinngine.math.Vector3(0,0.25,0));
               
        platformbox1.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	Body body = platformbox1body;
            	caller.setTranslation(body.state.position.x, body.state.position.y, body.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(body.state.rotation.a11, body.state.rotation.a12, body.state.rotation.a13,
            			body.state.rotation.a21, body.state.rotation.a22, body.state.rotation.a23, 
            			body.state.rotation.a31, body.state.rotation.a32, body.state.rotation.a33);
            	
            	caller.setRotation(mat);
            }
        });
	}

	@Override
	public void stop( Game game ) {
		// TODO Auto-generated method stub

	}

}
