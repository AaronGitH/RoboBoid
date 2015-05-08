import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;

public class BehaviorFindCenterPoint implements Behavior {
	
	private DifferentialPilot pilot;
	private SampleProvider provider;
	private float[] sampleValues;
    private java.util.Random noise;

	private boolean suppressed;
	private int speedRight;
	private int speedLeft;

	
	public BehaviorFindCenterPoint(DifferentialPilot pilot, SampleProvider provider) {
		this.pilot = pilot;
		this.provider = provider;
		this.sampleValues = new float[provider.sampleSize()];
	    this.noise = new java.util.Random(85271); //noise
	}
	
	@Override
	public boolean takeControl() {
        provider.fetchSample(sampleValues, 0);

        /* 
         * check whether there are any robots in the vicinity 
         * if yes, then this Behavior takes over
         */
		for (int i = 0; i < sampleValues.length/2; ++i) {
			float distance = sampleValues[(i*2)+1];
			
			//return TRUE as soon as at least 1 robot is found to be near
			return (distance > 15 && distance < 100);
		}
		
		return false;
	}

	@Override
	public void action() {
		suppressed = false;
		
		move();
		
	     while( !suppressed )
	        Thread.yield();
	     
	     
	}

	private void move() {
		float avgDirection = 0;
		float avgDistance = 0;
		int count = 0;
		
		for (int i = 0; i < sampleValues.length/2; ++i) {
			
			//TODO: Add checks for when values are out of range
        	float direction = sampleValues[i*2];
        	float distance = sampleValues[(i*2)+1];
        	        	
        	if (distance > 15 && distance < 100) {
        		avgDistance += distance;
            	avgDirection += direction;
            	count ++;
        	}
		}
		
		avgDirection /= count;
		avgDistance /= count;
		
		float turnAngle = Math.abs(avgDirection) * 10;
    	
    	//reduce it by turnAngle * 2 
    	// => make it turn faster if the central point is to the side of the robot
    	turnAngle = SensorValues.SPEEDSTRAIGHT - (turnAngle * 2);
    	
    	if (avgDirection < -5) {
    		//turn left
    		speedRight = SensorValues.SPEEDSTRAIGHT;
    		speedLeft = (int)turnAngle;
    	}
    	else if (avgDirection > 5) {
    		//turn right
    		speedRight = (int)turnAngle;
    		speedLeft = SensorValues.SPEEDSTRAIGHT;
    	}
    	else {
    		//move straight but add noise depending on the distance
    		
			//noise at low distance ==> between -50 and 50 wheel speed
			speedLeft = (int)(SensorValues.SPEEDSTRAIGHT + (noise.nextFloat() * 100) - 50);
			speedRight = (int)(SensorValues.SPEEDSTRAIGHT + (noise.nextFloat() * 100) - 50);

			if (avgDistance > 90) {
    			// the values from the sensors aren't that reliable, so I add some more noise
    			speedLeft += (int)((noise.nextFloat() * 100) - 50);
    			speedRight += (int)((noise.nextFloat() * 100) - 50);
    		}
    	}
    	
    	float distance = avgDistance;
    	
    	if(distance > 100){ distance = 100; }
        speedLeft = speedLeft + (int)(distance * 2.5);
	    speedRight = speedRight + (int)(distance * 2.5);
        
		
        //motorLeft.setSpeed(speedLeft);
        //motorRight.setSpeed(speedRight);

        //motorLeft.forward();  // this method really seems necessary again
        //motorRight.forward(); 
	    pilot.travel(distance);
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}
