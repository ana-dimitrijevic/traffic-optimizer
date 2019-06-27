package fxsimulator;

import controllers.LoadscreenController;
import controllers.CanvasController.NodeFX;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author ana
 */
public class RightClickMenu {

    ContextMenu menu;
    NodeFX sourceNode;
    Edge sourceEdge;
    MenuItem info, streetView;

    public RightClickMenu() {
        menu = new ContextMenu();
        info = new MenuItem("Information");
        streetView = new MenuItem("Streetview");

        Image infoIcon = new Image(getClass().getResourceAsStream("../res/info-icon.png"));
        ImageView infoIconView = new ImageView(infoIcon);
        info.setGraphic(infoIconView);

        Image searchIcon = new Image(getClass().getResourceAsStream("../res/icon-search.png"));
        ImageView searchIconView = new ImageView(searchIcon);
        streetView.setGraphic(searchIconView);

        menu.getItems().addAll(info, streetView);
        menu.setOpacity(0.9);
    }

    /**
     * Constructor for the context menu on node
     *
     * @param node
     */
    public RightClickMenu(NodeFX node) {
        this();
        info.setOnAction(e -> {
            LoadscreenController.getCanvasController().infoNode(node);
        });
       streetView.setOnAction(e -> {
            LoadscreenController.getCanvasController().streetView(node);
        });
    }


    public ContextMenu getMenu() {
        return menu;
    }
}
