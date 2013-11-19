package se.sics.gvod.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.address.Address;

/**
 * This class is not thread-safe for writing, although it is thread-safe for
 * reading.
 *
 * @author jdowling
 *
 */
public abstract class BaseCommandLineConfig {

    private static final Logger logger = LoggerFactory.getLogger(BaseCommandLineConfig.class);
    public static int BOOTSTRAP_HEARTBEAT_MS = 30 * 1000;
    /**
     * MTU of 1500 bytes - UDP header = 1472 bytes
     */
    public final static int DEFAULT_MTU = 1400;
    /**
     * IP packet minimum MTU of 576 - 20 (ip header) - 16 (udp header) = 540
     */
    public final static int MIN_MTU = 540;
    public static final String LOCALHOST = "127.0.0.1";
    public static final String USER_HOME;
    public static final String BOOTSTRAP_CONFIG_FILE = "bootstrap.properties";
    public static final String MONITOR_CONFIG_FILE = "monitor.properties";
    public static final String POM_FILENAME = "pom.xml";
    public static String GVOD_HOME;
    public static final String MAVEN_REPO_LOCAL;
    public static final String PROP_PORT = "port";
    public static final String PROP_MEDIA_PORT = "web.port";
    public static final String PROP_SEED = "seed";
    public static final String PROP_BOOTSTRAP_PORT = "bootstrap.server.port";
    public static final String PROP_BOOTSTRAP_IP = "bootstrap.server.ip";
    public static final String PROP_MONITOR_IP = "monitor.server.ip";
    public static final String PROP_MONITOR_PORT = "monitor.server.port";
    public static final String PROP_MONITOR_ID = "monitor.server.id";
    public static final String PROP_MONITOR_REFRESH_PERIOD = "client.refresh.period";
    public static final String PROP_MONITOR_RETRY_PERIOD = "client.retry.period";
    public static final String PROP_MONITOR_RETRY_COUNT = "client.retry.count";
    public static final String PROP_GVOD_HOME = "gvod.home";
    public static final String PROP_HOSTNAME = "host.name";
    public static final String PROP_NUM_WORKERS = "workers.num";

    static {


        if (isOperatingSystemWindows()) {
            GVOD_HOME = "gvod";
        } else {
            GVOD_HOME = ".config/nattraverser";
        }

        String kHome = System.getProperty(PROP_GVOD_HOME);
        USER_HOME = System.getProperty("user.home");

        if (USER_HOME != null && kHome == null) {
            System.setProperty(PROP_GVOD_HOME, new File(USER_HOME + File.separator
                    + ".config" + File.separator + "nattraverser").getAbsolutePath());
        } else if (USER_HOME == null && kHome == null) {
            throw new IllegalStateException(
                    "kompics.home and user.home environment variables not set.");
        }
        GVOD_HOME = System.getProperty(PROP_GVOD_HOME);

        if (GVOD_HOME == null) {
            GVOD_HOME = USER_HOME + File.separator + ".config" + File.separator + "nattraverser";
        }

        if (new File(BaseCommandLineConfig.GVOD_HOME).exists() == false) {
            if (new File(BaseCommandLineConfig.GVOD_HOME).mkdirs() == false) {
                throw new IllegalStateException("Could not create directory: " + BaseCommandLineConfig.GVOD_HOME);
            }
        }

        String mavenHome = System.getProperty("maven.repo.local");
        if (mavenHome == null) {
            System.setProperty("maven.repo.local", new File(USER_HOME
                    + File.separator + ".m2" + File.separator + "repository").getAbsolutePath());
        }
        MAVEN_REPO_LOCAL = System.getProperty("maven.repo.local");

        if (new File(BaseCommandLineConfig.MAVEN_REPO_LOCAL).exists() == false) {
            if ((new File(BaseCommandLineConfig.MAVEN_REPO_LOCAL).mkdirs()) == false) {
                throw new IllegalStateException("Couldn't set directory for Maven Local Repository: " + BaseCommandLineConfig.MAVEN_REPO_LOCAL + "\nCheck file permissions for this directory.");
            }
        }

    }

