package pcd.sequential.model;

public record V2d(double x, double y) {

    public double abs() {
        return Math.sqrt(x * x + y * y);
    }

    public V2d mul(double fact) {
        return new V2d(x * fact, y * fact);
    }

    public V2d getSwappedX() {
        return new V2d(-x, y);
    }

    public V2d getSwappedY() {
        return new V2d(x, -y);
    }

    public String toString() {
        return "V2d(" + x + "," + y + ")";
    }
}