package disc.command;

import io.anuke.arc.ApplicationListener;
import io.anuke.arc.Core;
import io.anuke.arc.Events;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.net.Server;
import io.anuke.mindustry.game.EventType.*;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.core.GameState;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.maps.Map;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.util.Optional;
//change maps
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class serverCommands implements MessageCreateListener {
    final long minMapChangeTime = 30L; //30 seconds
    final String commandDisabled = "This command is disabled.";
    final String noPermission = "You don't have permissions to use this command!";

    private JSONObject data;


    public serverCommands(JSONObject _data){
        this.data = _data;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("..gameover")) {
            if (!data.has("gameOver_role_id")){
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            Role r = getRole(event.getApi(), data.getString("gameOver_role_id"));

            if (!hasPermission(r, event)) return;
            // ------------ has permission --------------
            if (Vars.state.is(GameState.State.menu)) {
                return;
            }
            //inExtraRound = false;
            Events.fire(new GameOverEvent(Team.crux));
        } else if(event.getMessageContent().equalsIgnoreCase("..maps")){
            StringBuilder mapLijst = new StringBuilder();
            mapLijst.append("List of available maps:\n");
            for (Map m:Vars.maps.customMaps()){
                mapLijst.append("* "+m.name() + "/ " + m.width + " x " + m.height+"\n");
            }
            mapLijst.append("Total number of maps: " + Vars.maps.customMaps().size);
            new MessageBuilder().appendCode("", mapLijst.toString()).send(event.getChannel());

        } else if (event.getMessageContent().startsWith("..changemap")){
            if (!data.has("changeMap_role_id")){
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            Role r = getRole(event.getApi(), data.getString("changeMap_role_id"));
            if (!hasPermission(r, event)) return;

            String[] splittedArg = event.getMessageContent().split(" ");
            if (splittedArg.length == 1 || splittedArg.length > 3){
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage("*Invalid command* \nuse `..changemap <name> [mode: default=survival]`");
            } else if (splittedArg.length == 2){
                //find map
                //https://github.com/Anuken/Mindustry/blob/master/server/src/io/anuke/mindustry/server/ServerControl.java#L142
                Map map = Vars.maps.all().find(m-> m.name() == splittedArg[1]);
                if (map == null){
                    if (event.isPrivateMessage()) return;
                    event.getChannel().sendMessage(splittedArg[1] + " map not found.");
                } else {
                    Call.onInfoMessage("Next selected map:[accent] " + map.name() + "[]" +
                            (map.tags.containsKey("author") && !map.tags.get("author").trim().isEmpty() ? " by[accent] " + map.author() + "[]" : "") + ".");
                    //make maps smaller
                    //call gameover
                    //wait x seconds to repopulate maps

                }

            } else {
                event.getChannel().sendMessage("not implemented yet.");
            }

        } else if (event.getMessageContent().startsWith("..exit")){
            if (!data.has("closeServer_role_id")){
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            Role r = getRole(event.getApi(), data.getString("closeServer_role_id"));
            if (!hasPermission(r, event)) return;

            Vars.net.dispose(); //todo: check
            Core.app.exit();

        //testing
        /*} else if (event.getMessageContent().startsWith("..test")){
            //Vars.maps.removeMap(Vars.maps.customMaps().get(0)); //will delete a file
            FileHandle temp = Core.settings.getDataDirectory().child("maps/temp");
            temp.mkdirs();
            for (Map m1 : Vars.maps.customMaps()){
                if (m1.equals(Vars.world.getMap())) continue;
                if (m1.name().equals("Snowy Kingdom")) continue;

                m1.file.moveTo(temp);
            }
            //reload all maps from that folder
            Vars.maps.reload();

        */
        }
    }

    public Role getRole(DiscordApi api, String id){
        Optional<Role> r1 = api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] discordplugin: role not found!");
            return null;
        }
        return r1.get();
    }

    public Boolean hasPermission(Role r, MessageCreateEvent event){
        try {
            if (r == null) {
                if (event.isPrivateMessage()) return false;
                event.getChannel().sendMessage(commandDisabled);
                return false;
            } else if (!event.getMessageAuthor().asUser().get().getRoles(event.getServer().get()).contains(r)) {
                if (event.isPrivateMessage()) return false;
                event.getChannel().sendMessage(noPermission);
                return false;
            } else {
                return true;
            }
        } catch (Exception _){
            return false;
        }
    }


}