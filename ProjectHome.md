## Real time lightweight 3d physics engine written in Java ##
...  aimed at real-time capabilities. The user can setup and simulate physical configurations, by calling API functions to specify geometries, joints, and parameters.

The engine is build around a velocity based complementarity formulation, which is solved with a simple NCP solver. Friction is modeled using a simple approximation of the Coloumb law of friction. These techniques are state of the art and widely used in other engines such as ODE and Bullet.

**Jinngine** is purely a physics library/engine. This means that the focus is solely on physics and contact modelling, etc. There is no rendering features in Jinngine. However, you should be able to easily incorporate Jinngine into whatever 3d environment you are using. The examples code use jogl(1.1.1) for visualisation, but there is no dependence on jogl in Jinngine itself.

You can use jinngine as a physics engine, or you can use parts of the engine as a library, for instance collision detection or some of the graph-utils. You can also use the contact point generation features if that is what you need.

You can support jinngine development by makeing a dontation:<br>
<a href='https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=morten%40silcowitz%2edk&lc=DK&item_name=jinngine%2eorg&item_number=jinngine&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted'><img src='https://www.paypal.com/en_US/i/btn/btn_donate_LG.gif' /></a>
<hr />

YourKit is kindly supporting open source projects with its full-featured Java Profiler.<br>
YourKit, LLC is the creator of innovative and intelligent tools for profiling<br>
Java and .NET applications. Take a look at YourKit's leading software products:<br>
<a href='http://www.yourkit.com/java/profiler/index.jsp'>YourKit Java Profiler</a> and<br>
<a href='http://www.yourkit.com/.net/profiler/index.jsp'>YourKit .NET Profiler</a>.<br>
<br>
<hr />
<h2>0.8 released (28-05-2010)</h2>

- Much improved deactivation/sleeping<br>
- Contact point generation more stable and efficient<br>
- Cutting edge NNCG constraint solver (check out <a href='http://www.springerlink.com/content/e83432544t772126/'>http://www.springerlink.com/content/e83432544t772126/</a> if interested) <br>
- Sweep and prune algorithm rewritten and improved <br>
- Trigger event handling framework (only one trigger so far :) but its usefull ) <br>
- Robust support for Convex hulls, Boxes and Spheres <br>
- Many unit tests added to ensure robustness and correctness of Jinngine <br>


<hr />
<h1>Documentation, help and examples</h1>

<b>If you have any comments, suggestions or need any help, please don't hesitate use the Jinngine forum at</b> <a href='http://groups.google.com/group/jinngine'>http://groups.google.com/group/jinngine</a>

jinngine is short on documentation. However, the sub-project jinngine.examples will continuously be populated with simple examples of using jinngine. The source code for the examples are found at<br>
<br>
<a href='http://code.google.com/p/jinngine/source/browse/#svn/trunk/jinngine.examples/src/jinngine/examples'>http://code.google.com/p/jinngine/source/browse/#svn/trunk/jinngine.examples/src/jinngine/examples</a>

Further, there is the javadocs, there is a link for them in the project side bar. If you find some problem or bug, please repport it as an issue using this project page. Minor bug s are expected to be fixed quickly in the svn repo.<br>
<br>
<hr />
<h3>Contact point generation method from Jinngine on the SCA2010 conference (16-07-2010)</h3>

I traveled to Madrid to participate in ACM/SIGGRAPH SCA 2010, which is a small but really interesting and nice computer graphics conference. There was a lot of inspiring talks, and I got loads of positive feedback on the contact point generation ideas.  Hope to go next year too :)<br>
<br>
<img src='http://mo.silcowitz.dk/sca2010.jpg' />
<img src='http://mo.silcowitz.dk/poster.jpg' />
<img src='http://mo.silcowitz.dk/auditorium.jpg' />

<hr />
<b>How to contribute?</b>
I am always looking for qualified people to contribute to this project. Please contact me at morten@silcowitz.dk if you are interested<br>
<br>
<hr />
BibTex citation:<br>
<pre><code>@misc {jinngine.10, <br>
  author = {Silcowitz-Hansen, Morten},<br>
  affiliation = {University of Copenhagen, Dept. Computer Science},<br>
  title = {Jinngine, a Physics Engine Written In Java},<br>
  publisher = {Morten Silcowitz},<br>
  url = {http://code.google.com/p/jinngine},<br>
  year = {2008-2010},<br>
  abstract = { Real time lightweight 3d physics engine written in Java, aimed at <br>
real-time capabilities. The user can setup and simulate physical configurations, by<br>
calling API functions to specify geometries, joints, and parameters. The engine is build<br>
around a velocity based complementarity formulation, which is solved with a simple NCP<br>
solver. Friction is modeled using a simple approximation of the Coloumb law of friction.<br>
These techniques are state of the art and widely used in other engines such as ODE and Bullet.}<br>
}<br>
</code></pre>
<hr />
