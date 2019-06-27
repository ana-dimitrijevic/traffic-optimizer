package controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;

import fxsimulator.Edge;
import fxsimulator.Main;
import fxsimulator.Node;
import fxsimulator.RightClickMenu;

import java.awt.Desktop;
import java.awt.Point;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.StrokeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.controlsfx.control.HiddenSidesPane;

/**
*
* @author ana
*/
public class CanvasController implements Initializable, ChangeListener {

	@FXML
	private HiddenSidesPane hiddenPane;
	@FXML
	private AnchorPane anchorRoot;
	@FXML
	private StackPane stackRoot;
	@FXML
	private JFXButton canvasBackButton, clearButton, resetButton, playPauseButton;
	@FXML
	private JFXToggleButton bfsButton, dfsButton, dijkstraButton, mstButton;
	@FXML
	private ToggleGroup algoToggleGroup;
	@FXML
	private Pane viewer;
	@FXML
	private Group canvasGroup;
	@FXML
	private Line edgeLine;
	@FXML
	private Label sourceText = new Label("Source"), weight;
	@FXML
	private Pane border;
	@FXML
	private JFXNodesList nodeList;
	@FXML
	private JFXSlider slider = new JFXSlider();
	@FXML
	private ImageView playPauseImage, openHidden;

	boolean menuBool = false;
	ContextMenu globalMenu;

	int time = 500;
	NodeFX selectedNode = null;
	List<NodeFX> circles = new ArrayList<>();
	List<Edge> mstEdges = new ArrayList<>(), realEdges = new ArrayList<>();
	List<Shape> edges = new ArrayList<>();
	boolean calculate = false, calculated = false, playing = false, paused = false, pinned = false;
	List<Label> distances = new ArrayList<Label>(), visitTime = new ArrayList<>(), lowTime = new ArrayList<Label>();
	private boolean bfs = true, dfs = true, dijkstra = true, mst = true;
	Algorithm algo = new Algorithm();

	public SequentialTransition st;

	public AnchorPane hiddenRoot = new AnchorPane();

	public static TextArea textFlow = new TextArea();
	public ScrollPane textContainer = new ScrollPane();

	@Override
	public void initialize(URL url, ResourceBundle rb) {

		hiddenPane.setContent(canvasGroup);

		ResetHandle(null);
		viewer.prefHeightProperty().bind(border.heightProperty());
		viewer.prefWidthProperty().bind(border.widthProperty());

		clearButton.setDisable(true);

		// Set back button action
		canvasBackButton.setOnAction(e -> {
			try {
				ResetHandle(null);
				Parent root = FXMLLoader.load(getClass().getResource("../fxml/Loadscreen.fxml"));

				Scene scene = new Scene(root);
				Main.primaryStage.setScene(scene);
				Main.primaryStage.setFullScreen(true);
			} catch (IOException ex) {
				Logger.getLogger(CanvasController.class.getName()).log(Level.SEVERE, null, ex);
			}
		});

		// Setup Slider
		slider = new JFXSlider(10, 1000, 500);
		slider.setPrefWidth(150);
		slider.setPrefHeight(80);
		slider.setSnapToTicks(true);
		slider.setMinorTickCount(100);
		slider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
		slider.setBlendMode(BlendMode.MULTIPLY);
		slider.setCursor(Cursor.CLOSED_HAND);
		nodeList.addAnimatedNode(slider);
		nodeList.setSpacing(50D);
		nodeList.setRotate(270D);
		slider.toFront();
		nodeList.toFront();
		slider.valueProperty().addListener(this);

		hiddenRoot.setPrefWidth(220);
		hiddenRoot.setPrefHeight(Main.primaryStage.getHeight());

		hiddenRoot.setCursor(Cursor.DEFAULT);

		// Set Label "Detail"
		Label detailLabel = new Label("Detail");
		detailLabel.setPrefSize(hiddenRoot.getPrefWidth() - 20, 38);
		detailLabel.setAlignment(Pos.CENTER);
		detailLabel.setFont(new Font("Roboto", 20));
		detailLabel.setPadding(new Insets(7, 40, 3, -10));
		detailLabel.setStyle("-fx-background-color: #dcdde1;");
		detailLabel.setLayoutX(35);

		// Set TextFlow pane properties
		textFlow.setPrefSize(hiddenRoot.getPrefWidth(), hiddenRoot.getPrefHeight() - 2);
		textFlow.setStyle("-fx-background-color: #dfe6e9;");
		textFlow.setLayoutY(39);
		textContainer.setLayoutY(textFlow.getLayoutY());
		textFlow.setPadding(new Insets(5, 0, 0, 5));
		textFlow.setEditable(false);
		textContainer.setContent(textFlow);

		// Set Pin/Unpin Button
		JFXButton pinUnpin = new JFXButton();
		pinUnpin.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

		ImageView imgPin = new ImageView(new Image(getClass().getResourceAsStream("../res/pinned.png")));
		imgPin.setFitHeight(20);
		imgPin.setFitWidth(20);
		ImageView imgUnpin = new ImageView(new Image(getClass().getResourceAsStream("../res/unpinned.png")));
		imgUnpin.setFitHeight(20);
		imgUnpin.setFitWidth(20);
		pinUnpin.setGraphic(imgPin);

		pinUnpin.setPrefSize(20, 39);
		pinUnpin.setButtonType(JFXButton.ButtonType.FLAT);
		pinUnpin.setStyle("-fx-background-color: #dcdde1;");
		pinUnpin.setOnMouseClicked(e -> {
			if (pinned) {
				pinUnpin.setGraphic(imgPin);
				hiddenPane.setPinnedSide(null);
				pinned = false;
			} else {
				pinUnpin.setGraphic(imgUnpin);
				hiddenPane.setPinnedSide(Side.RIGHT);
				pinned = true;
			}
		});

		// Add Label and TextFlow to hiddenPane
		hiddenRoot.getChildren().addAll(pinUnpin, detailLabel, textContainer);
		hiddenPane.setRight(hiddenRoot);
		hiddenRoot.setOnMouseEntered(e -> {
			hiddenPane.setPinnedSide(Side.RIGHT);
			openHidden.setVisible(false);
			e.consume();
		});
		hiddenRoot.setOnMouseExited(e -> {
			if (!pinned) {
				hiddenPane.setPinnedSide(null);
				openHidden.setVisible(true);
			}
			e.consume();
		});
		hiddenPane.setTriggerDistance(60);

		dijkstraButton.setDisable(false);
		bfsButton.setDisable(false);
		dfsButton.setDisable(false);
		mstButton.setDisable(false);

	}

