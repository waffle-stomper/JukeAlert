package com.untamedears.JukeAlert.command.commands;

import static com.untamedears.JukeAlert.util.Utility.findLookingAtOrClosestSnitch;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.tasks.GetSnitchInfoPlayerTask;

public class InfoCommand extends PlayerCommand {

    public class History {
        public History(int snitchId, int page) {
            this.snitchId = snitchId;
            this.page = page;
        }
        public int snitchId;
        public int page;
    }

    private static Map<UUID, History> playerPage_ = new TreeMap<UUID, History>();

    public InfoCommand() {
        super("Info");
        setDescription("Displays information from a Snitch");
        setUsage("/jainfo <page number or 'next'> [censor]");
        setArguments(0, 2);
        setIdentifier("jainfo");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            final UUID accountId = player.getUniqueId();
            final Snitch snitch = findLookingAtOrClosestSnitch(player, PermissionType.getPermission("READ_SNITCHLOG"));
            if (snitch == null) {
                player.sendMessage(ChatColor.RED + " You do not own any snitches nearby or lack permission to view their logs!");
                return true;
            }
            final int snitchId = snitch.getId();
            int offset = 1;
            if (args.length > 0) {
                try {
                    offset = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    if (playerPage_.containsKey(accountId)) {
                        final History hist = playerPage_.get(accountId);
                        if (hist != null && hist.snitchId == snitchId) {
                            offset = hist.page + 1;
                        } else {
                            offset = 1;
                        }
                    } else {
                        offset = 1;
                    }
                }
            }
            if (offset < 1) {
                offset = 1;
            }
            playerPage_.put(accountId, new History(snitchId, offset));
            sendLog(sender, snitch, offset, args.length == 2);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");
            return false;
        }
    }

    private void sendLog(CommandSender sender, Snitch snitch, int offset, boolean shouldCensor) {
        Player player = (Player) sender;
        GetSnitchInfoPlayerTask task = new GetSnitchInfoPlayerTask(JukeAlert.getInstance(), snitch.getId(), snitch.getName(), offset, player, shouldCensor);
        Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(), task);

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // TODO Auto-generated method stub
        return null;
    }
}
