# Real time lightweight 3d physics engine written in Java
Jinngine is aimed at real-time capabilities. The user can setup and simulate physical configurations, by calling API functions to specify geometries, joints, and parameters.

The engine is build around a velocity based complementarity formulation, which is solved with a simple NCP solver. Friction is modeled using a simple approximation of the Coloumb law of friction. These techniques are state of the art and widely used in other engines such as ODE and Bullet.

Jinngine is purely a physics library/engine. This means that the focus is solely on physics and contact modelling, etc. There is no rendering features in Jinngine. However, you should be able to easily incorporate Jinngine into whatever 3d environment you are using. The examples code use jogl(1.1.1) for visualisation, but there is no dependence on jogl in Jinngine itself.

You can use jinngine as a physics engine, or you can use parts of the engine as a library, for instance collision detection or some of the graph-utils. You can also use the contact point generation features if that is what you need.
