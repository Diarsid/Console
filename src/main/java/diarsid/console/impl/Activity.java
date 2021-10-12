package diarsid.console.impl;

import diarsid.console.api.io.ConsolePlatform;

public class Activity {

    public ConsolePlatform platform;
    public String input;

    public Activity(ConsolePlatform platform, String input) {
        this.platform = platform;
        this.input = input;
    }
}
