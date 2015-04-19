
public class WorldModel {
	
	// find out about our neighbours.
	private float[] centerPoint;
	
	private float[] readSeekValues;

	
	public WorldModel(float[] readSeekValues){
		this.readSeekValues = readSeekValues;		
		this.centerPoint = new float[]{0,0};
	}
	
	public float[] getCenterPoint(){
		return centerPoint;
	}
	
	
	

}