	/**
	 * Change Listener for change in speed slider values.
	 *
	 * @param observable
	 * @param oldValue
	 * @param newValue
	 */
	@Override
	public void changed(ObservableValue observable, Object oldValue, Object newValue) {
		int temp = (int) slider.getValue();

		if (temp > 500) {
			int diff = temp - 500;
			temp = 500;
			temp -= diff;
			temp += 10;
		} else if (temp < 500) {
			int diff = 500 - temp;
			temp = 500;
			temp += diff;
			temp -= 10;
		}
		time = temp;
	
	}

	/**
	 * Handles events for mouse clicks on the canvas.
	 *
	 * @param ev
	 */
	@FXML
	public void handle(MouseEvent ev) {

		if (!ev.getSource().equals(canvasGroup)) {
			if (ev.getEventType() == MouseEvent.MOUSE_RELEASED && ev.getButton() == MouseButton.PRIMARY) {
				if (menuBool == true) {
					menuBool = false;
					return;
				}

			}
		}

	}


	/**
	 * Adds new node to the graph.
	 *
	 * @param station name
	 * @param x coordinate
	 * @param y coordinate
	 * @param route code
	 * @param code 
	 * @param lines 
	 */
	public void addNode(String name, double x, double y, String route, String code, String lines) {

		NodeFX circle = new NodeFX(x, y, 1.2, name, route, code, lines);
		canvasGroup.getChildren().add(circle);

		circle.setOnMousePressed(mouseHandler);
		circle.setOnMouseReleased(mouseHandler);
		circle.setOnMouseDragged(mouseHandler);
		circle.setOnMouseExited(mouseHandler);
		circle.setOnMouseEntered(mouseHandler);

		ScaleTransition tr = new ScaleTransition(Duration.millis(100), circle);
		tr.setByX(10f);
		tr.setByY(10f);
		tr.setInterpolator(Interpolator.EASE_OUT);
		tr.play();

	}

