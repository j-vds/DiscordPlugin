package disc.command;

//mindustry + arc
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.gen.Call;

//javacord

import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.modules.ItemModule;
import org.javacord.api.entity.channel.GroupChannelUpdater;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;


public class comCommands implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event){
        String[] incoming_msg = event.getMessageContent().split("\\s+");

        switch (incoming_msg[0]){
            case "..chat":
                String[] msg = (event.getMessageContent().replace('\n', ' ')).split("\\s+", 2);
                Call.sendMessage("[sky]" +event.getMessageAuthor().getName()+ " @discord >[] " + msg[1].trim());
                break;
            case "..players":
                StringBuilder lijst = new StringBuilder();
                StringBuilder admins = new StringBuilder();
                lijst.append("players: " + Groups.player.size()+"\n");
                if(Groups.player.count(p->p.admin) > 0) {
                    admins.append("online admins: ");// + Vars.playerGroup.all().count(p->p.isAdmin)+"\n");
                }
                for (Player p : Groups.player){
                    if (p.admin()){
                        admins.append("* " + p.name.trim() + "\n");
                    } else {
                        lijst.append("* " + p.name.trim() + "\n");
                    }
                }
                new MessageBuilder().appendCode("", lijst.toString() + admins.toString()).send(event.getChannel());
                break;
            case "..info":
                try {
                    StringBuilder lijst2 = new StringBuilder();
                    lijst2.append("map: " + Vars.state.map.name() + "\n" + "author: " + Vars.state.map.author() + "\n");
                    lijst2.append("wave: " + Vars.state.wave + "\n");
                    lijst2.append("enemies: " + Vars.state.enemies + "\n");
                    lijst2.append("players: " + Groups.player.size() + '\n');
                    //lijst.append("admins (online): " + Vars.playerGroup.all().count(p -> p.isAdmin));
                    new MessageBuilder().appendCode("", lijst2.toString()).send(event.getChannel());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;
            case "..infores":
                //event.getChannel().sendMessage("not implemented yet...");
                if (!Vars.state.rules.waves){
                    event.getChannel().sendMessage("Only available when playing survivalmode!");
                    return;
                } else if(Groups.player.isEmpty()) {
                    event.getChannel().sendMessage("No players online!");
                } else {
                    StringBuilder lijst3 = new StringBuilder();
                    lijst3.append("amount of items in the core\n\n");
                    ItemModule core = Groups.player.first().core().items;
                    lijst3.append("copper: " + core.get(Items.copper) + "\n");
                    lijst3.append("lead: " + core.get(Items.lead) + "\n");
                    lijst3.append("graphite: " + core.get(Items.graphite) + "\n");
                    lijst3.append("metaglass: " + core.get(Items.metaglass) + "\n");
                    lijst3.append("titanium: " + core.get(Items.titanium) + "\n");
                    lijst3.append("thorium: " + core.get(Items.thorium) + "\n");
                    lijst3.append("silicon: " + core.get(Items.silicon) + "\n");
                    lijst3.append("plastanium: " + core.get(Items.plastanium) + "\n");
                    lijst3.append("phase fabric: " + core.get(Items.phaseFabric) + "\n");
                    lijst3.append("surge alloy: " + core.get(Items.surgeAlloy) + "\n");

                    new MessageBuilder().appendCode("", lijst3.toString()).send(event.getChannel());
                }
                break;
            default:
                break;
        }
    }
}
