/**
 * 
 */
package net.cpprograms.minecraft.WeatherSync;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author cppchriscpp
 * Sends players information upon their joining, if necessary.
 */
public class WeatherSyncPlayerListener implements Listener {
	
	/**
	 * The WeatherSync plugin to refer to.
	 */
	WeatherSync plugin;
	
	/**
	 * The player that has most recently joined the game.
	 */
	Player cjoin;
	
	/**
	 * Initializes an instance.
	 * @param instance The plugin to refer to.
	 */
	public WeatherSyncPlayerListener(WeatherSync instance)
	{
		plugin = instance;
	}
	
	/**
	 * Called when a player joins the game.
	 * @param event The PlayerJoinEvent representing this.
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		cjoin = event.getPlayer();
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, 
				new Runnable() {
					@Override
					public void run()
					{
						if (plugin.weatherLocations.get(cjoin.getWorld().getName()) != null) {
							WeatherLocation weatherLocation = plugin.weatherLocations.get(cjoin.getWorld().getName());
							cjoin.sendMessage(plugin.getWeatherFormatted(weatherLocation));
							cjoin.sendMessage(plugin.getForecastFormatted(weatherLocation));
						}
					}
				}, 0L);
	}

}
