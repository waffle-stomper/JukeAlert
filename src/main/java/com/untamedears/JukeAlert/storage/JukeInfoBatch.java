package com.untamedears.JukeAlert.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.JukeAlert.model.LoggedAction;
import com.untamedears.JukeAlert.model.Snitch;

public class JukeInfoBatch {
    
    // Parent class
    public JukeAlertLogger JAL;
    
    // Max queue size before auto flush
    public final int max_batch_size = 100;
    // Current queue size
    public int batch_current = 0;
    private int batch_current_update_last_visit = 0;
    
    
    public JukeInfoBatch(JukeAlertLogger _jal) {
        this.JAL=_jal;
    }
    
    // Current working set is stored here
    private PreparedStatement currentSet = null;
    private Object currentSetLock = new Object();
    
    private PreparedStatement currentLastVisitSet = null;
    private Object currentLastVisitLock = new Object();
    
    // Add a set of last visit data.
    // NOTE:  If it turns out that this is still too spammy, we can
    //    instead add to a dictionary and simply not let the same snitch be
    //    updated twice in a single batch.  Holding on this optimization however.
    public void addLastVisitData(Snitch snitch) {
        try {
            synchronized(currentLastVisitLock) {
                // Check if starting a new batch
                if(this.currentLastVisitSet==null) {
                    this.currentLastVisitSet = this.JAL.getNewUpdateSnitchSemiOwnerVisitStmt();
                }

                currentLastVisitSet.setInt(1, snitch.getId());
                currentLastVisitSet.addBatch();
                
            } // synchronized
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            // If we're above the limit, execute all inserts
            if(++batch_current_update_last_visit >= this.max_batch_size) {
                this.flush_last_visit();
            }
    }
    
    // Add a set of data
    public void addSet(Snitch snitch, Material material, Location loc, Date date, LoggedAction action, String initiatedUser, String victimUser) {
        
        try {
        synchronized(currentSetLock) {
            // Check if starting a new batch
            if(this.currentSet==null) {
                this.currentSet = this.JAL.getNewInsertSnitchLogStmt();
            }

            // Add params
            currentSet.setInt(1, snitch.getId());
            currentSet.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
            currentSet.setByte(3, (byte) action.getLoggedActionId());
            currentSet.setString(4, initiatedUser);
            
            if (victimUser != null) {
                currentSet.setString(5, victimUser);
            } else {
                currentSet.setNull(5, java.sql.Types.VARCHAR);
            }
            if (loc != null) {
                currentSet.setInt(6, loc.getBlockX());
                currentSet.setInt(7, loc.getBlockY());
                currentSet.setInt(8, loc.getBlockZ());
            } else {
                currentSet.setNull(6, java.sql.Types.INTEGER);
                currentSet.setNull(7, java.sql.Types.INTEGER);
                currentSet.setNull(8, java.sql.Types.INTEGER);
            }
            if (material != null) {
                currentSet.setShort(9, (short) material.getId());
            } else {
                currentSet.setNull(9, java.sql.Types.SMALLINT);
            }
            
            currentSet.addBatch();
            
        } // synchronized
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // If we're above the limit, execute all inserts
        if(++batch_current >= this.max_batch_size) {
            this.flush_snitch_hits();
        }
        
    }
    
    public void flush() {
        flush_snitch_hits();
        flush_last_visit();
    }
    
    private void flush_last_visit() {
        PreparedStatement executeMe;
        synchronized(currentLastVisitLock) {
            executeMe = this.currentLastVisitSet;
            this.currentLastVisitSet=null;
        }
        batch_current_update_last_visit = 0;
        if(executeMe != null) {
            try {
                executeMe.executeBatch();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private void flush_snitch_hits() {
        PreparedStatement executeMe;
        synchronized(currentSetLock) {
            executeMe = this.currentSet;
            this.currentSet=null;
        }
        batch_current=0;
        if(executeMe != null) {
            try {
                executeMe.executeBatch();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
}