    public static final int DEFAULT_PORT = 58022;
    public static final int DEFAULT_STUN_PORT = 3478;
    public static final int DEFAULT_STUN_PORT_2 = 3479;
    // only use IP Addresses here - not hostnames.
    public static final String DEFAULT_BOOTSTRAP_IP = "193.10.64.216"; // cloud7
    public static final int DEFAULT_BOOTSTRAP_PORT = 8011;
    public static final String DEFAULT_MONITOR_IP = "193.10.64.216";
    public static final int DEFAULT_MONITOR_PORT = 8020;
    public static final int DEFAULT_MEDIA_PORT = 58026;
    public static final int DEFAULT_WEB_REQUEST_TIMEOUT_MS = 30 * 1000;
    public static final int DEFAULT_WEB_THREADS = 2;
    public static final int DEFAULT_SEED = 123;
    protected static final int DEFAULT_NUM_WORKERS = 4;
    protected static final String VAL_ADDRESS = "address";
    protected static final String VAL_NUMBER = "number";
    protected static final String VAL_PERIOD_SECS = "seconds";
    protected static final String VAL_PERIOD_MILLISECS = "milliseconds";
    protected InetAddress ip = null;
    protected static boolean SIMULATION = false;
    protected static boolean TEN_DOT = false;
    protected static boolean SKIP_UPNP = true;
    protected static boolean SKIP_GRADIENT = true;
    protected static boolean REPORT_NETTY_EXCEPTIONS = true;

    // All protocol default params
    public static final int DEFAULT_RTO = 1 * 1000;
    public static final int DEFAULT_RTO_RETRIES = 3;
    public static final double DEFAULT_RTO_SCALE = 1.33;
    
    // Bootstrap defaults
    public final static int DEFAULT_BS_CLIENT_RETRY_PERIOD = 10 * 1000;
    public final static int DEFAULT_BS_CLIENT_RETRY_COUNT = 2;
    public final static int DEFAULT_BS_CLIENT_KEEP_ALIVE_PERIOD = 15 * 60 * 1000;
    public final static int DEFAULT_BS_WEB_PORT = 1969;
    
    
    /**
     * Singleton instance of configuration
     */
    protected static BaseCommandLineConfig singleton = null;
    public CompositeConfiguration compositeConfig = new CompositeConfiguration();
    /**
     * Helper non-public fields
     */
    protected Options options = new Options();
    protected CommandLine line;
    protected int nodeId;

