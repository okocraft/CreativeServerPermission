package net.okocraft.creativeserverpermission;

import com.djrapitops.plan.query.CommonQueries;
import com.djrapitops.plan.query.QueryService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private static final String GROUP_NODE_NAME = "group.default_creative";
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    private long getGlobalPlayTime(UUID player) {
        long now = System.currentTimeMillis();
        CommonQueries queries = QueryService.getInstance().getCommonQueries();

        return queries.fetchServerUUIDs().stream()
                .map(server -> queries.fetchPlaytime(player, server, 0, now))
                .reduce(Long::sum)
                .orElse(0L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        tellPlay1DayIfNoPerm(event.getPlayer());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        tellPlay1DayIfNoPerm(event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        tellPlay1DayIfNoPerm(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        messageCooldownExpire.remove(event.getPlayer().getUniqueId());
    }

    private final Map<UUID, Long> messageCooldownExpire = new HashMap<>();
    private void tellPlay1DayIfNoPerm(Player player) {
        if (player.hasPermission(GROUP_NODE_NAME)) {
            return;
        }

        long now = System.currentTimeMillis();
        long prev = messageCooldownExpire.getOrDefault(player.getUniqueId(), 0L);
        if (prev < now) {
            messageCooldownExpire.put(player.getUniqueId(), now + 500);
            player.sendMessage(
                    Component.text("クリエイティブサーバーで遊ぶには1日以上メインサーバーで遊んでください！")
                            .color(NamedTextColor.RED)
            );
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(GROUP_NODE_NAME)) {
            return;
        }

        UUID uniqueId = player.getUniqueId();
        if (getGlobalPlayTime(uniqueId) < ONE_DAY_MILLIS) {
            return;
        }

        User user = LuckPermsProvider.get().getUserManager().getUser(uniqueId);
        if (user == null) {
            return;
        }
        user.data().add(Node.builder(GROUP_NODE_NAME).withContext("server", "creative").value(true).build());
        LuckPermsProvider.get().getUserManager().saveUser(user);
    }
}
