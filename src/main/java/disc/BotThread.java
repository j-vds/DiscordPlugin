package disc;

import disc.command.comCommands;
import org.javacord.api.DiscordApi;
import java.lang.Thread;

public class BotThread extends Thread{
    public DiscordApi api;
    private Thread mt;

    public BotThread(DiscordApi _api, Thread _mt) {
        api = _api; //new DiscordApiBuilder().setToken(data.get(0)).login().join();
        mt = _mt;
        // Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().equalsIgnoreCase("!ping")) {
                event.getChannel().sendMessage("Pong!");
            }
        });

        //communication commands
        api.addMessageCreateListener(new comCommands());
    }

    public void run(){
        while (this.mt.isAlive()){
            try{
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }

        api.disconnect();
    }
}
