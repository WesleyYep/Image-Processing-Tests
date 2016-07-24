
public class TestWifi {

	private double freqInMHz = 2400; //2.4GHz to 2.4835GHz
	
	public static void main(String[] args) {
		double signalLevelInDb = retrieveSignalLevel();
		System.out.println("distance is: " + signalLevelInDb);
	}

	private static double retrieveSignalLevel() {
		//call the linux shell command
		
		//parse it to get signal strength
		
		//return it
		return 0;
	}

	public double calculateDistance(double signalLevelInDb) {
	    double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
	    return Math.pow(10.0, exp);
	}
	
}
