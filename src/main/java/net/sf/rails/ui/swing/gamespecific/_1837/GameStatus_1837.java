/**
 *
 */
package net.sf.rails.ui.swing.gamespecific._1837;

import java.awt.event.ActionEvent;
import java.util.List;

import net.sf.rails.ui.swing.StatusWindow;
import net.sf.rails.ui.swing.elements.ConfirmationDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rails.game.action.MergeCompanies;
import rails.game.action.PossibleAction;
import net.sf.rails.common.LocalText;
import net.sf.rails.game.PublicCompany;
import net.sf.rails.ui.swing.GameStatus;
import net.sf.rails.ui.swing.elements.RadioButtonDialog;
import net.sf.rails.ui.swing.gamespecific._1837.GameUIManager_1837;

import javax.swing.*;

/**
 * @author martin based on work by Erik Voss for 18EU
 *
 */
public class GameStatus_1837 extends GameStatus {
    private static final Logger log = LoggerFactory.getLogger(GameStatus_1837.class);

    /**
     *
     */
    public GameStatus_1837() {
    }
    private static final long serialVersionUID = 1L;

    @Override
    protected void initGameSpecificActions() {

        PublicCompany mergingCompany;
        int index;

        List<MergeCompanies> mergers =
            possibleActions.getType(MergeCompanies.class);
        if (mergers != null) {
            for (MergeCompanies merger : mergers) {
                mergingCompany = merger.getMergingCompany();
                if (mergingCompany != null) {
                    index = mergingCompany.getPublicNumber();
                    setPlayerCertButton(index, merger.getPlayerIndex(), true,
                            merger);
                }
            }
        }

    }

    /** Start a company - specific procedure for 1837 copied from 18EU */
    @Override
    protected PossibleAction processGameSpecificActions(ActionEvent actor,
            PossibleAction chosenAction) {

        if (chosenAction instanceof MergeCompanies) {

            log.debug("Merge action: {}", chosenAction);

            MergeCompanies action = (MergeCompanies) chosenAction;
            PublicCompany minor = action.getMergingCompany();
            List<PublicCompany> targets = action.getTargetCompanies();

            if (minor == null || targets == null || targets.isEmpty()
                    || targets.size() > 1 || targets.get(0) == null) {
                log.error("Bad {}", action);
                return null;
            }

            /* As the complete rule sets say that merging coal companies
             * proceeds per target company, there will be only one
             * target company. Let's take advantage of that, and only
             * ask the player for confirmation.
             */
            /*
            String[] options = new String[targets.size()];
            int i = 0;
            for (PublicCompany target : targets) {
                if (target != null) {
                    options[i++] = target.getId() + " " + target.getLongName();
                } else {
                    options[i++] = LocalText.getText("CloseCoal", minor.getId());
                }
            }

            RadioButtonDialog dialog = new RadioButtonDialog (
                    GameUIManager_1837.SELECT_MERGING_MAJOR,
                    gameUIManager,
                    parent,
                    LocalText.getText("PleaseSelect"),
                    LocalText.getText("SelectCompanyToMergeMinorInto", minor.getId()),
                    options, -1);
            gameUIManager.setCurrentDialog(dialog, action);
            parent.disableButtons();
             */

            // ***** Should this be done here?

            /*
            PublicCompany target = targets.get(0);
            ConfirmationDialog currentDialog = new ConfirmationDialog(GameUIManager_1837.SELECT_MERGING_MINOR,
                    gameUIManager, parent,
                    LocalText.getText("Confirm"),
                    LocalText.getText("MergeCoalCompConfirm",
                            action.getPlayer().getId(),
                            minor, target),
                    "Yes",
                    "No"
            );
            gameUIManager.setCurrentDialog (currentDialog, action);
            parent.disableButtons();
            */
            PublicCompany major = targets.get(0);
            action.setSelectedTargetCompany(major);

            if (action.canReplaceToken(0)) {
                boolean replaceToken =
                        JOptionPane.showConfirmDialog(parent, LocalText.getText(
                                "WantToReplaceToken",
                                minor.getId(),
                                major.getId()),
                                LocalText.getText("PleaseSelect"),
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                action.setReplaceToken(replaceToken);
            }
            return action;

        }
        return null;
    }



}
