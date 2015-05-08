import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;


public class BehaviorMoveForward implements Behavior {

	private DifferentialPilot pilot;
	
	private boolean suppressed;

	public BehaviorMoveForward(DifferentialPilot pilot) {
		this.pilot = pilot;
	}
	
	@Override
	public boolean takeControl() {
		return true;
	}

	@Override
	public void action() {
		suppressed = false;
		pilot.travel(100);
		while( !suppressed )
	        Thread.yield();
	     
		pilot.stop();
	}

	@Override
	public void suppress() {
		suppressed =  true;
	}

}
