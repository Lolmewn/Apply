package nl.lolmen.apply;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import nl.lolmen.apply.Applicant.todo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin {

    private PermissionManager perm;
    protected HashMap<String, Applicant> list = new HashMap<String, Applicant>();
    private HashMap<String, String> lookingat = new HashMap<String, String>();
    private Settings set;
    private MySQL mysql;

    @Override
    public void onDisable() {
        this.mysql.close();
    }

    @Override
    public void onEnable() {
        new File("plugins/Apply/").mkdir();
        //new File("plugins/Apply/apps/").mkdir();
        this.checkPerm();
        this.set = new Settings();
        this.getServer().getPluginManager().registerEvents(new Listeners(this), this);
        this.mysql = new MySQL(
                this.set.getHost(),
                this.set.getPort(),
                this.set.getUsername(),
                this.set.getPassword(),
                this.set.getDatabase(),
                this.set.getTable());
    }

    protected MySQL getMySQL() {
        return this.mysql;
    }

    protected Settings getSettings() {
        return this.set;
    }

    private void checkPerm() {
        Plugin test = this.getServer().getPluginManager().getPlugin("PermissionsEx");
        if (test != null) {
            this.perm = PermissionsEx.getPermissionManager();
            this.getLogger().info("Permissions Plugin found! (PEX)");
        } else {
            this.getLogger().info("PEX not found! Disabling!");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("apply")) {
            return false;
        }
        if (sender.hasPermission("apply.check") && !this.list.containsKey(sender.getName())) {
            //has permission to check other's applications
            if (args.length == 0) {
                ResultSet resSet = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE promoted = 0 ORDER BY player");
                if (resSet == null) {
                    sender.sendMessage("It seems there was an error.. Check the logs.");
                    return true;
                }
                try {
                    while (resSet.next()) {
                        if (this.list.containsKey(resSet.getString("player"))) {
                            //is still busy
                            continue;
                        }
                        sender.sendMessage(ChatColor.RED + "IGN: " + ChatColor.WHITE + resSet.getString("player"));
                        sender.sendMessage(ChatColor.RED + "Banned: " + ChatColor.WHITE + resSet.getString("banned"));
                        sender.sendMessage(ChatColor.RED + "Good at: " + ChatColor.WHITE + resSet.getString("goodat"));
                        sender.sendMessage(ChatColor.RED + "Name: " + ChatColor.WHITE + resSet.getString("name"));
                        sender.sendMessage(ChatColor.RED + "Age: " + ChatColor.WHITE + resSet.getString("age"));
                        sender.sendMessage(ChatColor.RED + "Country: " + ChatColor.WHITE + resSet.getString("country"));
                        sender.sendMessage("Accept with /apply accept Reject with /apply deny");
                        this.lookingat.put(sender.getName(), resSet.getString("player"));
                        return true;
                    }
                    sender.sendMessage("No players to apply!");
                    return true;
                } catch (Exception e) {
                    sender.sendMessage("An error occured while reading the application!");
                    e.printStackTrace();
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("accept")) {
                if (!this.lookingat.containsKey(sender.getName())) {
                    sender.sendMessage("You have to see someone's application first. /apply");
                    return true;
                }
                String player = this.lookingat.get(sender.getName());
                ResultSet resSet = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE player='" + player + "'");
                if (resSet == null) {
                    sender.sendMessage("Well that's just weird.. " + player + " is not in the database O.o");
                    return true;
                }
                try {
                    while (resSet.next()) {
                        if (resSet.getInt("promoted") == 1) {
                            sender.sendMessage("Someone else already promoted him: " + resSet.getString("promoter"));
                            return true;
                        }
                        this.mysql.executeQuery("UPDATE " + this.set.getTable() + " SET "
                                + "promoter='" + sender.getName() + "', "
                                + "promoted=1, "
                                + "promotedTime='" + new Timestamp(new Date().getTime()) + "' "
                                + "WHERE player='" + player + "'");
                        if (!this.perm.getUser(player).inGroup("Non-Applied")) {
                            sender.sendMessage("He's not in the non-applied group anymore apparently!");
                            return true;
                        }
                        this.getServer().dispatchCommand(this.getServer().getConsoleSender(), "pex promote " + player + " Main");
                        Player prom = this.getServer().getPlayer(player);
                        if (prom == null || !prom.isOnline()) {
                            return true;
                        }
                        prom.sendMessage(ChatColor.RED + "You have been promoted by " + ChatColor.GREEN + sender.getName() + "!");
                        this.lookingat.remove(sender.getName());
                        return true;
                    }
                    sender.sendMessage("Well that's just weird.. " + player + " is not in the database O.o");
                    return true;
                } catch (SQLException e) {
                    sender.sendMessage("An error occured while reading the application!");
                    e.printStackTrace();
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("deny") || args[0].equalsIgnoreCase("reject")) {
                if (!this.lookingat.containsKey(sender.getName())) {
                    sender.sendMessage("You have to see someone's application first. /apply");
                    return true;
                }
                String player = this.lookingat.get(sender.getName());
                ResultSet resSet = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE player='" + player + "'");
                if (resSet == null) {
                    sender.sendMessage("Well that's just weird.. " + player + " is not in the database O.o");
                    return true;
                }
                try {
                    while (resSet.next()) {
                        if (resSet.getInt("promoted") == 1 || !this.perm.getUser(player).inGroup("Non-Applied")) {
                            sender.sendMessage("Someone else already promoted him: " + resSet.getString("promoter"));
                            return true;
                        }
                        this.mysql.executeQuery("DELETE FROM " + this.set.getTable() + " WHERE player='" + player + "'");
                        if (!this.perm.getUser(player).inGroup("Non-Applied")) {
                            sender.sendMessage("He's not in the non-applied group anymore apparently!");
                            return true;
                        }
                        Player prom = this.getServer().getPlayer(player);
                        if (prom == null || !prom.isOnline()) {
                            return true;
                        }
                        prom.sendMessage(ChatColor.RED + "Your application has been rejected, please apply again!");
                        this.lookingat.remove(sender.getName());
                        return true;
                    }
                    sender.sendMessage("Well that's just weird.. " + player + " is not in the database O.o");
                    return true;
                } catch (SQLException e) {
                    sender.sendMessage("An error occured while reading the application!");
                    e.printStackTrace();
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("lookup")) {
                if(!sender.hasPermission("apply.lookup")){
                    sender.sendMessage("You don't have permissions to do this!");
                    return true;
                }
                if (args.length == 1) {
                    sender.sendMessage("ERR: args. Correct usage: /apply lookup <player>");
                    return true;
                }
                String player = args[1];
                if (this.getServer().getPlayer(player) != null) {
                    player = this.getServer().getPlayer(player).getName();
                }
                ResultSet resSet = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE player='" + player + "' LIMIT 1");
                if (resSet == null) {
                    sender.sendMessage("This query returned null, sorry!");
                    return true;
                }
                try {
                    while (resSet.next()) {
                        sender.sendMessage(ChatColor.RED + "IGN: " + ChatColor.WHITE + resSet.getString("player"));
                        sender.sendMessage(ChatColor.RED + "Banned: " + ChatColor.WHITE + resSet.getString("banned"));
                        sender.sendMessage(ChatColor.RED + "Good at: " + ChatColor.WHITE + resSet.getString("goodat"));
                        sender.sendMessage(ChatColor.RED + "Name: " + ChatColor.WHITE + resSet.getString("name"));
                        sender.sendMessage(ChatColor.RED + "Age: " + ChatColor.WHITE + resSet.getString("age"));
                        sender.sendMessage(ChatColor.RED + "Country: " + ChatColor.WHITE + resSet.getString("country"));
                        sender.sendMessage(ChatColor.RED + "Promoted: " + ChatColor.WHITE + (resSet.getInt("promoted") == 0 ? "false" : "true"));
                        sender.sendMessage(ChatColor.RED + "Promoter: " + ChatColor.WHITE + (resSet.getString("promoter") == null ? "no-one" : resSet.getString("promoter")));
                        sender.sendMessage(ChatColor.RED + "PromoteTime: " + ChatColor.WHITE + (resSet.getTimestamp("promotedTime") == null ? "never" : resSet.getTimestamp("promotedTime").toString()));
                        return true;
                    }
                    sender.sendMessage("Player " + player + " apparently isn't in the database!");
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    sender.sendMessage("An error occured while reading the application!");
                    return true;
                }
            }
            sender.sendMessage("Unknown Apply command: /apply " + args[0]);
            return true;
        }
        //It's a normal player doing the command
        if (args.length == 0) {
            if (this.list.containsKey(sender.getName())) {
                //Confirms the data
                sender.sendMessage("Last thing you need to know: the rules.");
                this.list.get(sender.getName()).sendRules();
                this.list.get(sender.getName()).save(); //sends message to mods/admins
                this.list.remove(sender.getName());
                return true;
            }
            //check if he already applied
            ResultSet resSet = this.getMySQL().executeQuery("SELECT * FROM " + this.getSettings().getTable() + " WHERE player='" + sender.getName() + "' LIMIT 1");
            if (resSet == null) {
                sender.sendMessage("The query is null, sorry!");
                return true;
            }
            try {
                while (resSet.next()) {
                    if (resSet.getInt("promoted") == 1) {
                        //Already promoted
                        sender.sendMessage("You've already applied, and have been promoted by " + (resSet.getString("promoter") == null ? "no-one" : resSet.getString("promoter")));
                        return true;
                    }
                    sender.sendMessage("To apply, find a [Apply] sign!");
                    return true;
                }
                if (this.perm.getUser(sender.getName()).inGroup("Non-Applied")) {
                    sender.sendMessage("To apply, find a [Apply] sign!");
                    return true;
                }
                sender.sendMessage("You've already been promoted, but we've got no clue how :O");
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                sender.sendMessage("Something went terribly wrong while reading the data!");
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("reject") || args[0].equalsIgnoreCase("reset")) {
            if (this.list.containsKey(sender.getName())) {
                sender.sendMessage("We've reset your application, you can now try again!");
                Applicant c = this.list.get(sender.getName());
                c.setNext(todo.GOODAT);
                sender.sendMessage("So, what are you good at?");
                return true;
            }
            sender.sendMessage("What's there to reject?");
            return true;
        }
        return false;
    }
}
