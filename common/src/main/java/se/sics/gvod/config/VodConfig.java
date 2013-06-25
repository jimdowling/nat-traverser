/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.config;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.util.StartupConfig;
import se.sics.gvod.address.Address;
import static se.sics.gvod.config.BaseCommandLineConfig.baseInitialized;
import se.sics.gvod.common.util.VodAddressBean;

/**
 *
 * @author jdowling
 */
public class VodConfig extends BaseCommandLineConfig {

    private static final Logger logger = LoggerFactory.getLogger(VodConfig.class);
    public static boolean GUI = true;
    public static final String TORRENT_FILE_POSTFIX = ".data";
    public static final String TORRENT_INDEX_FILE = "activeStreams";
    public static final String NODE_CACHE_FILE = "node.cache";
    public static final String PROP_MOVIE = "movie";
    public static final String PROP_VIDEO_DIR = "video.dir";
    public static final String PROP_TORRENT_DIR = "torrent.dir";
    public static final String PROP_BOOTSTRAP_REFRESH_PERIOD = "client.refresh.period";
    public static final String PROP_BOOTSTRAP_RETRY_PERIOD = "client.retry.period";
    public static final String PROP_BOOTSTRAP_RETRY_COUNT = "client.retry.count";
    public static final String PROP_CONTROL_PORT = "control.server.port";
    public static final String PROP_TORRENT_URL = "torrent.url";
    public static final String PROP_SERVER = "server";
    public static final String PROP_MEDIA_PLAYER = "media.player";
    public static final String PROP_VIDEO_WIDTH = "video.width";
    public static final String PROP_VIDEO_HEIGHT = "video.height";
    public static String STARTUP_CONFIG_FILE;
    // Vod config params
    public static final int DEFAULT_BOOTSTRAP_REFRESH_PERIOD = 5 * 60 * 1000;
    public static final int DEFAULT_CONTROL_PORT = 58024;
    protected static final int DEFAULT_MEDIA_PLAYER = 0;
    protected static final int DEFAULT_VIDEO_WIDTH = 640;
    protected static final int DEFAULT_VIDEO_HEIGHT = 360;
    protected static final String DEFAULT_SERVER = "b00t.info";
    protected static final String DEFAULT_TORRENT_URL = "";
    protected static String DEFAULT_TORRENT_DIR;
    protected static String DEFAULT_VIDEO_DIR;
    public static final int MAX_NUM_HASH_RESPONSE_PACKETS = 4;
    protected static StartupConfig startupConfig;
    protected Option videoDirOption;
    protected Option playerOption;
    protected Option movieOption;
    protected Option torrentDirOption;
    protected Option serverOption;
    protected Option noGuiOption;
    protected Option torrentOption;
    protected Option widthOption;
    protected Option heightOption;
    protected Option seedOption;
    protected Option controlPortOption;
    protected Option bootstrapRefreshOption;
    
    // All protocol default params
    public static final int DEFAULT_RTO = 5 * 1000;
    public static final int DEFAULT_RTO_RETRIES = 3;
    public static final double DEFAULT_RTO_SCALE = 1.75;
    //Croupier parameters
    public static final long SHUFFLE_PERIOD = 30 * 1000;
    public static final long SHUFFLE_TIMEOUT = DEFAULT_RTO;
    public static final int SHUFFLE_LENGTH = 15;
    public static final int RANDOM_VIEW_SIZE = 15;

    public static enum CroupierSelectionPolicy {

        TAIL, RANDOM, HEALER;

        public static CroupierSelectionPolicy create(String policy) {
            if (policy.compareToIgnoreCase(CroupierSelectionPolicy.TAIL.name()) == 0) {
                return TAIL;
            } else if (policy.compareToIgnoreCase(CroupierSelectionPolicy.HEALER.name()) == 0) {
                return HEALER;
            } else if (policy.compareToIgnoreCase(CroupierSelectionPolicy.RANDOM.name()) == 0) {
                return RANDOM;
            }
            return null;
        }
    };
    public static final CroupierSelectionPolicy SELECTION_POLICY = CroupierSelectionPolicy.HEALER;
    
