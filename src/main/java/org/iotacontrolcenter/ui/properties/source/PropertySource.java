package org.iotacontrolcenter.ui.properties.source;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.iotacontrolcenter.ui.util.UiUtil;

public class PropertySource {

    private static PropertySource instance;
    private static Object SYNC_INST = new Object();
    public static PropertySource getInstance() {
        synchronized (SYNC_INST) {
            if(PropertySource.instance == null) {
                PropertySource.instance = new PropertySource();
            }
            return PropertySource.instance;
        }
    }

    private static final Pattern PATTERN_TRUE = Pattern.compile("1|on|true|yes", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_FALSE = Pattern.compile("0|off|false|no", Pattern.CASE_INSENSITIVE);

    private static final String CONF_FILE = "icc.properties";

    private static final String LOC_COUNTRY_PROP = "countryLocale";
    private static final String LOC_LANG_PROP = "languageLocale";
    private static final String LOC_LANG_DEFAULT = "en";
    private static final String LOC_COUNTRY_DEFAULT = "US";

    public static final String IOTA_NEIGHBORS_PROP="iotaNeighbors";

    public static final String ICCR_PORT_DEFAULT_PROP = "iccrDefaultPortNumber";
    public static final String ICCR_API_KEY_DEFAULT_PROP = "iccrDefaultApiKey";

    public static final String REFRESH_NBRS_PROP = "refreshNeighborsTime";
    public static final String REFRESH_NODEINFO_PROP = "refreshNodeInfoTime";
    public static final String RUN_IOTA_REFRESH_PROP = "runIotaRefresh";
    public static final String IOTA_DLD_LINK_PROP = "iotaDownloadLink";
    //public static final String IOTA_DLD_FILENAME_PROP = "iotaDownloadFilename";
    //public static final String WALLET_START_PROP = "iotaDownloadFilename";
    public static final String SERVERKEYS_PROP = "serverKeys";

    @SuppressWarnings("unused")
    private static final String SERVER_LOCAL_KEY_VAL="local";
    private static final String SERVERKEYS_NAME_PREFIX_PROP = "server.name.";
    private static final String SERVERKEYS_IP_PREFIX_PROP = "server.ip.";
    private static final String SERVERKEYS_ICCR_PORT_NUM_PREFIX_PROP = "server.iccrPortNumber.";
    private static final String SERVERKEYS_ICCR_API_KEY_PREFIX_PROP = "server.iccrApiKey.";
    //private static final String SERVERKEYS_WALLET_CMD_PREFIX_PROP = "server.walletCommand.";

    public static final String SERVER_ID_PROP = "id";
    public static final String SERVER_IP_PROP = "ip";
    public static final String SERVER_NAME_PROP = "name";
    public static final String SERVER_ICCR_PORT_NUM_PROP = "iccrPortNumber";
    public static final String SERVER_IOTA_DIR_PROP = "iotaDir";
    public static final String SERVER_IOTA_PORT_NUM_PROP = "iotaPortNumber";
    public static final String SERVER_IOTA_NBR_REFRESH_TIME_PROP = "iotaNeighborRefreshTime";
    public static final String SERVER_IOTA_START_CMD_PROP = "iotaStartCmd";
    public static final String SERVER_ICCR_API_KEY_PROP = "iccrApiKey";
    public static final String SERVER_ICCR_API_KEY_HEADER_NAME_PROP = "iccrApiKeyHeaderName";
    public static final String SERVER_ICCR_API_KEY_HEADER_NAME_VAL = "ICCR-API-KEY";
    //public static final String SERVER_WALLET_CMD_PROP = "walletCommand";

    private Properties _props;
    private String osName;
    private PropertiesConfiguration propWriter;
    private DateTimeFormatter ymdhmsFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private Map<String, Properties> servers;

    private PropertySource() {
        System.out.println("new PropertySource");

        osName = System.getProperty("os.name").toLowerCase();

        try {
            // Direct file path load/store:
            //propWriter = new PropertiesConfiguration(confFile);

            // From classpath:
            propWriter = new PropertiesConfiguration(CONF_FILE);
        }
        catch(Exception e) {
            System.out.println("PropertySource exception creating PropertiesConfiguration: " + e.getLocalizedMessage());
        }

        _props = new Properties();
        load();
    }

    public void load() {
        try {
            // Direct file path load
            //InputStream is = new FileInputStream(confFile);

            // From classpath:
            InputStream is = this.getClass().getResourceAsStream("/" + CONF_FILE);
            _props.load(is);
        }
        catch(Exception e) {
            System.out.println("failed to load icc.properties");
            e.printStackTrace();
        }
    }

    public String getIccrDefaultApiKey() {
        return getString(ICCR_API_KEY_DEFAULT_PROP);
    }

    public String getIccrDefaultPortNumber() {
        return getString(ICCR_PORT_DEFAULT_PROP);
    }

    /*
    public String getIotaDownloadFilename() {
        return getString(IOTA_DLD_FILENAME_PROP);
    }
    */

    public String getIotaDownloadLink() {
        return getString(IOTA_DLD_LINK_PROP);
    }

    public void setIotaDownloadLink(String key, String value){
        setString(key, value);
    }

    /*
    public String getWalletStartCmd() {
        return getString(WALLET_START_PROP);
    }
    */

    public boolean getRunIotaRefresh() {
        try {
            return getBoolean(RUN_IOTA_REFRESH_PROP);
        }
        catch(Exception e) {
            return Boolean.TRUE;
        }
    }

    public String getRefreshNbrsTime() {
        return getString(REFRESH_NBRS_PROP);
    }

    public String getRefreshNodeInfoTime() {
        return getString(REFRESH_NODEINFO_PROP);
    }

    public String getServerIdsString() {
        return getString(SERVERKEYS_PROP);
    }

    public List<String> getServerIds() {
        return getList(SERVERKEYS_PROP);
    }

    public int getNumServers() {
        return getServerIds().size();
    }

    public Map<String, Properties> getServers() {
        if(servers == null) {
            System.out.println("servers is null, new map");
            servers = new HashMap<>();
        }
        for(String id : getServerIds()) {
            try {
                Properties serverProps = new Properties();
                serverProps.put(SERVER_ID_PROP, id);
                serverProps.put(SERVER_NAME_PROP, getString(PropertySource.SERVERKEYS_NAME_PREFIX_PROP + id));
                serverProps.put(SERVER_IP_PROP, getString(PropertySource.SERVERKEYS_IP_PREFIX_PROP + id));
                serverProps.put(SERVER_ICCR_PORT_NUM_PROP, getString(PropertySource.SERVERKEYS_ICCR_PORT_NUM_PREFIX_PROP + id));
                serverProps.put(SERVER_ICCR_API_KEY_PROP, getString(PropertySource.SERVERKEYS_ICCR_API_KEY_PREFIX_PROP + id));
                serverProps.put(SERVER_ICCR_API_KEY_HEADER_NAME_PROP, SERVER_ICCR_API_KEY_HEADER_NAME_VAL);
                //serverProps.put(SERVER_WALLET_CMD_PROP, getString(PropertySource.SERVERKEYS_WALLET_CMD_PREFIX_PROP + id));

                servers.put(id, serverProps);

            }
            catch(Exception e) {
                System.out.println("getServers exception: " + e.getLocalizedMessage());
            }
        }
        return servers;
    }

    public boolean isServerNameChange(Properties newProps, Properties prevProps) {
        return newProps != null && prevProps != null &&
                newProps.getProperty(SERVER_NAME_PROP) != null &&
                prevProps.getProperty(SERVER_NAME_PROP) != null &&
                !newProps.getProperty(SERVER_NAME_PROP).equals(prevProps.getProperty(SERVER_NAME_PROP));

    }

    public boolean isServerIpTaken(String ip) {
        for(String id : getServerIds()) {
            if(ip.equalsIgnoreCase(getServerIp(id))) {
                return true;
            }
        }
        return false;
    }

    public boolean isServerNameTaken(String name) {
        for(String id : getServerIds()) {
            if(name.equalsIgnoreCase(getServerName(id))) {
                return true;
            }
        }
        return false;
    }

    public String getServerName(String id) {
        if(servers == null) {
            getServers();
        }
        return getServerProperties(id).getProperty(SERVER_NAME_PROP);
    }

    public String getServerIp(String id) {
        if(servers == null) {
            getServers();
        }
        return getServerProperties(id).getProperty(SERVER_IP_PROP);
    }

    public Properties getServerProperties(String id) {
        if(servers == null) {
            getServers();
        }
        return servers.get(id);
    }

    public Properties getLocalServerProperties() {
        if(servers == null) {
            getServers();
        }
        for(String id : getServerIds()) {
            Properties props = getServerProperties(id);
            if(props != null) {
                if (UiUtil.isLocalhostIp(props.getProperty(SERVER_IP_PROP))) {
                    return props;
                }
            }
        }
        return null;
    }

    public void addServerProperties(Properties newProps) {
        String serverIds = getServerIdsString();

        String id = newProps.getProperty(SERVER_ID_PROP);
        if(serverIds == null || serverIds.isEmpty()) {
            serverIds = id;
        }
        else {
            serverIds += "," + id;
        }
        setProperty(SERVERKEYS_PROP, serverIds);
        setProperty(SERVERKEYS_NAME_PREFIX_PROP + id, newProps.getProperty(SERVER_NAME_PROP));
        setProperty(SERVERKEYS_IP_PREFIX_PROP + id, newProps.getProperty(SERVER_IP_PROP));
        setProperty(SERVERKEYS_ICCR_PORT_NUM_PREFIX_PROP + id, newProps.getProperty(SERVER_ICCR_PORT_NUM_PROP));
        setProperty(SERVERKEYS_ICCR_API_KEY_PREFIX_PROP + id, newProps.getProperty(SERVER_ICCR_API_KEY_PROP));

        //setProperty(SERVERKEYS_WALLET_CMD_PREFIX_PROP + id, newProps.getProperty(SERVER_WALLET_CMD_PROP));

        storeProperties();

        // Force reload of server properties next time it is requested
        servers = null;
    }

    // Support for setting a server port number directly after it has been changed
    // on the server action
    public void setIccrServerPortNumber(String id, String portNum) {
        System.out.println("setIccrServerPortNumber, id: " + id + ", new portNum: " + portNum);
        setProperty(SERVERKEYS_ICCR_PORT_NUM_PREFIX_PROP + id, portNum);
        storeProperties();
    }

    public void setServerProperties(Properties newProps, Properties prevProps) {
        String id = prevProps.getProperty(SERVER_ID_PROP);

        setProperty(SERVERKEYS_NAME_PREFIX_PROP + id, newProps.getProperty(SERVER_NAME_PROP));
        setProperty(SERVERKEYS_IP_PREFIX_PROP + id, newProps.getProperty(SERVER_IP_PROP));
        setProperty(SERVERKEYS_ICCR_PORT_NUM_PREFIX_PROP + id, newProps.getProperty(SERVER_ICCR_PORT_NUM_PROP));
        setProperty(SERVERKEYS_ICCR_API_KEY_PREFIX_PROP + id, newProps.getProperty(SERVER_ICCR_API_KEY_PROP));

        //setProperty(SERVERKEYS_WALLET_CMD_PREFIX_PROP + id, newProps.getProperty(SERVER_WALLET_CMD_PROP));

        storeProperties();

        // Force reload of server properties next time it is requested
        servers = null;
    }

    public boolean removeServerConfigByName(String serverName) {
        if(servers == null) {
            getServers();
        }

        String serverIds = "";
        String sep = "";

        String foundId = null;
        for(String id : getServerIds()) {
            Properties props = getServerProperties(id);

            if(props != null && serverName.equals(props.getProperty(SERVER_NAME_PROP))) {
                foundId = id;
            }
            else {
                serverIds += sep + id;
                if(sep.isEmpty()) {
                    sep = ",";
                }
            }
        }

        if(foundId != null) {
            removeProperty(SERVERKEYS_NAME_PREFIX_PROP + foundId);
            removeProperty(SERVERKEYS_IP_PREFIX_PROP + foundId);
            removeProperty(SERVERKEYS_ICCR_PORT_NUM_PREFIX_PROP + foundId);
            removeProperty(SERVERKEYS_ICCR_API_KEY_PREFIX_PROP + foundId);

            //removeProperty(SERVERKEYS_WALLET_CMD_PREFIX_PROP + foundId);
        }

        setProperty(SERVERKEYS_PROP, serverIds);

        storeProperties();

        return foundId != null;
    }

    public Properties getServerPropertiesForServerName(String serverName) {
        if(servers == null) {
            getServers();
        }

        for(String id : getServerIds()) {

            Properties props = getServerProperties(id);

            if(props != null) {
                if (serverName.equals(props.getProperty(SERVER_NAME_PROP))) {
                    return props;
                }
            }
        }
        return null;
    }

    public String getNowDateTimestamp() {
        return ymdhmsFormatter.format(LocalDateTime.now());
    }

    public String getOsName() {
        return osName;
    }

    public boolean osIsWindows() {
        return (osName.indexOf("win") >= 0);
    }

    public boolean osIsMax() {
        return (osName.indexOf("mac") >= 0);
    }

    public void setProperty(String key, Object value) {
        _props.setProperty(key, (String)value);
        propWriter.setProperty(key, value);
    }

    public void removeProperty(String key) {
        _props.remove(key);
        propWriter.clearProperty(key);
    }

    public void storeProperties() {
        try {
            propWriter.save();
        }
        catch(Exception e) {
            System.out.println("PropertySource exception saving PropertiesConfiguration: " + e.getLocalizedMessage());
        }
    }

    public String getLocaleLanguage() {
        String val = getString(LOC_LANG_PROP);
        if(val == null || val.isEmpty()) {
            val = LOC_LANG_DEFAULT;
        }
        return val;
    }

    public String getLocaleCountry() {
        String val = getString(LOC_COUNTRY_PROP);
        if(val == null || val.isEmpty()) {
            val = LOC_COUNTRY_DEFAULT;
        }
        return val;
    }

    public String getString(String key) {
        return _props.getProperty(key);
    }

    public void setString(String key, String value){
        _props.setProperty(key,value);
    }

    public boolean getBoolean(String key) {
        String val = _props.getProperty(key);
        if(val != null) {
            if(PATTERN_TRUE.matcher(val).matches()) {
                return true;
            }
            else if(PATTERN_FALSE.matcher(val).matches()) {
                return false;
            }
            else {
                throw new IllegalArgumentException("Invalid boolean value provided for " + key);
            }
        }
        throw new IllegalArgumentException("No value provided for " + key);
    }

    public int getInteger(String key) {
        String val = _props.getProperty(key);
        if(val != null) {
            try {
                return Integer.parseInt(val);
            }
            catch(NumberFormatException nfe) {
                throw new IllegalArgumentException("Invalid integer value provided for " + key);
            }
        }
        throw new IllegalArgumentException("No value provided for " + key);
    }

    public List<String> getList(String key) {
        List<String> keys = new ArrayList<>();
        String val = getString(key);
        if(val != null && !val.isEmpty()) {
            if(val.contains(",")) {
                String[] arr = val.split(",");
                for(String id : arr) {
                    keys.add(id.trim());
                }
            }
            else {
                keys.add(val);
            }
        }
        return keys;
    }
}
