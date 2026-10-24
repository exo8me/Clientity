package dev.exo8.clientity.api.event;

import dev.exo8.clientity.api.raycast.ClientEntityRaycaster.RaytraceResult;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClientEntityInteractEvent extends Event implements Cancellable
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final ActionType action;
    private final RaytraceResult result;

    private boolean cancel = false;

    public ClientEntityInteractEvent(Player player, ActionType action, RaytraceResult result)
    {
        this.player = player;
        this.action = action;
        this.result = result;
    }

    public Player getPlayer() {
        return player;
    }

    public ActionType getAction() {
        return action;
    }

    public RaytraceResult getResult() {
        return result;
    }

    public ItemStack getItem() {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public enum ActionType
    {
        ATTACK,
        INTERACT;
    }
}