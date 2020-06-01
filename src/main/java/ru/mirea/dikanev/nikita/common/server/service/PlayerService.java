package ru.mirea.dikanev.nikita.common.server.service;

import java.util.Map;

import ru.mirea.dikanev.nikita.common.server.entity.PlayerState;

public interface PlayerService {

    void putState(int id, PlayerState state);

    PlayerState getState(int id);

    Map<Integer, PlayerState> getMap();
}
