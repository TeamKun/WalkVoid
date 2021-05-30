package net.kunmc.lab.walkvoid.walkvoid.listener;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
public class WalkListener implements Listener {
    @EventHandler
    public void on(PlayerMoveEvent e) {
        if(e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) for (int i = e.getFrom().getBlockY(); i > -1; i--) e.getFrom().getWorld().getBlockAt(e.getFrom().getBlockX(), i, e.getFrom().getBlockZ()).setType(Material.AIR, false);
    }
}
