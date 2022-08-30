package com.spicysauce.lessstress;

public enum Timers {
    HEALTH(2),
    FOOD(0),
    HAPPINESS(1);

    private final int id;

    Timers(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
