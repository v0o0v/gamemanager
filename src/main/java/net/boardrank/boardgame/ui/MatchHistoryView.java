package net.boardrank.boardgame.ui;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.Route;
import net.boardrank.boardgame.domain.Match;
import net.boardrank.boardgame.service.MatchService;

@Route(value = "MatchHistoryView", layout = MainLayout.class)
public class MatchHistoryView extends VerticalLayout {

    MatchService matchService;

    private Grid<Match> grid = new Grid<>(Match.class);

    public MatchHistoryView(MatchService matchService) {
        this.matchService = matchService;

        addClassName("list-view");
        setSizeFull();
        configureGrid();

        Div content = new Div(grid);
        content.addClassName("content");
        content.setSizeFull();

        add(content);
        updateList();
    }

    private void configureGrid() {
        grid.addClassName("contact-grid");
        grid.setSizeFull();
        grid.removeAllColumns();

        grid.addColumn(match -> {
            return match.getId();
        }).setHeader("ID");

        grid.addColumn(match -> {
            return match.getBoardGame().getName();
        }).setHeader("보드게임");

        grid.addColumn(match -> {
            return match.getMatchTitle();
        }).setHeader("방이름");

        grid.addColumn(match -> {
            return match.getWinnerByString();
        }).setHeader("1등");

        grid.addColumn(match -> {
            return match.getPaticiant().getAccounts().size();
        }).setHeader("방인원");

        grid.addColumn(new LocalDateTimeRenderer<>(
                Match::getFinishedTime,
                "yyyy-MM-dd HH:mm"))
                .setHeader("종료시간");

        grid.addColumn(match -> {
            return match.getPlayingTime();
        }).setHeader("플레이시간(분)");

//        grid.setColumns("보드게임", "방이름", "1등", "방인원", "방상태", "시작시간", "완료시간");

        grid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
            col.setResizable(true);
            col.setTextAlign(ColumnTextAlign.CENTER);
            col.setSortable(true);

        });

    }

    private void updateList() {
        grid.setItems(this.matchService.getGamesOfCurrentSessionAccount());
    }
}