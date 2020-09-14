package com.jme.effekseer.driver.fun;

public interface EffekseerEmissionCallback{
    public static enum CallbackType{
        SET_HANDLE,
        DESTROY_HANDLE
    }
    public void call(CallbackType type,Integer handler);

}