package org.attnetwork.server.component;

public interface NeuralNetworkSystem {
  void join();

  void leave();

  void vote();

  void split();

  class Admin {}

  class Node {}
}
