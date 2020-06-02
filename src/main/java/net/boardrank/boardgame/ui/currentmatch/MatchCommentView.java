package net.boardrank.boardgame.ui.currentmatch;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import net.boardrank.boardgame.domain.Comment;
import net.boardrank.boardgame.domain.GameMatch;
import net.boardrank.boardgame.service.GameMatchService;
import net.boardrank.boardgame.service.TimeUtilService;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class MatchCommentView extends VerticalLayout {

    GameMatchService gameMatchService;

    Long gameMatchId;

    Grid<Comment> commentGrid;

    public MatchCommentView(GameMatchService gameMatchService, Long gameMatchId) {
        this.gameMatchService = gameMatchService;
        this.gameMatchId = gameMatchId;

        setSizeFull();
        setSpacing(false);
        setMargin(false);
        setPadding(false);

        initComponent();
        resetComment();
    }

    private void initComponent() {
        add(initCommentGrid());
        add(initCommentInputLayout());
    }

    private Component initCommentInputLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        TextField textField = new TextField();
        textField.setMinLength(1);
        textField.setMaxLength(500);
        layout.addAndExpand(textField);
        Button button = new Button("입력");
        button.addClickListener(event -> {
            if(textField.isInvalid() || textField.isEmpty()) return;
            gameMatchService.addComment(gameMatchId
                    , gameMatchService.getAccountService().getCurrentAccount()
                    , textField.getValue());
            UI.getCurrent().getPage().reload();
        });
        layout.add(button);

        return layout;
    }

    private Component initCommentGrid() {
        commentGrid = new Grid<>(Comment.class);
        commentGrid.removeAllColumns();
        commentGrid.setWidthFull();

        commentGrid.addColumn(comment -> {
            return comment.getAccountName();
        }).setHeader("이름");

        commentGrid.addColumn(new LocalDateTimeRenderer<Comment>(
                comment -> {
                    return TimeUtilService.transUTCToKTC(comment.getCreatedAt());
                }
                , DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)))
                .setHeader("시간");

        commentGrid.setItemDetailsRenderer(TemplateRenderer.<Comment>of(
                "<div style='border: 1px solid gray; padding: 10px; width: 100%; box-sizing: border-box;'>"
                        + "<div>[[item.content]]</b></div>"
                        + "</div>")
                .withProperty("content", Comment::getContent)
                .withEventHandler("handleClick", comment -> {
                    commentGrid.getDataProvider().refreshItem(comment);
                }));

        return commentGrid;
    }

    private void resetComment(){
        List<Comment> comments = this.gameMatchService.getCommentsByMatchId(this.gameMatchId);
        this.commentGrid.setItems(comments);
    }
}