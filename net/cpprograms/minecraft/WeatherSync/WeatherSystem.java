package net.cpprograms.minecraft.WeatherSync;

/**
 * @(#)WeatherSystem.java
 * This class handles weather communications; it gets the weather for a given
 * location and returns it in ENUM format.
 *
 * @author
 * @version 1.00 2011/4/21
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

/**
 * Class to get the weather from a source (wunderground) and convert
 * it into the simple terms needed for our purposes.
 * @author cppchriscpp
 *
 */
public class WeatherSystem {

	/**
	 * The location to get weather for, in the format used in the query.
	 */
	private String location;
	
	/**
	 * Enumeration for the simple types of weather we support.
	 *
	 */
	public enum WeatherType
	{
		UNDEFINED,
		CLEAR,
		RAIN,
		THUNDER,
		SNOW
	};
	
	/**
	 * Debug mode?
	 */
	boolean debug = false;

	/**
	 * The last weather reading found.
	 */
	private String lastWeather = "Unknown";
	
	/*
	 * The last forecast reading found.
	 */
	private String lastForecast = "Unknown";
	
	/**
	 * Creates a blank instance of WeatherSystem.
	 */
	public WeatherSystem()
	{
		location = "";
	}
	
    /**
     * Creates a new instance of this class.
     * @param rssfile The rss file to use to update weather.
     */
    public WeatherSystem(String rssfile) 
    {
    	location = rssfile;
    }
    
    /**
     * Creates a new instance of this class.
     * @param rssfile The rss file to use to update weather.
     * @param debug Whether to turn debug mode on.
     */
    public WeatherSystem(String rssfile, boolean _debug) {
    	location = rssfile;
    	debug = _debug;
    }
    
    /**
     * Gets the WS's location.
     * @return The location of the WS; raw for now.
     */
    public String getLocation()
    {
    	return location;
    }
    
    /**
     * Sets the location of this WS.
     * @param rssfile The URL to an rss file to use.
     */
    public void setLocation(String rssfile)
    {
    	location = rssfile;
    }
    
    /**
     * Set whether to print debug output.
     * @param debug true to print debug info; false otherwise.
     */
    public void setDebug(boolean debug)
    {
    	this.debug = debug;
    }
    
    /**
     * Gets the last read weather in a user-friendly format.
     * @return The last weather in a string.
     */
    public String getLastWeather()
    {
    	return lastWeather;
    }
    
    /**
     * Gets the last read forecast in a user-friendly format.
     * @return The last forecast in a string.
     */
    public String getLastForecast()
    {
    	return lastForecast;
    }
	
    /**
     * Gets the weather for the location this is set to. 
     * @return WeatherType representing the current weather.
     */
	public WeatherType getWeather()
	{
		URL loc;
		WeatherType theweather = WeatherType.CLEAR;
		String weatherline = "Unknown";
		
		// Parse the location into a URL; making sure it works.
		try
		{
			loc = new URL(location);
		}
		catch (MalformedURLException e)
		{
			System.err.println("WeatherSync: Could not read the weather; the location specified was not valid! Try a zip code.");
			System.err.println("WeatherSync: You provided: \"" + location + "\"");
			
			if (debug)
				System.err.println(e.toString());
			
			return theweather;
		}
		
		// Read the actual data in.
		try
		{
			URLConnection yc = loc.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine;
			
			// Loop through all site output
			while ((inputLine = in.readLine()) != null)
			{
				// Try to find the current weather.
				if (inputLine.contains("Conditions:"))
				{
					// Parse it into a variable. 
					int condp = inputLine.indexOf("Conditions:");
					int condpp = inputLine.indexOf("|", condp);
					weatherline = inputLine.substring(condp+12, condpp-1);
					break;
				}
			}
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("<![CDATA["))
				{
					String dat = in.readLine();
					lastForecast = dat.trim().replace(" - ", ": ").replace("&amp;deg;", "degrees");
					break;
				} 
				else if (inputLine.contains("<description>"))
				{
					String dat = in.readLine();
					lastForecast = dat.trim().replace(" - ", ":").replace("&amp;deg;", "degrees");
					break;
				}
			}
			if (weatherline.equals("Unknown"))
			{
				System.err.println("WeatherSync: Could not read the weather; check your settings.");
				if (debug)
					System.err.println("Your location is: " + location);
			}
			in.close();
		}
		catch (Exception e)
		{ // Catches all errors; mainly IOExceptions. 
			System.err.println("WeatherSync: Could not read the weather; connection error.");
			
			if (debug)
				e.printStackTrace(System.err);
			
			return theweather;
		}
		
		lastWeather = weatherline;

		// Lowercase the weather for comparison.
		weatherline = weatherline.toLowerCase();

		// Check for the various types of weather, returning it if found.
		if (weatherline.contains("thunder"))
			return WeatherType.THUNDER;

		if (weatherline.contains("snow") || weatherline.contains("ice") || weatherline.contains("hail") || weatherline.contains("freezing"))
			return WeatherType.SNOW;

		if (weatherline.contains("rain") || weatherline.contains("drizzle") || weatherline.contains("spray"))
			return WeatherType.RAIN;

		// Return the default (CLEAR)
		return theweather;
	}
}
