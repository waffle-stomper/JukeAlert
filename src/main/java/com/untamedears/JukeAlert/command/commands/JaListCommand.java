package com.untamedears.JukeAlert.command.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.tasks.GetSnitchListPlayerTask;

public class JaListCommand extends PlayerCommand {
    public JaListCommand() {
        super("jalist");
        setDescription("Displays Juke List Information");
        setUsage("/jalist <page number>");
        setArguments(0,1);
        setIdentifier("jalist");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            int offset = 1;
            if (args.length > 0) {
                try {
                    offset = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    offset = 1;
                }
            }
            if (offset < 1) {
                offset = 1;
            }
            sendSnitchList(sender, offset);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + " You do not have access to snitches!");
            return false;
        }
    }

    private void sendSnitchList(CommandSender sender, int offset) {
        Player player = (Player) sender;
        GetSnitchListPlayerTask task = new GetSnitchListPlayerTask(JukeAlert.getInstance(), offset, player);
        Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(), task);

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
