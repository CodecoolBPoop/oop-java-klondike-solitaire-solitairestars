package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();
    private List<List<Pile>> validPiles = new ArrayList<>();

    private static double STOCK_GAP = 1;
    private static double DISCARD_GAP = 0;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    Button restartBtn = new Button("  Restart  ");
    Button winBtn = new Button("Instant win");






    public static void infoBox(String infoMessage, String titleBar)
    {
        /* By specifying a null headerMessage String, we cause the dialog to
           not have a header */
        infoBox(infoMessage, titleBar, null);
    }

    public static void infoBox(String infoMessage, String titleBar, String headerMessage)
    {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titleBar);
        alert.setHeaderText(headerMessage);
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();

        if (card == activePile.getTopCard()) {
            if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
                card.moveToPile(discardPile);
                card.flip();
                card.setMouseTransparent(false);
                System.out.println("Placed " + card + " to the waste.");
            }
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();

        if (card == activePile.getTopCard() || !card.isFaceDown()){

            if (activePile.getPileType() == Pile.PileType.STOCK)
                return;
            double offsetX = e.getSceneX() - dragStartX;
            double offsetY = e.getSceneY() - dragStartY;

            draggedCards.clear();
            draggedCards.add(card);

            card.getDropShadow().setRadius(20);
            card.getDropShadow().setOffsetX(10);
            card.getDropShadow().setOffsetY(10);

            card.toFront();
            card.setTranslateX(offsetX);
            card.setTranslateY(offsetY);
        }

    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        validPiles.add(tableauPiles);
        validPiles.add(foundationPiles);

        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();

            Pile pile = getValidIntersectingPile(card, validPiles);
            Pile previousPile = card.getContainingPile();
            int previousPilesCardNumber = previousPile.getCards().size();

            if (pile != null) {

            if (previousPilesCardNumber != 1 && previousPile.getPileType().equals(Pile.PileType.TABLEAU)){
                Card previousCard = previousPile.getBeforeTopCard();

                if (!previousCard.isFaceDown()){
                    handleValidMove(card, pile);
                }
                else{
                    handleValidMove(card, pile);
                    previousCard.flip();
                }


                } else {

                    handleValidMove(card, pile);
                    draggedCards.clear();
                }

            } else {
                draggedCards.forEach(MouseUtil::slideBack);
                draggedCards.clear();
            }
    };

    public boolean isGameWon() {
        if (foundationPiles.size() == 52){
                return true;
        }
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        getChildren().add(restartBtn);
        getChildren().add(winBtn);
        initPiles();
        dealCards();
        addRestartButtonEventHandlers();
        addWinButtonEventHandlers();

        winBtn.setLayoutY(750);
        if(isGameWon()){
            Game.infoBox("You won the game!", "Hurraaay!");
        }


    }

    private void restartGame() {

        for (Card card : stockPile.getCards()) {
            getChildren().remove(card);

        }

        for (Card card : discardPile.getCards()) {
            getChildren().remove(card);
        }

        for (int i = 0; i < tableauPiles.size(); i++) {
            for (Card card : tableauPiles.get(i).getCards()) {
                getChildren().remove(card);
            }
        }

        for (int i = 0; i < foundationPiles.size(); i++) {
            for (Card card : foundationPiles.get(i).getCards()) {
                getChildren().remove(card);
            }
        }

//        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
//        stockPile.setBlurredBackground();
//        stockPile.setLayoutX(95);
//        stockPile.setLayoutY(20);
//        getChildren().add(stockPile);
        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        dealCards();
    }

    private void winTheGame() {
        Game.infoBox("You won the game!", "Hurraaay!");
        restartGame();
    }


    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);

    }


    public void addRestartButtonEventHandlers() {
        restartBtn.setOnAction((event -> restartGame()));
    }

    public void addWinButtonEventHandlers() {
        winBtn.setOnAction((event -> winTheGame()));
    }



    public void refillStockFromDiscard() {
        List<Card> list = discardPile.getCards();
        Collections.reverse(list);
        if (stockPile.isEmpty()) {
            for (Card card : list) {
                card.flip();
                stockPile.addCard(card);
            }
            discardPile.clear();

        }
    }
    public boolean isMoveValid(Card card, Pile destPile) {


        if (destPile.getPileType().equals(Pile.PileType.TABLEAU)) {

            if (destPile.isEmpty() && card.getRank() == 13) {
                return true;
            }
            if (!destPile.isEmpty()) {
                if (card.isOppositeColor(card, destPile.getTopCard()) && card.getRank() == destPile.getTopCard().getRank() - 1) {
                    return true;
                }
                return false;
            }
        }

        if (destPile.getPileType().equals(Pile.PileType.FOUNDATION)) {

            if (destPile.isEmpty() && card.getRank() == 1) {
                return true;
            }
            if (!destPile.isEmpty()) {
                if (card.getSuit()== destPile.getTopCard().getSuit() && card.getRank() == destPile.getTopCard().getRank() + 1) {
                    return true;
                }
            }
            return false;

        }

        return false;
    }


    private Pile getValidIntersectingPile(Card card, List<List<Pile>> piles) {
        Pile result = null;
        for (List<Pile> list : piles) {
            for (Pile pile : list) {
                if (!pile.equals(card.getContainingPile()) &&
                        isOverPile(card, pile) &&
                        isMoveValid(card, pile))
                    result = pile;
            }
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", DISCARD_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(325);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }


    public void fillTableauPiles() {

        Card card;

        for (int i = 0; i < tableauPiles.size(); i++) {
            for (int j = 0; j < i+1; j++) {
                card = stockPile.getTopCard();
                card.moveToPile(tableauPiles.get(i));
                if(j == i ) {
                    card.flip();
                }
            }
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        stockPile.clear();
        discardPile.clear();
        for (int i = 0; i < tableauPiles.size() ; i++) {
            tableauPiles.get(i).clear();

        }
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
        fillTableauPiles();
    }


    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
