package nl.lolmen.apply;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Listeners implements Listener{

	private Main plugin;

	public Listeners(Main plugin){
		this.plugin = plugin;
	}

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

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		final String name = event.getPlayer().getName();
		if(event.getPlayer().hasPermission("apply.check")){

			ResultSet set = plugin.mysql.executeQuery("SELECT * FROM " + plugin.set.getTable() + " WHERE promoted=0");
			if(set == null){
				return;
			}
			try {
				set.last();
				Bukkit.getPlayer(name).sendMessage("There are " + set.getRow() + " applications waiting!");
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

}
