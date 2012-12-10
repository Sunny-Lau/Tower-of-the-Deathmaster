package me.deathjockey.tod.dy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import me.deathjockey.tod.InputHandler;
import me.deathjockey.tod.TowerComponent;
import me.deathjockey.tod.level.Entity;
import me.deathjockey.tod.level.Level;
import me.deathjockey.tod.level.Player;
import me.deathjockey.tod.level.Stairs;
import me.deathjockey.tod.level.Tile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DynamicsLoader {

	public static void init(TowerComponent game, InputHandler input) {
		loadTiles();
		loadEntities();
		loadLevels(game, input);
		loadSounds();
	}

	private static void loadSounds() {

	}

	private static void loadLevels(TowerComponent game, InputHandler input) {
		XMLFile levelDescrip = new XMLFile(TowerComponent.class.getResource("/level.xml"));
		Document doc = levelDescrip.asDocument();
		//Pixels
		NodeList pxlDefine = ((Element) ((Element) doc.getElementsByTagName("levels").item(0)).getElementsByTagName("define").item(0)).getElementsByTagName("pixel");
		List<Color> pxlcol = new ArrayList<Color>();
		List<String> bound = new ArrayList<String>();
		BufferedImage lv = null;
		try {
			lv = ImageIO.read(TowerComponent.class.getResourceAsStream("/levels.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int xt = lv.getWidth() / 12;
		int yt = lv.getHeight() / 13;
		BufferedImage[][] levels = new BufferedImage[xt][yt];
		Level[] lvs = new Level[xt * yt];
		int cw = 0;
		for(int i = 0; i < levels.length; i++) {
			for(int j = 0; j < levels[i].length; j++) {
				levels[i][j] = lv.getSubimage(i * 12, j * 13, 12, 13);
				lvs[cw] = new Level();
				lvs[cw].floor = cw + 1;
				cw++;
			}
		}
		for(int i = 0; i < pxlDefine.getLength(); i++) {
			Element e = (Element) pxlDefine.item(i);
			String[] rgb = e.getAttribute("rgb").split(",");
			if(rgb.length != 3) throw new RuntimeException("RGB Length is invalid in XML file!");
			int[] rgbv = new int[3];
			rgbv[0] = Integer.parseInt(rgb[0]);
			rgbv[1] = Integer.parseInt(rgb[1]);
			rgbv[2] = Integer.parseInt(rgb[2]);
			Color col = new Color(rgbv[0], rgbv[1], rgbv[2]);
			pxlcol.add(col);
			bound.add(e.getAttribute("bind"));
		}
		cw = 0;
		for(int a = 0; a < levels.length; a++) {
			for(int b = 0; b < levels[a].length; b++) {
				int w = levels[a][b].getWidth();
				int h = levels[a][b].getHeight();
				for(int i = 0; i < w; i++) {
					for(int j = 0; j < h; j++) {
						int rgb = levels[a][b].getRGB(i, j);
						int rr = (rgb >> 16) & 0xFF;
						int gg = (rgb >> 8) & 0xFF;
						int bb = rgb & 0xFF;
						for(int k = 0; k < pxlcol.size(); k++) {
							Color c = pxlcol.get(k);
							if(rr == c.getRed() && gg == c.getGreen() && bb == c.getBlue()) {
								lvs[cw].tiles[i][j] = Tile.tiles[Integer.parseInt(bound.get(k))].id;
							}
						}
					}
				}
				cw++;
			}
		}
		NodeList xmlLevels = ((Element) doc.getElementsByTagName("levels").item(0)).getElementsByTagName("level");
		for(int i = 0; i < xmlLevels.getLength(); i++) {
			Element e = (Element) xmlLevels.item(i);
			int floor = Integer.parseInt(e.getAttribute("floor"));
			NodeList entities = e.getElementsByTagName("entity");
			for(int j = 0; j < entities.getLength(); j++) {
				
				Element ee = (Element) entities.item(j);
				String type = ee.getAttribute("type");
				if(type.equalsIgnoreCase("Player")) {
					Player player = new Player(input, game, lvs[floor - 1]);
					player.setPos(Integer.parseInt(ee.getAttribute("x")), Integer.parseInt(ee.getAttribute("y")));
					lvs[floor - 1].addEntity(player);
					continue;
				}
				if(type.equalsIgnoreCase("StairsUp") || type.equalsIgnoreCase("StairsDown")) {
					Stairs stairs = new Stairs(game, type.equalsIgnoreCase("stairsup") ? 0 : 1, Integer.parseInt(ee.getAttribute("tx")), Integer.parseInt(ee.getAttribute("ty")));
					stairs.setPos(Integer.parseInt(ee.getAttribute("x")), Integer.parseInt(ee.getAttribute("y")));
					lvs[floor - 1].addEntity(stairs);
					continue;
				}
				Entity ts = Entity.newInstance(type);
				
				ts.setPos(Integer.parseInt(ee.getAttribute("x")), Integer.parseInt(ee.getAttribute("y")));
				lvs[floor - 1].addEntity(ts);
			}
		}
		for(int i = 0; i < lvs.length; i++) {
			Level.levels.put(lvs[i].floor, lvs[i]);
			
		}
	}

	private static void loadEntities() {
		XMLFile edyn = new XMLFile(TowerComponent.class.getResource("/entities.xml"));
		Document doc = edyn.asDocument();
		NodeList entities = doc.getElementsByTagName("entities");
		NodeList entityReg = ((Element) entities.item(0)).getElementsByTagName("entity");
		for(int i = 0; i < entityReg.getLength(); i++) {
			Element e = (Element) entityReg.item(i);
			Entity.entities.put(e.getAttribute("key"), new Entity(e.getAttribute("name"), e.getAttribute("sprite"),
					Integer.parseInt(e.getAttribute("hp")),
					Integer.parseInt(e.getAttribute("attack")),
					Integer.parseInt(e.getAttribute("defense")),
					Integer.parseInt(e.getAttribute("exp")),
					Integer.parseInt(e.getAttribute("gold"))));
		}
	}

	private static void loadTiles() {
		XMLFile tdyn = new XMLFile(TowerComponent.class.getResource("/tiles.xml"));
		Document doc = tdyn.asDocument();
		NodeList tileNodes = doc.getElementsByTagName("tiles");
		for(int i = 0; i < tileNodes.getLength(); i++) {
			Node n = tileNodes.item(i);
			NodeList tiles = ((Element) n).getElementsByTagName("tile");
			for(int j = 0; j < tiles.getLength(); j++) {
				Element e = (Element) tiles.item(j);
				new Tile(Integer.parseInt(e.getAttribute("id")),e.getAttribute("name"), e.getAttribute("solid").equals("1") ? true : false, e.getAttribute("frames"));
			}
		}
	}
}