    /**
     * You can call this constructor from the main method of your Main class,
     * with the args parameters from your main method.
     *
     * @param args
     * @return
     * @throws IOException
     */
    protected BaseCommandLineConfig(String[] args) throws IOException {

        List<String> arguments = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-X")) {
                arguments.add(args[i]);
            }
        }

        // Users can override the default options from the command line
        Option seedOption = new Option("seed", true,
                "Random number seed");
        seedOption.setArgName("seed");
        options.addOption(seedOption);


        Option mediaPortOption = new Option("mPort", true,
                "Media Port for browser to connect to Gvod");
        mediaPortOption.setArgName("mPort");
        options.addOption(mediaPortOption);


        Option ipOption = new Option("ip", true,
                "hostname/ip for Gvod");
        ipOption.setArgName("address");
        options.addOption(ipOption);

        Option portOption = new Option("port", true, "port for gvod");
        portOption.setArgName(VAL_NUMBER);
        options.addOption(portOption);

        Option bootstrapIpOption = new Option("bIp", true, "Bootstrap server host/ip");
        bootstrapIpOption.setArgName(VAL_ADDRESS);
        options.addOption(bootstrapIpOption);

        Option bootstrapPortOption = new Option("bPort", true, "Bootstrap server port");
        bootstrapPortOption.setArgName(VAL_NUMBER);
        options.addOption(bootstrapPortOption);

        Option numWorkersOption = new Option("workers", true, "Number of Workers to create");
        numWorkersOption.setArgName(VAL_NUMBER);
        options.addOption(numWorkersOption);

        Option monitorIpOption = new Option("mIp", true, "Monitor server host/ip");
        monitorIpOption.setArgName("address");
        options.addOption(monitorIpOption);

        Option help = new Option("help", false, "Help message printed");
        options.addOption(help);
        
        Option simulation = new Option("simulation", false, "Simulation mode enabled");
        options.addOption(simulation);

        Option tenDot = new Option("tendot", false, "Use only 10.* IP address");
        options.addOption(tenDot);

        Option upnp = new Option("noupnp", false, "Skip UPnP port mapping attempt");
        options.addOption(upnp);

        Option gradient = new Option("gradient", false, "Leave out gradient layer");
        options.addOption(gradient);

        Option monitorRefreshOption = new Option("mRefreshPeriod", true,
                "Client Monitor refresh Period");
        monitorRefreshOption.setArgName(VAL_PERIOD_SECS);
        options.addOption(monitorRefreshOption);

        // implemented by subclass
        parseAdditionalOptions(arguments.toArray(new String[arguments.size()]));

        CommandLineParser parser = new GnuParser();
        try {
            line = parser.parse(options, arguments.toArray(new String[arguments.size()]));
        } catch (ParseException e) {
            help(args, "Parsing failed.  " + e.getMessage(), options);
        }
        // implemented by subclass
        processAdditionalOptions();

        if (line.hasOption(help.getOpt())) {
            help(args, "GVod usage", options);
        }

        if (line.hasOption(simulation.getOpt())) {
            SIMULATION = true;
        }
        
        if (line.hasOption(upnp.getOpt())) {
            SKIP_UPNP = true;
        }
        
        if (line.hasOption(gradient.getOpt())) {
            SKIP_GRADIENT = true;
        }

        if (line.hasOption(tenDot.getOpt())) {
            TEN_DOT = true;
        }

        int seed = DEFAULT_SEED;
        if (line.hasOption(seedOption.getOpt())) {
            seed = new Integer(line.getOptionValue(seedOption.getOpt()));
            compositeConfig.setProperty(PROP_SEED, seed);
        }

        if (line.hasOption(mediaPortOption.getOpt())) {
            int mediaPort = new Integer(line.getOptionValue(mediaPortOption.getOpt()));

            if (mediaPort < 1024 || mediaPort > 65535) {
                help(args, "Out-of-range web-port : " + mediaPort
                        + ". Web-port must be between 1024 and 65535", options);
            }
            compositeConfig.setProperty(PROP_MEDIA_PORT, mediaPort);
        }


        int port = DEFAULT_PORT;
        if (line.hasOption(portOption.getOpt())) {
            port = new Integer(line.getOptionValue(portOption.getOpt()));
            if (port < 1024 || port > 65535) {
                help(args, "Out-of-range port : " + port
                        + ". Port must be between 1024 and 65535", options);
            }
            compositeConfig.setProperty(PROP_PORT, port);
        }

        if (line.hasOption(bootstrapIpOption.getOpt())) {
            String bootstrapHost = line.getOptionValue(bootstrapIpOption.getOpt());
            compositeConfig.setProperty(PROP_BOOTSTRAP_IP, bootstrapHost);
        }
        if (line.hasOption(bootstrapPortOption.getOpt())) {
            int bootstrapPort = new Integer(line.getOptionValue(bootstrapPortOption.getOpt()));
            if (bootstrapPort < 1024 || bootstrapPort > 65535) {
                help(args, "Out-of-range bootstrap-port : " + bootstrapPort
                        + ". Bootstrap-port must be between 1024 and 65535", options);
            }
            compositeConfig.setProperty(PROP_BOOTSTRAP_PORT, bootstrapPort);
        }

        if (line.hasOption(numWorkersOption.getOpt())) {
            int numWorkers = new Integer(line.getOptionValue(numWorkersOption.getOpt()));
            compositeConfig.setProperty(PROP_NUM_WORKERS, numWorkers);
        }

        if (line.hasOption(monitorIpOption.getOpt())) {
            String monitorHost = line.getOptionValue(monitorIpOption.getOpt());
            compositeConfig.setProperty(PROP_MONITOR_IP, monitorHost);
        }



        if (line.hasOption(monitorRefreshOption.getOpt())) {
            long monitorRefreshPeriod = new Long(line.getOptionValue(monitorRefreshOption.getOpt()));
            compositeConfig.setProperty(PROP_MONITOR_REFRESH_PERIOD, monitorRefreshPeriod);
        }


        // generate a random id from 0 to 1 million
//        Random r = new Random(seed);
        Random r = new Random(System.currentTimeMillis());

        nodeId = r.nextInt(10 * 1000);

        // if no port specified, use a random port in range up to 500.
        // This prevents clashes on upnp port
//        if (port == DEFAULT_PORT) {
//            port += id % 500;
//            compositeConfig.setProperty(PROP_PORT, port);
//        }

    }

    abstract protected void parseAdditionalOptions(String[] args) throws IOException;

    abstract protected void processAdditionalOptions() throws IOException;

    /**
     * @param options Op
     */
    protected void help(String[] args, String message, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        formatter.printHelp("Gvod", options);
        writer.close();
        displayHelper(args, message, stringWriter.getBuffer().toString());
        System.exit(1);
    }

    protected void displayHelper(String[] args, String message, String usage) {
        for (String a : args) {
            System.out.print(a);
            System.out.print(" ");
        }
        if (message != null) {
            System.out.println(message);
        }
        System.out.println(usage);
    }

    /**
     * Implemented by monitor program for concrete overlay
     *
     * @return id of monitor server for concrete overlay
     */
    protected Address getMonitorServerAddress() {
        // Override in subclass
        // new Address(DEFAULT_MONITOR_IP, DEFAULT_MONITOR_PORT, DEFAULT_MONITOR_ID);
        return null;
    }

    /**
     * Implemented by monitor program for concrete overlay
     *
     * @return id of monitor server for concrete overlay
     */
