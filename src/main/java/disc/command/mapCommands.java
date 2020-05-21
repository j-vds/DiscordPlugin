//package disc.command;
//
//import io.anuke.arc.Core;
//import io.anuke.arc.Events;
//import io.anuke.arc.collection.Array;
//import io.anuke.arc.files.FileHandle;
//import io.anuke.mindustry.Vars;
//import io.anuke.mindustry.game.EventType;
//import io.anuke.mindustry.game.Team;
//import io.anuke.mindustry.io.SaveIO;
//import io.anuke.mindustry.maps.Map;
//import org.javacord.api.DiscordApi;
//import org.javacord.api.entity.message.MessageAttachment;
//import org.javacord.api.entity.message.MessageBuilder;
//import org.javacord.api.entity.permission.Role;
//import org.javacord.api.event.message.MessageCreateEvent;
//import org.javacord.api.listener.message.MessageCreateListener;
//import org.json.JSONObject;
//
//import java.io.ByteArrayInputStream;
//import java.io.DataInputStream;
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//import java.util.zip.InflaterInputStream;
//
//public class mapCommands implements MessageCreateListener {
//    final long minMapChangeTime = 30L; //30 seconds
//    final String commandDisabled = "This command is disabled.";
//    final String noPermission = "You don't have permissions to use this command!";
//
//    private JSONObject data;
//    private long lastMapChange = 0L;
//
//
//    public mapCommands(JSONObject _data) {
//        this.data = _data;
//    }
//
//    @Override
//    public void onMessageCreate(MessageCreateEvent event) {
//        if (event.getMessageContent().equalsIgnoreCase("..maps")) {
//            Vars.maps.reload();
//            StringBuilder mapLijst = new StringBuilder();
//            mapLijst.append("List of available maps:\n");
//            for (Map m : Vars.maps.customMaps()) {
//                mapLijst.append("* " + m.name() + "/ " + m.width + " x " + m.height + "\n");
//            }
//            mapLijst.append("Total number of maps: " + Vars.maps.customMaps().size);
//            new MessageBuilder().appendCode("", mapLijst.toString()).send(event.getChannel());
//
//        } else if (event.getMessageContent().startsWith("..changemap")) {
//            if (!data.has("changeMap_role_id")) {
//                if (event.isPrivateMessage()) return;
//                event.getChannel().sendMessage(commandDisabled);
//                return;
//            }
//            Role r = getRole(event.getApi(), data.getString("changeMap_role_id"));
//            if (!hasPermission(r, event)) return;
//
//            if (System.currentTimeMillis() / 1000L - this.lastMapChange < this.minMapChangeTime) {
//                if (event.isPrivateMessage()) return;
//                event.getChannel().sendMessage(String.format("This commands has a %d s cooldown.", this.minMapChangeTime));
//                return;
//            }
//
//            //Vars.maps.removeMap(Vars.maps.customMaps().get(0)); //will delete a file
//            String[] splitted = event.getMessageContent().split(" ", 2);
//            if (splitted.length == 1) {
//                int index = 1;
//                StringBuilder sb = new StringBuilder();
//                for (Map m : Vars.maps.customMaps()) {
//                    sb.append(index++ + " : " + m.name() + "\n");
//                }
//                sb.append("\nUse ..changemap <number/name>");
//                new MessageBuilder().appendCode("", sb.toString()).send(event.getChannel());
//            } else {
//                //try number
//                Map found = null;
//                try {
//                    splitted[1] = splitted[1].trim();
//                    found = Vars.maps.customMaps().get(Integer.parseInt(splitted[1]) - 1);
//                } catch (Exception e) {
//                    //check if map exits
//                    for (Map m : Vars.maps.customMaps()) {
//                        if (m.name().equals(splitted[1])) {
//                            found = m;
//                            break;
//                        }
//                    }
//                }
//                if (found == null) {
//                    event.getChannel().sendMessage("Map not found...");
//                    return;
//                }
//                ;
//
//                FileHandle temp = Core.settings.getDataDirectory().child("maps").child("temp");
//                temp.mkdirs();
//
//                for (Map m1 : Vars.maps.customMaps()) {
//                    if (m1.equals(Vars.world.getMap())) continue;
//                    if (m1.equals(found)) continue;
//                    m1.file.moveTo(temp);
//                }
//                //reload all maps from that folder
//                Vars.maps.reload();
//                //Call gameover
//                Events.fire(new EventType.GameOverEvent(Team.crux));
//                //move maps
//                Vars.maps.reload();
//                FileHandle mapsDir = Core.settings.getDataDirectory().child("maps");
//                for (FileHandle fh : temp.list()) {
//                    fh.moveTo(mapsDir);
//                }
//                temp.deleteDirectory();
//                Vars.maps.reload();
//
//                event.getChannel().sendMessage("Next map selected: " + found.name() + "\nThe current map will change in 10 seconds.");
//
//                this.lastMapChange = System.currentTimeMillis() / 1000L;
//            }
//
//            /*
//            String[] splittedArg = event.getMessageContent().split(" ");
//            if (splittedArg.length != 2){
//                if (event.isPrivateMessage()) return;
//                event.getChannel().sendMessage("*Invalid command* \nuse `..changemap <name>`");
//            } else if (splittedArg.length == 2) {
//                //find map
//                //https://github.com/Anuken/Mindustry/blob/master/server/src/io/anuke/mindustry/server/ServerControl.java#L142
//                Map map = Vars.maps.all().find(m -> m.name() == splittedArg[1]);
//                if (map == null) {
//                    if (event.isPrivateMessage()) return;
//                    event.getChannel().sendMessage(splittedArg[1] + " map not found.");
//                } else {
//                    Call.onInfoMessage("Next selected map:[accent] " + map.name() + "[]" +
//                            (map.tags.containsKey("author") && !map.tags.get("author").trim().isEmpty() ? " by[accent] " + map.author() + "[]" : "") + ".");
//                    //make maps smaller
//                    //call gameover
//                    //wait x seconds to repopulate maps
//
//                }
//            }*/
//
//        } else if (event.getMessageContent().equals("..uploadmap")) {
//            if (!data.has("mapConfig_role_id")) {
//                if (event.isPrivateMessage()) return;
//                event.getChannel().sendMessage(commandDisabled);
//                return;
//            }
//            Role r = getRole(event.getApi(), data.getString("mapConfig_role_id"));
//            if (!hasPermission(r, event)) return;
//
//            Array<MessageAttachment> ml = new Array<MessageAttachment>();
//            for (MessageAttachment ma : event.getMessageAttachments()) {
//                if (ma.getFileName().split("\\.", 2)[1].trim().equals("msav")) {
//                    ml.add(ma);
//                }
//            }
//            if (ml.size != 1) {
//                if (event.isPrivateMessage()) return;
//                event.getChannel().sendMessage("You need to add 1 valid .msav file!");
//                return;
//            } else if (Core.settings.getDataDirectory().child("maps").child(ml.get(0).getFileName()).exists()) {
//                if (event.isPrivateMessage()) return;
//                event.getChannel().sendMessage("There is already a map with this name on the server!");
//                return;
//            }
//            //more custom filename checks possible
//
//            CompletableFuture<byte[]> cf = ml.get(0).downloadAsByteArray();
//            FileHandle fh = Core.settings.getDataDirectory().child("maps").child(ml.get(0).getFileName());
//
//            try {
//                byte[] data = cf.get();
//                if (!SaveIO.isSaveValid(new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data))))) {
//                    if (event.isPrivateMessage()) return;
//                    event.getChannel().sendMessage("invalid .msav file!");
//                    return;
//                }
//                fh.writeBytes(cf.get(), false);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Vars.maps.reload();
//            event.getChannel().sendMessage(ml.get(0).getFileName() + " added succesfully!");
//
//        } else if (event.getMessageContent().startsWith("..removemap")) {
//            if (!data.has("mapConfig_role_id")) {
//                if (event.isPrivateMessage()) return;
//                event.getChannel().sendMessage(commandDisabled);
//                return;
//            }
//            Role r = getRole(event.getApi(), data.getString("mapConfig_role_id"));
//            if (!hasPermission(r, event)) return;
//
//            //Vars.maps.removeMap(Vars.maps.customMaps().get(0)); //will delete a file
//            String[] splitted = event.getMessageContent().split(" ", 2);
//            if (splitted.length == 1) {
//                int index = 1;
//                StringBuilder sb = new StringBuilder();
//                for (Map m : Vars.maps.customMaps()) {
//                    sb.append(index++ + " : " + m.name() + "\n");
//                }
//                sb.append("\nUse ..removemap <number/name>");
//                new MessageBuilder().appendCode("", sb.toString()).send(event.getChannel());
//            } else {
//                //try number
//                Map found = null;
//                try {
//                    splitted[1] = splitted[1].trim();
//                    found = Vars.maps.customMaps().get(Integer.parseInt(splitted[1]) - 1);
//                } catch (Exception e) {
//                    //check if map exits
//                    for (Map m : Vars.maps.customMaps()) {
//                        if (m.name().equals(splitted[1])) {
//                            found = m;
//                            break;
//                        }
//                    }
//                }
//                if (found == null) {
//                    event.getChannel().sendMessage("Map not found...");
//                    return;
//                }
//                Vars.maps.removeMap(found);
//                Vars.maps.reload();
//
//                event.getChannel().sendMessage("Deleted succesfully: " + found.name());
//
//            }
//        }
//    }
//
//    public Role getRole(DiscordApi api, String id){
//        Optional<Role> r1 = api.getRoleById(id);
//        if (!r1.isPresent()) {
//            System.out.println("[ERR!] discordplugin: role not found!");
//            return null;
//        }
//        return r1.get();
//    }
//
//    public Boolean hasPermission(Role r, MessageCreateEvent event){
//        try {
//            if (r == null) {
//                if (event.isPrivateMessage()) return false;
//                event.getChannel().sendMessage(commandDisabled);
//                return false;
//            } else if (!event.getMessageAuthor().asUser().get().getRoles(event.getServer().get()).contains(r)) {
//                if (event.isPrivateMessage()) return false;
//                event.getChannel().sendMessage(noPermission);
//                return false;
//            } else {
//                return true;
//            }
//        } catch (Exception _){
//            return false;
//        }
//    }
//}
//
