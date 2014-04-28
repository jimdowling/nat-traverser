package se.sics.gvod.stun.client.events;

import se.sics.gvod.net.Nat;
import se.sics.kompics.Event;

public final class GetNatTypeResponseRuleExpirationTime extends Event {

    private final long ruleLifeTime;

    public GetNatTypeResponseRuleExpirationTime(long ruleLifeTime) {
        this.ruleLifeTime = (ruleLifeTime < Nat.DEFAULT_RULE_EXPIRATION_TIME) ?
                Nat.DEFAULT_RULE_EXPIRATION_TIME : ruleLifeTime;
    }

    public long getRuleLifeTime() {
        return ruleLifeTime;
    }
}
