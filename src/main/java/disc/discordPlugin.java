package disc;

import arc.Core;
import arc.Events;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.util.CommandHandler;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.gen.Call;

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;

//json
import org.json.JSONObject;
import org.json.JSONTokener;


import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Paths;

import static disc.discConstants.*;
import static disc.utilmethods.*;

public class discordPlugin extends Plugin {
    private final long CDT = 300L;
    private DiscordApi api = null;
    private ObjectMap<Long, String> cooldowns = new ObjectMap<>(); //uuid


    private boolean invalidConfig = false;
    public ObjectMap<String, Role> discRoles = new ObjectMap<>();
    public ObjectMap<String, TextChannel> discChannels = new ObjectMap<>();

    //private JSONObject config;
    private String totalPath;
    public String servername;


    //register event handlers and create variables in the constructor
    public discordPlugin() {
        //getting the config file:
        Fi path = Core.settings.getDataDirectory().child(diPath);
        totalPath = path.child(fileName).absolutePath();

        JSONObject config = null;

        if(path.exists()){
            discLog("PATH EXISTS");
            String pureJSON = Fi.get(totalPath).readString();

            config = new JSONObject(new JSONTokener(pureJSON));
            if(!config.has("version")){
                makeSettingsFile();
            }else if(config.getInt("version") < VERSION){
                discLog("configfile: VERSION");
                makeSettingsFile();
            }
        }else{
            makeSettingsFile();
        }

        if(config == null || invalidConfig) return;

        readSettingsFile(config);

        BotThread bt = new BotThread(this, Thread.currentThread());
        bt.setDaemon(true); //false
        bt.start();

        //live chat
        TextChannel tc = discChannels.get("live_chat_channel_id");
        if (tc != null) {
            Events.on(EventType.PlayerChatEvent.class, event -> {
                tc.sendMessage("**" + event.player.name.replace('*', '+') + "**: " + event.message);
            });
        }
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){

    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler) {
        if (api != null) return;

        TextChannel tc_d = discChannels.get("dchannel_id");
        if (tc_d != null) {
            discLog("- Command '/d' enabled");
            handler.<Player>register("d", "<text...>", "Sends a message to discord.", (args, player) -> {
                tc_d.sendMessage(player.name + " *@mindustry* : " + args[0]);
                Call.sendMessage(player.name + "[sky] to @discord[]: " + args[0]);
            });
        }

        TextChannel tc_c = discChannels.get("channel_id");
        Role ro = discRoles.get("role_id", (Role) null);
        if (tc_c != null && ro != null) {
            discLog("- Command '/gr' enabled");
            handler.<Player>register("gr", "[player] [reason...]", "Report a griefer by id (use '/gr' to get a list of ids)", (args, player) -> {
                //https://github.com/Anuken/Mindustry/blob/master/core/src/io/anuke/mindustry/core/NetServer.java#L300-L351

                for (Long key : cooldowns.keys()) {
                    if (key + CDT < System.currentTimeMillis() / 1000L) {
                        cooldowns.remove(key);
                        continue;
                    } else if (player.uuid() == cooldowns.get(key)) {
                        player.sendMessage("[scarlet]This command is on a 5 minute cooldown!");
                        return;
                    }
                }

                if (args.length == 0) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("[orange]List or reportable players: \n");
                    for (Player p : Groups.player) {
                        if (p.admin() || p.con == null) continue;

                        builder.append("[lightgray] ").append(p.name).append("[accent] (#").append(p.id).append(")\n");
                    }
                    player.sendMessage(builder.toString());
                } else {
                    Player found = null;
                    if (args[0].length() > 1 && args[0].startsWith("#") && Strings.canParseInt(args[0].substring(1))) {
                        int id = Strings.parseInt(args[0].substring(1));
                        for (Player p : Groups.player) {
                            if (p.id == id) {
                                found = p;
                                break;
                            }
                        }
                    } else {
                        for (Player p : Groups.player) {
                            if (p.name.equalsIgnoreCase(args[0])) {
                                found = p;
                                break;
                            }
                        }
                    }
                    if (found != null) {
                        if (found.admin()) {
                            player.sendMessage("[scarlet]Did you really expect to be able to report an admin?");
                        } else if (found.team() != player.team()) {
                            player.sendMessage("[scarlet]Only players on your team can be reported.");
                        } else {
                            //send message
                            if (args.length > 1) {
                                new MessageBuilder()
                                        .setEmbed(new EmbedBuilder()
                                                .setTitle("Potential griefer online")
                                                .setDescription(ro.getMentionTag())
                                                .addField("name", found.name)
                                                .addField("reason", args[1])
                                                .setColor(Color.ORANGE)
                                                .setFooter("Reported by " + player.name))
                                        .send(tc_c);
                            } else {
                                new MessageBuilder()
                                        .setEmbed(new EmbedBuilder()
                                                .setTitle("Potential griefer online")
                                                .setDescription(ro.getMentionTag())
                                                .addField("name", found.name)
                                                .setColor(Color.ORANGE)
                                                .setFooter("Reported by " + player.name))
                                        .send(tc_c);
                            }
                            Call.sendMessage(found.name + "[sky] is reported to discord.");
                            cooldowns.put(System.currentTimeMillis() / 1000L, player.uuid());
                        }
                    } else {
                        player.sendMessage("[scarlet]No player[orange] '" + args[0] + "'[scarlet] found.");
                    }
                }
            });
        }
    }

    //getters
    public DiscordApi getAPI(){
        return this.api;
    }


    private void readSettingsFile(JSONObject obj){
        if(obj.has("token")){
            try {
                api = new DiscordApiBuilder().setToken(obj.getString("token")).login().join();
                discLog("Valid token");
            }catch (Exception e){
                if (e.getMessage().contains("READY packet")){
                    discLog("invalid token");
                } else {
                    e.printStackTrace();
                }
                invalidConfig = true;
                return;
            }
        }else{
            invalidConfig = true;
            return;
        }

        if(obj.has("channel_ids")){
            JSONObject temp = obj.getJSONObject("channel_ids");
            for(String field : temp.keySet()){
                discChannels.put(field, getTextChannel(api, temp.getString(field)));
            }
        }

        if(obj.has("role_ids")){
            JSONObject temp = obj.getJSONObject("role_ids");
            for(String field : temp.keySet()){
                discRoles.put(field, getRole(api, temp.getString(field)));
            }
        }

        if(obj.has("servername")){
            servername = obj.getString("servername");
        }else{
            servername = "";
        }

        discLog("config loaded");
    }


    private void makeSettingsFile(){
        discLog("CREATING JSON FILE");
        Fi directory = Core.settings.getDataDirectory().child(diPath);
        if(!directory.isDirectory()){
            directory.mkdirs();
        }

        JSONObject config = new JSONObject();
        config.put("info", "more info available on: https://github.com/J-VdS/DiscordPlugin");
        config.put("version", VERSION);

        config.put("servername", "name of your server - can be empty");

        config.put("token", "put your token here");

        JSONObject channels = new JSONObject();
        channels.put("dchannel_id", "messages using /d will be send to this channel - can be empty");
        channels.put("channel_id", "id of the channel where /gr reports will appear - can be empty");
        channels.put("live_chat_channel_id", "id of the channel where live chat will appear - can be empty");

        config.put("channel_ids", channels);

        JSONObject roles = new JSONObject();
        String[] discordRoles = {
                "closeServer_role_id",
                "gameOver_role_id",
                "changeMap_role_id",
                "serverDown_role_id"
        };
        for (String field : discordRoles){
            roles.put(field, "");
        }

        config.put("role_ids", roles);

        discLog("Creating config.json");
        try{
            Files.write(Paths.get(totalPath), config.toString().getBytes());
        }catch (Exception e){
            discLog("Failed to create config.json");
        }
    }
}