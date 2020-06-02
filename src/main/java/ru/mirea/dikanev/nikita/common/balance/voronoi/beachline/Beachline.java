package ru.mirea.dikanev.nikita.common.balance.voronoi.beachline;

import java.util.Optional;

import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;

public class Beachline {

    private final InnerBeachNode rootContainer = new InnerBeachNode();

    public InsertionResult insertArc(VoronoiPoint newSite) {
        BeachNode root = getRoot();
        if (root != null) {
            return root.insertArc(newSite);
        } else {
            LeafBeachNode l = new LeafBeachNode(newSite);
            setRoot(l);
            return new InsertionResult(Optional.empty(), l);
        }
    }

    BeachNode getRoot() {
        return rootContainer.getLeftChild();
    }

    void setRoot(BeachNode n) {
        rootContainer.setLeftChild(n);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Beachline(");
        if (getRoot() != null) {
            Optional<LeafBeachNode> current = Optional.of(getRoot().getLeftmostLeaf());
            while (current.isPresent()) {
                sb.append(current.get().getSite()).append(',');
                current = current.flatMap(LeafBeachNode::getRightNeighbor);
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
