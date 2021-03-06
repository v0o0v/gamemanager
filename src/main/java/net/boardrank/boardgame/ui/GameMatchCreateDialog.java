package net.boardrank.boardgame.ui;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;
import net.boardrank.boardgame.domain.Account;
import net.boardrank.boardgame.domain.Boardgame;
import net.boardrank.boardgame.domain.GameMatch;
import net.boardrank.boardgame.service.AccountService;
import net.boardrank.boardgame.service.BoardgameService;
import net.boardrank.boardgame.service.GameMatchService;
import net.boardrank.boardgame.ui.common.ResponsiveDialog;
import net.boardrank.boardgame.ui.event.DialogSuccessCloseActionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GameMatchCreateDialog extends ResponsiveDialog {

    private AccountService accountService;

    private BoardgameService boardGameService;

    private GameMatchService gameMatchService;

    private ComboBox<Account> me;

    private ComboBox<Boardgame> combo_boardGame;

    private List<ComboBox<Boardgame>> comboList_expasion;

    private List<ComboBox<Account>> comboList_party;

    private ComboBox<Account> combo_boardgameProvider;
    private ComboBox<Account> combo_ruleSupporter;

    public GameMatchCreateDialog(AccountService accountService
            , BoardgameService boardGameService
            , GameMatchService gameMatchService
            , ComponentEventListener<DialogSuccessCloseActionEvent> listener
    ) {
        this.accountService = accountService;
        this.boardGameService = boardGameService;
        this.gameMatchService = gameMatchService;
        super.getEventBus().addListener(DialogSuccessCloseActionEvent.class, listener);

        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        createHeader();
        createContent();
        createFooter();
    }

    private void createHeader() {
        add(new Label("새로운 Match 생성"));
    }


    private void createContent() {
        Accordion content = new Accordion();

        //보드게임
        VerticalLayout layout_boardGame = new VerticalLayout();
        layout_boardGame.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout_boardGame.setPadding(false);
        layout_boardGame.setSpacing(false);

        Button btn_addNewBoardgame = new Button("신규 보드게임 추가", event -> {
            NewBoardGameDialog newBoardGameDialog = new NewBoardGameDialog(
                    boardGameService
                    , accountService
                    , event1 -> {
                combo_boardGame.setItems(this.boardGameService.getAllBaseBoardgames());
            });
            newBoardGameDialog.open();
        });
        btn_addNewBoardgame.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout layout_newBoardgame = new HorizontalLayout(btn_addNewBoardgame);
        layout_newBoardgame.setPadding(false);
        layout_newBoardgame.setMargin(false);
        layout_newBoardgame.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layout_newBoardgame.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        combo_boardGame = new ComboBox<>();
        combo_boardGame.addValueChangeListener(event -> {
            comboList_expasion.forEach(boardgameComboBox -> {
                layout_boardGame.remove(boardgameComboBox);
            });
            comboList_expasion.clear();
        });
        combo_boardGame.setLabel("기본판");
        combo_boardGame.setPlaceholder("플레이할 보드게임을 선택해주세요");
        combo_boardGame.setItems(this.boardGameService.getAllBaseBoardgames());

        comboList_expasion = new ArrayList<>();
        Button btn_addExpansion = new Button("확장판 포함", event -> {
            if (combo_boardGame.isEmpty()) {
                Notification notification = new Notification("기본판을 먼저 선택해주세요.");
                notification.setDuration(1500);
                notification.open();
                return;
            }
            if (combo_boardGame.getValue().getExpansionSet().size() <= 0) {
                Notification notification = new Notification("해당 보드게임은 확장판이 없습니다.");
                notification.setDuration(1500);
                notification.open();
                return;
            }
            ComboBox<Boardgame> exp = new ComboBox<>();
            exp.setLabel("확장판");
            exp.setItems(combo_boardGame.getValue().getExpansionSet());
            comboList_expasion.add(exp);
            layout_boardGame.addAndExpand(exp);
        });

        layout_boardGame.addAndExpand(layout_newBoardgame, combo_boardGame, btn_addExpansion);
        content.add("보드게임 설정", layout_boardGame);

        //참가자
        VerticalLayout layout_pati = new VerticalLayout();
        layout_pati.setAlignItems(FlexComponent.Alignment.STRETCH);

        VerticalLayout layout_party_combo = new VerticalLayout();
        layout_party_combo.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout_pati.add(layout_party_combo);

        comboList_party = new ArrayList<>();
        me = new ComboBox<>();
        me.setItems(this.accountService.getCurrentAccount());
        me.setValue(this.accountService.getCurrentAccount());
        me.setEnabled(false);
        comboList_party.add(me);
        layout_party_combo.add(me);

        ComboBox<Account> party1 = new ComboBox<>();
        party1.setItems(this.accountService.getCurrentAccount().getFriends().stream()
                .map(friend -> friend.getFriend())
                .collect(Collectors.toList())
        );
        layout_party_combo.addAndExpand(party1);
        comboList_party.add(party1);
        content.add("참가자 설정", layout_pati);

        Button btn_addParty = new Button("참가자 추가");
        Button btn_removeParty = new Button("참가자 삭제");

        btn_addParty.addClickListener(event -> {
            ComboBox<Account> newParty = new ComboBox<>();
            newParty.setItems(this.accountService.getCurrentAccount().getFriends().stream()
                    .map(friend -> friend.getFriend())
                    .collect(Collectors.toList())
            );
            layout_party_combo.add(newParty);
            comboList_party.add(newParty);
            btn_removeParty.setEnabled(true);
        });

        btn_removeParty.setEnabled(false);
        btn_removeParty.addClickListener(event -> {
            ComboBox<Account> remove = comboList_party.remove(comboList_party.size() - 1);
            layout_party_combo.remove(remove);
            if (comboList_party.size() <= 2) btn_removeParty.setEnabled(false);
        });
        HorizontalLayout partyBtnLayout = new HorizontalLayout();
        partyBtnLayout.addAndExpand(btn_removeParty, btn_addParty);
        partyBtnLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout_pati.addAndExpand(partyBtnLayout);

        VerticalLayout layout_supportor = new VerticalLayout();
        layout_supportor.setAlignItems(FlexComponent.Alignment.STRETCH);
        content.add("서포터 설정(Option)", layout_supportor);

        combo_boardgameProvider = new ComboBox<>();
        combo_boardgameProvider.setLabel("보드게임 제공");
        combo_boardgameProvider.setItems(this.accountService.getCurrentAccount().getMeAndFriends());
        layout_supportor.add(combo_boardgameProvider);
        combo_ruleSupporter = new ComboBox<>();
        combo_ruleSupporter.setLabel("룰 설명");
        combo_ruleSupporter.setItems(this.accountService.getCurrentAccount().getMeAndFriends());
        layout_supportor.add(combo_ruleSupporter);

        add(content);
    }

    private void createFooter() {
        Button abort = new Button("취소");
        abort.addClickListener(buttonClickEvent -> close());
        Button confirm = new Button("Match 생성");
        confirm.addClickListener(buttonClickEvent -> {
            try {
                checkValidation();
                makeGameMatch();
                getEventBus().fireEvent(new DialogSuccessCloseActionEvent(this));
                close();
            } catch (Exception e) {
                Notification notification = new Notification(
                        e.getMessage(), 2000,
                        Notification.Position.MIDDLE);
                notification.open();
            }
        });

        HorizontalLayout footer = new HorizontalLayout();
        footer.add(abort, confirm);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        add(footer);
    }

    private void makeGameMatch() {
        GameMatch match = this.gameMatchService.makeNewMatch(
                this.combo_boardGame.getValue()
                , this.comboList_party.stream()
                        .map(accountComboBox -> accountComboBox.getValue())
                        .collect(Collectors.toList())
                , this.me.getValue()
        );
        log.info("새로운 match가 생성되었습니다 : " + match);

        if (combo_boardgameProvider.getValue() != null && !combo_boardgameProvider.isEmpty())
            this.gameMatchService.setBoardgameProvider(match, combo_boardgameProvider.getValue());

        if (combo_ruleSupporter.getValue() != null && !combo_ruleSupporter.isEmpty())
            this.gameMatchService.setRuleSupporter(match, combo_ruleSupporter.getValue());

        this.gameMatchService.addExpansion(match, comboList_expasion.stream()
                .filter(boardgameComboBox -> !boardgameComboBox.isEmpty())
                .map(ComboBox::getValue)
                .collect(Collectors.toList())
        );
    }

    private void checkValidation() {

        //보드게임 체크
        if (this.combo_boardGame.isEmpty())
            throw new RuntimeException("보드게임을 확인해주세요.");

        //참석자 체크
        this.comboList_party.forEach(comboBox -> {
            if (comboBox.isEmpty())
                throw new RuntimeException("참석자를 확인해주세요.");
        });

        for (ComboBox me : this.comboList_party) {
            for (ComboBox other : this.comboList_party) {
                if (me.equals(other)) continue;

                if (me.getValue().equals(other.getValue()))
                    throw new RuntimeException("중복된 참석자가 있습니다.");
            }
        }
    }

}
