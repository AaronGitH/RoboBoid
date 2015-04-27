public class WorldModel {

	// find out about our neighbors.
	public WorldModel(){	
	}
	
	/**
	 * Find the average direction & distance
	 * This will be used to determine where the robot should move next
	 * @param sampleValues - The float array read in by the sensor
	 * @return float array of size 2 containing averaged direction / distance 
	 */
	public AveragePoint getCenterPoint(float[] sampleValues){
		float avgDirection = 0;
		float avgDistance = 0;
		int count = 0;
		
		for (int i = 0; i < sampleValues.length/2; ++i) {
			
			//TODO: Add checks for when values are out of range
        	float direction = sampleValues[i*2];
        	float distance = sampleValues[(i*2)+1];
        	        	
        	if (distance > 0 && distance < 100) {
        		avgDistance += distance;
            	avgDirection += direction;
            	count ++;
        	}
		}
		
		avgDirection /= count;
		avgDistance /= count;
		
		
		return new AveragePoint(avgDistance, avgDirection);
	}
	
	

}
