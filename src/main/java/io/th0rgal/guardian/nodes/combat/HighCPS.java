package io.th0rgal.guardian.nodes.combat;

import io.th0rgal.guardian.GuardianPlayer;
import io.th0rgal.guardian.config.NodeConfig;
import io.th0rgal.guardian.events.PlayersManager;
import io.th0rgal.guardian.nodes.Node;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

final class CPSQueue {
    private final long[] queue;
    private int index;

    public CPSQueue(int size) {
        queue = new long[size];
        Arrays.fill(queue, 0);
        index = 0;
    }

    public double getCPS() {
        double sum = 0;
        for (int i = 1; i < queue.length; i++) {
            long a = queue[(index + i) % queue.length];
            long b = queue[(index + i + 1) % queue.length];
            if (a == 0)
                return 0;
            sum += b - a;
        }
        return (1000 * (queue.length - 1)) / sum;
    }

    public void update() {
        queue[++index % queue.length] = System.currentTimeMillis();
    }
}

public class HighCPS extends Node implements Listener {

    public HighCPS(JavaPlugin plugin, PlayersManager playersManager, String name, NodeConfig configuration) {
        super(plugin, playersManager, name, configuration);
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(final PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        GuardianPlayer player = playersManager.getPlayer(event.getPlayer());
        CPSQueue cpsQueue = (CPSQueue) player.getData(this.getClass());
        if (cpsQueue == null) {
            cpsQueue = new CPSQueue(20);
            player.setData(this.getClass(), cpsQueue);
        }
        cpsQueue.update();
        Bukkit.broadcastMessage("CPS: " + cpsQueue.getCPS());
    }
}