package utilities;

import controllers.CanvasController;

/**
 *
 * @author ana
 */
public class GMLParser extends Parser {

	public GMLParser(CanvasController canvasController) {
		super(canvasController);
	}

	@Override
	public void parseLine(String line) {

		if (line.equals("graph") || line.equals("node") || line.equals("[") || line.equals("]") || line.equals("edge"))
			return;

		String[] splittedLine = line.split(" ");

		switch (splittedLine[0]) {

		case "label": {
			name = splittedLine[1];
			break;
		}
		case "x": {
			x = Double.parseDouble(splittedLine[1]);
			break;
		}
		case "y": {
			y = Double.parseDouble(splittedLine[1]);
			break;
		}
		case "route": {
			route = splittedLine[1];
			break;
		}
		case "code": {
			code = splittedLine[1];
			break;
		}
		case "lines": {
			canvasController.addNode(name, x, y, route, code, splittedLine[1]);
			break;
		}
		case "source": {
			src = canvasController.getNode(splittedLine[1]);
			break;
		}
		case "target": {
			dst = canvasController.getNode(splittedLine[1]);
			break;
		}
		case "distance": {
			canvasController.addEdge(src, dst, splittedLine[1]);
			break;
		}

		}

	}

}
