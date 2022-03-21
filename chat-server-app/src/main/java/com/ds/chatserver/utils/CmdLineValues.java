package com.ds.chatserver.utils;

import org.kohsuke.args4j.Option;

public class CmdLineValues {
    @Option(required=true, name = "-i", aliases="--serverid", usage="Name of the server")
    private String serverId;

    @Option(required=true, name="-p", aliases="--servers_conf", usage="Path to the config file")
    private String configFilePath;

    public String getServerId() {
        return serverId;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }
}
