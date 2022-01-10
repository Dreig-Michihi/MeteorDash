package me.dreig_michihi.meteordash;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.projectkorra.projectkorra.BendingPlayer;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class MeteorDashListener implements Listener {

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        BendingPlayer bp = BendingPlayer.getBendingPlayer(p);
        if (
                event.isCancelled()
                        || bp == null
                        || !bp.canUseSubElement(Element.COMBUSTION)
                        || !bp.isToggled()
                        || !bp.isElementToggled(Element.FIRE)
                        || bp.isChiBlocked()
                        || p.getGameMode() == GameMode.SPECTATOR
                        || p.isSneaking()
                        || bp.isOnCooldown("MeteorDash")) {
            //p.sendMessage("\nExplosiveDriveListener: return;");
            return;
        } else if (bp.getBoundAbilityName().equalsIgnoreCase("MeteorDash")) {
            //p.sendMessage("\nExplosiveDriveListener: new ExplosiveDrive(p);");
            new MeteorDash(p);
        }
    }
}
