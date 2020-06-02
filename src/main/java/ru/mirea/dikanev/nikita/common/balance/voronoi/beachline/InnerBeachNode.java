package ru.mirea.dikanev.nikita.common.balance.voronoi.beachline;

import ru.mirea.dikanev.nikita.common.balance.voronoi.graph.VoronoiPoint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static ru.mirea.dikanev.nikita.common.balance.voronoi.Math.sq;
import static java.lang.Math.sqrt;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class InnerBeachNode extends BeachNode {

    private BeachNode leftChild;
    private BeachNode rightChild;

    InnerBeachNode() {

    }

    public InnerBeachNode(BeachNode leftChild, BeachNode rightChild) {
        setLeftChild(leftChild);
        setRightChild(rightChild);
    }

    @Override
    public InsertionResult insertArc(VoronoiPoint newSite) {
        // Find leafs represented by this inner node
        VoronoiPoint l = leftChild.getRightmostLeaf().getSite();
        VoronoiPoint r = rightChild.getLeftmostLeaf().getSite();

        // Transform coordinate to local coords
        double lxOld = l.x();
        r = new VoronoiPoint(r.x() - l.x(), r.y() - newSite.y());
        l = new VoronoiPoint(0, l.y() - newSite.y());

        // Compute intersection of parabolas
        double x;
        if (Double.compare(l.y(), r.y()) == 0) {
            x = r.x() / 2.0;
        } else if (l.y() == 0.0) {
            x = l.x();
        } else if (r.y() == 0.0) {
            x = r.x();
        } else {
            x = (l.y() * r.x() - sqrt(l.y() * r.y() * (sq(l.y() - r.y()) + sq(r.x())))) / (l.y() - r.y());
        }

        x += lxOld;

        return newSite.x() < x ? leftChild.insertArc(newSite) : rightChild.insertArc(newSite);
    }

    @Override
    public LeafBeachNode getLeftmostLeaf() {
        return leftChild.getLeftmostLeaf();
    }

    @Override
    public LeafBeachNode getRightmostLeaf() {
        return rightChild.getRightmostLeaf();
    }

    void setLeftChild(BeachNode leftChild) {
        this.leftChild = leftChild;
        leftChild.setParent(this);
    }

    void setRightChild(BeachNode rightChild) {
        this.rightChild = rightChild;
        rightChild.setParent(this);
    }

}