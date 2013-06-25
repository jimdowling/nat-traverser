/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.gvod.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.VodAddress;

/**
 * The
 * <code>BootstrapConfiguration</code> class.
 *
 */
public final class BootstrapConfiguration
        extends AbstractConfiguration<BootstrapConfiguration> {

    /**
     * Fields cannot be private. Package protected, ok.
     */
//    Address bootstrapServerAddress;
    String ip;
    int port;
    int id;
    long clientRetryPeriod;
    int clientRetryCount;
    long clientKeepAlivePeriod;
    int clientWebPort;

    /**
     * Default constructor comes first.
     */
    BootstrapConfiguration() {
        this(
                BaseCommandLineConfig.getSeed(),
                BaseCommandLineConfig.getBootstrapServer().getIp().getHostAddress(),
                BaseCommandLineConfig.getBootstrapServer().getPort(),
                BaseCommandLineConfig.getBootstrapServer().getId(),
                BaseCommandLineConfig.DEFAULT_BS_CLIENT_RETRY_PERIOD,
                BaseCommandLineConfig.DEFAULT_BS_CLIENT_RETRY_COUNT,
                BaseCommandLineConfig.DEFAULT_BS_CLIENT_KEEP_ALIVE_PERIOD,
                BaseCommandLineConfig.DEFAULT_BS_CLIENT_WEB_PORT);
    }

    /**
     * Full argument constructor comes second.
     */
    public BootstrapConfiguration(int seed, String ip, int port, int id,
            long clientRetryPeriod, int clientRetryCount,
            long clientKeepAlivePeriod, int clientWebPort) {
        super(seed);
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.clientRetryPeriod = clientRetryPeriod;
        this.clientRetryCount = clientRetryCount;
        this.clientKeepAlivePeriod = clientKeepAlivePeriod;
        this.clientWebPort = clientWebPort;
    }

    public static BootstrapConfiguration build() {
        return new BootstrapConfiguration();
    }

    public Address getBootstrapServerAddress() {
        try {
            return new Address(InetAddress.getByName(ip), port, id);
        } catch (UnknownHostException ex) {
            Logger.getLogger(BootstrapConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public VodAddress getBootstrapServerVodAddress() {
        return new VodAddress(getBootstrapServerAddress(),
                BaseCommandLineConfig.DEFAULT_BOOTSTRAP_ID);
    }

    public long getClientRetryPeriod() {
        return clientRetryPeriod;
    }

    public int getClientRetryCount() {
        return clientRetryCount;
    }

    public long getClientKeepAlivePeriod() {
        return clientKeepAlivePeriod;
    }

    public int getClientWebPort() {
        return clientWebPort;
    }

    public BootstrapConfiguration setBootstrapServerAddress(Address bootstrapServerAddress) {
        this.ip = bootstrapServerAddress.getIp().getHostAddress();
        this.port = bootstrapServerAddress.getPort();
        this.id = bootstrapServerAddress.getId();
        return this;
    }

    public BootstrapConfiguration setClientRetryPeriod(long clientRetryPeriod) {
        this.clientRetryPeriod = clientRetryPeriod;
        return this;
    }

    public BootstrapConfiguration setClientRetryCount(int clientRetryCount) {
        this.clientRetryCount = clientRetryCount;
        return this;
    }

    public BootstrapConfiguration setClientKeepAlivePeriod(long clientKeepAlivePeriod) {
        this.clientKeepAlivePeriod = clientKeepAlivePeriod;
        return this;
    }

    public BootstrapConfiguration setClientWebPort(int clientWebPort) {
        this.clientWebPort = clientWebPort;
        return this;
    }
}
