package de.harich.thilo.factoring.data;

public class Factorisation {
    static double nanosecond = 0.000000001;

    public String csvList;
    public String product;
    public double seconds;

    public Factorisation(String product, String csv) {
        this.product = product;
        this.csvList = csv;
    }

    public Factorisation(String product, String csv, long nanoseconds) {
        this.product = product;
        this.csvList = csv;
        this.seconds = nanoseconds * nanosecond;
    }
}
