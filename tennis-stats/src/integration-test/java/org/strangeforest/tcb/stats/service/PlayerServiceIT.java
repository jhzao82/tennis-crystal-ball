package org.strangeforest.tcb.stats.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit.jupiter.*;
import org.strangeforest.tcb.stats.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.core.*;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ServiceTest
class PlayerServiceIT {

	@Autowired private PlayerService playerService;

	@Test
	void playerExists() {
		for (String player : PlayersFixture.PLAYERS)
			playerExists(player);
	}

	private void playerExists(String playerName) {
		Player player = playerService.getPlayer(playerName);

		assertThat(player).withFailMessage("Player %1$s does not exist", playerName).isNotNull();
	}
}
