package com.p532.tracker.command;


public interface TrackerCommand {
    Object execute();
    String getCommandType();
    String getPayloadJson();
}
