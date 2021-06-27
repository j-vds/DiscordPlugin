package disc.command;


import arc.Core;
import arc.Events;
import mindustry.game.EventType.*;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.Team;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import disc.discordPlugin;


public class serverCommands implements MessageCreateListener {
    final String commandDisabled = "This command is disabled.";
    final String noPermission = "You don't have permissions to use this command!";

    private discordPlugin mainData;


    public serverCommands(discordPlugin _data){
        this.mainData = _data;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("..gameover")) {
            Role gameOverRole = mainData.discRoles.get("gameOver_role_id");
            if (gameOverRole == null){
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            if (!hasPermission(gameOverRole, event)) return;
            // ------------ has permission --------------
            if (Vars.state.is(GameState.State.menu)) {
                return;
            }
            Events.fire(new GameOverEvent(Team.crux));

        } else if (event.getMessageContent().startsWith("..exit")) {
            Role closeServerRole = mainData.discRoles.get("closeServer_role_id");
            if (closeServerRole == null) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            if (!hasPermission(closeServerRole, event)) return;

            Vars.net.dispose(); //todo: check
            Core.app.exit();

        //testing
        //} else if (event.getMessageContent().startsWith("..test")){

        }
    }

    public void testhallo(byte[] a){
        System.out.println("done");
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
        } catch (Exception ignore){
            return false;
        }
    }
}