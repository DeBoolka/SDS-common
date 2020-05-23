package ru.mirea.dikanev.nikita.common.balance.voronoi.beachline;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InsertionResult {
    public final Optional<LeafBeachNode> splitLeaf;
    public final LeafBeachNode newLeaf;
}
