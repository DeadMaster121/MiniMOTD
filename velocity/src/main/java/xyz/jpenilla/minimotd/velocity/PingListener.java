/*
 * This file is part of MiniMOTD, licensed under the MIT License.
 *
 * Copyright (c) 2021 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.minimotd.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.minimotd.common.Constants;
import xyz.jpenilla.minimotd.common.MOTDIconPair;
import xyz.jpenilla.minimotd.common.MiniMOTD;
import xyz.jpenilla.minimotd.common.config.MiniMOTDConfig;

public final class PingListener {
  private final MiniMOTD<Favicon> miniMOTD;

  PingListener(final @NonNull MiniMOTD<Favicon> miniMOTD) {
    this.miniMOTD = miniMOTD;
  }

  @Subscribe
  public void handlePing(final @NonNull ProxyPingEvent event) {
    final MiniMOTDConfig config = this.miniMOTD.configManager().resolveConfig(event.getConnection().getVirtualHost().orElse(null));

    final ServerPing.Builder pong = event.getPing().asBuilder();

    final MiniMOTDConfig.PlayerCount count = config.modifyPlayerCount(pong.getOnlinePlayers(), pong.getMaximumPlayers());
    final int onlinePlayers = count.onlinePlayers();
    final int maxPlayers = count.maxPlayers();
    pong.onlinePlayers(onlinePlayers);
    pong.maximumPlayers(maxPlayers);

    final boolean legacy = config.legacyEnabled() && pong.getVersion().getProtocol() < Constants.MINECRAFT_1_16_PROTOCOL_VERSION;
    final MOTDIconPair<Favicon> pair = legacy ? miniMOTD.createLegacyMOTD(config, onlinePlayers, maxPlayers) : this.miniMOTD.createMOTD(config, onlinePlayers, maxPlayers);

    pair.icon(pong::favicon);
    pair.motd(pong::description);

    if (config.disablePlayerListHover()) {
      pong.clearSamplePlayers();
    }
    if (config.hidePlayerCount()) {
      pong.nullPlayers();
    }

    event.setPing(pong.build());
  }
}
