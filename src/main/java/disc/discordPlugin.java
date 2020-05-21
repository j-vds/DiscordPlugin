package disc;


//javacord
import arc.Core;
import arc.Events;

import arc.util.CommandHandler;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.gen.Call;

import mindustry.plugin.Plugin;
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

public class discordPlugin extends Plugin {
    private final Long CDT = 300L;
    private final String FileNotFoundErrorMessage = "File not found: config\\mods\\settings.json";
    private JSONObject alldata;
    private JSONObject data; //token, channel_id, role_id
    private DiscordApi api = null;
    private HashMap<Long, String> cooldowns = new HashMap<Long, String>(); //uuid

    //register event handlers and create variables in the constructor
    public discordPlugin() {
        try {
            String pureJson = Core.settings.getDataDirectory().child("mods/settings.json").readString();
            alldata = new JSONObject(new JSONTokener(pureJson));
            if (!alldata.has("in-game")){
                System.out.println("[ERR!] discordplugin: settings.json has an invalid format!\n");
                //this.makeSettingsFile("settings.json");
                return;
            } else {
                data = alldata.getJSONObject("in-game");
            }
        } catch (Exception e) {
            if (e.getMessage().contains(FileNotFoundErrorMessage)){
                System.out.println("[ERR!] discordplugin: settings.json file is missing.\nBot can't start.");
                //this.makeSettingsFile("settings.json");
                return;
            } else {
                System.out.println("[ERR!] discordplugin: Init Error");
                e.printStackTrace();
                return;
            }
        }
        try {
            api = new DiscordApiBuilder().setToken(alldata.getString("token")).login().join();
        }catch (Exception e){
            if (e.getMessage().contains("READY packet")){
                System.out.println("\n[ERR!] discordplugin: invalid token.\n");
            } else {
                e.printStackTrace();
            }
        }
        BotThread bt = new BotThread(api, Thread.currentThread(), alldata.getJSONObject("discord"));
        bt.setDaemon(false);
        bt.start();

        //live chat
        if (data.has("live_chat_channel_id")) {
            TextChannel tc = this.getTextChannel(data.getString("live_chat_channel_id"));
            if (tc != null) {
                Events.on(EventType.PlayerChatEvent.class, event -> {
                    tc.sendMessage("**" + event.player.name.replace('*', '+') + "**: " + event.message);
                });
            }
        }
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){

    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        if (api != null) {
            handler.<Player>register("d", "<text...>", "Sends a message to discord.", (args, player) -> {

                if (!data.has("dchannel_id")) {
                    player.sendMessage("[scarlet]This command is disabled.");
                } else {
                    TextChannel tc = this.getTextChannel(data.getString("dchannel_id"));
                    if (tc == null) {
                        player.sendMessage("[scarlet]This command is disabled.");
                        return;
                    }
                    tc.sendMessage(player.name + " *@mindustry* : " + args[0]);
                    Call.sendMessage(player.name + "[sky] to @discord[]: " + args[0]);
                }

            });

            handler.<Player>register("gr", "[player] [reason...]", "Report a griefer by id (use '/gr' to get a list of ids)", (args, player) -> {
                //https://github.com/Anuken/Mindustry/blob/master/core/src/io/anuke/mindustry/core/NetServer.java#L300-L351
                if (!(data.has("channel_id") && data.has("role_id"))) {
                    player.sendMessage("[scarlet]This command is disabled.");
                    return;
                }

                //if (true) return; //some things broke in arc and or Vars.playergroup

                for (Long key : cooldowns.keySet()) {
                    if (key + CDT < System.currentTimeMillis() / 1000L) {
                        cooldowns.remove(key);
                        continue;
                    } else if (player.uuid == cooldowns.get(key)) {
                        player.sendMessage("[scarlet]This command is on a 5 minute cooldown!");
                        return;
                    }
                }

                if (args.length == 0) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("[orange]List or reportable players: \n");
                    for (Player p : Vars.playerGroup.all()) {
                        if (p.isAdmin || p.con == null) continue;

                        builder.append("[lightgray] ").append(p.name).append("[accent] (#").append(p.id).append(")\n");
                    }
                    player.sendMessage(builder.toString());
                } else {
                    Player found = null;
                    if (args[0].length() > 1 && args[0].startsWith("#") && Strings.canParseInt(args[0].substring(1))) {
                        int id = Strings.parseInt(args[0].substring(1));
                        //found = Vars.playerGroup.find(p -> p.id == id);
                        for (Player p: Vars.playerGroup.all()){
                            if (p.id == id){
                                found = p;
                                break;
                            }
                        }
                    } else {
                        for (Player p: Vars.playerGroup.all()){
                            if (p.name.equalsIgnoreCase(args[0])){
                                found = p;
                                break;
                            }
                        }
                        //found = Vars.playerGroup.find(p -> p.name.equalsIgnoreCase(args[0]));
                    }
                    if (found != null) {
                        if (found.isAdmin) {
                            player.sendMessage("[scarlet]Did you really expect to be able to report an admin?");
                        } else if (found.getTeam() != player.getTeam()) {
                            player.sendMessage("[scarlet]Only players on your team can be reported.");
                        } else {
                            TextChannel tc = this.getTextChannel(data.getString("channel_id"));
                            Role r = this.getRole(data.getString("role_id"));
                            if (tc == null || r == null) {
                                player.sendMessage("[scarlet]This command is disabled.");
                                return;
                            }
                            //send message
                            if (args.length > 1) {
                                new MessageBuilder()
                                        .setEmbed(new EmbedBuilder()
                                                .setTitle("Potential griefer online")
                                                .setDescription(r.getMentionTag())
                                                .addField("name", found.name)
                                                .addField("reason", args[1])
                                                .setColor(Color.ORANGE)
                                                .setFooter("Reported by " + player.name))
                                        .send(tc);
                            } else {
                                new MessageBuilder()
                                        .setEmbed(new EmbedBuilder()
                                                .setTitle("Potential griefer online")
                                                .setDescription(r.getMentionTag())
                                                .addField("name", found.name)
                                                .setColor(Color.ORANGE)
                                                .setFooter("Reported by " + player.name))
                                        .send(tc);
                            }
                            Call.sendMessage(found.name + "[sky] is reported to discord.");
                            cooldowns.put(System.currentTimeMillis() / 1000L, player.uuid);
                        }
                    } else {
                        player.sendMessage("[scarlet]No player[orange] '" + args[0] + "'[scarlet] found.");
                    }
                }
            });
        }
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
    /*
    private void makeSettingsFile(String _name){
        JSONObject obj = new JSONObject();
        obj.put("token", "put your token here");

        JSONObject inGame = new JSONObject();
        inGame.put("dchannel_id", "");
        inGame.put("channel_id", "");
        inGame.put("role_id", "");

        obj.put("in-game", inGame);

        JSONObject discord = new JSONObject();
        String[] discordFields = {
                "closeServer_role_id",
                "gameOver_role_id",
                "changeMap_role_id",
                "serverdown_role_id",
                "serverdown_name"
        };
        for (String fname : discordFields){
            discord.put(fname, "");
        }
        obj.put("discord", discord);

        //make file
        Path path = Paths.get(String.valueOf(Core.settings.getDataDirectory().child("mods/"+_name)));
        try {
            PrintWriter writer = new PrintWriter(path.toString(), "UTF-8");
            writer.println(obj.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }*/
}