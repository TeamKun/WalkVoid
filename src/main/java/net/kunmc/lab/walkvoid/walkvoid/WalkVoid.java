package net.kunmc.lab.walkvoid.walkvoid;
import net.kunmc.lab.walkvoid.walkvoid.listener.WalkListener;
import org.bukkit.plugin.java.JavaPlugin;
public final class WalkVoid extends JavaPlugin {
    @Override
    public void onEnable() {
        Kei.out(Kei.a(this));
        Kei.a(new WalkListener(), this);
    }
}
