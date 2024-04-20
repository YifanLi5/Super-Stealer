package Paint;

import Util.PickpocketUtil;
import org.osbot.rs07.api.def.NPCDefinition;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.canvas.paint.Painter;
import org.osbot.rs07.input.mouse.BotMouseListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class NPCSelectionPainter extends BotMouseListener implements Painter {
    private final Color ALPHA_GREEN = new Color(25, 240, 25, 156);
    private final Filter<NPC> paintableNPCsFilter;
    private List<NPC> queriedNPCs;
    private final HashSet<NPCDefinition> selectedNPCDefinitions;
    private final Script script;
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
        if (mouseEvent.getID() != MouseEvent.MOUSE_PRESSED) {
            return;
        }

        Point clickPt = mouseEvent.getPoint();
        for (NPC npc : queriedNPCs) {
            Rectangle npcArea = getNPCBoundingBox(npc);
            if (!npcArea.contains(clickPt))
                continue;

            if (selectedNPCDefinitions.contains(npc.getDefinition())) {
                script.log(String.format("Removed %s instance", npc.getName()));
                selectedNPCDefinitions.remove(npc.getDefinition());
            }
            else {
                script.log(String.format("Added %s instance", npc.getName()));
                selectedNPCDefinitions.add(npc.getDefinition());
            }
        }

        if (finishSelectionRect != null && finishSelectionRect.contains(clickPt)) {
            isSelectionComplete = true;
            mouseEvent.consume();
        }
    }

    @Override
    public void onPaint(Graphics2D graphics2D) {
        frameCounter += 1;
        if (frameCounter % 100 == 0) {
            queryThievableNPCs();
        }
        for(NPC npc: queriedNPCs) {
            if(npcMatchesSelectedNPCs(npc)) {
                graphics2D.setColor(Color.GREEN);
            } else {
                graphics2D.setColor(Color.RED);
            }
            graphics2D.draw(getNPCOutline(npc));
        }

        finishSelectionRect = drawCenteredStr(graphics2D, "Finish Selection");
    }

    public List<NPCDefinition> awaitSelectedNPCDefinitions() throws InterruptedException {
        while(!isSelectionComplete) {
            MethodProvider.sleep(500);
        }
        script.getBot().removeMouseListener(this);
        script.getBot().removePainter(this);
        if(selectedNPCDefinitions.isEmpty()) {
            script.warn("Nothing was selected!");
        }

        StringBuilder builder = new StringBuilder("Selected NPC Definition(s)\n");
        for(NPCDefinition definition: selectedNPCDefinitions) {
            builder.append(String.format("Name: %s / Id: %s / Level: %s\n", definition.getName(), definition.getId(), definition.getLevel()));
        }
        script.log(builder.toString());

        return new ArrayList<>(selectedNPCDefinitions);
    }

    private boolean npcMatchesSelectedNPCs(NPC npc) {
        boolean isSelected = false;
        NPCDefinition definition = npc.getDefinition();
        for(NPCDefinition selectedDefinition: selectedNPCDefinitions) {
            if(selectedDefinition.getName().equalsIgnoreCase(definition.getName())
                    && selectedDefinition.getLevel() == definition.getLevel()
                    && Arrays.equals(selectedDefinition.getActions(), definition.getActions())
            ) {
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }

    private void queryThievableNPCs() {
        queriedNPCs = script.npcs.filter(paintableNPCsFilter);

        if(queriedNPCs.isEmpty()) {
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

    private Rectangle getNPCBoundingBox(NPC npc) {
        return npc.getModel().getBoundingBox(npc.getGridX(), npc.getGridY(), npc.getZ());
    }

    private Area getNPCOutline(NPC npc) {
        return npc.getModel().getArea(npc.getGridX(), npc.getGridY(), npc.getZ());
    }
}
