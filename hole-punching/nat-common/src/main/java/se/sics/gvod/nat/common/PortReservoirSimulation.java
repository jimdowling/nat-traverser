package se.sics.gvod.nat.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Port Reservior
 *
 */
public class PortReservoirSimulation implements PortReservoir
{
    private List<Integer> allocatedPorts = null;
    private Random rand;

    public PortReservoirSimulation(long seed)
    {
        allocatedPorts = new ArrayList<Integer>();
        rand = new Random();
        rand.setSeed(seed);
    }

    public int getAvailableRandomPort()
    {
        int randPort = -1;
        do
        {
            randPort = rand.nextInt(65535 - 1024 + 1) + 1024;

        } while (allocatedPorts.contains(randPort));
        
        allocatedPorts.add(randPort);

        return randPort;
    }

    public void releasePort(int port)
    {
        allocatedPorts.remove(port);
    }
}