	/**
	 * Checks if edge already exists between 2 nodes.
	 *
	 * @param u source node
	 * @param v destination node
	 */
	boolean edgeExists(NodeFX u, NodeFX v) {
		for (Edge e : realEdges) {
			if (e.source == u.node && e.target == v.node) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * Adds new edge to the graph.
	 *
	 * @param source node
	 * @param destination node
	 * @param distance between stations
	 */
	public void addEdge(NodeFX src, NodeFX dst, String distance) {

		if (!edgeExists(src, dst)) {
			edgeLine = new Line(src.point.x, src.point.y, dst.point.x, dst.point.y);
			canvasGroup.getChildren().add(edgeLine);
			edgeLine.setId("line");
			weight = new Label();
			weight.setText(distance);

			Shape line_arrow = null;
			Edge temp = null;

			temp = new Edge(src.node, dst.node, Integer.valueOf(weight.getText()), edgeLine, weight);

			src.node.adjacents.add(new Edge(src.node, dst.node, Double.valueOf(weight.getText()), edgeLine, weight));
			dst.node.adjacents.add(new Edge(dst.node, src.node, Double.valueOf(weight.getText()), edgeLine, weight));
			edges.add(edgeLine);
			realEdges.add(src.node.adjacents.get(src.node.adjacents.size() - 1));
			realEdges.add(dst.node.adjacents.get(dst.node.adjacents.size() - 1));
			line_arrow = edgeLine;
			mstEdges.add(temp);
		}

	}

	/**
	 * Returns node with given name if exists.
	 *
	 * @param name
	 */
	public NodeFX getNode(String name) {

		for (NodeFX tmp : circles) {
			if (tmp.node.name.equals(name)) {
				return tmp;
			}
		}
		return null;
	}

	/**
	 * Handles events for mouse clicks on a node.
	 * 
	 */
	EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent mouseEvent) {
			NodeFX circle = (NodeFX) mouseEvent.getSource();
			if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED
					&& mouseEvent.getButton() == MouseButton.PRIMARY) {

				if (!circle.isSelected) {

					FillTransition ft = new FillTransition(Duration.millis(300), circle, Color.BLACK, Color.RED);
					ft.play();
					circle.isSelected = true;
					selectedNode = circle;

					// When selected on active algorithm
					if (calculate && !calculated) {
						if (bfs) {
							algo.newBFS(circle.node);
						} else if (dfs) {
							algo.newDFS(circle.node);
						}
						if (dijkstra) {
							algo.newDijkstra(circle.node);
						}

						calculated = true;
					} else if (calculate && calculated && !mst) {

						for (NodeFX n : circles) {
							n.isSelected = false;
							FillTransition ft1 = new FillTransition(Duration.millis(300), n);
							ft1.setToValue(Color.BLACK);
							ft1.play();
						}
						List<Node> path = algo.getShortestPathTo(circle.node);
						for (Node n : path) {
							FillTransition ft1 = new FillTransition(Duration.millis(300), n.circle);
							ft1.setToValue(Color.BLUE);
							ft1.play();
						}
					}
				} else {
					circle.isSelected = false;
					FillTransition ft1 = new FillTransition(Duration.millis(300), circle, Color.RED, circle.color);
					ft1.play();
					selectedNode = null;
				}

			}
		}

	};

	/**
	 * Event handler for the Play/Pause button.
	 *
	 * @param event
	 */
	@FXML
	public void PlayPauseHandle(ActionEvent event) {

		try {
			if (playing && st != null && st.getStatus() == Animation.Status.RUNNING) {
				Image image = new Image(getClass().getResourceAsStream("../res/play_arrow_black_48x48.png"));
				playPauseImage.setImage(image);

				st.pause();
				paused = true;
				playing = false;
				return;
			} else if (paused && st != null) {
				Image image = new Image(getClass().getResourceAsStream("../res/pause_black_48x48.png"));
				playPauseImage.setImage(image);
				if (st.getStatus() == Animation.Status.PAUSED)
					st.play();
				else if (st.getStatus() == Animation.Status.STOPPED)
					st.playFromStart();
				playing = true;
				paused = false;
				return;
			}
		} catch (Exception e) {
			System.out.println("Error while play/pause: " + e);
			ClearHandle(null);
		}
	}

	/**
	 * Event handler for the Reset button. Clears all the lists and empties the
	 * canvas.
	 *
	 * @param event
	 */
	@FXML
	public void ResetHandle(ActionEvent event) {
		ClearHandle(null);

		canvasGroup.getChildren().clear();
		canvasGroup.getChildren().addAll(viewer);
		selectedNode = null;
		circles = new ArrayList<NodeFX>();
		distances = new ArrayList<Label>();
		visitTime = new ArrayList<Label>();
		lowTime = new ArrayList<Label>();

		calculate = false;
		calculated = false;

		clearButton.setDisable(true);
		algo = new Algorithm();
		Image image = new Image(getClass().getResourceAsStream("../res/pause_black_48x48.png"));
		playPauseImage.setImage(image);
		hiddenPane.setPinnedSide(null);

		bfsButton.setDisable(true);

		dfsButton.setDisable(true);
		dijkstraButton.setDisable(true);

		mstButton.setDisable(true);
		playing = false;
		paused = false;
	}

	/**
	 * Event handler for the Clear button. Re-initiates the distance and node values
	 * and labels.
	 *
	 * @param event
	 */
	@FXML
	public void ClearHandle(ActionEvent event) {
		if (st != null && st.getStatus() != Animation.Status.STOPPED)
			st.stop();
		if (st != null)
			st.getChildren().clear();
		menuBool = false;
		selectedNode = null;
		calculated = false;

		for (NodeFX n : circles) {
			n.isSelected = false;
			n.node.visited = false;
			n.node.previous = null;
			n.node.minDistance = Double.POSITIVE_INFINITY;
			n.node.DAGColor = 0;

			FillTransition ft1 = new FillTransition(Duration.millis(300), n);
			ft1.setToValue(n.color);
			ft1.play();
		}
		for (Shape x : edges) {

			StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), x);
			ftEdge.setToValue(Color.BLACK);
			ftEdge.play();

		}
		canvasGroup.getChildren().remove(sourceText);
		for (Label x : distances) {
			x.setText("Distance : INFINITY");
			canvasGroup.getChildren().remove(x);
		}
		for (Label x : visitTime) {
			x.setText("Visit : 0");
			canvasGroup.getChildren().remove(x);
		}
		for (Label x : lowTime) {
			x.setText("Low Value : NULL");
			canvasGroup.getChildren().remove(x);
		}
		textFlow.clear();

		Image image = new Image(getClass().getResourceAsStream("../res/pause_black_48x48.png"));
		playPauseImage.setImage(image);

		distances = new ArrayList<>();
		visitTime = new ArrayList<>();
		lowTime = new ArrayList<>();

		bfs = false;
		dfs = false;
		dijkstra = false;
		mst = false;

		playing = false;
		paused = false;
	}

	@FXML
	public void BFSHandle(ActionEvent event) {

		calculate = true;
		clearButton.setDisable(false);
		bfs = true;
		dfs = false;
		dijkstra = false;
		mst = false;

	}

	@FXML
	public void DFSHandle(ActionEvent event) {

		calculate = true;
		clearButton.setDisable(false);
		dfs = true;
		bfs = false;
		dijkstra = false;
		mst = false;

	}

	@FXML
	public void DijkstraHandle(ActionEvent event) {

		calculate = true;
		clearButton.setDisable(false);
		bfs = false;
		dfs = false;
		dijkstra = true;
		mst = false;

	}

	@FXML
	public void MSTHandle(ActionEvent event) {

		calculate = true;
		clearButton.setDisable(false);
		bfs = false;
		dfs = false;
		dijkstra = false;
		mst = true;
		algo.newMST();
	}

	/**
	 * Shape class for the nodes.
	 */
	public class NodeFX extends Circle {

		Node node;
		Point point;
		Label distance = new Label("Dist. : INFINITY");
		Label visitTime = new Label("Visit : 0");
		Label lowTime = new Label("Low : 0");
		Label id;
		String route;
		Color color;
		boolean isSelected = false;
		String code, lines;

		public NodeFX(double x, double y, double rad, String name, String route, String code, String lines) {
			super(x, y, rad);
			node = new Node(name, this);
			point = new Point((int) x, (int) y);
			id = new Label(name);
			canvasGroup.getChildren().add(id);
			id.setLayoutX(x - 20);
			id.setLayoutY(y - 35);
			this.setOpacity(0.5);
			this.setBlendMode(BlendMode.MULTIPLY);
			this.setId("node");
			this.route = route;
			this.code=code;
			this.lines=lines;

			this.color = fillNode(route);

			RightClickMenu rt = new RightClickMenu(this);
			ContextMenu menu = rt.getMenu();
			globalMenu = menu;
			this.setOnContextMenuRequested(e -> {
				menu.show(this, e.getScreenX(), e.getScreenY());
				menuBool = true;
			});
			menu.setOnAction(e -> {
				menuBool = false;
			});

			circles.add(this);

		}

		public Color fillNode(String route) {

			switch (route) {

			case "0": {
				this.setFill(Color.CHARTREUSE);
				return Color.CHARTREUSE;
			}
			case "1": {
				this.setFill(Color.FIREBRICK);
				return Color.FIREBRICK;
			}
			case "2": {
				this.setFill(Color.YELLOW);
				return Color.YELLOW;
			}
			case "3": {
				this.setFill(Color.MIDNIGHTBLUE);
				return Color.MIDNIGHTBLUE;
			}
			case "4": {
				this.setFill(Color.ORANGE);
				return Color.ORANGE;
			}
			case "5": {
				this.setFill(Color.GREEN);
				return Color.GREEN;
			}
			case "6": {
				this.setFill(Color.DARKVIOLET);
				return Color.DARKVIOLET;
			}
			case "7": {
				this.setFill(Color.CORNFLOWERBLUE);
				return Color.CORNFLOWERBLUE;
			}
			}
			return null;

		}
	}

	/*
	 * Algorithm Declarations ------------------------------------------
	 */
	public class Algorithm {

		public void newDijkstra(Node source) {
			new Dijkstra(source);
		}

		class Dijkstra {

			Dijkstra(Node source) {

				for (NodeFX n : circles) {
					distances.add(n.distance);
					n.distance.setLayoutX(n.point.x + 20);
					n.distance.setLayoutY(n.point.y);
					canvasGroup.getChildren().add(n.distance);
				}
				sourceText.setLayoutX(source.circle.point.x + 20);
				sourceText.setLayoutY(source.circle.point.y + 10);
				canvasGroup.getChildren().add(sourceText);
				SequentialTransition st = new SequentialTransition();
				source.circle.distance.setText("Dist. : " + 0);

				source.minDistance = 0;
				PriorityQueue<Node> pq = new PriorityQueue<Node>();
				pq.add(source);
				while (!pq.isEmpty()) {
					Node u = pq.poll();
					System.out.println(u.name);
					FillTransition ft = new FillTransition(Duration.millis(time), u.circle);
					ft.setToValue(Color.CHOCOLATE);
					st.getChildren().add(ft);
					String str = "";
					str = str.concat("Popped : Node(" + u.name + "), Current Distance: " + u.minDistance + "\n");
					final String str2 = str;
					FadeTransition fd = new FadeTransition(Duration.millis(10), textFlow);
					fd.setOnFinished(e -> {
						textFlow.appendText(str2);
					});
					fd.onFinishedProperty();
					st.getChildren().add(fd);
			
					for (Edge e : u.adjacents) {
						if (e != null) {
							Node v = e.target;

							if (u.minDistance + e.weight < v.minDistance) {
								pq.remove(v);
								v.minDistance = u.minDistance + e.weight;
								v.previous = u;
								pq.add(v);

								FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
								ft1.setToValue(Color.FORESTGREEN);
								ft1.setOnFinished(ev -> {
									v.circle.distance.setText("Dist. : " + v.minDistance);
								});
								ft1.onFinishedProperty();
								st.getChildren().add(ft1);

								str = "\t";
								str = str.concat("Pushing : Node(" + v.name + "), (" + u.name + "--" + v.name
										+ ") Distance : " + v.minDistance + "\n");
								final String str1 = str;
								FadeTransition fd2 = new FadeTransition(Duration.millis(10), textFlow);
								fd2.setOnFinished(ev -> {
									textFlow.appendText(str1);
								});
								fd2.onFinishedProperty();
								st.getChildren().add(fd2);

							}
						}
					}

					FillTransition ft2 = new FillTransition(Duration.millis(time), u.circle);
					ft2.setToValue(Color.BLUEVIOLET);
					st.getChildren().add(ft2);

				}

				st.setOnFinished(ev -> {
					for (NodeFX n : circles) {
						FillTransition ft1 = new FillTransition(Duration.millis(time), n);
						ft1.setToValue(Color.BLACK);
						ft1.play();
					}

					for (Shape n : edges) {
						n.setStroke(Color.BLACK);
					}

					FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
					ft1.setToValue(Color.RED);
					ft1.play();
					Image image = new Image(getClass().getResourceAsStream("../res/play_arrow_black_48x48.png"));
					playPauseImage.setImage(image);
					paused = true;
					playing = false;
					textFlow.appendText("---Finished--\n");
				});
				st.onFinishedProperty();
				st.play();
				playing = true;
				paused = false;
			}
		}

		public void newBFS(Node source) {
			new BFS(source);
		}

		class BFS {

			BFS(Node source) {

				for (NodeFX n : circles) {
					distances.add(n.distance);
					n.distance.setLayoutX(n.point.x + 20);
					n.distance.setLayoutY(n.point.y);
					canvasGroup.getChildren().add(n.distance);
				}
				sourceText.setLayoutX(source.circle.point.x + 20);
				sourceText.setLayoutY(source.circle.point.y + 10);
				canvasGroup.getChildren().add(sourceText);
				st = new SequentialTransition();
				source.circle.distance.setText("Dist. : " + 0);

				source.minDistance = 0;
				source.visited = true;
				LinkedList<Node> q = new LinkedList<Node>();
				q.push(source);
				while (!q.isEmpty()) {
					Node u = q.removeLast();

					FillTransition ft = new FillTransition(Duration.millis(time), u.circle);
					if (u.circle.getFill() == Color.BLACK) {
						ft.setToValue(Color.CHOCOLATE);
					}
					st.getChildren().add(ft);

					String str = "";
					str = str.concat("Popped : Node(" + u.name + ")\n");
					final String str2 = str;
					FadeTransition fd = new FadeTransition(Duration.millis(10), textFlow);
					fd.setOnFinished(e -> {
						textFlow.appendText(str2);
					});
					fd.onFinishedProperty();
					st.getChildren().add(fd);

					
					for (Edge e : u.adjacents) {
						if (e != null) {
							Node v = e.target;

							if (!v.visited) {
								v.minDistance = u.minDistance + 1;
								v.visited = true;
								q.push(v);
								v.previous = u;

								FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
								ft1.setToValue(Color.FORESTGREEN);
								ft1.setOnFinished(ev -> {
									v.circle.distance.setText("Dist. : " + v.minDistance);
								});
								ft1.onFinishedProperty();
								st.getChildren().add(ft1);

								str = "\t";
								str = str.concat("Pushing : Node(" + v.name + ")\n");
								final String str1 = str;
								FadeTransition fd2 = new FadeTransition(Duration.millis(10), textFlow);
								fd2.setOnFinished(ev -> {
									textFlow.appendText(str1);
								});
								fd2.onFinishedProperty();
								st.getChildren().add(fd2);

							}
						}
					}

					FillTransition ft2 = new FillTransition(Duration.millis(time), u.circle);
					ft2.setToValue(Color.BLUEVIOLET);
					st.getChildren().add(ft2);

				}

				st.setOnFinished(ev -> {
					for (NodeFX n : circles) {
						FillTransition ft1 = new FillTransition(Duration.millis(time), n);
						ft1.setToValue(Color.BLACK);
						ft1.play();
					}

					for (Shape n : edges) {
						n.setStroke(Color.BLACK);
					}

					FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
					ft1.setToValue(Color.RED);
					ft1.play();
					Image image = new Image(getClass().getResourceAsStream("../res/play_arrow_black_48x48.png"));
					playPauseImage.setImage(image);
					paused = true;
					playing = false;
					textFlow.appendText("---Finished--\n");
				});
				st.onFinishedProperty();
				st.play();
				playing = true;
				paused = false;

			}

		}

		public void newMST() {

			new MST();
		}

		class MST {

			int mstValue = 0;

			Node findParent(Node x) {
				if (x == x.previous) {
					return x;
				}
				x.previous = findParent(x.previous);
				return x.previous;
			}

			void unionNode(Node x, Node y) {
				Node px = findParent(x);
				Node py = findParent(y);
				if (px == py) {
					return;
				}
				if (px.name.length() < py.name.length()) {
					px.previous = py;
				} else {
					py.previous = px;
				}
			}

			public MST() {

				st = new SequentialTransition();
				for (NodeFX x : circles) {
					x.node.previous = x.node;
				}

				
				String init = "Intially : \n";
				for (NodeFX x : circles) {
					final String s = "Node : " + x.node.name + " , Parent: " + x.node.previous.name + "\n";
					FadeTransition fd = new FadeTransition(Duration.millis(10), textFlow);
					fd.setOnFinished(e -> {
						textFlow.appendText(s);
					});
					fd.onFinishedProperty();
					st.getChildren().add(fd);
				}
				final String s = "Start Algorithm :---\n";
				FadeTransition fdss = new FadeTransition(Duration.millis(10), textFlow);
				fdss.setOnFinished(ev -> {
					textFlow.appendText(s);
				});
				fdss.onFinishedProperty();
				st.getChildren().add(fdss);
				
				Collections.sort(mstEdges, new Comparator<Edge>() {
					public int compare(Edge o1, Edge o2) {
						if (o1.weight == o2.weight) {
							return 0;
						}
						return o1.weight > o2.weight ? 1 : -1;
					}
				});

				for (Edge e : mstEdges) {

					StrokeTransition ft1 = new StrokeTransition(Duration.millis(time), e.line);
					ft1.setToValue(Color.DARKORANGE);
					st.getChildren().add(ft1);

		
					final String se = "Selected Edge:- (" + e.source.name.trim() + "--" + e.target.name.trim()
							+ ") Weight: " + String.valueOf(e.weight) + " \n";
					FadeTransition fdx = new FadeTransition(Duration.millis(10), textFlow);
					fdx.setOnFinished(evx -> {
						textFlow.appendText(se);
					});
					fdx.onFinishedProperty();
					st.getChildren().add(fdx);

					final String s1 = "\t-> Node :" + e.source.name.trim() + "  Parent: "
							+ findParent(e.source.previous).name.trim() + "\n";
					FadeTransition fdx2 = new FadeTransition(Duration.millis(10), textFlow);
					fdx2.setOnFinished(evx -> {
						textFlow.appendText(s1);
					});
					fdx2.onFinishedProperty();
					st.getChildren().add(fdx2);

					final String s2 = "\t-> Node :" + e.target.name.trim() + "  Parent: "
							+ findParent(e.target.previous).name.trim() + "\n";
					FadeTransition fdx3 = new FadeTransition(Duration.millis(10), textFlow);
					fdx3.setOnFinished(evx -> {
						textFlow.appendText(s2);
					});
					fdx3.onFinishedProperty();
					st.getChildren().add(fdx3);
				
					if (findParent(e.source.previous) != findParent(e.target.previous)) {
						unionNode(e.source, e.target);
						mstValue += e.weight;

					
						final String sa = "\t---->Unioned\n";
						final String sa1 = "\t\t->Node :" + e.source.name.trim() + "  Parent: "
								+ findParent(e.source.previous).name.trim() + "\n";
						final String sa2 = "\t\t->Node :" + e.target.name.trim() + "  Parent: "
								+ findParent(e.target.previous).name.trim() + "\n";
						FadeTransition fdx4 = new FadeTransition(Duration.millis(10), textFlow);
						fdx4.setOnFinished(evx -> {
							textFlow.appendText(sa);
						});
						fdx4.onFinishedProperty();
						st.getChildren().add(fdx4);
						FadeTransition fdx5 = new FadeTransition(Duration.millis(10), textFlow);
						fdx5.setOnFinished(evx -> {
							textFlow.appendText(sa1);
						});
						fdx5.onFinishedProperty();
						st.getChildren().add(fdx5);
						FadeTransition fdx6 = new FadeTransition(Duration.millis(10), textFlow);
						fdx6.setOnFinished(evx -> {
							textFlow.appendText(sa2);
						});
						fdx6.onFinishedProperty();
						st.getChildren().add(fdx6);

						StrokeTransition ft2 = new StrokeTransition(Duration.millis(time), e.line);
						ft2.setToValue(Color.DARKGREEN);
						st.getChildren().add(ft2);

						FillTransition ft3 = new FillTransition(Duration.millis(time), e.source.circle);
						ft3.setToValue(Color.AQUA);
						st.getChildren().add(ft3);

						ft3 = new FillTransition(Duration.millis(time), e.target.circle);
						ft3.setToValue(Color.AQUA);
						st.getChildren().add(ft3);
				
					} else {
						
						final String sa = "\t---->Cycle Detected\n";
						FadeTransition fdx7 = new FadeTransition(Duration.millis(10), textFlow);
						fdx7.setOnFinished(evx -> {
							textFlow.appendText(sa);
						});
						fdx7.onFinishedProperty();
						st.getChildren().add(fdx7);
				
						StrokeTransition ft2 = new StrokeTransition(Duration.millis(time), e.line);
						ft2.setToValue(Color.DARKRED);
						st.getChildren().add(ft2);

						ft2 = new StrokeTransition(Duration.millis(time), e.line);
						ft2.setToValue(Color.web("#E0E0E0"));
						st.getChildren().add(ft2);

					}
				}

		
				st.setOnFinished(ev -> {
					Image image = new Image(getClass().getResourceAsStream("../res/play_arrow_black_48x48.png"));
					playPauseImage.setImage(image);
					paused = true;
					playing = false;
					textFlow.appendText("Minimum Cost of the Graph " + mstValue);
				});
				st.onFinishedProperty();
				st.play();
				playing = true;
			}
		}
	
		public void newDFS(Node source) {
			new DFS(source);
		}

		class DFS {

			DFS(Node source) {

				for (NodeFX n : circles) {
					distances.add(n.distance);
					n.distance.setLayoutX(n.point.x + 20);
					n.distance.setLayoutY(n.point.y);
					canvasGroup.getChildren().add(n.distance);
				}
				sourceText.setLayoutX(source.circle.point.x + 20);
				sourceText.setLayoutY(source.circle.point.y + 10);
				canvasGroup.getChildren().add(sourceText);
				st = new SequentialTransition();
				source.circle.distance.setText("Dist. : " + 0);

				source.minDistance = 0;
				source.visited = true;
				DFSRecursion(source, 0);

				st.setOnFinished(ev -> {
					for (NodeFX n : circles) {
						FillTransition ft1 = new FillTransition(Duration.millis(time), n);
						ft1.setToValue(Color.BLACK);
						ft1.play();
					}

					for (Shape n : edges) {
						n.setStroke(Color.BLACK);
					}

					FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
					ft1.setToValue(Color.RED);
					ft1.play();
					Image image = new Image(getClass().getResourceAsStream("../res/play_arrow_black_48x48.png"));
					playPauseImage.setImage(image);
					paused = true;
					playing = false;
					textFlow.appendText("---Finished--\n");
				});
				st.onFinishedProperty();
				st.play();
				playing = true;
				paused = false;

			}

			public void DFSRecursion(Node source, int level) {

				FillTransition ft = new FillTransition(Duration.millis(time), source.circle);
				if (source.circle.getFill() == Color.BLACK) {
					ft.setToValue(Color.FORESTGREEN);
				}
				st.getChildren().add(ft);

				String str = "";
				for (int i = 0; i < level; i++) {
					str = str.concat("\t");
				}
				str = str.concat("DFS(" + source.name + ") Enter\n");
				final String str2 = str;
				FadeTransition fd = new FadeTransition(Duration.millis(10), textFlow);
				fd.setOnFinished(e -> {
					textFlow.appendText(str2);
				});
				fd.onFinishedProperty();
				st.getChildren().add(fd);

				for (Edge e : source.adjacents) {
					if (e != null) {
						Node v = e.target;
						if (!v.visited) {
							v.minDistance = source.minDistance + 1;
							v.visited = true;
							v.previous = source;

							DFSRecursion(v, level + 1);

							FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
							ft1.setToValue(Color.BLUEVIOLET);
							ft1.onFinishedProperty();
							ft1.setOnFinished(ev -> {
								v.circle.distance.setText("Dist. : " + v.minDistance);
							});
							st.getChildren().add(ft1);

						}
					}
				}
				str = "";
				for (int i = 0; i < level; i++) {
					str = str.concat("\t");
				}
				str = str.concat("DFS(" + source.name + ") Exit\n");
				final String str1 = str;
				fd = new FadeTransition(Duration.millis(10), textFlow);
				fd.setOnFinished(e -> {
					textFlow.appendText(str1);
				});
				fd.onFinishedProperty();
				st.getChildren().add(fd);
			}
		}

		public List<Node> getShortestPathTo(Node target) {
			List<Node> path = new ArrayList<Node>();
			for (Node i = target; i != null; i = i.previous) {
				path.add(i);
			}
			Collections.reverse(path);
			return path;
		}
	}

	public void infoNode(NodeFX node) {

		Alert a = new Alert(AlertType.INFORMATION);
		a.setTitle("Station information");
		a.setHeaderText(node.node.name);
		a.setContentText("Code : #" + node.code + " \n Coordinates: (41.122342, 44.2213443) \n Route: " + node.route + " \n Lines: " + node.lines);
		a.show();

	}

	public void streetView(NodeFX node) {

		try {
			Desktop.getDesktop().browse(
					//new URI("http://maps.google.com/maps?saddr=Current%20Location&daddr=52.5651667,-8.7895846"));
					new URI("http://maps.google.com/maps?saddr=Current%20Location"));
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
