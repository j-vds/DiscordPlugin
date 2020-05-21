package disc.command;


import arc.Core;
import arc.Events;
import mindustry.game.EventType.*;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.Team;


import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;


import java.util.Optional;

public class serverCommands implements MessageCreateListener {
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
            Events.fire(new GameOverEvent(Team.crux));

        } else if (event.getMessageContent().startsWith("..exit")) {
            if (!data.has("closeServer_role_id")) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            Role r = getRole(event.getApi(), data.getString("closeServer_role_id"));
            if (!hasPermission(r, event)) return;

            Vars.net.dispose(); //todo: check
            Core.app.exit();

        //testing
        //} else if (event.getMessageContent().startsWith("..test")){

        }
    }

    public void testhallo(byte[] a){
        System.out.println("done");
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