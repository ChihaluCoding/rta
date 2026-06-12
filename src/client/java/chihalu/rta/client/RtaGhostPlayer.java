package chihalu.rta.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.PlayerSkin;

public class RtaGhostPlayer extends RemotePlayer {
	private final PlayerSkin skin;

	public RtaGhostPlayer(ClientLevel level, GameProfile profile, PlayerSkin skin) {
		super(level, profile);
		this.skin = skin;
	}

	@Override
	public PlayerSkin getSkin() {
		return skin;
	}
}
