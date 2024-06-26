package UI;

import org.osbot.rs07.api.def.NPCDefinition;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.canvas.paint.Painter;
import org.osbot.rs07.input.mouse.BotMouseListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NPCSelectionPainter extends BotMouseListener implements Painter {
    private final Color ALPHA_GREEN = new Color(25, 240, 25, 156);
    private final Filter<NPC> paintableNPCsFilter;
    private final HashSet<NPCDefinition> selectedNPCDefinitions;
    private final Script script;
    private List<NPC> queriedNPCs;
    private Rectangle finishSelectionRect;
    private int frameCounter = 0;
    private boolean isSelectionComplete;


    public NPCSelectionPainter(Script script, Filter<NPC> paintableNPCsFilter) {
        this.script = script;
        this.paintableNPCsFilter = paintableNPCsFilter;
        this.selectedNPCDefinitions = new HashSet<>();
        this.isSelectionComplete = false;
        queryThievableNPCs();
        script.getBot().addPainter(this);
        script.getBot().addMouseListener(this);
    }

    @Override
    public void checkMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getID() != MouseEvent.MOUSE_PRESSED && mouseEvent.getButton() == MouseEvent.BUTTON1) {
            return;
        }

        Point clickPt = mouseEvent.getPoint();
        for (NPC npc : queriedNPCs) {
            Rectangle npcArea = getNPCBoundingBox(npc);
            // If too many players are thieving one NPC (ex: Ardy knights w/ splash host) then outline will be null.
            // Allow users to select npcs by clicking on the position.
            Shape positionOutline = getNPCPositionShape(npc);
            if (npcArea == null && positionOutline == null) {
                script.warn("Unable to get either npc position or npc outlines");
                continue;
            }
            if (npcArea != null && npcArea.contains(clickPt) || positionOutline != null && positionOutline.contains(clickPt)) {
                if (selectedNPCDefinitions.contains(npc.getDefinition())) {
                    script.log(String.format("Removed %s instance", npc.getName()));
                    selectedNPCDefinitions.remove(npc.getDefinition());
                } else {
                    script.log(String.format("Added %s instance", npc.getName()));
                    selectedNPCDefinitions.add(npc.getDefinition());
                }
            }
        }

        if (finishSelectionRect != null && finishSelectionRect.contains(clickPt)) {
            isSelectionComplete = true;
            mouseEvent.consume();
        }
    }

    @Override
    public void onPaint(Graphics2D graphics2D) {
        try {
            frameCounter += 1;
            if (frameCounter % 100 == 0) {
                queryThievableNPCs();
                script.log("Found " + queriedNPCs.size());
            }
            for (NPC npc : queriedNPCs) {
                if (npc == null) {
                    script.log("Found a null");
                    continue;
                }
                if (npcMatchesSelectedNPCs(npc)) {
                    graphics2D.setColor(Color.GREEN);
                } else {
                    graphics2D.setColor(Color.RED);
                }

                Area npcOutline = getNPCOutline(npc);
                if (npcOutline != null) {
                    graphics2D.draw(npcOutline);
                }
                Shape positionOutline = getNPCPositionShape(npc);
                if (positionOutline != null) {
                    graphics2D.draw(positionOutline);
                }
            }

            finishSelectionRect = drawCenteredStr(graphics2D, "Finish Selection");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            script.warn(sStackTrace);
        }


    }

    public List<NPCDefinition> awaitSelectedNPCDefinitions() throws InterruptedException {
        while (!isSelectionComplete) {
            MethodProvider.sleep(500);
        }
        script.getBot().removeMouseListener(this);
        script.getBot().removePainter(this);
        if (selectedNPCDefinitions.isEmpty()) {
            script.warn("Nothing was selected!");
        }

        StringBuilder builder = new StringBuilder("Selected NPC Definition(s)\n");
        for (NPCDefinition definition : selectedNPCDefinitions) {
            builder.append(String.format("Name: %s / Id: %s / Level: %s\n", definition.getName(), definition.getId(), definition.getLevel()));
        }
        script.log(builder.toString());

        return new ArrayList<>(selectedNPCDefinitions);
    }

    private boolean npcMatchesSelectedNPCs(NPC npc) {
        boolean isSelected = false;
        NPCDefinition definition = npc.getDefinition();
        for (NPCDefinition selectedDefinition : selectedNPCDefinitions) {
            if (selectedDefinition.getId() == npc.getId()) {
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }

    private void queryThievableNPCs() {
        queriedNPCs = script.npcs.filter(paintableNPCsFilter);

        if (queriedNPCs.isEmpty()) {
            script.log("Found no NPCs with supplied filter");
        }
    }

    private Rectangle drawCenteredStr(Graphics2D g2d, String str) {
        g2d.setColor(ALPHA_GREEN);

        FontMetrics metrics = g2d.getFontMetrics();

        int rectWidth = metrics.stringWidth(str) + 30;
        int rectHeight = metrics.getHeight() + 30;

        int x = 0;
        int y = 0;

        Rectangle rectangle = new Rectangle(x, y, rectWidth, rectHeight);

        g2d.fill(rectangle);

        int textX = x + 15;
        int textY = y + 15 + metrics.getAscent();

        g2d.setColor(Color.WHITE);
        g2d.drawString(str, textX, textY);

        return rectangle;
    }

    private Shape getNPCPositionShape(NPC npc) {
        Position position = npc.getPosition();
        if (position != null) {
            return position.getPolygon(script.getBot());
        }
        return null;
    }

    private Rectangle getNPCBoundingBox(NPC npc) {
        return npc.getModel().getBoundingBox(npc.getGridX(), npc.getGridY(), npc.getZ());
    }

    private Area getNPCOutline(NPC npc) {
        return npc.getModel().getArea(npc.getGridX(), npc.getGridY(), npc.getZ());
    }

    public void onStopCleanup() {
        script.getBot().removePainter(this);
        script.getBot().removeMouseListener(this);
    }
}
