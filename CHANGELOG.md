Change Log
==========

Version 2.1.3
-------------

* Fixes an issue with R6 and a plugin base method conflicting with the name of a new function in the Bukkit class it supercedes. 


Version 2.1.2
-------------

* Updates to the new Event system
* Fixes a naming conflict with the general plugin architecture this is based off of.


Version 2.1.1
-------------

* Fixes an issue with unpacking the default configuration file.


Version 2.1
-----------

* Moved to the new plugin architecture (You won't see much from this)
* We now unpack a default configuration file if needed. 
* Fix for the NullPointerException documented on the forums.

Version 2.0_1
-------------

* Fixed a dumb mistake causing some users to get the forecast for the last day available instead of the first.


Version 2.0
-----------

* Multiworld support
* Re-enabled messages to users on the weather changing
* Redid the configuration file for multiworld and some other stuff
* Customization options for messages; you can now set what the plugin sends to users!


Version 1.3
-----------

* Never existed. Ignore references to it; I decided to jump to 2.0 at the last minute.


Version 1.2
-----------

* Fixed (hopefully) the bug with weather not updating once and for all. The weather will be FORCIBLY updated every time the weather is checked.
** Note that client desynchronization issues may still occur. If you disconnect and reconnect and the weather is correct again, there is nothing I can do to fix this, sorry.
* Changed the default method of specifying location to an RSS feed. Let's see how this can break things D:
* Accidentally removed the show-forecast option which shows when the weather changes in chat. Did anyone actually use this? Redoing it for multiworld is going to be a massive pain if people want it.
* Fixed broken forecasts
