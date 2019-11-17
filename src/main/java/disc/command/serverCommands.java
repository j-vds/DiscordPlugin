package disc.command;

import io.anuke.arc.ApplicationListener;
import io.anuke.arc.Core;
import io.anuke.arc.Events;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.net.Server;
import io.anuke.mindustry.game.EventType.*;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.core.GameState;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.maps.Map;
import io.anuke.mindustry.io.*;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.zip.InflaterInputStream;


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