    //Stun parameters
    static int DEFAULT_STUN_RTO = 3*1000;
    static int DEFAULT_STUN_NUM_RETRIES = 2;
    static double DEFAULT_STUN_RTO_SCALE = 1.75d;
    static int DEFAULT_UPNP_TIMEOUT = 10*1000;
    static int DEFAULT_UPNP_DISCOVERY_TIMEOUT = 3*1000;
    static boolean DEFAULT_UPNP_ENABLED = false;
    static final int DEFAULT_PARTNER_HEARTBEAT_PERIOD = 10 * 1000;
    static final int DEFAULT_MAX_NUM_PARTNERS = 10;
    
    //Parent Maker's params
    public static long DEFAULT_PARENT_RTO = DEFAULT_RTO;
    public static long DEFAULT_PARENT_UPDATE_PERIOD = 60 * 1000;
    public static int DEFAULT_PARENT_SIZE = 3;
    public static int DEFAULT_PARENT_KEEP_RTT_TOLERANCE = 20;
    public static int DEFAULT_CHILDREN_SIZE = 50;
    public static int DEFAULT_CHILDREN_REMOVE_TIMEOUT = 0 * 1000;
    public static long DEFAULT_PARENT_TIMEOUT_DELAY = 4 * 1000;
    public static long DEFAULT_PARENT_REJECTED_CLEANUP_TIMEOUT = 300 * 1000;
    // VOD's params
    public static final int BITTORRENT_SET_SIZE = 100;
    public static final int UPPER_SET_SIZE = 100;
    public static final int BELOW_SET_SIZE = 100;
    public static final long CONNECTION_TIMEOUT = 30 * 1000;
    public static final long DATA_REQUEST_TIMEOUT = 5 * 1000;
    public static final long REF_TIMEOUT = DEFAULT_RTO;
    public static final long VERIFY_PERIOD = 10 * 1000;
    public static final int OFFSET = 3;
    public static final long DATA_OFFER_PERIOD = 1 * 1000;
    public static final long READING_PERIOD = 1410 /*readingPeriod  misfits 1048kbps*/;
    public static final long LIM_READING_WINDOW = 11;
    public static final int INF_UTIL_FREC = 63;
    public static final int PERCENT_BACK = 70;
    public static final long CHECK_POSITION_PERIOD = 20 * 1000; // 2 seconds
    public static final Random random;
    public final static int ACK_TIMEOUT = 10 * 1000; // 5 seconds
    public final static int BUFFERING_WINDOW_NUM_PIECES = 1; // in number of pieces
    public final static int PERCENTAGE_FREERIDERS = 15;
    public static final int SEEDER_UTILITY_VALUE = 9999;
    // GRADIENT'S DEFAULTS
    public static final int SIMILAR_SET_SIZE = 10;
    public static final long SETS_EXCHANGE_TIMEOUT = DEFAULT_RTO;
    public static final long SETS_EXCHANGE_PERIOD = 5000;
    public static final long PROBE_REQUEST_TIMEOUT = DEFAULT_RTO * 6;
    public static final int UTILITY_THRESHOLD = 10;
    public static final int NUM_OF_PROBES = 5;
    public static final int PROBE_TTL = 5;
    public static final int NUM_BEST_SIMILAR_PEERS = 10;
    public static final int UTILITY_SET_FILLING_RATE = 10;
    // NAT-TRAVERSER'S DEFAULTS
    public final static int DEFAULT_NT_STUN_RETRIES = 5;
    public final static int DEFAULT_NT_MAX_NUM_OPENED_CONNECTIONS = 5000;
    public final static int DEFAULT_NT_CONNECTION_ESTABLISHMENT_TIMEOUT = 10*1000;
    public final static long DEFAULT_NT_SERVER_INIT_RETRY_PERIOD = 1000;
    public final static long DEFAULT_NT_GARBAGE_COLLECT_STALE_CONNS_PERIOD = 10 * 1000;
    public final static long DEFAULT_NT_STALE_RELAY_MSG_TIME = 60 * 1000;
    // HOLE-PUNCHING CLIENT DEFAULTS
    public final static int DEFAULT_HP_DELTA = 1;
    public final static int DEFAULT_HP_SCANNING_RETRIES = 1;
    // Timeout a hpSession if it hasn't been used for this period of time.
    public final static int DEFAULT_HP_SESSION_EXPIRATION = 55*1000;
    // LEDBAT Defaults
    public static final int MAX_PIPELINE_SIZE = 100;
    public static final int DEFAULT_PIPELINE_SIZE = 15;
    public static final int MAX_SEGMENT_SIZE = 1500 * DEFAULT_PIPELINE_SIZE; // MTU - 1024?
    public static final int DEFAULT_WINDOW_SIZE = 2 * MAX_SEGMENT_SIZE;
    public static final int DEFAULT_MAX_WINDOW_SIZE = 64 * MAX_SEGMENT_SIZE; // 64
    public static final int DEFAULT_LEDBAT_TARGET_DELAY = 100;
    public static final int MINUTE_IN_MS = 60 * 1000;
    public static final int DEFAULT_DELAY_BOUNDARY = 13 * 60 * 1000;
    public static final double GAIN = 0.9d; // must be set to 1 or less
    public static final int MIN_SIZE = 1400; // MTU 1024
    public static final int MIN_CWND = 2;
    public static final int INIT_CWND = 2;
    public static final int BASE_HISTORY = 10; // 10
    public static final int CURRENT_FILTER = 5;
    public static final float SCALE_DOWN_SIZE_TIMEOUT = 0.75f;
    public static final double ALLOWED_INCREASE = 1.0d; // SHOULD be 1, and it MUST be greater than 0.
    public static final int ADSL_MTU = 1460;
    public static int MTU_MEASURED = 1500;
    public static int ASN;
    public static final int IPV4_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;
    public static final int LEDBAT_HEADER_SIZE = 20;
    public static final int UDP_IPV4_OVERHEAD = IPV4_HEADER_SIZE + UDP_HEADER_SIZE;
    public static final int UDP_IPV4_MTU = ADSL_MTU - UDP_IPV4_OVERHEAD - LEDBAT_HEADER_SIZE;
    // Bittorrent and Heartbeat
    public static final int HASH_REQUEST_TIMEOUT = 10 * 1000;
    public static final int UPLOADING_RATE_REQ_TIMEOUT = 5 * 1000;
    public static final int SUSPECTED_DEAD_TIMEOUT_MS = 60 * 1000;
    public static final int DOWNLOAD_CHECK_PERIOD_MS = 5 * 1000;
    public static final int NUM_CYCLES_QUERY_GRANDCHILDREN = 3;
    // TORRENT FILE
    public static final int NUM_HASHES_IN_TORRENT_FILE = 20;
    // OVERLAY-ID RESERVATIONS. Don't change these.
    public static final int SYSTEM_OVERLAY_ID = 0; // System-croupier
    public static final int STUN_OVERLAY_ID = 2; // stunClient, stunServer
    public static final int HP_OVERLAY_ID = 3; // hpClient, zServer, ParentMaker
    public static final int MONITOR_OVERLAY_ID = 4; // monitorClient, monitorServer
    public static long maxUploadBwCapacity = 0;
    public static long uploadBwUsage = 0;
    public static int delta = DEFAULT_HP_DELTA;

