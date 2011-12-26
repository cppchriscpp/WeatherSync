/**
 * 
 */
package net.cpprograms.minecraft.WeatherSync;

/**
 * Simple class to hold the various things used for a location to sync weather to.
 * @author cppchriscpp
 *
 */
public class WeatherLocation {
	/**
	 * Getter of all things weather.
	 */
	private WeatherSystem weatherSystem;
	
	/**
	 * What world are we going to be modifying?
	 */
	private String weatherWorld;
	
	/**
	 * The rss file to use. This overrides ALL other options.
	 */
	private String rssfile;
	
	/**
	 * The current weather in this world.
	 */
	WeatherSystem.WeatherType currentWeather = WeatherSystem.WeatherType.UNDEFINED;
	
	/**
	 * Constructor. Does about what you'd expect it to do.
	 * @param weatherSync The plugin to attach this to.
	 * @param _weatherWorld The world name to update weather for.
	 * @param _rssfile The rss file to use to get information.
	 * @param debug Whether to enable debug mode for this location.
	 */
	public WeatherLocation(WeatherSync weatherSync, String _weatherWorld, String _rssfile, boolean debug) {
		weatherWorld = _weatherWorld;
		rssfile = _rssfile;
		weatherSystem = new WeatherSystem(weatherSync, rssfile, debug);
	}
	
	/**
	 * Get the WeatherSystem in use by this. 
	 * @return The WeatherSystem being used by this.
	 */
	public WeatherSystem getWeatherSystem() {
		return weatherSystem;
	}
	
	/**
	 * Get the name of the world this location represents. 
	 * @return The name of the world this location represents.
	 */
	public String getWorld() {
		return weatherWorld;
	}
	
	/**
	 *Get the URL to the rss file that this location syncs with.
	 * @return The URL to the rss file that this location syncs with.
	 */
	public String getRSSFile() {
		return rssfile;
	}
	
	/**
	 * Sets the world name to use for weather.
	 * @param _weatherWorld The new world name.
	 */
	public void setWorld(String _weatherWorld) {
		weatherWorld = _weatherWorld;
	}
	
	/**
	 * Sets the URL to the rss file to use to synchronize weather.
	 * @param _rssfile The URL to the rss file to use.
	 */
	public void setRSSFile(String _rssfile) {
		rssfile = _rssfile;
		weatherSystem.setLocation(_rssfile);
	}
	
	/**
	 * Gets the current weather at this location.
	 * @return The current weather represented in a WeatherType enum.
	 */
	public WeatherSystem.WeatherType getCurrentWeather() {
		return currentWeather;
	}
	
	/**
	 * Sets the current weather to the given enum value.
	 * @param weather The new enum to use in place of what we have.
	 */
	public void setWeather(WeatherSystem.WeatherType weather) {
		currentWeather = weather;
	}
	
}
