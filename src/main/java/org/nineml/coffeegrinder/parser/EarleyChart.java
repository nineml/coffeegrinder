package org.nineml.coffeegrinder.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * The chart used for Earley parsing.
 */
public class EarleyChart {
    private final ArrayList<ArrayList<EarleyItem>> chart;

    protected EarleyChart() {
        chart = new ArrayList<>();
    }

    /**
     * How big is the chart?
     * @return the number of rows in the chart.
     */
    public int size() {
        return chart.size();
    }

    /**
     * Get a row from the chart.
     * <p>The chart will be enlarged if necessary.</p>
     * @param row the row number (0-indexed).
     * @return the contents of the row.
     */
    public List<EarleyItem> get(int row) {
        assureRow(row);
        return chart.get(row);
    }

    protected void clear() {
        chart.clear();
    }

    protected ArrayList<ArrayList<EarleyItem>> rows() {
        return chart;
    }

    protected void add(List<EarleyItem> row) {
        ArrayList<EarleyItem> data = new ArrayList<>(row);
        chart.add(data);
    }

    protected void add(int row, EarleyItem item) {
        assureRow(row);
        chart.get(row).add(item);
    }

    private void assureRow(int row) {
        if (chart.size() <= row) {
            chart.add(new ArrayList<>());
        }
    }
}