    static {
        STARTUP_CONFIG_FILE = BaseCommandLineConfig.GVOD_HOME + File.separator + "sconfig.xml";
        DEFAULT_VIDEO_DIR = BaseCommandLineConfig.GVOD_HOME;
        DEFAULT_TORRENT_DIR = BaseCommandLineConfig.GVOD_HOME;
        random = new Random(System.currentTimeMillis());
    }

    protected VodConfig(String[] args) throws IOException {
        super(args);
    }

    public static final synchronized VodConfig init(String[] args) throws IOException {

        if (singleton != null) {
            return (VodConfig) singleton;
        }

        XMLDecoder decoder = null;
        try {
            decoder = new XMLDecoder(
                    //                    new GZIPInputStream(
                    new BufferedInputStream(new FileInputStream(
                    STARTUP_CONFIG_FILE)) //                    )
                    );

            Object obj = decoder.readObject();
            if (obj == null) {
                System.err.println("Configuration was null. Initializing new config.");
                startupConfig = new StartupConfig(false, new VodAddressBean(), 0, 0);
            } else {
                startupConfig = (StartupConfig) obj;
            }
        } catch (FileNotFoundException e) {
            logger.warn("No configuration found: " + STARTUP_CONFIG_FILE);
            startupConfig = new StartupConfig(false, new VodAddressBean(), 0, 0);
        } catch (Throwable e) {
            logger.warn(e.toString());
            startupConfig = new StartupConfig(false, new VodAddressBean(), 0, 0);
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }

        singleton = new VodConfig(args);
        return (VodConfig) singleton;
    }

