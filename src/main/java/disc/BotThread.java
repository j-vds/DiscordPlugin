package disc;

import arc.struct.ObjectMap;
import disc.command.comCommands;
import disc.command.mapCommands;
import disc.command.serverCommands;
import org.javacord.api.DiscordApi;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;

import java.lang.Thread;

public class BotThread extends Thread{
    public DiscordApi api;
    private Thread mt;
    private discordPlugin mainData;

    public BotThread(discordPlugin _mainData, Thread _mt) {
        api = _mainData.getAPI(); //new DiscordApiBuilder().setToken(data.get(0)).login().join();
        mt = _mt;
        mainData = _mainData;

        //communication commands
        api.addMessageCreateListener(new comCommands());
        //server manangement commands
        api.addMessageCreateListener(new serverCommands(mainData));
        api.addMessageCreateListener(new mapCommands(mainData));
    }

    public void run(){
        while (this.mt.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }
        Role downRole = mainData.discRoles.get("serverdown_role_id");
        TextChannel downChannel = mainData.discChannels.get("serverdown_channel_id");
        if (downRole != null && downChannel != null){
                if (!mainData.servername.isEmpty()){
                    new MessageBuilder()
                            .append(String.format("%s\nServer %s is down",downRole.getMentionTag(), "**"+mainData.servername+"**"))
                            .send(downChannel);
                } else {
                    new MessageBuilder()
                            .append(String.format("%s\nServer is down.", downRole.getMentionTag()))
                            .send(downChannel);
                }
        }else{
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {}
        }
        api.disconnect();
    }
}
