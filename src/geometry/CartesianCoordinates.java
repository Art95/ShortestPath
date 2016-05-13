package geometry;

import main.Constants;

/**
 * Created by Artem on 17.04.2016.
 */
public class CartesianCoordinates {
    private double x;
    private double y;

    public CartesianCoordinates() {
        this.x = 0;
        this.y = 0;
    }

    public CartesianCoordinates(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return this.x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return this.y;
    }

    public PolarCoordinates toPolarCoordinates() {
        double radius = Math.sqrt(y * y + x * x);
        double angle;

        if (x > 0 && y >= 0)
            angle = Math.atan(y / x);
        else if (x > 0 && y < 0)
            angle = Math.atan(y / x) + 2 * Math.PI;
        else if (x < 0)
            angle = Math.atan(y / x) + Math.PI;
        else if (x == 0 && y > 0)
            angle = Math.PI / 2;
        else if (x == 0 && y < 0)
            angle = 3 * Math.PI / 2;
        else
            angle = 0.0;

        return new PolarCoordinates(angle, radius);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartesianCoordinates)) return false;

        CartesianCoordinates that = (CartesianCoordinates) o;

        return Math.abs(this.x - that.x) < Constants.EPS && Math.abs(this.y - that.y) < Constants.EPS;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "(" + x + "; " + y + ')';
    }

    public static CartesianCoordinates parseCartesianCoordinates(String text) {
        text = text.trim();

        if (text.isEmpty())
            throw new IllegalArgumentException("Cartesian coordinates: Can't parse empty string!");

        if (text.charAt(0) != text.charAt(text.length() - 1) || text.charAt(0) != '(')
            throw new IllegalArgumentException("Cartesian coordinates: Wrong input format! Can't parse " + text);

        text = text.substring(1, text.length() - 1);

        String[] coordinates = text.split(";");

        if (coordinates.length != 2)
            throw new IllegalArgumentException("Cartesian coordinates: Wrong input format! Can't parse " + text);

        double x = Double.parseDouble(coordinates[0].trim());
        double y = Double.parseDouble(coordinates[1].trim());

        return new CartesianCoordinates(x, y);
    }

    private double roundToNDecimalPlaces(double value, int n) {
        long rounder = (long) Math.pow(10.0, n);

        value = Math.round(value * rounder);
        value /= rounder;

        return value;
    }
}
