package dev.exo8.clientity.api.entity;

import org.bukkit.entity.Entity;

@FunctionalInterface
public interface ClientEntityTicker<T extends Entity> {
    void tick(ClientEntity<T> entity);
}