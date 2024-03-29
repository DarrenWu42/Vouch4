package io.github.Infinight999.vouch4;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.permission.Permission;

public class vouch4 extends JavaPlugin{
	final String newGroupconfig = "new-player-group-name";
	final String userGroupconfig = "able-to-vouch-group-name";

	Logger logger = null;
	Permission permissions = null;
	
	//run when plugin is enabled
	@Override
	public void onEnable(){
		logger = getLogger();
		if(!loadComponents()) logger.severe("Vouch4 failed to be vouched for! Error resulted in Vouch4 not working");
		else logger.info("Vouch4 has been vouched for! Already vouched players can now vouch for new players.");
	}

	//run when plugin is disabled
	@Override
	public void onDisable(){
		logger.info("Vouch4 has been unvouched for! Rip.");
	}
	
	// run when loading (or reloading) plugin
	public boolean loadComponents(){
		saveDefaultConfig();
		
		if(!setupPermissions()){
			logger.severe("Vouch4 requires a permission plugin to be installed in order to work properly!");
			return false;
		}
		if(!permissions.hasGroupSupport()){
			logger.severe("Vouch4 requires a permission plugin that has group support to work properly!");
			return false;
		}

		return true;
	}
	
	// setup permissions for plugin
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permissions = rsp.getProvider();
        return permissions != null;
    }

	// handle commands
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		switch(cmd.getName().toLowerCase()){
			case "vouch":
			case "v":
				return handleVouch(sender, args);
			case "vouchedFor":
			case "vf":
				return handleVouchedFor(sender, args);
			default:
				return false;
		}
	}

	// handle vouch command
	public boolean handleVouch(CommandSender sender, String[] args){
		String senderID = sender instanceof ConsoleCommandSender ? "console" :
							sender instanceof Player ? ((Player)sender).getUniqueId().toString() : "0";
		if(args.length == 0){
			sender.sendMessage("You need to specify a person");
			return false;
		}
		if(args.length > 1) {
			sender.sendMessage("Too many arguments, specify only one person");
			return false;
		}

		String targetPlayerName = args[0];
		UUID targetPlayerID = getServer().getPlayerUniqueId(targetPlayerName);
		Player targetPlayer = getServer().getPlayer(targetPlayerID);
		if(targetPlayer == null){
			sender.sendMessage("The target player isn't online or doesn't exist");
			return false;
		}
		if(!permissions.playerInGroup(targetPlayer, getConfig().getString(newGroupconfig))){
			sender.sendMessage("The target player is already a user");
			return false;
		}

		permissions.playerRemoveGroup(targetPlayer, getConfig().getString(newGroupconfig));
		permissions.playerAddGroup(targetPlayer, getConfig().getString(userGroupconfig));

		getServer().dispatchCommand(getServer().getConsoleSender(), "wg flushstates");
		
		getConfig().set("vouchee-voucher-pairs."+targetPlayerID.toString(),senderID);
		saveConfig();

		sender.sendMessage("You have vouched for "+targetPlayerName+". You will be responsible if they break any rules.");
		return true;
	}

	// handle vouchedfor command
	public boolean handleVouchedFor(CommandSender sender, String[] args){
		if(args.length == 0){
			sender.sendMessage("You need to specify a person");
			return false;
		}
		if(args.length > 1) {
			sender.sendMessage("Too many arguments, specify only one person");
			return false;
		}

		String targetPlayerName = args[0];
		UUID targetPlayerID = getServer().getPlayerUniqueId(targetPlayerName);
		String voucheePlayerID = getConfig().getString("vouchee-voucher-pairs."+targetPlayerID.toString());
		if(voucheePlayerID == null) {
			sender.sendMessage("This person hasn't been vouched for yet.");
			return false;
		}
		
		String voucheePlayerName = getServer().getPlayer(UUID.fromString(voucheePlayerID)).getName();
		sender.sendMessage("The player that vouched for "+targetPlayerName+" is "+voucheePlayerName);
		return true;
	}
}
