package ru.mirea.dikanev.nikita.common.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mirea.dikanev.nikita.common.math.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerState {

    public Point position;

    @Override
    public String toString() {
        return "PlayerState{" + "position=" + position + '}';
    }
}
