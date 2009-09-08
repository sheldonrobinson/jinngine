package jinngine.unused;

public class Threadtest extends Thread {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		final Thread t = new Threadtest();

		Thread shutdownThread = new Thread() {
			public void run() {
				t.interrupt();
			}
		};
		
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		
		t.start();
		
		System.out.println("t was started");
		

		
		Thread.sleep(10000);
		
		t.interrupt();
	}
	
	@Override
	public void run() {
		//super.run();

		System.out.println("Im a new thread");

		
		try {
		 sleep(10000000);
		} catch (InterruptedException e) {
			System.out.println("SIGTERM Catch!");
		}
	}
	
	

}
