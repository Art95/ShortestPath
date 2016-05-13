package geometry;

import main.Constants;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by Artem on 17.04.2016.
 */
public class PolarCoordinates {
    private double angle;
    private double radius;

    public PolarCoordinates() {
        this.angle = 0;
        this.radius = 0;
    }

    public PolarCoordinates(double angle, double radius) {
        this.angle = angle;
        this.radius = radius;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return this.angle;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return this.radius;
    }

    public CartesianCoordinates toCartesianCoordinates() {
        double x = radius * Math.cos(angle);
        double y = radius * Math.sin(angle);

        x = roundToNDecimalPlaces(x, Constants.PRECISION);
        y = roundToNDecimalPlaces(y, Constants.PRECISION);

        return new CartesianCoordinates(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolarCoordinates)) return false;

        PolarCoordinates that = (PolarCoordinates) o;

        return Math.abs(this.angle - that.angle) < Constants.EPS && Math.abs(this.radius - that.radius) < Constants.EPS;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(angle);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "(" +
                "angle = " + angle +
                ", radius = " + radius +
                ')';
    }

    private double roundToNDecimalPlaces(double value, int n) {
        long rounder = (long) Math.pow(10.0, n);

        value = Math.round(value * rounder);
        value /= rounder;

        return value;
    }
}
