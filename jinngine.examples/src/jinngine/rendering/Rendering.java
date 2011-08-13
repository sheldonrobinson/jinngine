/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or <http://code.google.com/p/jinngine/>.
 */

package jinngine.rendering;

import java.awt.Canvas;

import javax.media.opengl.GL;

import jinngine.geometry.Geometry;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public interface Rendering {
    public interface Callback {
        public void tick();
    }

    // interface for objects to be drawn
    public interface DrawShape {
        public void init(GL gl);

        public void preDisplay(GL gl);

        public int getDisplayList();

        public int getShadowDisplayList();

        public void getTransform(Matrix4 T);

        public Body getReferenceBody();
    }

    public void takeScreenShot(String filename);

    public void createWindow();

    public Canvas getCanvas();

    public interface EventCallback {
        public void mousePressed(double x, double y, Vector3 point, Vector3 direction);

        public void mouseDragged(double x, double y, Vector3 point, Vector3 direction);

        public void mouseReleased();

        public void spacePressed();

        public void spaceReleased();

        public void enterPressed();

        public void keyPressed(char key);

        public void keyReleased(char key);

    }

    public void addCallback(EventCallback c);

    public DrawShape drawMe(Geometry g);

    public void drawMe(DrawShape shape, Geometry g);

    public void start();

}
