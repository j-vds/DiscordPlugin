*130+ compatible*

### Setup
Make a bot and request a token. Make sure to give the bot some permissions.

#### Startup
When you run the plugin for the first time a config file will be made `config/disc/config.json`. In this file you can set some parameters to enable/disable some commands. 
You will also need to make a [discord bot](https://discord.com/developers/applications) and request a token.

I recommend to enable `Developer Mode` on discord. This is the easiest way to get the `id` values.

### Disable commands
You can disable some commands by removing or leaving some fields blank `""` in the config file.

#### Discord
* `..chat <text...>` Send a message to the in-game chat
* `..players` a list of the online players
* `..info` gives some info (amount of enemies, number of waves, name of the map)
* `..infores` amount of resources collected
* `..gameover` ends a game 
* `..maps` *custom* maps on the server

#### In-game
* `/d <text...>` Send a message to discord.
* `/gr [player] [reason...]` Alert admins on discord if someone is griefing (5 minute cooldown)

### Building the Jar

`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.
