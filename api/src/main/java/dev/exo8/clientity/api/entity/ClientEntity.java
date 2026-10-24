package dev.exo8.clientity.api.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public interface ClientEntity<T extends Entity>
{
    int id();
    UUID uuid();

    T bukkit();
    Object handle();

    VisibleMode mode();
    void mode(VisibleMode mode);

    void addTicker(ClientEntityTicker<T> ticker);
    void removeTicker(ClientEntityTicker<T> ticker);

    Collection<ClientEntityTicker<T>> tickers();

    void tick();

    void show();
    void hide();
    boolean visible(Player player);

    void remove();

    boolean addViewer(Player player);
    boolean removeViewer(Player player);
    boolean hasViewer(Player player);
    void clearViewers();

    Collection<Player> viewers();

    void edit(Consumer<T> consumer);

    enum VisibleMode {
        ALL, VIEWERS;
    }
}