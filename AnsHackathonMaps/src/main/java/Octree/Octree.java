package Octree;

public final class Octree {

    private Octree[] children;
    private AABB bounds;
    private int level;

    public Octree(AABB bounds_, int level_) {
        this.children = new Octree[8];
        this.bounds = bounds_;
        this.level = level_;
    }

    public Octree getChildrenAt(int i) {
        return children[i];
    }

    public void setChildrenAt(int i, Octree octree) {
        this.children[i] = octree;
    }


    public AABB getBounds() {
        return bounds;
    }

    public boolean isLeaf(){
        if (children==null){
            return true;
        }else {
            return false;
        }

    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
