
public class WorldModel {

	// find out about our neighbors.
	private float[] centerPoint;

	public WorldModel(){	
		this.centerPoint = new float[]{0,0};
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
		
		for (int i = 0; i < sampleValues.length/2; ++i) {
			
			//TODO: Add checks for when values are out of range
        	float direction = sampleValues[i*2];
        	float distance = sampleValues[(i*2)+1];
        	
        	avgDirection += direction;
        	avgDistance += distance;
		}
		
		//get average (divide by (size*2) - since each beacon gives 2 values)
		avgDirection /= sampleValues.length / 2;
		avgDistance /= sampleValues.length / 2;
		
		
		return new AveragePoint(avgDistance, avgDirection);
	}
	
	
	

}
