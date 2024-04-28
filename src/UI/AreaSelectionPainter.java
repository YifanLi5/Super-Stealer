package UI;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.canvas.paint.Painter;
import org.osbot.rs07.input.mouse.BotMouseListener;
import org.osbot.rs07.script.Script;

import java.awt.*;
import java.awt.event.MouseEvent;

public class AreaSelectionPainter extends BotMouseListener implements Painter {

    private enum SelectionState {
        NONE_SELECTED, A_SELECTED, A_B_SELECTED, ERROR;

        public static SelectionState getSelectionState(Position positionA, Position positionB) {
            if(positionA == null && positionB == null) {
                return NONE_SELECTED;
            } else if (positionA != null && positionB == null) {
                return A_SELECTED;
            } else if (positionA != null && positionB != null) {
                return A_B_SELECTED;
            } else {
                return ERROR;
            }
        }
    }

    Position positionA;
    Position positionB;
    Position playersLastPosition;
    Area areaAroundPlayer;

    private final Script script;

    public AreaSelectionPainter(Script script) {
        this.script = script;

        script.getBot().addPainter(this);
        script.getBot().addMouseListener(this);
    }

    @Override
    public void onPaint(Graphics2D graphics2D) {
        if(playersLastPosition == null) {
            playersLastPosition = script.myPosition();
        }
        if(areaAroundPlayer == null || script.myPosition().equals(playersLastPosition)) {
            script.log("Player has moved. Get new Area around Player");
            areaAroundPlayer = script.myPosition().getArea(15);
        }

        switch(SelectionState.getSelectionState(positionA, positionB)) {
            case A_SELECTED:
                graphics2D.draw(positionA.getPolygon(script.bot));
                break;
            case A_B_SELECTED:
                Area mapArea = getOsbArea();
                if(mapArea != null)
                    graphics2D.draw(mapArea.getPolygon());
            case NONE_SELECTED:
                break;
            case ERROR:
                script.warn("Got Error enum!!!");
        }
    }


    @Override
    public void checkMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseEvent.BUTTON1 || mouseEvent.getID() != MouseEvent.MOUSE_PRESSED || playersLastPosition == null || areaAroundPlayer == null) {
            return;
        }
        if(positionA != null && positionB != null) {
            return;
        }

        Point clickPt = mouseEvent.getPoint();
        for(Position positionIter: areaAroundPlayer.getPositions()) {
            Polygon positionPoly = positionIter.getPolygon(script.getBot());
            if(positionPoly.contains(clickPt)) {
                if(positionA == null) {
                    positionA = positionIter;
                } else if (positionB == null) {
                    positionB = positionIter;
                }
            }
        }
    }

    // Find NE and SW, translate to new positions if needed for Area(sw, ne) constructor.
    private Area getOsbArea() {
        if(positionA.getX() > positionB.getX() && positionA.getY() > positionB.getY()) {
            return new Area(positionB, positionA);
        } else if(positionA.getX() < positionB.getX() && positionA.getY() < positionB.getY()) {
            return new Area(positionA, positionB);
        } else if(positionA.getX() < positionB.getX() && positionA.getY() > positionB.getY()) {
            int transX = positionB.getX() - positionA.getX();
            return new Area(positionB.translate(-transX, 0), positionA.translate(transX, 0));
        } else if(positionA.getX() > positionB.getX() && positionA.getY() < positionB.getY()) {
            int transX = positionA.getX() - positionB.getX();
            return new Area(positionA.translate(-transX, 0), positionB.translate(transX, 0));
        }
        else {
            script.warn("Bad getOsbArea logic!!!");
            return null;
        }
    }

    public void onStopCleanup() {
        script.getBot().removePainter(this);
        script.getBot().removeMouseListener(this);
    }


}
