package nl.lolmen.apply;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
		if(event.getPlayer().hasPermission("apply.check")){
			final String name = event.getPlayer().getName();
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
	
	private MySQL getMySQL(){
		return this.getPlugin().mysql;
	}
	
	private Main getPlugin(){
		return this.plugin;
	}
	
	private String getTable(){
		return this.getPlugin().set.getTable();
	}
	
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.isCancelled()){
			return;
		}
		if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(!event.getClickedBlock().getType().equals(Material.WALL_SIGN)){
				return;
			}
			Sign s = (Sign)event.getClickedBlock().getState();
			if(!s.getLine(0).equalsIgnoreCase("[Apply]")){
				return;
			}
			//Check if the player already is applied, applying or whatever
			ResultSet set = this.getMySQL().executeQuery("SELECT * FROM " + this.getTable() + " WHERE player='" + event.getPlayer().getName() + "' LIMIT 1");
			if(set == null){
				event.getPlayer().sendMessage("Something went wrong with MySQL.. not sure why.");
				return;
			}
			try {
				while(set.next()){
					boolean promoted = (set.getInt("promoted") == 0 ? false : true);
					if(promoted){
						if(event.getPlayer().hasPermission("apply.check")){
							this.getPlugin().getServer().dispatchCommand(this.getPlugin().getServer().getConsoleSender(), "apply");
						}else{
							event.getPlayer().sendMessage("You've already been promoted, by " + set.getString("promoter"));
						}
						return;
					}else{
						if(set.getString("country")!= null){
							event.getPlayer().sendMessage("A moderator will look at your application soon!");
						}else{
							event.getPlayer().sendMessage("You have to finish the application first!");
						}
						return;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
