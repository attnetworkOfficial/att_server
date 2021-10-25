package org.attnetwork.server.component;

public interface TokenEconomy {
  void stake();

  void unstake();

  void claim();

  int proposal();

  void vote(int proposalId, boolean isApprove);

  void transfer();
}
