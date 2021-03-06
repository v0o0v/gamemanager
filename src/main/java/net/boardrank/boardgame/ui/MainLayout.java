package net.boardrank.boardgame.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import net.boardrank.boardgame.service.AccountService;
import net.boardrank.boardgame.service.BoardgameService;
import net.boardrank.boardgame.service.GameMatchService;
import net.boardrank.boardgame.ui.boardgames.BoardgameTab;
import net.boardrank.boardgame.ui.common.UserIcon;
import net.boardrank.boardgame.ui.currentmatch.CurrentMatchListTab;
import net.boardrank.boardgame.ui.matchhistory.GameMatchHistoryTab;
import net.boardrank.boardgame.ui.myfriend.FriendTab;
import net.boardrank.boardgame.ui.myrank.MyRankTab;
import net.boardrank.boardgame.ui.todo.ToDoTab;

@CssImport("./styles/shared-styles.css")
@PageTitle("BoardRank")
public class MainLayout extends AppLayout implements PageConfigurator {

    private AccountService accountService;

    private BoardgameService boardGameService;

    private GameMatchService gameMatchService;
    private Button btn_currentMatch;
    private Tabs tabs;

    public MainLayout(AccountService accountService
            , BoardgameService boardGameService
            , GameMatchService gameMatchService
    ) {
        this.accountService = accountService;
        this.boardGameService = boardGameService;
        this.gameMatchService = gameMatchService;

        createHeader();
        createDrawer();
    }

    private void createHeader() {

        Anchor logout = new Anchor("/logout", new Icon(VaadinIcon.EXIT_O));

        Anchor user = new Anchor("/userModifyView"
                , accountService.getCurrentAccount().getPictureURL() == null
                ? new Icon(VaadinIcon.USER)
                : new UserIcon(accountService.getCurrentAccount())
        );

        Button createMatch = new Button("새게임");
        createMatch.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        createMatch.addClickListener(event -> {
            GameMatchCreateDialog gameMatchCreateDialog =
                    new GameMatchCreateDialog(
                            accountService
                            , boardGameService
                            , gameMatchService
                            , event1 -> setReloadAndNavigateToCurrentMatch()
                    );
            gameMatchCreateDialog.open();
        });
        Label lbl_empty = new Label();


        DrawerToggle drawerToggle = new DrawerToggle();
        Image boardRank = new Image("logo/title.png", "BoardRank");
        boardRank.setMaxWidth("150px");
        drawerToggle.setIcon(boardRank);
        drawerToggle.setMaxWidth("150px");
        HorizontalLayout header = new HorizontalLayout(
                new H5("")
                , new H5("")
                , new H5("")
                , drawerToggle
                , lbl_empty
                , createMatch
                , user
                , logout
                , new H5("")
        );
        header.setWidth("100%");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(lbl_empty);

        addToNavbar(header);
    }

    private void createDrawer() {
        tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        Button btn_myRank = new Button("My Rank", new Icon(VaadinIcon.CHART_3D));
        btn_myRank.addClickListener(e -> UI.getCurrent().navigate(MyRankTab.class));
        btn_myRank.setWidthFull();
        tabs.add(new Tab(btn_myRank));

        btn_currentMatch = new Button("My Current Match", new Icon(VaadinIcon.PLAY));
        btn_currentMatch.addClickListener(e -> UI.getCurrent().navigate(CurrentMatchListTab.class));
        btn_currentMatch.setWidthFull();
        tabs.add(new Tab(btn_currentMatch));

        Button btn_matchHistory = new Button("My Match History", new Icon(VaadinIcon.LINE_CHART));
        btn_matchHistory.addClickListener(e -> UI.getCurrent().navigate(GameMatchHistoryTab.class));
        btn_matchHistory.setWidthFull();
        tabs.add(new Tab(btn_matchHistory));

        Button btn_ToDoList = new Button("My To Do", new Icon(VaadinIcon.PACKAGE));
        btn_ToDoList.addClickListener(e -> UI.getCurrent().navigate(ToDoTab.class));
        btn_ToDoList.setWidthFull();
        tabs.add(new Tab(btn_ToDoList));

        Button btn_friend = new Button("My Friends", new Icon(VaadinIcon.GROUP));
        btn_friend.addClickListener(e -> UI.getCurrent().navigate(FriendTab.class));
        btn_friend.setWidthFull();
        tabs.add(new Tab(btn_friend));

        Button btn_boardgame = new Button("Boardgames", new Icon(VaadinIcon.VIEWPORT));
        btn_boardgame.addClickListener(e -> UI.getCurrent().navigate(BoardgameTab.class));
        btn_boardgame.setWidthFull();
        tabs.add(new Tab(btn_boardgame));

        tabs.setSelectedIndex(0);
        addToDrawer(tabs);
    }

    public void setReloadAndNavigateToCurrentMatch() {
        btn_currentMatch.click();
        UI.getCurrent().getPage().reload();
        this.tabs.setSelectedIndex(1);
    }

    @Override
    public void configurePage(InitialPageSettings settings) {
        settings.addLink("shortcut icon", "icons/favicon.png");
        settings.addFavIcon("icon", "icons/favicon.png", "32x32");
    }
}
