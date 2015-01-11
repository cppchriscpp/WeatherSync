package net.cpprograms.minecraft.WeatherSync;

import org.bukkit.World;
import org.bukkit.entity.Player;

import net.cpprograms.minecraft.WeatherSync.WeatherSystem.WeatherType;

/**
 * The actual weather synchronization class. Runnable.
 * @author cppchriscpp
 */
public class WeatherSynchronizer implements Runnable {

	/**
	 * The plugin to use to access everything.
	 */
	private WeatherSync weatherSync;
	
	/**
	 * Creates a new instance of this runnable thing.
	 * @param plugin The plugin to interface with the world.
	 */
	public WeatherSynchronizer(WeatherSync plugin)
	{
		weatherSync = plugin;
	}
	
	private void setStorm(World world, boolean stormy, boolean thundery) {
		if (world.hasStorm() != stormy) {
			world.setStorm(stormy);
		}
		
		if (world.isThundering() != thundery) {
			world.setThundering(thundery);
		}
	}
	
	/**
	 * The run method, which updates the weather every X amount of time.
	 */
	@Override
	public void run() 
	{
		for (WeatherLocation weatherLocation : weatherSync.weatherLocations.values()) {
			// Get the weather
			WeatherSystem.WeatherType weather = weatherLocation.getWeatherSystem().getWeather();
			
			World world = weatherSync.getServer().getWorld(weatherLocation.getWorld());
			
			// Update the weather. We no longer care if it has changed.
				
			if (weatherSync.showForecast && weatherLocation.currentWeather != weather && world != null && world.getPlayers() != null)
			 	for (Player pl : world.getPlayers())
			 		pl.sendMessage(weatherSync.getUpdatedWeatherFormatted(weatherLocation));
			
			if (weather == WeatherType.CLEAR)
			{
				setStorm(world, false, false);
			}
			else if (weather == WeatherType.RAIN)
			{
				setStorm(world, true, false);
			}
			else if (weather == WeatherType.SNOW)
			{
				// TODO: Can anything be done with this?
				setStorm(world, true, false);
			}
			else if (weather == WeatherType.THUNDER)
			{
				setStorm(world, true, true);
				world.setThunderDuration(24000);
			}
			
			weatherSync.logDebug("The weather in world "+world.getName()+" is now " + weather.toString().toLowerCase());
			
			world.setWeatherDuration(24000);
			// Save this so that we do not update needlessly in the future.
			weatherLocation.setWeather(weather);
		}
		
	}

}
