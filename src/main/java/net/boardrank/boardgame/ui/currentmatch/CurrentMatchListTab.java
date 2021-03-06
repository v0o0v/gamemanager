package net.boardrank.boardgame.ui.currentmatch;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import net.boardrank.boardgame.domain.Account;
import net.boardrank.boardgame.domain.GameMatch;
import net.boardrank.boardgame.service.AccountService;
import net.boardrank.boardgame.service.BoardgameService;
import net.boardrank.boardgame.service.GameMatchService;
import net.boardrank.boardgame.ui.MainLayout;

import java.util.List;

@Route(layout = MainLayout.class)
public class CurrentMatchListTab extends VerticalLayout {

    GameMatchService gameMatchService;
    AccountService accountService;
    BoardgameService boardgameService;

    public CurrentMatchListTab(GameMatchService gameMatchService
            , AccountService accountService
            , BoardgameService boardgameService
    ) {
        this.gameMatchService = gameMatchService;
        this.accountService = accountService;
        this.boardgameService = boardgameService;

        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        initComponent();
        setSizeFull();
    }

    public void initComponent() {
        Account account = accountService.getCurrentAccount();
        List<GameMatch> inprogressMatches = gameMatchService.getInprogressMatches(account);
        inprogressMatches.sort((o1, o2) -> o2.getCreatedTime().compareTo(o1.getCreatedTime()));

        if (inprogressMatches != null) {
            inprogressMatches.stream()
                    .forEach(gameMatch -> {
                        MatchView matchView = new MatchView(gameMatchService, gameMatch, boardgameService, accountService);
                        addAndExpand(matchView);
                    });
        }
    }

}