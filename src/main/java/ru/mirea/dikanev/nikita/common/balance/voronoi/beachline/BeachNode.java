package ru.mirea.dikanev.nikita.common.balance.voronoi.beachline;

import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;

public abstract class BeachNode {

    private InnerBeachNode parent;

    public abstract InsertionResult insertArc(VoronoiPoint newSite);

    public abstract LeafBeachNode getLeftmostLeaf();

    public abstract LeafBeachNode getRightmostLeaf();

    void replaceBy(BeachNode n) {
        if (getParent() != null) {
            if (getParent().getLeftChild() == this) {
                getParent().setLeftChild(n);
            } else {
                getParent().setRightChild(n);
            }
        }
    }

    public InnerBeachNode getParent() {
        return parent;
    }

    public void setParent(InnerBeachNode parent) {
        this.parent = parent;
    }
}
