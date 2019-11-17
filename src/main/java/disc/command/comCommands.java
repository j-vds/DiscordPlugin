package disc.command;

//mindustry + arc
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.entities.type.Player;
import io.anuke.mindustry.gen.Call;

//javacord

import io.anuke.mindustry.world.modules.ItemModule;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;


public class comCommands implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event){
        if (event.getMessageContent().startsWith("..chat ")){
            //discord -> server
            String[] msg = event.getMessageContent().split(" ", 2);
            Call.sendMessage("[sky]" +event.getMessageAuthor().getName()+ " @discord >[] " + msg[1].trim());
        }

        //playerlist
        else if (event.getMessageContent().equalsIgnoreCase("..players")){
            StringBuilder lijst = new StringBuilder();
            StringBuilder admins = new StringBuilder();
            lijst.append("players: " + Vars.playerGroup.size()+"\n");
            admins.append("online admins: " + Vars.playerGroup.all().count(p->p.isAdmin)+"\n");
            for (Player p :Vars.playerGroup.all()){
                if (p.isAdmin){
                    admins.append("* " + p.name.trim() + "\n");
                } else {
                    lijst.append("* " + p.name.trim() + "\n");
                }
            }
            new MessageBuilder().appendCode("", lijst.toString() + admins.toString()).send(event.getChannel());
        }
        //info
        else if (event.getMessageContent().equalsIgnoreCase("..info")){
            try {
                StringBuilder lijst = new StringBuilder();
                lijst.append("map: " + Vars.world.getMap().name() + "\n" + "author: " + Vars.world.getMap().author() + "\n");
                lijst.append("wave: " + Vars.state.wave + "\n");
                lijst.append("enemies: " + Vars.state.enemies + "\n");
                lijst.append("players: " + Vars.playerGroup.size() + '\n');
                lijst.append("admins (online): " + Vars.playerGroup.all().count(p -> p.isAdmin));
                new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        //infores, werkt enkel als er minstens 1 speler online is!
        else if (event.getMessageContent().equalsIgnoreCase("..infores")){
            //event.getChannel().sendMessage("not implemented yet...");
            if (!Vars.state.rules.waves){
                event.getChannel().sendMessage("Only available when playing survivalmode!");
                return;
            } else if(Vars.playerGroup.isEmpty()) {
                event.getChannel().sendMessage("No players online!");
            } else {
                StringBuilder lijst = new StringBuilder();
                lijst.append("amount of items in the core\n\n");
                ItemModule core = Vars.playerGroup.all().get(0).getClosestCore().items;
                lijst.append("copper: " + core.get(Items.copper) + "\n");
                lijst.append("lead: " + core.get(Items.lead) + "\n");
                lijst.append("graphite: " + core.get(Items.graphite) + "\n");
                lijst.append("metaglass: " + core.get(Items.metaglass) + "\n");
                lijst.append("titanium: " + core.get(Items.titanium) + "\n");
                lijst.append("thorium: " + core.get(Items.thorium) + "\n");
                lijst.append("silicon: " + core.get(Items.silicon) + "\n");
                lijst.append("plastanium: " + core.get(Items.plastanium) + "\n");
                lijst.append("phase fabric: " + core.get(Items.phasefabric) + "\n");
                lijst.append("surge alloy: " + core.get(Items.surgealloy) + "\n");

                new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
            }


        }

    }
}
