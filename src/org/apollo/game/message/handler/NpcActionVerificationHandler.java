package org.apollo.game.message.handler;

import org.apollo.fs.def.NpcDefinition;
import org.apollo.game.message.MessageHandler;
import org.apollo.game.message.impl.NpcActionMessage;
import org.apollo.game.model.World;
import org.apollo.game.model.entity.Npc;
import org.apollo.game.model.entity.Player;
import org.apollo.util.MobRepository;

/**
 * A verification {@link MessageHandler} for the {@link NpcActionMessage}.
 *
 * @author Stuart
 * @author Major
 */
public final class NpcActionVerificationHandler extends MessageHandler<NpcActionMessage> {

	/**
	 * Creates the NpcActionVerificationHandler.
	 *
	 * @param world The {@link World} the {@link NpcActionMessage} occurred in.
	 */
	public NpcActionVerificationHandler(World world) {
		super(world);
	}

	@Override
	public void handle(Player player, NpcActionMessage message) {
		int index = message.getIndex();
		MobRepository<Npc> repository = world.getNpcRepository();

		if (index < 0 || index >= repository.capacity()) {
			message.terminate();
			return;
		}

		Npc npc = repository.get(index);

		if (npc == null || !player.getPosition().isWithinDistance(npc.getPosition(), player.getViewingDistance() + 1)) {
			// +1 in case it was decremented after the player clicked the action.
			message.terminate();
			return;
		}

		NpcDefinition definition = npc.getDefinition();
		if (message.getOption() >= definition.getInteractions().length) {
			message.terminate();
			return;
		}
	}

}