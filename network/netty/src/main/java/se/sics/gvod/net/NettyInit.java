/**
 * This file is part of the Kompics component model runtime.
 * 
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.gvod.net;

import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.kompics.Init;

/**
 * 
 * @author Jim Dowling <jdowling@sics.se>
 */
public final class NettyInit extends Init<NettyNetwork> {

    private final int seed;
    private final int maxPacketSize;
    private final Class<? extends MsgFrameDecoder> msgDecoderClass;
    private final boolean enableBandwidthStats;
    
    public NettyInit(int seed, boolean bindAllNetworkIfs, 
            Class<? extends MsgFrameDecoder> msgDecoderClass) {
        this(seed, BaseCommandLineConfig.DEFAULT_MTU, msgDecoderClass);
    }

    public NettyInit(int seed, int mtu,
            Class<? extends MsgFrameDecoder> msgDecoderClass) {
        this(seed, mtu, msgDecoderClass, true);
    }

    public NettyInit(int seed, int mtu, Class<? extends MsgFrameDecoder> msgDecoderClass, 
            boolean enableBandwidthStats) {
        super();
        this.seed = seed;
        this.maxPacketSize = mtu;
        this.msgDecoderClass = msgDecoderClass;
        this.enableBandwidthStats = enableBandwidthStats;
    }


    public boolean isEnableBandwidthStats() {
        return enableBandwidthStats;
    }

    public Class<? extends MsgFrameDecoder> getMsgDecoderClass() {
        return msgDecoderClass;
    }

    public int getSeed() {
        return seed;
    }

    public int getMTU() {
        return maxPacketSize;
    }

}
