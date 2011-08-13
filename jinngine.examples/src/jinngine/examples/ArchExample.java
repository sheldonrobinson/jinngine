
package jinngine.examples;

import java.util.ArrayList;
import java.util.List;

import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.geometry.ConvexHull;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.DefaultScene;
import jinngine.physics.DisabledDeactivationPolicy;
import jinngine.physics.Scene;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;

public class ArchExample implements Rendering.Callback {
    private final Scene scene;

    public ArchExample() {
        // start jinngine
        this.scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(15),
                new DisabledDeactivationPolicy());
        this.scene.setTimestep(0.1);

        // add boxes to bound the world
        //        final Box floor = new Box("floor", 1500, 20, 1500);
        //        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, -30, 0), floor);
        ////        floor.setEnvelope(5);
        //        this.scene.fixBody(floor.getBody(), true);
        //
        //        final Box back = new Box("back", 200, 200, 20);
        //        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -55), back);
        //        this.scene.fixBody(back.getBody(), true);
        //
        //        final Box front = new Box("front", 200, 200, 20);
        //        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -7), front);
        //        this.scene.fixBody(front.getBody(), true);
        //
        //        final Box left = new Box("left", 20, 200, 200);
        //        this.scene.addGeometry(Matrix3.identity(), new Vector3(-35, 0, 0), left);
        //        this.scene.fixBody(left.getBody(), true);
        //
        //        final Box right = new Box("right", 20, 200, 200);
        //        this.scene.addGeometry(Matrix3.identity(), new Vector3(10, 0, 0), right);
        //        this.scene.fixBody(right.getBody(), true);

        // gravity absorbing body
        final Body gravity = new Body("gravity");
        this.scene.addBody(gravity);
        this.scene.fixBody(gravity, true);

        // handle drawing
        final Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
        rendering.addCallback(new Interaction(this.scene));

        // make arch
        //    	int N = 25;
        //    	int M = 25;
        //    	double theta = -Math.PI/2;
        //    	double dtheta = Math.PI/N;
        //    	double dthetaOffset1 = dtheta*0.855;
        //    	double dthetaOffset2 = dtheta*0.912;

        // second config
        final int N = 15;
        final int M = 15;
        double theta = -Math.PI / 2;
        final double dtheta = Math.PI / N;
        final double dthetaOffset1 = dtheta * 0.94;
        final double dthetaOffset2 = dtheta * 0.97;

        for (int j = 0; j < M; j++) {
            final double xp = -12;
            final double yp = -19.97;
            final double r1 = 6.5;
            final double r2 = 11.0;
            final double zoff = j % 2 * 0.5 * 0;

            final double x11 = Math.sin(theta) * r1 + xp;
            final double y11 = Math.cos(theta) * r1 + yp;
            final double x12 = Math.sin(theta) * r2 + xp;
            final double y12 = Math.cos(theta) * r2 + yp;

            final double x21 = Math.sin(theta + dthetaOffset1) * r1 + xp;
            final double y21 = Math.cos(theta + dthetaOffset1) * r1 + yp;
            final double x22 = Math.sin(theta + dthetaOffset2) * r2 + xp;
            final double y22 = Math.cos(theta + dthetaOffset2) * r2 + yp;

            final List<Vector3> list = new ArrayList<Vector3>();
            //front side
            list.add(new Vector3(x11, y11, -24 + zoff));
            list.add(new Vector3(x12, y12, -24 + zoff));
            list.add(new Vector3(x21, y21, -24 + zoff));
            list.add(new Vector3(x22, y22, -24 + zoff));

            list.add(new Vector3(x11, y11, -21 + zoff));
            list.add(new Vector3(x12, y12, -21 + zoff));
            list.add(new Vector3(x21, y21, -21 + zoff));
            list.add(new Vector3(x22, y22, -21 + zoff));

            final ConvexHull g = new ConvexHull("brick", list, 0.0);
            g.setEnvelope(0.120);
            g.setRestitution(0.5);

            this.scene.addGeometry(Matrix3.identity(), new Vector3(), g);
            this.scene.addConstraint(new GravityForce(g.getBody(), gravity));
            rendering.drawMe(g);

            theta += dtheta;

        }

        // add boxes to bound the world
        final Box floor = new Box("floor", 1500, 20, 1500);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, -30, 0), floor);
        this.scene.fixBody(floor.getBody(), true);

        rendering.createWindow();
        rendering.start();

    }

    @Override
    public void tick() {
        // each frame, to a time step on the Scene
        this.scene.tick();
    }

    public static void main(final String[] args) {
        new ArchExample();
    }

}
