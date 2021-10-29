package org.attnetwork.server.component;

public interface TokenEconomy {
  void stake(int amount);

  void unstake(int amount);

  void claim();

  int proposal(String content);

  void vote(int proposalId, boolean isApprove);

  void transfer(int amount);
}
