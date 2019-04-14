package pogo.assistance.bot.responder.relay.pokedex100;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import com.google.common.collect.Sets;

class CandySelectorTest {

    @Test
    void internalStaticVariables_HasCorrectState() {
        assertThat(Sets.intersection(CandySelector.CANDY_POKEMON_IDS, CandySelector.NON_CANDY_POKEMON_IDS), Matchers.empty());
        assertThat(CandySelector.CANDY_POKEMON_IDS.size() + CandySelector.NON_CANDY_POKEMON_IDS.size(), Matchers.equalTo(493));
    }

}