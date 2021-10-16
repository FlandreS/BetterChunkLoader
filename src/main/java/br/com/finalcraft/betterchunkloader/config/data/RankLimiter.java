package br.com.finalcraft.betterchunkloader.config.data;

public class RankLimiter {

    private final String rankName;
    private final int onlineOnly;
    private final int alwaysOn;
    private final String permission;

    public RankLimiter(String rankName, int onlineOnly, int alwaysOn, String permission) {
        this.rankName = rankName;
        this.onlineOnly = onlineOnly;
        this.alwaysOn = alwaysOn;
        this.permission = permission;
    }

    public String getRankName() {
        return rankName;
    }

    public int getOnlineOnly() {
        return onlineOnly;
    }

    public int getAlwaysOn() {
        return alwaysOn;
    }

    public String getPermission() {
        return permission;
    }
}