//    protected int getMonitorId() {
//        // Override in subclass
//        return SYSTEM_OVERLAY_ID;
//    }

    public static InetAddress getIp() {
        baseInitialized();
        return singleton.ip;
    }

    public static void setIp(InetAddress nodeIp) {
        baseInitialized();
        singleton.ip = nodeIp;
    }

    
    
    public static int getNodeId() {
        baseInitialized();
        return singleton.nodeId;
    }

    public static void setNodeId(int nodeId) {
        baseInitialized();
        singleton.nodeId = nodeId;
    }

    public static int getPort() {
        baseInitialized();
        return singleton.compositeConfig.getInt(PROP_PORT, DEFAULT_PORT);
    }

    public static int getMediaPort() {
        baseInitialized();
        return singleton.compositeConfig.getInt(PROP_MEDIA_PORT, DEFAULT_MEDIA_PORT);
    }

    public static int getSeed() {
        baseInitialized();
        return singleton.compositeConfig.getInt(PROP_SEED, DEFAULT_SEED);
    }

    public static int getBootstrapServerPort() {
        baseInitialized();
        return singleton.compositeConfig.getInt(PROP_BOOTSTRAP_PORT,
                DEFAULT_BOOTSTRAP_PORT);
    }
    
    public static void setBoostrapServerIp(InetAddress ip) {
        baseInitialized();
        singleton.compositeConfig.setProperty(PROP_BOOTSTRAP_IP, ip.getHostAddress());
    }    
    
    /**
     * 
     * @return Address or null if it cannot resolve the hostname
     */
    public static Address getBootstrapServer() {
        baseInitialized();
        String addr = singleton.compositeConfig.getString(PROP_BOOTSTRAP_IP,
                DEFAULT_BOOTSTRAP_IP);
        int bootPort = singleton.compositeConfig.getInt(PROP_BOOTSTRAP_PORT,
                DEFAULT_BOOTSTRAP_PORT);
        int bootId = VodConfig.SYSTEM_OVERLAY_ID;
        InetAddress ip;
        try {
            ip = InetAddress.getByName(addr);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(BaseCommandLineConfig.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return new Address(ip, bootPort, bootId);
    }

    public static Address getMonitorServer() {
        baseInitialized();
        String addr = singleton.compositeConfig.getString(PROP_MONITOR_IP,
                DEFAULT_MONITOR_IP);
        int monitorPort = singleton.compositeConfig.getInt(PROP_MONITOR_PORT,
                DEFAULT_MONITOR_PORT);
        int id = VodConfig.SYSTEM_OVERLAY_ID;
        try {
            InetAddress ip = InetAddress.getByName(addr);
            return new Address(ip, monitorPort, id);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public static int getNumWorkers() {
        return singleton.compositeConfig.getInt(PROP_NUM_WORKERS,
                DEFAULT_NUM_WORKERS);
    }

    protected static void baseInitialized() {
        if (singleton == null) {
            throw new IllegalStateException(
                    "Configuration not initialized. You must call init method first, before other methods.");
        }
    }

    public static void printConfigurationValues() {
        logger.info("============= Start Configuration Values =============================");
        logger.info("");
        logger.info("");
        logger.info("============= End Configuration Values =============================");
    }

    public static boolean isOperatingSystemWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        //windows
        return (os.indexOf("win") >= 0);
    }

    public static boolean isOperatingSystemMac() {
        String os = System.getProperty("os.name").toLowerCase();
        //Mac
        return (os.indexOf("mac") >= 0);
    }

    public static boolean isOperatingSystemUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        //linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }


    public static boolean isTenDot() {
        return TEN_DOT;
    }

    public static boolean isSkipUpnp() {
        return SKIP_UPNP;
    }
    
    public static boolean isSimulation() {
        return SIMULATION;
    }

    public static boolean isSkipGradient() {
        return SKIP_GRADIENT;
    }

    protected static CompositeConfiguration getCompositeConfiguration() {
        return singleton.compositeConfig;
    }
    
    public static boolean reportNettyExceptions() {
        return REPORT_NETTY_EXCEPTIONS;
    }
}
