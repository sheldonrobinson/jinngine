package jinngine.collision;

import java.util.*;

import jinngine.geometry.AxisAlignedBoundingBox;
import jinngine.geometry.Geometry;
import jinngine.math.Vector3;
import jinngine.util.Pair;

/**
 * A naive broad-phase collision detection algorithm. AllPairsTest obviously just performs an
 * overlap test on each possible pair in the considered configuration. Despite its O(n^2) complexity, 
 * the method sometimes out-performs more advanced implementations on configurations with few objects.
 * In addition, all pairs test is often useful for test purposes.
 * @author Silcowitz
 *
 */
public class AllPairsTest implements BroadfaseCollisionDetection {
	
	private final Set<Pair<Geometry>> existingPairs = new HashSet<Pair<Geometry>>();
	private final Set<Pair<Geometry>> leavingPairs = new HashSet<Pair<Geometry>>();
	private final List<Geometry> geometries = new ArrayList<Geometry>();
	private final List<BroadfaseCollisionDetection.Handler> handlers = new ArrayList<Handler>();
	
	public AllPairsTest(BroadfaseCollisionDetection.Handler handler) {
		this.handlers.add(handler);
	}

	public void run() {
		leavingPairs.addAll(existingPairs);

//		O(N^2) broad-phase collision detection
		int size = geometries.size();
		for ( int i=0; i<size; i++) {
			Geometry c1 = geometries.get(i);
			for (int j=i+1; j<size; j++ ) {
				Geometry c2 = geometries.get(j);
				if (c1 != c2 ) {
					if ( overlap(c1,c2)  ) {
						Pair<Geometry> pair = new Pair<Geometry>(c1,c2);
						
						//if we discover a new pair, report it and add to table
						if ( !existingPairs.contains(pair)) {
							existingPairs.add( new Pair<Geometry>(c1,c2) );
							
							//notify handlers
							for ( Handler handler: handlers)
								handler.overlap(pair);
						}
						
						//any pair we observe is not leaving
						leavingPairs.remove(pair);
					}
				}
			}
		}
		
		//handle disappearing pairs
		Iterator<Pair<Geometry>> leaving = leavingPairs.iterator();
		while (leaving.hasNext()) {
			Pair<Geometry> pair = leaving.next();
			
			for ( Handler handler: handlers)
				handler.separation(pair);

			existingPairs.remove(pair);
		}
		
		leavingPairs.clear();
	}
	
	private static final boolean overlap( AxisAlignedBoundingBox i , AxisAlignedBoundingBox j) {

		Vector3 iminBoundsTranslated = i.getMinBounds();
		Vector3 imaxBoundsTranslated = i.getMaxBounds();
		Vector3 jminBoundsTranslated = j.getMinBounds();
		Vector3 jmaxBoundsTranslated = j.getMaxBounds();
		
		double ixMin = iminBoundsTranslated.a1;
		double iyMin = iminBoundsTranslated.a2;
		double izMin = iminBoundsTranslated.a3;
		double ixMax = imaxBoundsTranslated.a1;
		double iyMax = imaxBoundsTranslated.a2;
		double izMax = imaxBoundsTranslated.a3;
		double jxMin = jminBoundsTranslated.a1;
		double jyMin = jminBoundsTranslated.a2;
		double jzMin = jminBoundsTranslated.a3;
		double jxMax = jmaxBoundsTranslated.a1;
		double jyMax = jmaxBoundsTranslated.a2;
		double jzMax = jmaxBoundsTranslated.a3;

		//TODO test this 
		if( (((jxMin < ixMin) && (ixMin <= jxMax)) || ((ixMin <= jxMin) && (jxMin < ixMax ))) &&
				(((jyMin < iyMin) && (iyMin <= jyMax)) || ((iyMin <= jyMin) && (jyMin < iyMax ))) &&
				(((jzMin < izMin) && (izMin <= jzMax)) || ((izMin <= jzMin) && (jzMin < izMax )))) {
			return true;
		} else {
			return true;
		}
	}

	@Override
	public void add(Geometry a) {
		geometries.add(a);
	}

	@Override
	public void remove(Geometry a) {
		geometries.remove(a);
	}

	@Override
	public void addHandler(Handler h) {
		handlers.add(h);		
	}

	@Override
	public void removeHandler(Handler h) {
		handlers.remove(h);
	}
	
	

}
