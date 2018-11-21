/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.network.server;

import com.vecsight.dragonite.proxy.config.ProxyServerConfig;
import com.vecsight.dragonite.proxy.misc.ProxyGlobalConstants;
import com.vecsight.dragonite.sdk.cryptor.PacketCryptor;
import com.vecsight.dragonite.sdk.socket.DragoniteServer;
import com.vecsight.dragonite.sdk.socket.DragoniteSocket;
import org.pmw.tinylog.Logger;

import java.net.InetSocketAddress;
import java.net.SocketException;

public class ProxyServer {

    private final InetSocketAddress bindAddress;

    private final int limitMbps;

    private final String welcomeMessage;

    private final boolean allowLoopback;

    private final PacketCryptor packetCryptor;

    private final DragoniteServer dragoniteServer;

    private volatile boolean doAccept = true;

    private final Thread acceptThread;

    public ProxyServer(final ProxyServerConfig config) throws SocketException {
        this.bindAddress = config.getBindAddress();
        this.limitMbps = config.getMbpsLimit();
        this.welcomeMessage = config.getWelcomeMessage();
        this.allowLoopback = config.isAllowLoopback();
        this.packetCryptor = config.getDragoniteSocketParameters().getPacketCryptor();

        this.dragoniteServer = new DragoniteServer(bindAddress.getAddress(), bindAddress.getPort(),
                ProxyGlobalConstants.INIT_SEND_SPEED, config.getDragoniteSocketParameters());

        acceptThread = new Thread(() -> {
            try {
                DragoniteSocket socket;
                while (doAccept && (socket = dragoniteServer.accept()) != null) {
                    Logger.debug("New client from {}", socket.getRemoteSocketAddress().toString());
                    handleClient(socket);
                }
            } catch (final InterruptedException e) {
                Logger.error(e, "Unable to accept Dragonite connections");
            }
        }, "PS-Accept");
        acceptThread.start();
    }

    private void handleClient(final DragoniteSocket socket) {
        final ProxyClientHandler clientHandler = new ProxyClientHandler(socket, limitMbps, welcomeMessage,
                allowLoopback, packetCryptor);
        final Thread handlerThread = new Thread(clientHandler::run, "PS-Handler");
        handlerThread.start();
    }

    public boolean isDoAccept() {
        return doAccept;
    }

    public void stopAccept() {
        acceptThread.interrupt();
        doAccept = false;
    }
}
