package com.jme.effekseer;

import com.jme3.app.state.AbstractAppState;

public class EffekseerUpdater extends AbstractAppState {
    public void update(float tpf) {
        Effekseer.update(tpf);
    }
}