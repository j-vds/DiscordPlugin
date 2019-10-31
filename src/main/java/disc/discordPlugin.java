package disc;

import io.anuke.arc.Core;
import io.anuke.arc.util.CommandHandler;
import io.anuke.arc.util.Strings;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.type.Player;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.plugin.Plugin;
//javacord
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;

//json
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.lang.Thread;
import java.util.HashMap;
import java.util.Optional;

public class discordPlugin extends Plugin{
    final Long CDT = 300L;
    public JSONObject data; //token, channel_id, role_id
    public DiscordApi api;
    private HashMap<Long, String> cooldowns = new HashMap<Long, String>(); //uuid

    //register event handlers and create variables in the constructor
    public discordPlugin() {
        try {
            String pureJson = Core.settings.getDataDirectory().child("mods/settings.json").readString();
            data = new JSONObject(new JSONTokener(pureJson));
        } catch (Exception e) {
            System.out.println("[ERR!] discordplugin: " + e.toString());
        }
        api = new DiscordApiBuilder().setToken(data.getString("token").trim()).login().join();
        BotThread bt = new BotThread(api, Thread.currentThread());
        bt.setDaemon(false);
        bt.start();
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){

    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("d", "<text...>", "Sends a message to discord.", (args, player) -> {
            TextChannel tc = this.getTextChannel(data.getString("channel_id"));
            if (tc == null){
                player.sendMessage("[scarlet]This command is disabled.");
                return;
            }
            tc.sendMessage(player.name +": " + args[0]);
        });

        handler.<Player>register("gr", "[player] [reason...]", "Report a griefer by id (use '/gr' to get a list of ids)", (args, player)->{
            //https://github.com/Anuken/Mindustry/blob/master/core/src/io/anuke/mindustry/core/NetServer.java#L300-L351

            for (Long key: cooldowns.keySet()) {
                if (key + CDT < System.currentTimeMillis() / 1000L) {
                    cooldowns.remove(key);
                    continue;
                } else if (player.uuid == cooldowns.get(key)) {
                    player.sendMessage("[scarlet]This command is on a 5 minute cooldown!");
                    return;
                }
            }

            if (args.length == 0){
                StringBuilder builder = new StringBuilder();
                builder.append("[orange]List or reportable players: \n");
                for(Player p : Vars.playerGroup.all()){
                    if(p.isAdmin || p.con == null) continue;

                    builder.append("[lightgray] ").append(p.name).append("[accent] (#").append(p.id).append(")\n");
                }
                player.sendMessage(builder.toString());
            } else {
                Player found;
                if(args[0].length() > 1 && args[0].startsWith("#") && Strings.canParseInt(args[0].substring(1))){
                    int id = Strings.parseInt(args[0].substring(1));
                    found = Vars.playerGroup.find(p -> p.id == id);
                }else{
                    found = Vars.playerGroup.find(p -> p.name.equalsIgnoreCase(args[0]));
                }
                if(found != null){
                    if(found.isAdmin){
                        player.sendMessage("[scarlet]Did you really expect to be able to report an admin?");
                    }else if(found.getTeam() != player.getTeam()) {
                        player.sendMessage("[scarlet]Only players on your team can be reported.");
                    } else {
                        TextChannel tc = this.getTextChannel(data.getString("channel_id"));
                        Role r = this.getRole(data.getString("role_id"));
                        if (tc == null || r == null){
                            player.sendMessage("[scarlet]This command is disabled.");
                            return;
                        }
                        //send message
                        if (args.length > 1){
                            new MessageBuilder()
                                    .append(r)
                                    .setEmbed(new EmbedBuilder()
                                            .setTitle("Griefer online")
                                            .addField("name", found.name)
                                            .addField("reason", args[1])
                                            .setColor(Color.ORANGE)
                                            .setFooter("Reported by " + player.name))
                                    .send(tc);
                        } else {
                            new MessageBuilder()
                                    .append(r)
                                    .setEmbed(new EmbedBuilder()
                                            .setTitle("Griefer online")
                                            .addField("name", found.name)
                                            .setColor(Color.ORANGE)
                                            .setFooter("Reported by " + player.name))
                                    .send(tc);
                        }
                        Call.sendMessage(found.name + "[sky] is reported to discord.");
                        cooldowns.put(System.currentTimeMillis()/1000L, player.uuid);
                    };
                }else{
                    player.sendMessage("[scarlet]No player[orange] '" + args[0] + "'[scarlet] found.");
                }
            }
        });
    }

    public TextChannel getTextChannel(String id){
        Optional<Channel> dc =  ((Optional<Channel>)this.api.getChannelById(id));
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

    public Role getRole(String id){
        Optional<Role> r1 = this.api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] discordplugin: adminrole not found!");
            return null;
        }
        return r1.get();
    }
}