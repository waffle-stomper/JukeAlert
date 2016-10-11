package com.untamedears.JukeAlert.model;

import java.util.logging.Level;

import org.bukkit.ChatColor;

import com.untamedears.JukeAlert.JukeAlert;

/**
 * Enum that represents a type of action that a snitch can record, and the value
 * that goes into the database for said action
 *
 *
 */
public enum LoggedAction {

    // ONCE THIS GOES INTO PRODUCTION, _NEVER_ CHANGE THESE, only mark some as not used and add more with larger values!
    UNKNOWN(0x7FFFFFFF),
    KILL(0),
    BLOCK_PLACE(1),
    BLOCK_BREAK(2),
    BUCKET_FILL(3),
    BUCKET_EMPTY(4),
    ENTRY(5),
    USED(6),
    IGNITED(7),
    BLOCK_BURN(8),
    BLOCK_USED(9),
    LOGIN(10),
    LOGOUT(11),
    EXCHANGE(12),
    VEHICLE_DESTROY(13),
    ENTITY_MOUNT(14),
    ENTITY_DISMOUNT(15);
    
    private int value;

    // constructor, has to be private
    private LoggedAction(int value) {
        this.value = value;
    }

    public int getLoggedActionId() {
        return this.value;
    }

    public static LoggedAction getFromId(int id) {
        switch(id) {
            case 0: return KILL;
            case 1: return BLOCK_PLACE;
            case 2: return BLOCK_BREAK;
            case 3: return BUCKET_FILL;
            case 4: return BUCKET_EMPTY;
            case 5: return ENTRY;
            case 6: return USED;
            case 7: return IGNITED;
            case 8: return BLOCK_BURN;
            case 9: return BLOCK_USED;
            case 10: return LOGIN;
            case 11: return LOGOUT;
            case 12: return EXCHANGE;
            case 13: return VEHICLE_DESTROY;
            case 14: return ENTITY_MOUNT;
            case 15: return ENTITY_DISMOUNT;
            default: return UNKNOWN;
        }
    }
    
    /**
     * Returns the 'human-friendly' version of the action
     * Currently this is only used when displaying snitch logs
     * @return
     */
    public String toActionString(){
        switch(this) {
            case KILL: return "Killed";
            case BLOCK_PLACE: return "Block Place";
            case BLOCK_BREAK: return "Block Break";
            case BUCKET_FILL: return "Bucket Fill";
            case BUCKET_EMPTY: return "Buckety Empty";
            case ENTRY: return "Entry";
            case USED: case BLOCK_USED: return "Used";
            case IGNITED: return "Ignited";
            case BLOCK_BURN: return "Block Burn";
            case LOGIN: return "Login";
            case LOGOUT: return "Logout";
            case EXCHANGE: return "Exchanged";
            case VEHICLE_DESTROY: return "Destroyed";
            case ENTITY_MOUNT: return "Mount";
            case ENTITY_DISMOUNT: return "Dismount";
            default: return "Unknown";
        }
    }
}
