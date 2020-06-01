package ru.mirea.dikanev.nikita.common.server.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.mirea.dikanev.nikita.common.server.entity.PlayerState;

public class SimplePlayerService implements PlayerService {

    private Map<Integer, PlayerState> states = new ConcurrentHashMap<>();

    @Override
    public void putState(int id, PlayerState state) {
        states.put(id, state);
    }

    @Override
    public PlayerState getState(int id) {
        return states.get(id);
    }

    @Override
    public Map<Integer, PlayerState> getMap() {
        return states;
    }

}
