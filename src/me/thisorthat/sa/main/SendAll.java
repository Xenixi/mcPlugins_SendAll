package me.thisorthat.sa.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class SendAll extends JavaPlugin {
	public static Socket soc;
	public static boolean enabled = false, reset = false;
	public static Thread t1;
	public static File configFile;
	public static FileConfiguration config;
	public static String passwordHashed;
	public static boolean awaitingListData = false;
	public static CommandSender dataListRequestSender;

	public static Socket getSoc() {
		return soc;
	}

	public void onEnable() {
		saveResource("Config.yml", false);
		configFile = new File(getDataFolder(), "Config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		int port = config.getInt("port");
		String name = (String) config.get("name");
		String pass = (String) config.get("pass");

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(pass.getBytes());
			passwordHashed = java.util.Base64.getEncoder().encodeToString(hash);

		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

		if (!reset) {
			Bukkit.getLogger().info("(c)[SendAll] - SendAll has been successfully enabled.");
		} else {
			Bukkit.getLogger().info("(c)[SendAll] - SendAll has been successfully reset -- Awaiting server connection");
		}
		t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!enabled) {
					try {
						soc = new Socket("127.0.0.1", port);
						enabled = true;

						PrintWriter out = new PrintWriter(SendAll.getSoc().getOutputStream(), true);
						out.println(name);
						System.out.println("Connected to server!");

					} catch (ConnectException e) {
						continue;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
					while (true) {
						if (enabled) {
							try {
								String cmd = in.readLine();
								if (cmd.startsWith("^%$list-") && awaitingListData) {
									System.out.println("Got list data");
									String list = cmd.replace("^%$list-", "");
									dataListRequestSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
											"&d(c)&r&4[&r&dSendAll&r&4]&r&d - &r&4Server List: &r&d"
													+ list.replace(" | ", ", ")));
									awaitingListData = false;
									dataListRequestSender = null;

								} else {
									System.out.println("Running received command: '" + cmd + "'");
									Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(SendAll.class),
											new Runnable() {

												@Override
												public void run() {
													Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
												}
											});
								}
							} catch (SocketException j) {
								System.err.println("ConnectionReset");
								reload();
							}
						}
					}
				} catch (IOException g) {
					g.printStackTrace();
				}

			}

		});
		t1.start();

	}

	private void reload() {
		try {
			t1.interrupt();
			soc.close();
			enabled = false;
			reset = true;
			onEnable();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void onDisable() {
		enabled = false;
		Bukkit.getLogger().info("(c)[SendAll] - SendAll has been closed");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if ((cmd.getName().equalsIgnoreCase("SendAll") || cmd.getName().equalsIgnoreCase("sa")
					|| cmd.getName().equalsIgnoreCase("sall")) && sender.hasPermission("SendAll.execute")) {

				if (args.length < 1) {

					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							"&d(c)&r&4[&r&dSendAll&r&4]&r&d - &r&4Usage: /sa <cmd>"));

				} else {
					StringBuilder cb = new StringBuilder();
					for (String arg : args) {
						cb.append(arg);
						cb.append(" ");
					}
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							"&d(c)&r&4[&r&dSendAll&r&4]&r&d - &r&4Executing server command '" + cb.toString() + "'"));

					// get all commands in string with spaces even
					if (!enabled) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
								"&d(c)&r&4[&r&dSendAll&r&4]&r&d - &r&4Error: NO CONNECTION TO SERVER!"));
					}
					CMDSender.sendCommand(cb.toString(), "%%all");

				}

			} else if ((cmd.getName().equalsIgnoreCase("sarl") || cmd.getName().equalsIgnoreCase("srl"))
					&& sender.hasPermission("SendAll.rl")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&d(c)&r&4[&r&dSendAll&r&4]&r&d - &r&4Reloading SendAll by Kobe McManus"));
				reload();
			} else if ((cmd.getName().equalsIgnoreCase("ss")) || cmd.getName().equalsIgnoreCase("sserver")
					|| cmd.getName().equalsIgnoreCase("sendserver") && sender.hasPermission("SendAll.execute")) {
				if (args.length < 2) {

					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							"&d(c)&r&4[&r&dSendAll&r&4]&r&d - &r&4Usage: /sa <server> <cmd>"));

				} else {
					StringBuilder cb = new StringBuilder();
					boolean skipRound = true;
					for (String arg : args) {
						if (!skipRound) {
							cb.append(arg);
							cb.append(" ");
						} else {
							skipRound = false;
						}
					}
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							"&d(c)&r&4[&r&dSendAll&r&4]&r&d - &r&4Executing server command '" + cb.toString()
									+ "' on server '" + args[0] + "'"));

					// get all commands in string with spaces even
					if (!enabled) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
								"&d(c)&r&4[&r&dSendAll&r&4]&r&d - &r&4Error: NO CONNECTION TO SERVER!"));
					}
					CMDSender.sendCommand(cb.toString(), args[0]);

				}
			} else if ((cmd.getName().equalsIgnoreCase("salist") || cmd.getName().equalsIgnoreCase("sal"))
					&& sender.hasPermission("SendAll.list")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&d(c)&r&4[&r&dSendAll&r&4]&r&d - &r&4Requesting server list"));
				dataListRequestSender = sender;
				awaitingListData = true;
				CMDSender.sendCommand(config.getString("name"), "$list");
			}

		} else {
			sender.sendMessage("(c)[SendAll] - This command must be executed by a player!");
		}
		return false;
	}

}
