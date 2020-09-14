package com.jme.effekseer;

import java.io.IOException;
import java.io.InputStream;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

/**
 * EffekseerLoader, to load efkefc files
 * @author Riccardo Balbo
 */
public class EffekseerLoader implements AssetLoader{
    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        InputStream is=assetInfo.openStream();
        EffekseerEmitterControl efc=Effekseer.loadEffect(assetInfo.getManager(),assetInfo.getKey().getName(),is,null);
        is.close();
        return efc;
    }

}