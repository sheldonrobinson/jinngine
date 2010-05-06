package jinngine.game;

import java.io.IOException;

import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.SoundStore;

public class SoundTest {
	
	public SoundTest() throws IOException, InterruptedException {
	      // init sound
		SoundStore.get().init();	

		//SoundStore.get().setSoundsOn(true);
		Audio click = SoundStore.get().getWAV("woody2.wav");
//		click.playAsSoundEffect(1, 100, false);

//		Thread.sleep(1000);
		System.out.println("Play");
		click.playAsSoundEffect(1, 100, false);
		Thread.sleep(8000);

		System.out.println("Play Again");
		click.playAsSoundEffect(1, 100, false);
		Thread.sleep(8000);

		System.out.println("Play Again");
		click.playAsSoundEffect(1, 100, false);
		Thread.sleep(2000);

	}
	
	public static void main(String args[]) throws Exception {
		new SoundTest();
	}

}
