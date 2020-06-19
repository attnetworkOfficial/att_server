package test.org.attnetwork.proto.sl.msg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class ComplicatedMsg extends AbstractSeqLanObject {
  public ArrayList<LinkedList<List<Integer>>> lists;

  public Map<String, List<TreeMap<String, Integer>>> map;

  public List<Map<Integer, Map<String, String>>>[] array;
}
