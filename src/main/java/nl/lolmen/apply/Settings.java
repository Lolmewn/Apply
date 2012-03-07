package nl.lolmen.apply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Settings {
	
	private File settingsFile = new File("plugins" + File.separator + "Apply" + File.separator + "settings.yml");
	
	private String host, username, password, database, table;
	private int port;
	
	public Settings(){
		this.loadSettings();
	}
	
	private void loadSettings() {
		if(!this.settingsFile.exists()){
			this.extractFile();
			System.out.println("[Apply] Restarting plugin!");
			Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("Apply"));
			Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin("Apply"));
			System.out.println("[Apply] Plugin restarted!");
			return;
		}
		FileConfiguration c = YamlConfiguration.loadConfiguration(this.settingsFile);
		this.setDatabase(c.getString("dbDatabase", "minecraft"));
		this.setTable(c.getString("dbTable", "apply"));
		this.setHost(c.getString("dbHost", "localhost"));
		this.setPort(c.getInt("dbPort", 3306));
		this.setUsername(c.getString("dbUser"));
		this.setPassword(c.getString("dbPass"));
	}

	private void extractFile() {
		Bukkit.getLogger().info("Trying to create default settings...");
		try {
			this.settingsFile.getParentFile().mkdirs();
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("settings.yml");
			OutputStream out = new BufferedOutputStream(new FileOutputStream(this.settingsFile));
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
			out.flush();
			out.close();
			in.close();
			Bukkit.getLogger().info("Default settings created succesfully!");
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().severe("Error creating settings file! Disabling!");
			Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("RuneSkillz"));
		}
	}

	protected String getHost() {
		return host;
	}
	
	private void setHost(String host) {
		this.host = host;
	}

	protected String getUsername() {
		return username;
	}

	private void setUsername(String username) {
		this.username = username;
	}

	protected String getPassword() {
		return password;
	}

	private void setPassword(String password) {
		this.password = password;
	}

	protected String getDatabase() {
		return database;
	}

	private void setDatabase(String database) {
		this.database = database;
	}

	protected String getTable() {
		return table;
	}

	private void setTable(String table) {
		this.table = table;
	}

	protected int getPort() {
		return port;
	}

	private void setPort(int port) {
		this.port = port;
	}

}
