package com.campusloop.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A student's purchases or sales, grouped for display:
 * needsAction (seller: requests to confirm; buyer: confirmed exchanges to pay),
 * inProgress (waiting on the other party), completed, and closed (rejected / cancelled).
 */
public class TradeHistory {

    private final List<Transaction> needsAction = new ArrayList<>();
    private final List<Transaction> inProgress = new ArrayList<>();
    private final List<Transaction> completed = new ArrayList<>();
    private final List<Transaction> closed = new ArrayList<>();

    /** Groups transactions from the point of view of a buyer (asBuyer) or a seller. */
    public static TradeHistory of(List<Transaction> all, boolean asBuyer) {
        TradeHistory h = new TradeHistory();
        for (Transaction t : all) {
            switch (t.getStatus()) {
                case AWAITING_SELLER:
                    (asBuyer ? h.inProgress : h.needsAction).add(t);
                    break;
                case AWAITING_PAYMENT:
                    (asBuyer ? h.needsAction : h.inProgress).add(t);
                    break;
                case COMPLETED:
                    h.completed.add(t);
                    break;
                default:
                    h.closed.add(t);
                    break;
            }
        }
        return h;
    }

    public List<Transaction> getNeedsAction() {
        return needsAction;
    }

    public List<Transaction> getInProgress() {
        return inProgress;
    }

    public List<Transaction> getCompleted() {
        return completed;
    }

    public List<Transaction> getClosed() {
        return closed;
    }

    public int getTotal() {
        return needsAction.size() + inProgress.size() + completed.size() + closed.size();
    }
}
