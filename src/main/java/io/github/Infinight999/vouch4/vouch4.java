package io.github.Infinight999.vouch4;

import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class vouch4 extends JavaPlugin{
	//run when plugin is enabled
	@Override
	public void onEnable(){
		getLogger().info("Vouch4 has been vouched for! Already vouched players can now vouch for new players.");
		getConfig();
		getLogger().info("Config has been getted.");
	}

	//run when plugin is disabled
	@Override
	public void onDisable(){
		getLogger().info("Vouch4 has been unvouched for! Rip.");
	}
	
	//commands
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("vouch") || cmd.getName().equalsIgnoreCase("v")){
			//String guestGroup = getConfig().getString("new-player-group"); //not used really, here jic
			String userGroup = getConfig().getString("able-to-vouch-group");
			String track = getConfig().getString("track");
			String senderID = "0";
			if(sender instanceof ConsoleCommandSender){
				senderID = "console";
			}
			if(sender instanceof Player) {
				if(!getServer().getPlayer(sender.getName()).hasPermission(userGroup)){
					sender.sendMessage("You need to be at least a user");
					return false;
				}
				senderID = getServer().getPlayerUniqueId(sender.getName()).toString();
			}
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
				sender.sendMessage("The target player isn't online");
				return false;
			}
			if(targetPlayer.hasPermission(userGroup)){
				sender.sendMessage("The target player is already a user");
				return false;
			}
			String command = "lp user "+targetPlayerName+" promote "+track;
			getServer().dispatchCommand(getServer().getConsoleSender(), command);
			command = "wg flushstates";
			getServer().dispatchCommand(getServer().getConsoleSender(), command);
			getConfig().set("vouchee-voucher-pairs."+targetPlayerID.toString(),senderID);
			saveConfig();
			sender.sendMessage("You have vouched for "+targetPlayerName+". You will be responsible if they break any rules.");
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("vouchedfor") || cmd.getName().equalsIgnoreCase("vf")){
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
		return false; 
	}
}
