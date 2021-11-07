package org.attnetwork.server.component;

public interface TokenEconomy {
  void stake(int amount);

  void unStake(int amount);

  void claim();

  int proposal(Proposal proposal);

  void vote(int proposalId, boolean isApprove);

  void transfer(int amount, String target);
}