    @Override
    protected void parseAdditionalOptions(String[] args) throws IOException {
        playerOption = new Option("player", true,
                "Media Player [0=jwplayer, 1=flowplayer]");
        playerOption.setArgName("player");
        options.addOption(playerOption);

        movieOption = new Option("movie", true, "Movie filename");
        movieOption.setArgName("movie");
        options.addOption(movieOption);

        torrentDirOption = new Option("dir", true,
                "Directory for torrent files");
        torrentDirOption.setArgName("dir");
        options.addOption(torrentDirOption);

        videoDirOption = new Option("videoDir", true,
                "Directory for video files");
        videoDirOption.setArgName("videoDir");
        options.addOption(videoDirOption);

        widthOption = new Option("width", true,
                "Video width in pixels");
        widthOption.setArgName("width");
        options.addOption(widthOption);

        heightOption = new Option("height", true,
                "Video height in pixels");
        heightOption.setArgName("height");
        options.addOption(heightOption);

        serverOption = new Option("server", true,
                "Hostname for server");
        serverOption.setArgName("host");
        options.addOption(serverOption);

        noGuiOption = new Option("nogui", false,
                "Launch without a GUI");
        options.addOption(noGuiOption);

        torrentOption = new Option("torrent", true,
                "Url to torrent file to start loading immediately");
        torrentOption.setArgName("torrentFile");
        options.addOption(torrentOption);

        controlPortOption = new Option("cPort", true, "Control port");
        controlPortOption.setArgName(VAL_ADDRESS);
        options.addOption(controlPortOption);

        seedOption = new Option("seed", true, "Random number seed");
        seedOption.setArgName("number");
        options.addOption(seedOption);

        bootstrapRefreshOption = new Option("bRefreshPeriod", true,
                "Bootstrap refresh Period");
        bootstrapRefreshOption.setArgName(VAL_PERIOD_SECS);
        options.addOption(bootstrapRefreshOption);
    }

