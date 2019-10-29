package disc.command;

//mindustry + arc
import io.anuke.arc.collection.Array;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.type.Player;
import io.anuke.mindustry.gen.Call;

//javacord
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class comCommands implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event){
        if (event.getMessageContent().startsWith("..chat ")){
            //discord -> server
            String[] msg = event.getMessageContent().split(" ", 2);
            Call.sendMessage("[sky]" +event.getMessageAuthor().getName()+ " @discord > " + msg[1].trim());
        }
        //gameover

        //playerlist
        else if (event.getMessageContent().equalsIgnoreCase("..players")){
            System.out.println("..players called");
            StringBuilder lijst = new StringBuilder();
            lijst.append("players: " + Vars.playerGroup.size()+"\n");
            lijst.append("online admins: " + Vars.playerGroup.all().count(p->p.isAdmin)+"\n");
            for (Player p :Vars.playerGroup.all()){
                lijst.append("* " + p.name.trim() + "\n");
            }
            new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
        }

    }
}
