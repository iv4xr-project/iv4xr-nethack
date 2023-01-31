//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar;

public enum TileType {
  Hex,
  /** Octiles with cost 1 to adjacent and sqrt(2) to diagonal. */
  Octile,
  /** Octiles with uniform cost 1 to adjacent and diagonal. */
  OctileUnicost,
  Tile
}
