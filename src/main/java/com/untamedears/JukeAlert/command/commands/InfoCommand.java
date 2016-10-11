package com.untamedears.JukeAlert.command.commands;

import static com.untamedears.JukeAlert.util.Utility.findLookingAtOrClosestSnitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.LoggedAction;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.tasks.GetSnitchInfoPlayerTask;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InfoCommand extends PlayerCommand {

    public class History {
        public History(int snitchId, int page, LoggedAction filterAction, String filterPlayer) {
            this.snitchId = snitchId;
            this.page = page;
            this.filterAction = filterAction;
            this.filterPlayer = filterPlayer;
        }
        public int snitchId;
        public int page;
        public LoggedAction filterAction;
        public String filterPlayer;
    }
    
    private static Map<UUID, History> playerPage_ = new TreeMap<UUID, History>();
    private static final String[] autocompleteCommands = {"next", "censor", "action=", "player="};

    public InfoCommand() {
        super("Info");
        setDescription("Displays information from a Snitch");
        setUsage("/jainfo [<page number> or 'next'] [censor] [action=<action type>] [player=<player name>]");
        setArguments(0, 6); // Max args = 6 because the action might be split into two
        setIdentifier("jainfo");
    }
    
    /**
     * Attempts to match a string to one of the LoggedAction actions
     * @param action - case insensitive
     * @return The matching LoggedAction, or null if a matching action couldn't be found
     */
    private static LoggedAction decodeAction(String action){
        // Strip quotes
        if (action.startsWith("\"") && action.length() > 1){
            action = action.substring(1);
        }
        if (action.endsWith("\"") && action.length() > 1){
            action = action.substring(0,  action.length()-1);
        }
        action = action.toUpperCase();
        for (LoggedAction a : LoggedAction.values()){
            String actionName = a.toString().toUpperCase();
            String actionDisp = a.toActionString().toUpperCase();
            if (actionName.equals(action) || actionDisp.equals(action)){
                return a;
            }
        }
        return null;
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
            LoggedAction filterAction = null;
            String filterPlayer = "";
            boolean censorFlag = false;
            boolean nextFlag = false;
            
            if (args.length > 0) {
                
                // Reassemble any arguments that are enclosed in quotes and were split
                List<String> fixedArgs = new ArrayList<String>();
                for (int pos=0; pos<args.length; pos++){
                    String currArg = args[pos];
                    if (currArg.contains("\"")){
                        // Found the first quote. Scan ahead to assemble the rest of the argument (if there's another quote present)
                        boolean secondQuotePresent = false;
                        String joinedArg = currArg;
                        int nextPos=pos+1;
                        for (; nextPos<args.length; nextPos++){
                            String nextArg = args[nextPos];
                            if (nextArg.contains("\"")){
                              secondQuotePresent = true;
                              joinedArg += " " + nextArg;
                              break;
                          }
                          else{
                                joinedArg += " " + nextArg;
                            }
                        }
                        if (secondQuotePresent){
                            fixedArgs.add(joinedArg);
                            pos = nextPos;
                        }
                        else{
                            fixedArgs.add(currArg);
                        }
                    }
                    else{
                        fixedArgs.add(currArg);
                    }
                }
                
                // Parse each argument
                for (String arg : fixedArgs){
                    arg = arg.toLowerCase().trim();
                    if (arg.equals("next")){
                        nextFlag = true;
                        continue;
                    }
                    else if (arg.equals("censor")){
                        censorFlag = true;
                        continue;
                    }
                    else if (arg.startsWith("action=")){
                        if (arg.length() > 7){
                            filterAction = decodeAction(arg.substring(7));
                            if (filterAction == null){
                                sender.sendMessage(ChatColor.RED + "Couldn't parse action type '" + arg.substring(7) + "'");
                                return false;
                            }
                            continue;
                        }
                    }
                    else if (arg.startsWith("player=")){
                        if (arg.length() > 7){
                            filterPlayer = arg.substring(7);
                            continue;
                        }
                    }
                    else{
                        try {
                            offset = Integer.parseInt(arg);
                            continue;
                        } catch (NumberFormatException e) {}
                    }
                    
                    sender.sendMessage(ChatColor.RED + "Unrecognized argument: '" + arg + "'");
                    return false;
                }

                if (nextFlag && playerPage_.containsKey(accountId)) {
                    final History hist = playerPage_.get(accountId);
                    if (hist != null && hist.snitchId == snitchId && hist.filterAction == filterAction && hist.filterPlayer.equals(filterPlayer)) {
                        offset = hist.page + 1;
                    } else {
                        offset = 1;
                    }
                }
            }
            if (offset < 1) {
                offset = 1;
            }
            playerPage_.put(accountId, new History(snitchId, offset, filterAction, filterPlayer));
            sendLog(sender, snitch, offset, censorFlag, filterAction, filterPlayer);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");
            return false;
        }
    }

    private void sendLog(CommandSender sender, Snitch snitch, int offset, boolean shouldCensor, LoggedAction filterAction, String filterPlayer) {
        Player player = (Player) sender;
        GetSnitchInfoPlayerTask task = new GetSnitchInfoPlayerTask(JukeAlert.getInstance(), snitch.getId(), snitch.getName(), offset, player, shouldCensor, filterAction, filterPlayer);
        Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(), task);

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completedArgs = new ArrayList<String>();
        if (args.length > 0){
            // Copy all of the arguments except the last one to the output
            for (int i=0; i<args.length-2; i++){
                completedArgs.add(args[i]);
            }
            // Try to complete the last argument
            String lastArg = args[args.length-1];
            for (String command : autocompleteCommands){
                if (command.startsWith(lastArg)){
                    lastArg = command;
                    break;
                }
            }
            // Add the last argument to the output
            completedArgs.add(lastArg);
        }
        return completedArgs;
    }
}
