### Setup
Make a bot and request a token. Make sure to give the bot some permissions (Admin for now, this will change 
because this plugin is in early dev). 

Before you start your server you will need to create a `token.txt` file in the `config/mods` directory.
You need to follow a specific template. 

```
token
channel_id (msg from the in-game chat will be posted here)
```




### Basic Usage
You can use some basic commands. I will add more in the future.

#### Discord
* `..chat <text...>` Send a message to the in-game chat
* `..players` a list of the online players<br>
(TODO)
* `..info` gives some info (amount of enemies, number of waves, name of the map)
* `..infores` amount of resources collected
* `..gameover` ends a game (adminonly)
#### In-game
* `/d <text...>` Send a message to discord.
<br>(TODO)
* `/gr [name]` Alert admins on discord if someone is griefing (probably add cooldown)
* `/report <name> <reason...>` Send a griefreport to discord
* `/ping` Ping a certain role eg for a game event



### Building the Jar

`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.


### Installing

Simply place the output jar from the step above in your server's `config/mods` directory and restart the server.
List your currently installed plugins by running the `mods` command.