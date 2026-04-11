package de.harich.thilo.factoring.data;

public class Factorisation {
    public String csvList;
    public String product;
    public long durationMs;

    public Factorisation(String product, String csv) {
        this.product = product;
        this.csvList = csv;
    }

    public Factorisation(String product, String csv, long durationMs) {
        this.product = product;
        this.csvList = csv;
        this.durationMs = durationMs;
    }
}
