package dev.exo8.clientity.api.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Collection;

public interface ClientEntityManager
{
    <T extends Entity> ClientEntity<T> create(Class<T> type, Location location);
    ClientEntity<?> get(int id);
    Collection<ClientEntity<?>> all();
}