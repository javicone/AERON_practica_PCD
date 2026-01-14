package secuencial;

import java.util.List;
import java.util.Queue;

public class ControlTower {

	private List<Runway> runways;
	private List<Gate> gates;
	private Queue<Airplane> landingQueue;
	private Queue<Airplane> takeOffQueue;
	
	
	
	public ControllTower()
	{
	
		
	}
	
	/**
	 * @return the runways
	 */
	public List<Runway> getRunways() {
		return runways;
	}
	/**
	 * @return the gates
	 */
	public List<Gate> getGates() {
		return gates;
	}
	/**
	 * @return the landingQueue
	 */
	public Queue<Airplane> getLandingQueue() {
		return landingQueue;
	}
	/**
	 * @return the takeOffQueue
	 */
	public Queue<Airplane> getTakeOffQueue() {
		return takeOffQueue;
	}
	/**
	 * @param runways the runways to set
	 */
	public void setRunways(List<Runway> runways) {
		this.runways = runways;
	}
	/**
	 * @param gates the gates to set
	 */
	public void setGates(List<Gate> gates) {
		this.gates = gates;
	}
	/**
	 * @param landingQueue the landingQueue to set
	 */
	public void setLandingQueue(Queue<Airplane> landingQueue) {
		this.landingQueue = landingQueue;
	}
	/**
	 * @param takeOffQueue the takeOffQueue to set
	 */
	public void setTakeOffQueue(Queue<Airplane> takeOffQueue) {
		this.takeOffQueue = takeOffQueue;
	}
	
}


