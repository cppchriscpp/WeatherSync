package net.cpprograms.minecraft.WeatherSync;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import net.cpprograms.minecraft.General.CommandSet;
import net.cpprograms.minecraft.General.PluginBase;

/**
 * Handler for the Forecast command.
 * @author cppchriscpp
 *
 */
public class ForecastCommandSet extends CommandSet {
	
	/**
	 * Plugin.
	 */
	WeatherSync plugin;
	
	/**
	 * Setter for our plugin.
	 */
	public void setPlugin(PluginBase plugin)
	{
		this.plugin = (WeatherSync) plugin;
	}
	
	/**
	 * Method to call by default when the forecast command is called. Get the weather for the specified world.
	 * @param sender The entity responsible for sending the command.
	 * @param method The world to get the weather for. (This is straightforward!!)
	 * @param params Unused.
	 * @return true if this was handled, false otherwise.
	 */
	public boolean noSuchMethod(CommandSender sender, String method, String[] params)
	{
		if (!plugin.forecastCommandEnabled)
			return false;
		
		if (plugin.weatherLocations.get(method) != null) {
			WeatherLocation useWl = plugin.weatherLocations.get(method);
			sender.sendMessage(plugin.getWeatherFormatted(useWl));
			sender.sendMessage(plugin.getForecastFormatted(useWl));
		}
		else
			sender.sendMessage("§7No forecast available for that world.");
			
		return true;
	}
	
	/**
	 * The method called by default when just /forecast is used. Gets the forecast for the current world for users.
	 * @param sender The entity responsible for sending this request.
	 * @return true if this was handled, false otherwise.
	 */
	public boolean noParams(CommandSender sender)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("§7You must provide a world name.");
			return true;
		}
		Player pl = (Player)sender;
		if (plugin.weatherLocations.get(pl.getWorld().getName()) != null) 
		{
			WeatherLocation useWl = plugin.weatherLocations.get(pl.getWorld().getName());
			sender.sendMessage(plugin.getWeatherFormatted(useWl));
			sender.sendMessage(plugin.getForecastFormatted(useWl));
		}
		else
			sender.sendMessage("§7No forecast available for this location.");
		return true;
	}
	
}