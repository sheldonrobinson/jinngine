package jinngine.scientific;

import java.io.PrintStream;
import java.util.List;


public interface Test {

	/**
	 * Run the test
	 * @param testname TODO
	 */
	public void run( String testname, String[] configurations, PrintStream latexOut, PrintStream matlabOut );
	
}
