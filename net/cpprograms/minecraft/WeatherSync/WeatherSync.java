package net.cpprograms.minecraft.WeatherSync;

import net.cpprograms.minecraft.General.CommandHandler;
import net.cpprograms.minecraft.General.PluginBase;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;

/**
 * Plugin to sync the weather with local weather.
 * @author cppchriscpp
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
	private String weatherFormat = "§7Currently: [weather]";
	private String weatherUpdatedFormat = "§7 The weather is now [weather]";
	private String forecastFormat = "§7[forecast]";
	private String textForClear = "[default]";
	private String textForRain = "[default]";
	private String textForSnow = "[default]";
	private String textForThunder = "[default]";
	
	/**
	 * Debug mode on or off?
	 */
	public boolean debug = false;
	
	/**
	 * YAML config reader.
	 */
	private final Yaml yaml = new Yaml(new SafeConstructor());
	
	/**
	 * Runs when the plugin is enabled; starts the thread that updates the weather.
	 */
	public void onEnable()
	{
		weatherLocations = new HashMap<String, WeatherLocation>();
		// Read in YAML
		try
		{

			FileInputStream fIn = new FileInputStream(new File(this.getDataFolder(), "config.yml"));
			
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>)yaml.load(new UnicodeReader(fIn));
			
			if (data.containsKey("world")) {
				logSevere("You are using an old config file! You need to update to the newest version. (Blame multiworld.)");
				return;
			}
			
			if (data.containsKey("worlds")) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> lst = (List<Map<String, Object>>)data.get("worlds");
				for (Map<String, Object> curr : lst) {
					String tempworld = "";
					String temprss = "";
					if (curr.containsKey("name"))
						tempworld = curr.get("name").toString();
					if (curr.containsKey("rssfile"))
						temprss = curr.get("rssfile").toString();
					if (!new File(tempworld+"/level.dat").exists()) {
						logWarning((tempworld.equals("")?"You forgot to include a name for one of the worlds in your configuration file. This world has been skipped.":"The world "+tempworld+" does not exist. Skipping"));
						continue;
					}
					if (temprss.equals("")) {
						logWarning("Configuration for "+tempworld+" does not include an rss file to use! Skipping");
						continue;
					}
					weatherLocations.put(tempworld, new WeatherLocation(this, tempworld, temprss, debug));
				}
			} else {
				logWarning("You did not include any worlds in your configuration file!");
			}
			
			if (data.containsKey("updatetime"))
				updateTimer = Integer.parseInt(data.get("updatetime").toString());

			if(data.containsKey("forecast-on-join"))
				forecastOnJoin = Boolean.parseBoolean(data.get("forecast-on-join").toString());
			if(data.containsKey("show-forecast"))
				showForecast = Boolean.parseBoolean(data.get("show-forecast").toString());
			if(data.containsKey("forecast-command-enabled"))
				forecastCommandEnabled = Boolean.parseBoolean(data.get("forecast-command-enabled").toString());
			if(data.containsKey("debug"))
				debug = Boolean.parseBoolean(data.get("debug").toString());
			
			// Message customization!
			if (data.containsKey("messages")) {
				@SuppressWarnings("unchecked")
				Map<String, Object> msgs = (Map<String, Object>)data.get("messages");
				if (msgs.containsKey("weather"))
					weatherFormat = msgs.get("weather").toString().replace("[color]", "§");
				if (msgs.containsKey("weather-updated"))
					weatherUpdatedFormat = msgs.get("weather-updated").toString().replace("[color]", "§");
				if (msgs.containsKey("forecast"))
					forecastFormat = msgs.get("forecast").toString().replace("[color]", "§");
				if (msgs.containsKey("clear"))
					textForClear = msgs.get("clear").toString().replace("[color]", "§");
				if (msgs.containsKey("rain"))
					textForRain = msgs.get("rain").toString().replace("[color]", "§");
				if (msgs.containsKey("snow"))
					textForSnow = msgs.get("snow").toString().replace("[color]", "§");
				if (msgs.containsKey("thunder"))
					textForThunder = msgs.get("thunder").toString().replace("[color]", "§");
			}
			
		}
		catch (IOException e) // Problem reading the file; it probably does not exist.
		{
			logSevere("Could not read your configuration file. Try reinstalling the plugin!");
			if (debug)
				logSevere(e.toString());
			return;
		}
		catch (java.lang.NumberFormatException e) // The config file is broken.
		{
			logSevere("An exception occurred when trying to read your config file.");
			logSevere("Check your config.yml!");
			if (debug)
				logSevere(e.toString());
			return;
		}
		
		
		// Start the thread for Synchronizing weather.
		wsRunner = new WeatherSynchronizer(this);
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, wsRunner, 120, 1200*updateTimer);
			
		PluginManager pm = getServer().getPluginManager();
		if (forecastOnJoin)
			pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		
		// We've loaded successfully!!
		
		super.onEnable();
        if (debug)
        	logInfo("Debug mode is active.");
        
        commandHandler = new CommandHandler(this, ForecastCommandSet.class);
	}
	
	/**
	 * Run this when the plugin is disabled. 
	 * (Required method.)
	 */
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