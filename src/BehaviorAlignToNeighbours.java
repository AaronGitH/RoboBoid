import lejos.hardware.lcd.LCD;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

/*
 * This class is the hardest to implement because of the many possible scenarios
 * The boid has to first identify that it is within close range on another boid
 * Then it must track the movement of the other boid to see where it is moving towards
 * And then match the same direction 
 */

public class BehaviorAlignToNeighbours implements Behavior {
	private boolean suppressed;
	private DifferentialPilot pilot;
	private SampleProvider provider;
	private float[] sampleValues;
	private float[] tempValues;

	public BehaviorAlignToNeighbours(DifferentialPilot pilot,
			SampleProvider provider) {
		this.pilot = pilot;
		this.provider = provider;
		this.sampleValues = new float[provider.sampleSize()];
		this.tempValues = new float[provider.sampleSize()];
	}

	@Override
	public boolean takeControl() {

		provider.fetchSample(sampleValues, 0);
		/*
		 * check whether there are any robots in the vicinity if yes, then this
		 * Behavior takes over
		 */
		for (int i = 0; i < sampleValues.length / 2; ++i) {
			float distance = sampleValues[(i * 2) + 1];

			tempValues[i * 2] = sampleValues[i * 2];

			// return TRUE as soon as at least 1 robot is found to be near
			if (distance >= 0 && distance < 20) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void action() {
		suppressed = false;

		LCD.drawString("Aligning...", 0, 0);

		matchNeighbour();
		while (!suppressed) {
			Thread.yield();
		}
	}

	private void matchNeighbour() {
		for (int i = 0; i < sampleValues.length / 2; ++i) {
			tempValues[i * 2] = sampleValues[i * 2];
		}

		Delay.msDelay(1000);
		provider.fetchSample(sampleValues, 0);

		for (int i = 0; i < sampleValues.length / 2; ++i) {
			LCD.drawString(i + ": " + tempValues[i * 2] + ", "
					+ sampleValues[i * 2], 0, 3 + i);
		}

		// provider.fetchSample(sampleValues, 0); //fetch again first
		//
		// for (int i = 0; i < sampleValues.length / 2; ++i) {
		//
		// // TODO: Add checks for when values are out of range
		// float direction = sampleValues[i * 2];
		// float distance = sampleValues[(i * 2) + 1];
		//
		// if (distance >= 0 && distance < 20) {
		//
		// }
		// }

		suppressed = true;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}
