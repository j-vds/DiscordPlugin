package disc;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Optional;

public class utilmethods {
    public static String commandDisabled = "This command is disabled.";
    public static String noPermission = "You don't have permissions to use this command!";

    public static TextChannel getTextChannel(DiscordApi api, String id){
        Optional<Channel> dc =  ((Optional<Channel>)api.getChannelById(id));
        if (!dc.isPresent()) {
            System.out.println("[ERR!] discordplugin: channel not found!");
            return null;
        }
        Optional<TextChannel> dtc = dc.get().asTextChannel();
        if (!dtc.isPresent()){
            System.out.println("[ERR!] discordplugin: textchannel not found!");
            return null;
        }
        return dtc.get();
    }

    public static Role getRole(DiscordApi api, String id){
        Optional<Role> r1 = api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] discordplugin: role not found!");
            return null;
        }
        return r1.get();
    }

    public static Boolean hasPermission(Role r, MessageCreateEvent event){
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
