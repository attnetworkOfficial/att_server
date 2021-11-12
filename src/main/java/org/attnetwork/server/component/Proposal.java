package org.attnetwork.server.component;

import java.util.List;

public interface Proposal {
  String method();

  List<Object> args();

  String content();
}
