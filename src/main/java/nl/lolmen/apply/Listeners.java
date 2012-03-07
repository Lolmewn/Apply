package nl.lolmen.apply;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class Listeners implements Listener{
	
	@EventHandler
	public void onSignChange(SignChangeEvent event){
		if(event.isCancelled()){
			return;
		}
		if(event.getLine(0).equalsIgnoreCase("[Apply]")){
			if(!event.getPlayer().hasPermission("apply.createSign")){
				event.getPlayer().sendMessage("You can't do that :O");
				event.setCancelled(true);
				return;
			}
			event.setLine(1, ChatColor.GREEN + "Hit this sign");
			event.setLine(2, ChatColor.GREEN + "to apply to Cent");
			event.setLine(3, ChatColor.GREEN + "and start fun!");
		}
	}

}
