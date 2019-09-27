package server;

import java.rmi.Naming;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import tools.Address;

/**
 * The class Reducer.
 */
public class Reducer implements Callable<Boolean> {

  private Address helperAddress;
  private ConcurrentHashMap<String, Integer> WordAndCountMap;
  private ConcurrentHashMap<Address, HelperTaskWrapper> AddressAndHelperTaskMap;
  private HelperTaskWrapper helperTask;
  private HelperForSearchQuery helperForSearchStub;

  /**
   * Instantiates a new Reducer.
   *
   * @param helperAddress the helper address
   * @param AddressAndHelperTaskMap <helperAddress, HelperTask>
   * @param WordAndCountMap the count by word table
   * @param helperTask the helperTask
   */
  public Reducer(Address helperAddress,
      ConcurrentHashMap<Address, HelperTaskWrapper> AddressAndHelperTaskMap,
      ConcurrentHashMap<String, Integer> WordAndCountMap, HelperTaskWrapper helperTask) {
    this.helperAddress = helperAddress;
    this.AddressAndHelperTaskMap = AddressAndHelperTaskMap;
    this.WordAndCountMap = WordAndCountMap;
    this.helperTask = helperTask;

  }


  public Boolean call() throws Exception {
    String wordLoadToString = helperTask.toString();
    helperForSearchStub = (HelperForSearchQuery) Naming
        .lookup("//" + helperAddress.toString() + "/helperServerForSearch");
    ConcurrentHashMap<String, Integer> helperWordAndCountMap = helperForSearchStub
        .doHelperTask(wordLoadToString);
    System.out.println("Receiving response from helper server : " + helperAddress.toString());
    helperTask.setDone(true);
    AddressAndHelperTaskMap.get(helperAddress).setDone(true);
    for (String word : helperWordAndCountMap.keySet()) {
      int count = helperWordAndCountMap.get(word);
      if (!WordAndCountMap.containsKey(word)) {
        WordAndCountMap.put(word, count);
      } else {
        int newVal = WordAndCountMap.get(word) + count;
        WordAndCountMap.put(word, newVal);
      }
    }
    return true;
  }
}
