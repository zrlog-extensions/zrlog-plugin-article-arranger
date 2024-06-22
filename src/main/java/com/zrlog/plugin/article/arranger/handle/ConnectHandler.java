package com.zrlog.plugin.article.arranger.handle;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.api.IConnectHandler;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.type.RunType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConnectHandler implements IConnectHandler {

    @Override
    public void handler(IOSession ioSession, MsgPacket msgPacket) {

    }

}