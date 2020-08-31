package net.sf.rails.game.specific._SOH;

import net.sf.rails.common.GameOption;
import net.sf.rails.game.*;
import net.sf.rails.game.financial.StockRound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameManager_SOH extends GameManager {

    /** The number of operating rounds is fixed. */
    private final int MAX_ORS = 10;

    //private static final Logger log = LoggerFactory.getLogger(GameManager_SOH.class);

    public GameManager_SOH (RailsRoot parent, String id) {
        super(parent, id);
    }

    /*
    protected void beginStartRound() {

        if (StartRound_SOH.Procedure.valueOf(GameOption.getValue(
                this, "StartRound").toUpperCase()) == StartRound_SOH.Procedure.DEAL) {
            executeDeal();
            startStockRound();
        } else {
            super.beginStartRound();
        }
    }

     */

    @Override
    public void nextRound(Round round) {
        if (round instanceof StartRound) {
            startStockRound();

        } else if (round instanceof StockRound) {
            numOfORs.set(getCurrentPhase().getNumberOfOperatingRounds());
            relativeORNumber.set(1);
            startOperatingRound(true);

        } else if (round instanceof OperatingRound) {
            if (absoluteORNumber.value() == MAX_ORS) {
                finishGame();
            } else if (relativeORNumber.add(1) <= numOfORs.value()) {
                // There will be another OR
                startOperatingRound(true);
            } else {
                startStockRound();
            }
        }
    }
}
