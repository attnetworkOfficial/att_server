package org.attnetwork.server.component;

import java.util.List;

public interface Proposal {
  String summary();

  String URL();

  String proposer();

  Type type();

  List<Object> parameters();

  enum Type {
  }
}
