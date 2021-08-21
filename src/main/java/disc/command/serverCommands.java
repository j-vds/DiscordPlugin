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

import static disc.utilmethods.*;


public class serverCommands implements MessageCreateListener {
    final String commandDisabled = "This command is disabled.";

    private discordPlugin mainData;

    public serverCommands(discordPlugin _data){
        this.mainData = _data;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        String[] incoming_msg = event.getMessageContent().split("\\s+");

        switch (incoming_msg[0]){
            case "..gameover":
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
                break;
            case "..exit":
                Role closeServerRole = mainData.discRoles.get("closeServer_role_id");
                if (closeServerRole == null) {
                    if (event.isPrivateMessage()) return;
                    event.getChannel().sendMessage(commandDisabled);
                    return;
                }
                if (!hasPermission(closeServerRole, event)) return;

                Vars.net.dispose();
                Core.app.exit();
                break;
            default:
                break;
        }
    }
}