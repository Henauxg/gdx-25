package fr.baldurcrew.gdx25.utils;

import com.badlogic.gdx.math.MathUtils;

public class Range {
    public final float extent;
    public final float halfExtent;
    public float from;
    public float to;

    private Range(float from, float to) {
        this.from = from;
        this.to = to;
        this.extent = to - from;
        this.halfExtent = extent / 2f;
    }

    public static Range buildRangeEx(float from, float to) throws IllegalArgumentException {
        if (to < from) {
            throw new IllegalArgumentException("Incorrect range, y < x");
        }
        return new Range(from, to);
    }

    public static Range buildRange(float from, float to) {
        if (to < from) {
            from = to + 0.1f; // Quick & dirty
        }
        return new Range(from, to);
    }


    public boolean contains(float point) {
        return point >= from && point <= to;
    }

    /**
     * @param point Value to evaluate
     * @return a value, between 0 and 1 only if point is within the range
     */
    public float percentage(float point) {
        return (point - from) / extent;
    }

    /**
     * @param point Value to evaluate
     * @return a value between 0 and 1. 1 if the point is at or after the end of the range, 0 if the point is at or before the start of the range.
     */
    public float clampedPercentage(float point) {
        return Math.max(0, Math.min(1, percentage(point)));
    }

    public float getCenter() {
        return this.from + halfExtent;
    }

    public Range buildSubRange(float fromOffset, float extent) throws IllegalArgumentException {
        if (extent < 0 || fromOffset < 0) {
            throw new IllegalArgumentException("Incorrect sub-range, extent < 0 or offset < 0");
        }
        if (fromOffset + extent > this.to) {
            throw new IllegalArgumentException("Incorrect sub-range, fromOffset + extent > this.to");
        }
        final var subRangeFrom = this.from + fromOffset;
        return new Range(subRangeFrom, subRangeFrom + extent);
    }

    public float getRandom() {
        return MathUtils.random(from, to);
    }

    public void addToEnd(float v) {
        if (to + v >= from) {
            to += v;
        }
    }

    public void addToStart(float v) {
        if (from + v <= to) {
            from += v;
        }
    }

    public void changeEnd(float v) {
        if (v >= from) {
            to = v;
        }
    }

    public void changeStart(float v) {
        if (v <= to) {
            from = v;
        }
    }
}