    @Override
    protected void processAdditionalOptions() throws IOException {

        if (line.hasOption(playerOption.getOpt())) {
            int player = new Integer(line.getOptionValue(playerOption.getOpt()));
            compositeConfig.setProperty(PROP_MEDIA_PLAYER, player);
        }

        if (line.hasOption(movieOption.getOpt())) {
            String movieFilename = line.getOptionValue(movieOption.getOpt());
            compositeConfig.setProperty(PROP_MOVIE, movieFilename);
        }

        String torrentDir = DEFAULT_TORRENT_DIR;
        if (line.hasOption(torrentDirOption.getOpt())) {
            String dir = line.getOptionValue(torrentDirOption.getOpt());
            File folder = new File(dir);
            if (!folder.exists() || !folder.isDirectory()) {
                help(new String[]{""}, "Torrent path either doesn't exist or is not "
                        + "a directory: " + dir, options);
            }
            compositeConfig.setProperty(PROP_TORRENT_DIR, dir);
            torrentDir = dir;
        }
        if (line.hasOption(videoDirOption.getOpt())) {
            String dir = line.getOptionValue(videoDirOption.getOpt());
            File folder = new File(dir);
            if (!folder.exists() || !folder.isDirectory()) {
                help(new String[]{""}, "Video library path either doesn't exist or is not"
                        + "a directory: " + dir, options);
            }
            compositeConfig.setProperty(PROP_VIDEO_DIR, dir);
        }

        if (line.hasOption(serverOption.getOpt())) {
            String server = line.getOptionValue(serverOption.getOpt());
            compositeConfig.setProperty(PROP_SERVER, server);
        }

        if (line.hasOption(noGuiOption.getOpt())) {
            VodConfig.GUI = false;
        }

        if (line.hasOption(torrentOption.getOpt())) {
            String torrent = line.getOptionValue(torrentOption.getOpt());
            // if user doesn't enter a full URL, assume it's a file

            // if user has set movieOption, then torrent is the name of the torrent to create.
            if (line.hasOption(movieOption.getOpt())) {
                if (torrent.substring(0, 1).compareTo("/") != 0 // unix
                        && torrent.substring(1, 2).compareTo(":\\") != 0 // windows hard drive c:\
                        && torrent.substring(0, 1).compareTo("\\\\") != 0) // windows net drive
                {
                    torrent = torrentDir + File.separator + torrent;
                }
            } else {
                if (torrent.contains("://") == false) {
                    torrent = "file://" + torrent;
                }
            }
            compositeConfig.setProperty(PROP_TORRENT_URL, torrent);
        }
        if (line.hasOption(bootstrapRefreshOption.getOpt())) {
            long bootstrapRefreshPeriod = new Long(line.getOptionValue(bootstrapRefreshOption.getOpt()));
            compositeConfig.setProperty(PROP_BOOTSTRAP_REFRESH_PERIOD, bootstrapRefreshPeriod);
        }

        if (line.hasOption(controlPortOption.getOpt())) {
            int cPort = new Integer(line.getOptionValue(controlPortOption.getOpt()));
            if (cPort < 1024 || cPort > 65535) {
                help(new String[]{""}, "Out-of-range mPort: " + cPort
                        + ". localhost control TCP server must be between 1024 and 65535", options);
            }
            compositeConfig.setProperty(PROP_CONTROL_PORT, cPort);
        }

        if (line.hasOption(seedOption.getOpt())) {
            int seed = new Integer(line.getOptionValue(seedOption.getOpt()));
            compositeConfig.setProperty(PROP_SEED, seed);
        }

        if (line.hasOption(widthOption.getOpt())) {
            int width = new Integer(line.getOptionValue(widthOption.getOpt()));
            if (width < 100 || width > 5000) {
                help(new String[]{""}, "Out-of-range video width: " + width
                        + ". localhost webServer must be between 100 and 5000", options);
            }
            compositeConfig.setProperty(PROP_VIDEO_WIDTH, width);
        }

        if (line.hasOption(heightOption.getOpt())) {
            int height = new Integer(line.getOptionValue(heightOption.getOpt()));
            if (height < 100 || height > 5000) {
                help(new String[]{""}, "Out-of-range video height: " + height
                        + ". localhost webServer must be between 100 and 5000", options);
            }
            compositeConfig.setProperty(PROP_VIDEO_HEIGHT, height);
        }
    }

    public static int getMediaPlayer() {
        baseInitialized();
        return singleton.compositeConfig.getInt(PROP_MEDIA_PLAYER, DEFAULT_MEDIA_PLAYER);
    }

    public static int getWidth() {
        baseInitialized();
        return singleton.compositeConfig.getInt(PROP_VIDEO_WIDTH, DEFAULT_VIDEO_WIDTH);
    }

