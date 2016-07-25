import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestWifi {

	private static double freqInMHz = 2462; //2.462GHz as shown in iwconfig
	
	public static void main(String[] args) {
		double signalLevelInDb;
		try {
			//get signal level
			signalLevelInDb = retrieveSignalLevel();
			
			//convert to distance
			 double distance = calculateDistance(signalLevelInDb);
			
			System.out.println("distance is: " + distance + " metres");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static double retrieveSignalLevel() throws IOException, InterruptedException {
		//call the linux shell command
		Process p = Runtime.getRuntime().exec("ipconfig");
	    p.waitFor();
	    BufferedReader reader = 
	         new BufferedReader(new InputStreamReader(p.getInputStream()));
	    String line = "";
	    double value = -1;
	    
	    while ((line = reader.readLine())!= null) {
	    	//System.out.println(line);
	    	if (line.contains("Signal level=")) {
	    		value = getValueFromRegex(line);
	    		System.out.println("Signal level is: " + value);
	    		break;
	    	}
	    }

	    value = getValueFromRegex("Link Quality=67/70  Signal level=-43 dBm");
		System.out.println("Signal level is: " + value);
	    
		//return it
		return value;
	}

	private static double getValueFromRegex(String line) {
		//parse it to get signal strength
	    String regex = "Link Quality=(.*) Signal level=(.*) dBm";
	    Pattern pattern = Pattern.compile(regex);
	    Matcher matcher = pattern.matcher(line);
	    if (matcher.matches()) {
	        String signalLevelString = matcher.group(2);
			return Double.parseDouble(signalLevelString);
	    }
	    System.err.println("Could not find signal level value in string");
	    return 0;
	}

	public static double calculateDistance(double signalLevelInDb) {
	    double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
	    return Math.pow(10.0, exp);
	}
	
}
