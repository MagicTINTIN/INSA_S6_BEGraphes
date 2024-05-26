package org.insa.graphs.algorithm.termas;

public class Vec2 {
    public double x;
    public double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void add(Vec2 a) {
        this.x += a.x;
        this.y += a.y;
    }
    public static Vec2 add(Vec2 a, Vec2 b) {
        return new Vec2(a.x + b.x, a.y + b.y);
    }

    public void sub(Vec2 a) {
        this.x -= a.x;
        this.y -= a.y;
    }
    public static Vec2 sub(Vec2 a, Vec2 b) {
        return new Vec2(a.x - b.x, a.y - b.y);
    }


    public void mul(double a) {
        this.x *= a;
        this.y *= a;
    }
    public static Vec2 mul(Vec2 a, double b) {
        return new Vec2(a.x * b, a.y * b);
    }

    public static double dot(Vec2 a, Vec2 b) {
        return a.x * b.x + a.y * b.y;
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
