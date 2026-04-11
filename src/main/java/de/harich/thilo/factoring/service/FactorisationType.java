package de.harich.thilo.factoring.service;

public enum FactorisationType {

    TRIAL_DIVISION_AND_HART(1, "Trial division and Hart"),
    TRIAL_DIVISION_ONLY(2, "Trial division only"),
    ELLIPTIC_CURVE_METHOD(3, "Elliptic curve method");


    private final int type;
    private final String name;

    // Der Konstruktor eines Enums ist immer implizit private
    FactorisationType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    // Getter, um auf die Werte zuzugreifen
    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
