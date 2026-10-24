package dev.exo8.clientity.nms;

import dev.exo8.clientity.api.entity.ClientEntity;
import dev.exo8.clientity.api.entity.ClientEntityManager;
import dev.exo8.clientity.api.entity.ClientEntityTicker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractClientEntity<P, T extends Entity> implements ClientEntity<T>
{
    protected final Plugin plugin;
    protected final ClientEntityManager clientEntityManager;
    protected final PacketSender<P> packetSender;

    protected final Set<UUID> viewerSet = new HashSet<>();
    protected final Set<UUID> showedSet = new HashSet<>();

    protected final List<ClientEntityTicker<T>> tickers = new ArrayList<>();

    protected VisibleMode mode = VisibleMode.VIEWERS;

    public AbstractClientEntity(Plugin plugin, ClientEntityManager clientEntityManager, PacketSender<P> packetSender)
    {
        this.plugin = plugin;
        this.clientEntityManager = clientEntityManager;
        this.packetSender = packetSender;
    }

    @Override
    public VisibleMode mode() {
        return mode;
    }

    @Override
    public void mode(VisibleMode mode)
    {
        if (this.mode == mode) {
            return;
        }

        hide();
        this.mode = mode;
        show();
    }

    @Override
    public void addTicker(ClientEntityTicker<T> ticker) {
        tickers.add(ticker);
    }

    @Override
    public void removeTicker(ClientEntityTicker<T> ticker) {
        tickers.remove(ticker);
    }

    @Override
    public Collection<ClientEntityTicker<T>> tickers() {
        return Collections.unmodifiableList(tickers);
    }

    @Override
    public void tick()
    {
        if (showedSet.isEmpty()) {
            return;
        }

        for (var ticker : new ArrayList<>(tickers)) {
            ticker.tick(this);
        }

        tickInternal();
        update();
    }

    @Override
    public void show() {
        forEachPlayers(this::show);
    }

    private void show(Player player)
    {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (!canSee(player))
        {
            hide(player);
            return;
        }

        if (visible(player))
        {
            update(player);
            return;
        }

        packetSender.send(player, showPackets());
        showedSet.add(player.getUniqueId());
    }

    @Override
    public void hide()
    {
        for (var uuid : new HashSet<>(showedSet))
        {
            var player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) hide(player);
            else showedSet.remove(uuid);
        }
    }

    protected void hide(Player player)
    {
        if (player == null) return;
        var uuid = player.getUniqueId();
        showedSet.remove(uuid);
        packetSender.send(player, destroyPackets());
    }

    @Override
    public boolean visible(Player player) {
        return player != null && showedSet.contains(player.getUniqueId());
    }

    @Override
    public boolean addViewer(Player player) {
        if (player != null) {
            return viewerSet.add(player.getUniqueId());
        }
        return false;
    }

    @Override
    public boolean removeViewer(Player player)
    {
        if (player != null) {
            return viewerSet.remove(player.getUniqueId());
        }
        return false;
    }

    @Override
    public boolean hasViewer(Player player) {
        return player != null && viewerSet.contains(player.getUniqueId());
    }

    @Override
    public void clearViewers()
    {
        for (var uuid : new HashSet<>(viewerSet))
        {
            var player = plugin.getServer().getPlayer(uuid);

            if (player != null && player.isOnline()) {
                hide(player);
            }
        }

        viewerSet.clear();
    }

    @Override
    public Collection<Player> viewers()
    {
        var result = new HashSet<Player>();

        for (var uuid : viewerSet)
        {
            var player = plugin.getServer().getPlayer(uuid);

            if (player != null && player.isOnline()) {
                result.add(player);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    @Override
    public void edit(Consumer<T> consumer)
    {
        consumer.accept(bukkit());
        update();
    }

    protected void update()
    {
        for (var uuid : new HashSet<>(showedSet))
        {
            var player = plugin.getServer().getPlayer(uuid);

            if (player == null || !player.isOnline())
            {
                showedSet.remove(uuid);
                continue;
            }

            update(player);
        }
    }

    protected void update(Player player)
    {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (!canSee(player))
        {
            hide(player);
            return;
        }

        packetSender.send(player, updatePackets());
    }

    private boolean canSee(Player player)
    {
        if (player == null || !player.isOnline()) {
            return false;
        }

        return mode == VisibleMode.ALL || viewerSet.contains(player.getUniqueId());
    }

    protected void forEachPlayers(Consumer<Player> consumer)
    {
        if (mode == VisibleMode.ALL)
        {
            for (var player : plugin.getServer().getOnlinePlayers()) {
                consumer.accept(player);
            }

            return;
        }

        for (var id : new HashSet<>(viewerSet))
        {
            var player = plugin.getServer().getPlayer(id);

            if (player == null || !player.isOnline()) {
                continue;
            }

            consumer.accept(player);
        }
    }

    protected void tickInternal() { }
    abstract protected List<P> showPackets();
    abstract protected List<P> destroyPackets();
    abstract protected List<P> updatePackets();
}