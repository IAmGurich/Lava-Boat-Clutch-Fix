package com.lavaboatclutch.util;

public interface LbcBoatImmunity {
    int  lbc_getImmunityTicks();
    void lbc_setImmunityTicks(int ticks);
    boolean lbc_wasInLavaLastTick();
    void lbc_setWasInLavaLastTick(boolean value);

    int  lbc_getFireSuppressTicks();
    void lbc_setFireSuppressTicks(int ticks);
}
