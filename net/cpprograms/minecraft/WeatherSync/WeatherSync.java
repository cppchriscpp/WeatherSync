package net.cpprograms.minecraft.WeatherSync;

import net.cpprograms.minecraft.General.CommandHandler;
import net.cpprograms.minecraft.General.PluginBase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;

/**
 * Plugin to sync the weather with local weather.
 *
 */
public class WeatherSync extends PluginBase
{
	
	/**
	 * A Player listener
	 */
    private final WeatherSyncPlayerListener playerListener = new WeatherSyncPlayerListener(this);
	
	/** 
	 * The locations to synchronize the weather for. All info will be here.
	 */
    public Map<String, WeatherLocation> weatherLocations;
	
	/**
	 * How frequently to update the weather, in minutes.
	 */
	public int updateTimer = 10;
	
	/**
	 * The WeatherSynchronizer that runs to constantly update the weather.
	 */
	WeatherSynchronizer wsRunner;
	
	/**
	 * The station to use. 
	 */
	public String station = "";
	
	/**
	 * Whether to show the forecast on weather change.
	 */
	public boolean showForecast = false;
	
	/**
	 * Whether to show the forecast upon joining the server.
	 */
	public boolean forecastOnJoin = true;
	
	/**
	 * Is the forecast command available?
	 */
	public boolean forecastCommandEnabled = true;
	
	/**
	 * Various formatting options. Overridden by configuration files, and used
	 * internally.
	 */
	private String weatherFormat = "�7Currently: [weather]";
	private String weatherUpdatedFormat = "�7 The weather is now [weather]";
	private String forecastFormat = "�7[forecast]";
	private String textForClear = "[default]";
	private String textForRain = "[default]";
	private String textForSnow = "[default]";
	private String textForThunder = "[default]";
	
	/**
	 * Runs when the plugin is enabled; starts the thread that updates the weather.
	 */
	@Override
	public void onEnable()
	{
		weatherLocations = new HashMap<String, WeatherLocation>();
		// Read in YAML
		try
		{
			FileConfiguration config = getConfig();
			if (config.contains("world")) {
				logSevere("You are using an old config file! You need to update to the newest version. (Blame multiworld.)");
				return;
			}
			
			if (config.contains("worlds")) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> lst = (List<Map<String, Object>>)config.get("worlds");
				for (Map<String, Object> curr : lst) {
					String tempworld = "";
					String temprss = "";
					if (curr.containsKey("name"))
						tempworld = curr.get("name").toString();
					if (curr.containsKey("rssfile"))
						temprss = curr.get("rssfile").toString();
					if (Bukkit.getWorld(tempworld) == null) {
						logWarning((tempworld.equals("")?"You forgot to include a name for one of the worlds in your configuration file. This world has been skipped.":"The world "+tempworld+" does not exist. Skipping"));
						continue;
					}
					if (temprss.equals("")) {
						logWarning("Configuration for "+tempworld+" does not include an rss file to use! Skipping");
						continue;
					}
					weatherLocations.put(tempworld, new WeatherLocation(this, tempworld, temprss, isDebugging()));
				}
			} else {
				logWarning("You did not include any worlds in your configuration file!");
			}
			
			if (config.contains("updatetime"))
				updateTimer = config.getInt("updatetime");

			if(config.contains("forecast-on-join"))
				forecastOnJoin = config.getBoolean("forecast-on-join");
			if(config.contains("show-forecast"))
				showForecast = config.getBoolean("show-forecast");
			if(config.contains("forecast-command-enabled"))
				forecastCommandEnabled = config.getBoolean("forecast-command-enabled");
			
			// Message customization!
			if (config.contains("messages") && config.isConfigurationSection("messages")) {
				ConfigurationSection sect = getConfig().getConfigurationSection("messages");
				Map<String, Object> msgs = sect.getValues(false);
				if (msgs.containsKey("weather"))
					weatherFormat = msgs.get("weather").toString().replace("[color]", "�");
				if (msgs.containsKey("weather-updated"))
					weatherUpdatedFormat = msgs.get("weather-updated").toString().replace("[color]", "�");
				if (msgs.containsKey("forecast"))
					forecastFormat = msgs.get("forecast").toString().replace("[color]", "�");
				if (msgs.containsKey("clear"))
					textForClear = msgs.get("clear").toString().replace("[color]", "�");
				if (msgs.containsKey("rain"))
					textForRain = msgs.get("rain").toString().replace("[color]", "�");
				if (msgs.containsKey("snow"))
					textForSnow = msgs.get("snow").toString().replace("[color]", "�");
				if (msgs.containsKey("thunder"))
					textForThunder = msgs.get("thunder").toString().replace("[color]", "�");
			}
			
		}
		catch (java.lang.NumberFormatException e) // The config file is broken.
		{
			logSevere("An exception occurred when trying to read your config file.");
			logSevere("Check your config.yml!");
			if (isDebugging())
				e.printStackTrace();
			return;
		}
		
		
		// Start the thread for Synchronizing weather.
		wsRunner = new WeatherSynchronizer(this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, wsRunner, 120, 1200*updateTimer);
			
		PluginManager pm = getServer().getPluginManager();
		if (forecastOnJoin)
			pm.registerEvents(playerListener, this);
		
		// We've loaded successfully!!
		
		super.onEnable();
        
        commandHandler = new CommandHandler(this, ForecastCommandSet.class);
	}
	
	/**
	 * Run this when the plugin is disabled. 
	 * (Required method.)
	 */
	@Override
	public void onDisable()
	{
		super.onDisable();
	}
	
	/**
	 * Gets the string for a forecast for the given weather location.
	 * @param wl The WeatherLocation to get a forecast string for.
	 * @return A string representing the forecast formatted based on the config file.
	 */
	public String getForecastFormatted(WeatherLocation wl) {
		return forecastFormat.replace("[forecast]",wl.getWeatherSystem().getLastForecast());
	}
	
	/**
	 * Gets the string for the weather for a given weather location.
	 * @param wl The WeatherLocation to get a weather string for.
	 * @return A string representing the current weather for the location.
	 */
	public String getWeatherFormatted(WeatherLocation wl) {
		return weatherFormat.replace("[weather]", getCurrentWeather(wl));
	}
	
	/**
	 * Gets the newly-updated weather in a formatted manner.
	 * @param wl The WeatherLocation to get the weather for.
	 * @return A string containing this new weather which can be displayed to the user.
	 */
	public String getUpdatedWeatherFormatted(WeatherLocation wl) {
		return weatherUpdatedFormat.replace("[weather]", getCurrentWeather(wl));
	}
	
	
	/**
	 * Gets the current weather for the given WeatherLocation, replacing any constants the 
	 * user has changed.
	 * @param wl The WeatherLocation to get this for.
	 * @return A string representing the current weather for that location.
	 */
	private String getCurrentWeather(WeatherLocation wl) {
		switch (wl.currentWeather) {
		case CLEAR:
			return textForClear.replace("[default]", wl.getWeatherSystem().getLastWeather());
		case RAIN:
			return textForRain.replace("[default]", wl.getWeatherSystem().getLastWeather());
		case SNOW: 
			return textForSnow.replace("[default]", wl.getWeatherSystem().getLastWeather());
		case THUNDER:
			return textForThunder.replace("[default]", wl.getWeatherSystem().getLastWeather());
		default: 
			return wl.getWeatherSystem().getLastWeather();
		
		}
	}
	
}