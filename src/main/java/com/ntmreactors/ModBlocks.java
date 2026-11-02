package com.ntmreactors;

import com.ntmreactors.block.BlockReactor;

public final class ModBlocks {

    public static final BlockReactor FAST_REACTOR = new BlockReactor(ReactorType.FAST);
    public static final BlockReactor THORIUM_REACTOR = new BlockReactor(ReactorType.THORIUM);
    public static final BlockReactor HEAVY_WATER_REACTOR = new BlockReactor(ReactorType.HEAVY_WATER);

    public static BlockReactor[] all() {
        return new BlockReactor[]{FAST_REACTOR, THORIUM_REACTOR, HEAVY_WATER_REACTOR};
    }

    private ModBlocks() {}
}
