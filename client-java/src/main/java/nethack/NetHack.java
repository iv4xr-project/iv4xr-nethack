package nethack;

import java.io.IOException;
import java.io.Console;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import connection.SendCommandClient;
import eu.iv4xr.framework.spatial.IntVec2D;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.Entity.*;
import nl.uu.cs.aplib.utils.Pair;

public class NetHack {	
	public Blstats stats;
	public boolean done = false;
	public Entity[][] entities;
	
	public NetHack (Entity[][] entities, Blstats stats) {
		step(entities, stats);
	}
	
	public void step(Entity[][] entities, Blstats stats) {
		this.entities = entities;
		this.stats = stats;
	}
}