    public static int getHeight() {
        baseInitialized();
        return singleton.compositeConfig.getInt(PROP_VIDEO_HEIGHT, DEFAULT_VIDEO_HEIGHT);
    }

    public static String getTorrentDir() {
        baseInitialized();
        return singleton.compositeConfig.getString(PROP_TORRENT_DIR, DEFAULT_TORRENT_DIR);
    }

    public static String getVideoDir() {
        baseInitialized();
        return singleton.compositeConfig.getString(PROP_VIDEO_DIR, DEFAULT_VIDEO_DIR);
    }

    public static String getMovie() {
        baseInitialized();
        return singleton.compositeConfig.getString(PROP_MOVIE, "");
    }

    public static String getTorrentUrl() {
        baseInitialized();
        return singleton.compositeConfig.getString(PROP_TORRENT_URL, DEFAULT_TORRENT_URL);
    }

    public static int getBootstrapRefreshPeriod() {
        baseInitialized();
        return singleton.compositeConfig.getInt(PROP_BOOTSTRAP_REFRESH_PERIOD,
                DEFAULT_BOOTSTRAP_REFRESH_PERIOD);
    }

    public static Address getServer() {
        baseInitialized();
        String server = singleton.compositeConfig.getString(PROP_SERVER, DEFAULT_SERVER);
        InetAddress serverIp = null;
        try {
            serverIp = InetAddress.getByName(server);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(BaseCommandLineConfig.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return null;
//        Address addr = new Address(serverIp, DEFAULT_BOOTSTRAP_ID);
    }

    public static boolean saveConfiguration(VodAddressBean vodAddressBean, boolean upnp,
            int numUnchanged, int numTimesSinceStunLastRun) {
        boolean isSaved;
        XMLEncoder encoder = null;
        StartupConfig sConfig = new StartupConfig(upnp, vodAddressBean, ++numUnchanged,
                ++numTimesSinceStunLastRun);
        try {
            encoder = new XMLEncoder(
                    //                    new GZIPOutputStream(
                    new BufferedOutputStream(
                    new FileOutputStream(STARTUP_CONFIG_FILE, false)) //                    )
                    );

            encoder.writeObject(sConfig);
            encoder.flush();
            isSaved = true;
        } catch (FileNotFoundException e) {
            logger.warn(e.toString(), e);
            isSaved = false;
        } catch (IOException e) {
            logger.warn(e.toString(), e);
            isSaved = false;
        } finally {
            if (encoder != null) {
                encoder.close();
            }
        }
        return isSaved;
    }

    public static Boolean removeDiskCache() {
        File sConfig = new File(STARTUP_CONFIG_FILE);
        return sConfig.delete();
    }

    public static StartupConfig getStartupConfig() {
        return startupConfig;
    }

    public static String getTorrentIndexFile() {
        return getTorrentDir() + File.separator + TORRENT_INDEX_FILE;
    }

    public static String getNodeCacheFile() {
        return GVOD_HOME + File.separator + NODE_CACHE_FILE;
    }

    public synchronized static long getMaxUploadBwCapacity() {
        return maxUploadBwCapacity;
    }

    public synchronized static void setMaxUploadBwCapacity(long newMax) {
        maxUploadBwCapacity = newMax;
    }

    public synchronized static long getUploadBwUsage() {
        return uploadBwUsage;
    }

    public synchronized static void setUploadBwUsage(long newBw) {
        uploadBwUsage = newBw;
    }

    public synchronized static long getAvailableUploadBw() {
        return maxUploadBwCapacity - uploadBwUsage;
    }

    public synchronized static int getDelta() {
        return delta;
    }

    public synchronized static void setDelta(int updateDelta) {
        delta = updateDelta;
    }

    public static int getControlPort() {
        baseInitialized();
        return singleton.compositeConfig.getInt(PROP_CONTROL_PORT, DEFAULT_CONTROL_PORT);
    }
}
